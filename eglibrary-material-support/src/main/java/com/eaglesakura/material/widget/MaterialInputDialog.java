package com.eaglesakura.material.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.eaglesakura.material.R;

/**
 * 入力用ダイアログを構築する
 */
public abstract class MaterialInputDialog extends MaterialAlertDialog {

    private EditText inputText;

    private TextView headerText;

    private TextView fooderText;

    public MaterialInputDialog(Context context) {
        super(context);

        View content = View.inflate(context, R.layout.esm_material_input_dialog, null);

        inputText = (EditText) content.findViewById(R.id.EsMaterial_Dialog_Input_EditText);
        headerText = (TextView) content.findViewById(R.id.EsMaterial_Dialog_Input_Header);
        fooderText = (TextView) content.findViewById(R.id.EsMaterial_Dialog_Input_Fooder);

        setPositiveButton(R.string.EsMaterial_Dialog_OK, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCommit(inputText);
            }
        });
        setNegativeButton(R.string.EsMaterial_Dialog_Cancel, null);

        setDialogContent(content);
    }

    @Override
    public void show() {
        onInitializeViews(headerText, inputText, fooderText);
        super.show();
    }

    /**
     * Viewの初期化を行わせる
     */
    protected abstract void onInitializeViews(TextView header, EditText input, TextView fooder);

    /**
     * メッセージが確定された
     *
     * @param input
     */
    protected abstract void onCommit(EditText input);
}
