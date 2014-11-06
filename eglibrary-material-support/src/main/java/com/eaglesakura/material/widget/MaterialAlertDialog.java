package com.eaglesakura.material.widget;

import android.content.Context;
import android.view.View;

import com.androidquery.AQuery;
import com.eaglesakura.material.R;

/**
 *
 */
public class MaterialAlertDialog extends MaterialDialogBase {
    public MaterialAlertDialog(Context context) {
        super(context);
    }

    /**
     * ダイアログのメッセージを選択する
     */
    public void setMessage(CharSequence message) {
        new AQuery(root)
                .id(R.id.EsMaterial_Dialog_Message).text(message).visible();
    }

    public void setPositiveButton(CharSequence button, final OnClickListener positiveListener) {

        new AQuery(root)
                .id(R.id.EsMaterial_Dialog_BasicButtons_Positive).text(button).visible()
                .clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (positiveListener != null) {
                            positiveListener.onClick(MaterialAlertDialog.this, -1);
                        }
                        dismiss();
                    }
                })
                .id(R.id.EsMaterial_Dialog_BasicButtons_Root).visible()
        ;
    }

    public void setNegativeButton(CharSequence button, final OnClickListener negativeListener) {
        new AQuery(root)
                .id(R.id.EsMaterial_Dialog_BasicButtons_Negative).text(button).visible()
                .clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (negativeListener != null) {
                            negativeListener.onClick(MaterialAlertDialog.this, -1);
                        }
                        dismiss();
                    }
                })
                .id(R.id.EsMaterial_Dialog_BasicButtons_Root).visible();
    }

    public void setNeutralButton(CharSequence button, final OnClickListener neutralListener) {
        new AQuery(root)
                .id(R.id.EsMaterial_Dialog_BasicButtons_Neutral).text(button).visible()
                .clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (neutralListener != null) {
                            neutralListener.onClick(MaterialAlertDialog.this, -1);
                        }
                        dismiss();
                    }
                })
                .id(R.id.EsMaterial_Dialog_BasicButtons_Root).visible();
    }
}
