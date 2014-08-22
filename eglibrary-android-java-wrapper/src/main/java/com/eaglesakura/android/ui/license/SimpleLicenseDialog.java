package com.eaglesakura.android.ui.license;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;
import com.eaglesakura.android.connect.R;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.io.IOUtil;

/**
 * 単純なLICENSE表示を行うFragment
 */
public class SimpleLicenseDialog extends Dialog {

    StringBuilder licenses = new StringBuilder();

    View layout;

    public SimpleLicenseDialog(Context context) {
        super(context);

        layout = View.inflate(context, R.layout.dialog_license, null);
        setContentView(layout);
        setTitle(context.getString(R.string.License_Dialog_Title));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        new AQuery(layout).id(R.id.License_Dialog_Close).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
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
        q.id(R.id.License_Dialog_Text).text(message);
    }

    /**
     * バックグラウンドスレッドから表示をリクエストする
     */
    public void showFromBackground() {
        if (AndroidUtil.isUIThread()) {
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
