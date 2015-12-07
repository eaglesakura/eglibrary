package com.eaglesakura.material.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.eaglesakura.android.R;
import com.eaglesakura.util.StringUtil;

/**
 * 入力用ダイアログを構築する
 */
public abstract class MaterialInputDialog extends MaterialAlertDialog {

    private EditText inputText;

    private TextView headerText;

    private TextView fooderText;

    private TextView validateText;

    public MaterialInputDialog(Context context) {
        super(context);

        View content = View.inflate(context, R.layout.esm_material_input_dialog, null);

        inputText = (EditText) content.findViewById(R.id.EsMaterial_Dialog_Input_EditText);
        headerText = (TextView) content.findViewById(R.id.EsMaterial_Dialog_Input_Header);
        fooderText = (TextView) content.findViewById(R.id.EsMaterial_Dialog_Input_Fooder);
        validateText = (TextView) content.findViewById(R.id.EsMaterial_Dialog_Input_ValidateHint);

        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (validateInputText(inputText, validateText)) {
                    // OKを表示する
//                    findViewById(R.id.EsMaterial_Dialog_BasicButtons_Positive).setVisibility(View.VISIBLE);
                    validateText.setVisibility(View.GONE);
                } else {
                    // OKをdisable
//                    findViewById(R.id.EsMaterial_Dialog_BasicButtons_Positive).setVisibility(View.INVISIBLE);
                    validateText.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
     * 入力された内容がcommit可能であればtrueを返す
     *
     * @param text
     * @param validateHintText
     * @return
     */
    protected boolean validateInputText(EditText text, TextView validateHintText) {
        return !StringUtil.isEmpty(text.getText().toString());
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
