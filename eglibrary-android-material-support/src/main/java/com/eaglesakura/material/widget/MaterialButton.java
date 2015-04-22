package com.eaglesakura.material.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.eaglesakura.material.R;
import com.eaglesakura.util.LogUtil;

import static android.support.v7.internal.widget.ThemeUtils.getDisabledThemeAttrColor;

/**
 *
 */
public class MaterialButton extends AppCompatButton {
    private static final int TEXTCOLOR_MODE_LIGHT = 0;
    private static final int TEXTCOLOR_MODE_DARK = 1;
    private static final int TEXTCOLOR_MODE_PALETTE = 2;
    private static final int TEXTCOLOR_MODE_AUTO = 3;


    public MaterialButton(Context context) {
        super(context);
        initMaterialButton(context, null, 0);
    }

    public MaterialButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMaterialButton(context, attrs, 0);
    }

    public MaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMaterialButton(context, attrs, defStyleAttr);
    }

    private void initMaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
        if (isInEditMode()) {
            return;
        }


        if (attrs != null) {

            Resources res = getResources();
            LogUtil.log("has attribute");
            TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                    R.attr.esmButtonBaseColor,
                    R.attr.esmButtonHighlightColorWeight,
                    R.attr.esmButtonTextColorMode,
            });
            int baseColor = typedArray.getColor(0, res.getColor(R.color.EsMaterial_Grey_500));

            // ボタンそのものの背景指定
            {
                int highlight = baseColor;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // Kitkat以下はRippleエフェクトが指定できないため、色を変える
                    float weight = typedArray.getFloat(1, 0.9f);
                    int a = Color.alpha(baseColor);
                    int r = (int) (weight * Color.red(baseColor));
                    int g = (int) (weight * Color.green(baseColor));
                    int b = (int) (weight * Color.blue(baseColor));
                    highlight = Color.argb(a, r, g, b);
                }

                // set style
                setSupportBackgroundTintList(createButtonColorStateList(context, baseColor, highlight));
            }

            // base colorから各種TextColorを生成する
            {
                int textBaseColor = 0;
                int textHighlightColor = 0;
                int colorMode = typedArray.getInt(2, TEXTCOLOR_MODE_AUTO);

                if (colorMode == TEXTCOLOR_MODE_AUTO || colorMode == TEXTCOLOR_MODE_PALETTE) {
                    // Paletteから色を指定する
                    Palette.Swatch swatch = new Palette.Swatch(baseColor, 100);
                    textBaseColor = swatch.getTitleTextColor();
                    textHighlightColor = swatch.getBodyTextColor();

                    // 自動選別の場合、HSVからLight / Darkを選定する
                    if (colorMode == TEXTCOLOR_MODE_AUTO) {
                        float[] hsv = new float[3];
                        Color.RGBToHSV(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor), hsv);
                        if (hsv[2] > 0.5f) {
                            // 明るい色なので、フォントを暗くする
                            colorMode = TEXTCOLOR_MODE_DARK;
                        } else {
                            // 暗い色なので、フォントを明るくする
                            colorMode = TEXTCOLOR_MODE_LIGHT;
                        }
                    }
                }

                if (colorMode == TEXTCOLOR_MODE_LIGHT) {
                    // フォントを明るくする
                    textBaseColor = res.getColor(R.color.EsMaterial_Grey_50);
                    textHighlightColor = res.getColor(R.color.EsMaterial_Grey_100);
                } else if (colorMode == TEXTCOLOR_MODE_DARK) {
                    // フォントを暗くする
                    textBaseColor = res.getColor(R.color.EsMaterial_Grey_750);
                    textHighlightColor = res.getColor(R.color.EsMaterial_Grey_800);
                }

                // TextColor
                setTextColor(createButtonColorStateList(context, textBaseColor, textHighlightColor));
            }
        }
    }

    static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    static final int[] FOCUSED_STATE_SET = new int[]{android.R.attr.state_focused};
    static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};

    private ColorStateList createButtonColorStateList(Context context, int colorButtonNormal, int colorControlHighlight) {
        final int[][] states = new int[4][];
        final int[] colors = new int[4];
        int i = 0;

        // Disabled state
        states[i] = DISABLED_STATE_SET;
        colors[i] = getDisabledThemeAttrColor(context, R.attr.colorButtonNormal);
        i++;

        states[i] = PRESSED_STATE_SET;
        colors[i] = ColorUtils.compositeColors(colorControlHighlight, colorButtonNormal);
        i++;

        states[i] = FOCUSED_STATE_SET;
        colors[i] = ColorUtils.compositeColors(colorControlHighlight, colorButtonNormal);
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = colorButtonNormal;
        i++;

        return new ColorStateList(states, colors);
    }
}
