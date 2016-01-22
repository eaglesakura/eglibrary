package com.eaglesakura.material.widget;

import com.eaglesakura.android.R;
import com.eaglesakura.android.aquery.AQuery;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.ViewGroup;

/**
 * Material Design support Dialog
 */
public class MaterialDialogBase extends AppCompatDialog {

    protected View root;

    public MaterialDialogBase(Context context) {
        super(context);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setBackgroundDrawable(new ColorDrawable(0));
//        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        root = View.inflate(context, R.layout.esm_material_dialog, null);
        setContentView(root);
    }

    /**
     * タイトルの表示・非表示を設定する
     * <br>
     * default = visible
     */
    public void setTitleVisibility(boolean visible) {
        final int VISIBLE_SETTING = visible ? View.VISIBLE : View.GONE;
        findViewById(R.id.EsMaterial_Dialog_Title_Root).setVisibility(VISIBLE_SETTING);
    }

    /**
     * タイトルの文字列を設定する
     */
    public void setTitle(CharSequence title) {
        AQuery q = new AQuery(root);
        q.id(R.id.EsMaterial_Dialog_Title).text(title);
    }

    /**
     * コンテンツ本体を指定する
     */
    public void setDialogContent(int layout) {
        setDialogContent(View.inflate(getContext(), layout, null));
    }

    public View getPositiveButton() {
        return findViewById(R.id.EsMaterial_Dialog_BasicButtons_Positive);
    }

    public View getNegativeButton() {
        return findViewById(R.id.EsMaterial_Dialog_BasicButtons_Negative);
    }

    public View getNeutralButton() {
        return findViewById(R.id.EsMaterial_Dialog_BasicButtons_Neutral);
    }

    /**
     * コンテンツ本体を指定する
     */
    public void setDialogContent(View view) {
        AQuery q = new AQuery(root);
        ((ViewGroup) q.id(R.id.EsMaterial_Dialog_Content_Root).getView()).addView(view);
    }
}
