package com.eaglesakura.material.widget;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.androidquery.AQuery;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.material.R;

/**
 * 単純なLICENSE表示を行うFragment
 */
public class HtmlLicenseDialog extends MaterialAlertDialog {
    View layout;

    WebView webView;

    public HtmlLicenseDialog(Context context) {
        super(context);

        layout = View.inflate(context, R.layout.esm_material_dialog_html_license, null);
        webView = (WebView) layout.findViewById(R.id.EsMaterial_Dialog_WebView);
        setDialogContent(layout);
        setTitle(R.string.EsMaterial_OSS_Title);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setPositiveButton(R.string.EsMaterial_Dialog_Close, null);
    }

    /**
     * 読み込みのURLを指定する
     *
     * @param uri ライセンスのURI
     */
    public void setLicense(String uri) {
        webView.loadUrl(uri);
    }
}
