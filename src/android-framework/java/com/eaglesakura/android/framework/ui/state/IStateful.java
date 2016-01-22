package com.eaglesakura.android.framework.ui.state;

/**
 * 内部にライフサイクルステートを持っているクラス
 * <p/>
 * Activity/Fragment
 */
public interface IStateful {

    /**
     * Activity/Fragmentのライフサイクル状態を示す
     */
    enum LifecycleState {
        /**
         * Newされたばかり
         */
        NewObject,

        /**
         * OnCreateが完了した
         */
        OnCreated,

        /**
         * OnStartが完了した
         */
        OnStarted,

        /**
         * OnResumeが完了した
         */
        OnResumed,

        /**
         * OnPauseが完了した
         */
        OnPaused,

        /**
         * OnStopが完了した
         */
        OnStopped,

        /**
         * OnDestroyが完了した
         */
        OnDestroyed,
    }

    /**
     * 現在のステートを取得する
     */
    LifecycleState getCurrentState();
}
