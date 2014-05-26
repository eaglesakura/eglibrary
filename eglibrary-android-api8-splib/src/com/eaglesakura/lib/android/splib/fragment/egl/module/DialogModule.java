package com.eaglesakura.lib.android.splib.fragment.egl.module;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;

import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
import com.eaglesakura.lib.android.splib.fragment.egl.EGLFragmentModule;

/**
 * ダイアログ表示用のモジュール
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class DialogModule extends EGLFragmentModule {

    Dialog dialog = null;

    @Override
    public void onAttach(EGLFragment fragment) {
        super.onAttach(fragment);
        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                if (!isAttached()) {
                    return;
                }
                dialog = createDialog();
                dialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        DialogModule.this.onDialogCancel();
                    }
                });

                dialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        DialogModule.this.onDialogDismiss();
                        dismiss();
                    }
                });

                try {
                    dialog.show();
                } catch (Exception e) {
                    unbind();
                }
            }
        });
    }

    /**
     * ダイアログがキャンセルされた
     */
    protected void onDialogCancel() {

    }

    /**
     * ダイアログが閉じられた
     */
    protected void onDialogDismiss() {

    }

    /**
     * ダイアログを閉じてModuleを破棄する
     */
    public void dismiss() {
        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    try {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    } catch (Exception e) {

                    }
                    dialog = null;
                }
            }
        });
        unbind();
    }

    /**
     * ダイアログを取得する。
     * @return
     */
    public Dialog getDialog() {
        return dialog;
    }

    @Override
    public final void unbind() {
        super.unbind();
    }

    /**
     * ダイアログを作成する。
     * @return
     */
    protected abstract Dialog createDialog();

    /**
     * 管理しているリソースを解放する
     */
    @Override
    public void dispose() {
        dismiss();
    }
}
