package com.eaglesakura.android.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * View系の便利メソッド
 */
public class ViewUtil {

    /**
     * CardView配下のitemを横幅に合わせてマッチングする
     *
     * @param itemView
     */
    public static void matchCardWidth(View itemView) {
        if (itemView.getLayoutParams() == null) {
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    /**
     * ImageViewの横幅いっぱいになるようにセットする
     * <br>
     * Viewの高さは画像に合わせて変更される。
     *
     * @param view
     * @param image
     */
    public static void setWidthMatchImage(ImageView view, Bitmap image) {
        Matrix matrix = new Matrix();
        float scale = (float) view.getWidth() / (float) image.getWidth();
        int newHeight = (int) (scale * image.getHeight());
        matrix.postScale(
                scale, scale
        );
        view.setScaleType(ImageView.ScaleType.MATRIX);
        view.setImageMatrix(matrix);
        setViewHeight(view, newHeight);

        // 画像を再設定する
        view.setImageBitmap(image);

    }

    /**
     * Viewの高さを再設定する
     *
     * @param itemView
     * @param height
     */
    public static void setViewHeight(View itemView, int height) {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = height;
        itemView.setLayoutParams(params);
    }

    /**
     * カーソル位置にテキストを挿入する
     *
     * @param editText
     * @param text
     */
    public static void insertTextInCursor(EditText editText, String text) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Editable editable = editText.getText();
        editable.replace(Math.min(start, end), Math.max(start, end), text);
    }

    /**
     * 入力を整数に限るようにする
     *
     * @param editText
     */
    public static void setInputIntegerOnly(EditText editText) {
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    /**
     * 入力を数値に限るようにする
     *
     * @param editText
     */
    public static void setInputDecimal(EditText editText) {
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    /**
     * 実数を取得する
     *
     * @param text
     * @param defValue
     * @return
     */
    public static double getDoubleValue(EditText text, double defValue) {
        try {
            return Double.valueOf(text.getText().toString());
        } catch (Exception e) {
            return defValue;
        }
    }

    /**
     * 整数を取得する
     *
     * @param text
     * @param defValue
     * @return
     */
    public static long getLongValue(EditText text, long defValue) {
        try {
            return Long.valueOf(text.getText().toString());
        } catch (Exception e) {
            return defValue;
        }
    }

    public static int getTouchPointerId(MotionEvent event) {
        int pointerId = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        return event.getPointerId(pointerId);
    }

    public static WebView setupDefault(WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                return false;
            }
        });
        return webView;
    }
}
