package com.eaglesakura.android.async;

public interface IAsyncTask<T> {
    /**
     * タスクを実行する
     *
     * @param controller
     * @return
     */
    T doInBackground(AsyncTaskController controller) throws Exception;
}
