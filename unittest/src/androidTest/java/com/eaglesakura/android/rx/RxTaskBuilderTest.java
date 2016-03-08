package com.eaglesakura.android.rx;

import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.unittest.ModuleTestCase;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import rx.subjects.BehaviorSubject;

public class RxTaskBuilderTest extends ModuleTestCase {

    class LifecycleItem {
        BehaviorSubject<LifecycleState> mSubject = BehaviorSubject.create(LifecycleState.NewObject);
        SubscriptionController mSubscriptionController = new SubscriptionController();

        public LifecycleItem() {
            mSubscriptionController.bind(mSubject);
            next(LifecycleState.OnCreated);
            next(LifecycleState.OnStarted);
        }

        public void onResume() {
            next(LifecycleState.OnResumed);
        }

        public void onPause() {
            next(LifecycleState.OnPaused);
        }

        public void onDestroy() {
            next(LifecycleState.OnStopped);
            next(LifecycleState.OnDestroyed);
        }

        void next(LifecycleState state) {
            UIHandler.postUI(() -> {
                mSubject.onNext(state);
            });

            while (state != mSubscriptionController.getState()) {
                Util.sleep(10);
            }
        }
    }

    public void test_タスクチェインが実行されて最後の値が取得できる() throws Throwable {
        AndroidThreadUtil.assertBackgroundThread();
        assertTrue(isTestingThread());

        LifecycleItem item = new LifecycleItem();
        RxTask rxTask;
        try {
            item.onResume();

            rxTask = new RxTaskBuilder<Boolean>(item.mSubscriptionController)
                    .async(task -> {
                        LogUtil.log("Call Async!");

                        assertFalse(isTestingThread()); // バックグラウンドで実行されている
                        task.waitTime(100);
                        return true;
                    })
                    .observeOn(ObserveTarget.FireAndForget)
                    .subscribeOn(SubscribeTarget.Pipeline)
                    .chain((ret, task) -> {
                        LogUtil.log("Call Chain!!");
                        assertFalse(isTestingThread());
                        assertFalse(isTestingThread()); // バックグラウンドで実行されている
                        task.waitTime(100);
                        return Integer.valueOf(3103);
                    }).start();

        } finally {
            item.onPause();
            item.onDestroy();
        }

        assertEquals(rxTask.await(1000 * 5), Integer.valueOf(3103));
    }

    public void test_タスクが同期実行できる() throws Throwable {
        AndroidThreadUtil.assertBackgroundThread();
        assertTrue(isTestingThread());

        LifecycleItem item = new LifecycleItem();
        RxTask rxTask;
        try {
            item.onResume();

            rxTask = new RxTaskBuilder<Boolean>(item.mSubscriptionController)
                    .async(task -> {
                        LogUtil.log("Call Async!");

                        assertFalse(isTestingThread()); // バックグラウンドで実行されている

                        task.waitTime(100);
                        return true;
                    })
                    .observeOn(ObserveTarget.FireAndForget)
                    .subscribeOn(SubscribeTarget.Pipeline)
                    .start();

        } finally {
            item.onPause();
            item.onDestroy();
        }

        assertEquals(rxTask.await(1000 * 5), Boolean.TRUE);
    }

    public void test_ライフサイクル状態によってコールバックが保留される() throws Throwable {
        AndroidThreadUtil.assertBackgroundThread();
        assertTrue(isTestingThread());

        LifecycleItem item = new LifecycleItem();
        RxTask rxTask;
        Holder<Boolean> callbackCheck = new Holder<>();
        callbackCheck.set(Boolean.FALSE);
        try {
            item.onResume();

            rxTask = new RxTaskBuilder<Boolean>(item.mSubscriptionController)
                    .async(task -> {
                        LogUtil.log("Call Async!");

                        assertFalse(isTestingThread()); // バックグラウンドで実行されている

                        task.waitTime(100);
                        return true;
                    })
                    .observeOn(ObserveTarget.Foreground)
                    .subscribeOn(SubscribeTarget.Pipeline)
                    .completed((it, task) -> {
                        callbackCheck.set(Boolean.TRUE);
                    }).start();

            item.onPause(); // アプリがバックグラウンドに移った
            rxTask.await(1000); // 処理が終わるまで待つ

            Util.sleep(100);
            assertEquals(callbackCheck.get(), Boolean.FALSE);   // まだコールバックされていない

            item.onResume();    // アプリがフォアグラウンドに復帰した
            Util.sleep(100);

            assertEquals(callbackCheck.get(), Boolean.TRUE);     // resumeされればちゃんとコールバックされる
        } finally {
            item.onPause();
            item.onDestroy();
        }
    }

    public void test_ライフサイクル状態によってタスクがキャンセルされる() throws Throwable {
        AndroidThreadUtil.assertBackgroundThread();
        assertTrue(isTestingThread());

        LifecycleItem item = new LifecycleItem();
        RxTask rxTask;
        Holder<Boolean> callbackCheck = new Holder<>();
        callbackCheck.set(Boolean.FALSE);
        try {
            item.onResume();

            rxTask = new RxTaskBuilder<Boolean>(item.mSubscriptionController)
                    .async(task -> {
                        LogUtil.log("Call Async!");

                        assertFalse(isTestingThread()); // バックグラウンドで実行されている

                        task.waitTime(500);
                        return true;
                    })
                    .observeOn(ObserveTarget.CurrentForeground)
                    .subscribeOn(SubscribeTarget.Pipeline)
                    .completed((it, task) -> {
                        callbackCheck.set(Boolean.TRUE);
                    }).start();

            item.onPause(); // アプリがバックグラウンドに移った
            try {
                rxTask.await(1000); // 処理が終わるまで待つ

                fail(); // アプリがバックグラウンドにあるため、task.waitTimeによってキャンセル例外で死んでいるはずである
            } catch (TaskCanceledException e) {
                LogUtil.log("Task Canceled");
            }

            Util.sleep(100);
            assertEquals(callbackCheck.get(), Boolean.FALSE);   // まだコールバックされていない

            item.onResume();    // アプリがフォアグラウンドに復帰した
            Util.sleep(100);

            assertEquals(callbackCheck.get(), Boolean.FALSE);     // コールバックは捨てられるので、呼びだされてはならない。
        } finally {
            item.onPause();
            item.onDestroy();
        }
    }

}
