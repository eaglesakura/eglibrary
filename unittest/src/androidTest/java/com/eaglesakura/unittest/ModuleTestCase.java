package com.eaglesakura.unittest;

import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.LogUtil;

import android.test.AndroidTestCase;
import android.util.Log;

import java.io.File;

public abstract class ModuleTestCase extends AndroidTestCase {

    private File mCacheDirectory;

    private Thread mTestingThread;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestingThread = Thread.currentThread();

        LogUtil.setOutput(true);
        final String TAG = getClass().getSimpleName();
        LogUtil.setLogger(new LogUtil.Logger() {
            @Override
            public void i(String msg) {
                try {
                    StackTraceElement[] trace = new Exception().getStackTrace();
                    StackTraceElement elem = trace[Math.min(trace.length - 1, 3)];
                    Log.i(TAG, String.format("%s[%d] : %s", elem.getFileName(), elem.getLineNumber(), msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void d(String msg) {
                try {
                    StackTraceElement[] trace = new Exception().getStackTrace();
                    StackTraceElement elem = trace[Math.min(trace.length - 1, 3)];
                    Log.d(TAG, String.format("%s[%d] : %s", elem.getFileName(), elem.getLineNumber(), msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mCacheDirectory = TestUtils.getCacheDirectory(getContext());
    }

    public Thread getTestingThread() {
        return mTestingThread;
    }

    /**
     * UnitTest用のスレッドで実行されている場合はtrue
     */
    public boolean isTestingThread() {
        return Thread.currentThread().equals(mTestingThread);
    }

    public File getCacheDirectory() {
        return mCacheDirectory;
    }

    public void cleanCache() {
        IOUtil.cleanDirectory(mCacheDirectory);
    }
}
