/**
 *
 */
package com.eaglesakura.lib.android.game.thread;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

/**
 * 非同期待ち行列の処理を行う。
 */
@Deprecated
public class AsyncActionQueue {
    private List<Action> queue = new ArrayList<Action>();
    private ActionTask current = null;

    /**
     * 開始済みだったらtrue
     */
    private boolean isStarted = false;

    public AsyncActionQueue() {

    }

    /**
     * 末尾にアクションを追加する。
     *
     * @param action
     */
    public void pushBack(Action action) {
        synchronized (queue) {
            queue.add(action);
        }
    }

    /**
     * 先頭にアクションを追加する。
     *
     * @param action
     */
    public void pushFront(Action action) {
        if (action == null) {
            return;
        }
        synchronized (queue) {
            queue.add(0, action);
        }
    }

    /**
     * キャンセルする。
     *
     * @param action
     */
    public void cancel(Action action) {
        synchronized (queue) {
            queue.remove(action);
            if (current != null && current.action == action) {
                synchronized (current) {
                    current.cancel(true);
                    current = null;
                }
            }
        }
    }

    /**
     * equalsで比較し、一致したものをキャンセルする。
     *
     * @param action
     */
    public void cancelEquals(Action action) {
        synchronized (queue) {
            for (Action act : queue) {
                if (act.equals(action)) {
                    queue.remove(act);
                }
            }
        }

        if (current != null && current.action.equals(action)) {
            synchronized (current) {
                current.cancel(true);
                current = null;
            }
        }
    }

    /**
     * すべての動作をキャンセルする。
     */
    public void cancelAll() {
        synchronized (queue) {
            queue.clear();
            if (current != null) {
                current.cancel(true);
                synchronized (current.action) {
                    current = null;
                }
            }

        }
        isStarted = false;
    }

    private void _startAction() {
        if (queue.size() == 0) {
            isStarted = false;
            return;
        }

        synchronized (queue) {
            Action action = queue.get(0);
            queue.remove(0);
            if (action.isNoStart()) {
                _startAction();
                return;
            } else {
                current = (ActionTask) (new ActionTask(action)).execute();
                isStarted = true;
            }
        }
    }

    /**
     * アクションリストが開始されているか。
     *
     * @return
     */
    public boolean isStartActions() {
        return isStarted;
    }

    public int getQueueSize() {
        return queue.size();
    }

    /**
     * 行動リスト処理を開始する。
     */
    public void startActions() {
        if (isStarted || queue.size() == 0) {
            return;
        }

        _startAction();
    }

    /**
     * アクションが正常終了した。
     */
    private void onActionExit(Action action) {
        if (queue.size() > 0) {
            //! 次の行動を開始する。
            _startAction();
        } else {
            current = null;
            isStarted = false;
        }
    }

    /**
     * タスクを一時停止する。
     */
    public void onPause() {
        if (isStarted) {
            if (current != null) {
                synchronized (current) {
                    current.cancel(true);
                    //! 先頭に追加しておく
                    pushFront(current.action);
                    current = null;
                }
            }
        }
    }

    /**
     * タスクを再開する。
     */
    public void onResume() {
        if (isStarted) {
            _startAction();
        }
    }

    /**
     * 各々のアクションを行う。
     */
    private class ActionTask extends AsyncTask<Object, Object, Object> {
        private Action action = null;

        /**
         *
         * @param action
         */
        public ActionTask(Action action) {
            this.action = action;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            action.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... params) {
            return action.onBackgroundAction();
        }

        /**
         * 途中で停止させられた。
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
            action.onCancel();
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            //! 正常に終了したことを通知する。
            action.onPost(result);
            onActionExit(action);
        }
    }

    /**
     * １回ごとの動作を示す。
     */
    public interface Action {
        /**
         * スレッド開始直前に呼ばれる。
         */
        public void onPreExecute();

        /**
         * 裏での動作を行う。
         */
        public Object onBackgroundAction();

        /**
         * 動作がキャンセルされた。
         */
        public void onCancel();

        /**
         * 動作が正常に完了した。
         */
        public void onPost(Object obj);

        /**
         * 本当に開始するかを確かめる。
         * @return
         */
        public boolean isNoStart();
    };
}
