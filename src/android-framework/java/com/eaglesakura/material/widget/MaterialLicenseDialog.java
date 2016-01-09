package com.eaglesakura.material.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.eaglesakura.android.R;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.IOUtil;

/**
 * 単純なLICENSE表示を行うFragment
 */
public class MaterialLicenseDialog extends MaterialAlertDialog {

    StringBuilder licenses = new StringBuilder();

    View layout;

    public MaterialLicenseDialog(Context context) {
        super(context);

        layout = View.inflate(context, R.layout.esm_material_dialog_license, null);
        setDialogContent(layout);
        setTitle(R.string.EsMaterial_OSS_Title);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setPositiveButton(R.string.EsMaterial_Dialog_Close, null);
    }

    /**
     * 直接テキストを指定する
     *
     * @param text LICENSEテキスト
     */
    public void setLicense(String text) {
        licenses = new StringBuilder(text);
    }

    /**
     * LICENSE情報を追加する
     *
     * @param title 表示されるタイトル
     * @param text  表示内容
     */
    public void addLicense(String title, String text) {
        if (licenses.length() > 0) {
            licenses.append("\n");
        }
        licenses.append(title + ":\n");
        licenses.append(text);
    }

    /**
     * RawリソースからIDを指定する
     *
     * @param rawId
     */
    public void setLicenseFromRawResource(int rawId) {
        try {
            setLicense(IOUtil.toString(getContext().getResources().openRawResource(rawId), true));
        } catch (Exception e) {
        }
    }

    /**
     * rawからLICENSE情報を生成する
     *
     * @param title 表示タイトル
     * @param rawId rawリソースID
     */
    public void addLicenseFromRawResource(String title, int rawId) {
        try {
            addLicense(title, IOUtil.toString(getContext().getResources().openRawResource(rawId), true));
        } catch (Exception e) {

        }
    }

    private void build() {
        if (licenses == null) {
            return;
        }

        // 表示内容を整形する
        String message = licenses.toString();
        licenses = null;

        AQuery q = new AQuery(layout);
        q.id(R.id.EsMaterial_Dialog_Message).text(message);
    }

    /**
     * バックグラウンドスレッドから表示をリクエストする
     */
    public void showFromBackground() {
        if (AndroidThreadUtil.isUIThread()) {
            throw new IllegalStateException("call background thread");
        }

        build();
        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                show();
            }
        });
    }

    /**
     * 表示を行う
     */
    @Override
    public void show() {
        build();
        super.show();
    }
}
