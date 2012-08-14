package com.eaglesakura.lib.android.dropbox;

import java.io.File;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.dropbox.client2.session.Session.AccessType;
import com.eaglesakura.lib.android.dropbox.DropboxFile.DownloadCallback;
import com.eaglesakura.lib.android.game.util.FileUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.list.UniqueList;
import com.eaglesakura.lib.sample.activity.DropboxAuthActivity;

public class DropboxAPITest extends AndroidTestCase {
    DropboxAPIHelper helper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        SharedPreferences preference = DropboxAuthActivity.getPreference(getContext());
        String token = preference.getString(DropboxAuthActivity.PREF_TOKEN, null);
        String tokenSecret = preference.getString(DropboxAuthActivity.PREF_TOKENSECRET, null);

        helper = new DropboxAPIHelper(DropboxAuthActivity.APP_KEY, DropboxAuthActivity.APP_SECRET, token, tokenSecret,
                AccessType.APP_FOLDER);
        helper.login();
    }

    void postMessage(final String msg) {
        LogUtil.log(msg);
    }

    public void displayNameTest() throws Exception {
        String userName = helper.getUserDisplayName();
        assertNotNull(userName);
        postMessage("name = " + userName);
    }

    /**
     * ファイル一覧を並べる。
     * リビジョンが一致してはいけない
     * @param file
     * @param list
     * @throws Exception
     */
    void ls(DropboxFile file, UniqueList<String> list) throws Exception {
        postMessage(file.getAbsolutePath() + " :: " + file.getRev());
        assertTrue(list.add(file.getRev()));

        if (!file.isFile()) {
            List<DropboxFile> files = file.listFiles(helper);
            for (DropboxFile sub : files) {
                ls(sub, list);
            }
        }
    }

    /**
     * すべてのRevコードがuniqueだということを検証する
     * @throws Exception
     */
    public void revisionListTest() throws Exception {
        DropboxFile file = DropboxFile.get(helper, "/bookshelf");
        UniqueList<String> uniqueList = new UniqueList<String>();
        ls(file, uniqueList);

        LogUtil.log("files :: " + uniqueList.size());
    }

    /**
     * ダウンロードを行う
     * @throws Exception
     */
    public void downloadTest() throws Exception {
        List<DropboxFile> search = DropboxFile.search(helper, "/bookshelf", ".jdh", 1);
        assertEquals(search.size(), 1);

        DropboxFile file = search.get(0);
        assertTrue(file.isFile());

        File dstFile = new File(Environment.getExternalStorageDirectory(), file.getTitle());
        dstFile.delete();
        boolean completed = file.download(helper, dstFile, new DownloadCallback() {
            @Override
            public void onUpdate(DropboxFile file, long downloaded) {
            }

            @Override
            public void onStart(DropboxFile file) {
            }

            @Override
            public boolean isCanceled(DropboxFile file) {
                return false;
            }
        });
        assertTrue(completed);

        assertEquals(dstFile.length(), file.getFileSize());

        file.syncDetails(helper);
        LogUtil.log("file :: " + file.getAbsolutePath());
        LogUtil.log("local md5   :: " + FileUtil.genMD5(dstFile));
        LogUtil.log("server hash :: " + file.entry.hash);
    }
}
