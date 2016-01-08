package com.eaglesakura.android.thread.async;

public interface IAsyncTask<T> {
    /**
     * タスクを実行する
     *
     * @param result     戻り値の格納先。キャンセルチェックにも使える
     * @return
     */
    T doInBackground(AsyncTaskResult<T> result) throws Exception;
}
