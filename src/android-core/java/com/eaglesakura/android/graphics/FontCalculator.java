package com.eaglesakura.android.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.eaglesakura.math.Vector2;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * フォントサイズやテキスト表示用の計算を行う
 */
public class FontCalculator {
    /**
     * フォント
     */
    Typeface font;

    /**
     * Paint用のフォントサイズ
     */
    int fontSize;

    /**
     * ピクセル単位のフォントサイズ
     */
    int fontHeightPixel;

    public FontCalculator(Typeface typeface) {
        this.font = typeface;
        setFontHeight(24);
    }

    public FontCalculator() {
        this.font = Typeface.DEFAULT;
        setFontHeight(24);
    }

    public Typeface getFont() {
        return font;
    }

    /**
     * フォントの高さを指定する
     *
     * @param pixelHeight
     */
    public void setFontHeight(int pixelHeight) {
        this.fontSize = calcFontSize("Ag", pixelHeight, font);
        this.fontHeightPixel = pixelHeight;
    }

    /**
     * 指定した1行幅と1行高さに一致するテキストを計算する。
     *
     * @param fooderText 文字列が収まらない場合のフッダーテキスト
     * @param lineWidth  1行の幅
     * @param maxLines   最大行数
     * @return
     */
    public List<String> calcTextLinesFromRect(String text, String fooderText, int lineWidth, int maxLines) {
        if (StringUtil.isEmpty(text)) {
            return new ArrayList<>();
        }

        List<String> lines = Util.convert(text.split("\n"));

        // 行ごとのチェックを行う
        for (int lineNumber = 0; lineNumber < lines.size(); ++lineNumber) {
            String line = lines.get(lineNumber);

            // 各行に収まるテキストに変換する
            line = calcTextInRectNewLine(line, lineWidth);
            lines.set(lineNumber, line);
        }

        // 再度改行分割をかける
        List<String> renderingLines = new ArrayList<String>();
        for (String line : lines) {
            renderingLines.addAll(Util.convert(line.split("\n")));
        }

        // 最大行数を超える場合はdropする
        boolean dropLines = false;
        while (renderingLines.size() > maxLines) {
            renderingLines.remove(renderingLines.size() - 1);
            dropLines = true;
        }

        // 最終行のテキストを調整する
        if (dropLines) {
            String lastLine = renderingLines.get(renderingLines.size() - 1);
            lastLine = calcTextInRect(lastLine, fooderText, true, lineWidth);
            renderingLines.set(renderingLines.size() - 1, lastLine);
        }

        return renderingLines;
    }

    /**
     * 文字列の描画を行う。特定範囲内に収まらない場合は、適当なフッタテキストを追加する。
     *
     * @param text         描画対象文字列
     * @param x            描画するX位置
     * @param y            描画するY位置
     * @param fooderText   文字列が収まらない場合のフッダーテキスト
     * @param lineWidth    1行の幅
     * @param maxLines     最大行数
     * @param yMarginPixel 改行ごとのマージンピクセル数
     * @param canvas
     * @param paint
     */
    public void drawString(String text, String fooderText, int x, final int y, int lineWidth, int maxLines, int yMarginPixel, Canvas canvas, Paint paint) {
        if (StringUtil.isEmpty(text)) {
            return;
        }

        // 再度改行分割をかける
        List<String> renderingLines = calcTextLinesFromRect(text, fooderText, lineWidth, maxLines);

        // テキストの描画を行う
        Rect bounds = new Rect();
        // フォント情報の更新
        paint.setTextSize(fontSize);
        paint.setTypeface(font);
        paint.setAntiAlias(true);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        final int IMAGE_HEIGHT = (int) Math.max(//
                Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent),
                //
                (Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom)));

        // 複数行テキストをチェック
        {
            Rect temp = new Rect();
            for (String singleLine : renderingLines) {
                if (bounds.bottom == 0) {
                    paint.getTextBounds(singleLine, 0, singleLine.length(), bounds);
                } else {
                    paint.getTextBounds(singleLine, 0, singleLine.length(), temp);
                    bounds.bottom += temp.height();
                    bounds.right = Math.max(bounds.right, temp.right);
                }
            }
        }

        // 複数行テキストをレンダリング
        {
            int yOffset = 0;
            for (String singleLine : renderingLines) {
                paint.getTextBounds(singleLine, 0, singleLine.length(), bounds);
                canvas.drawText(singleLine, -bounds.left + x, yOffset - fontMetrics.top + y, paint);
                yOffset += (IMAGE_HEIGHT + yMarginPixel);
            }
        }
    }

    /**
     * 入力された文字列とフォントサイズから、描画した場合の幅と高さを計算する
     *
     * @param text   計算するテキスト
     * @param result 戻り値の格納
     * @return resultの参照
     */
    private static Vector2 calcTextSize(final String text, final Paint paint, final int fontSize, Vector2 result) {
        Rect bounds = new Rect();
        paint.setTextSize(fontSize);
        paint.getTextBounds(text, 0, text.length(), bounds);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        final int IMAGE_WIDTH = Math.max(1, bounds.width());
        final int IMAGE_HEIGHT = (int) Math.max(//
                Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent),
                //
                (Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom)));

        result.x = IMAGE_WIDTH;
        result.y = IMAGE_HEIGHT;
        return result;
    }

    /**
     * 指定した高さピクセル数を満たすフォントサイズを取得する
     *
     * @param text        レンダリング対象の文字列
     * @param heightPixel 計算する高さピクセル数
     * @return
     */
    private static int calcFontSize(final String text, final int heightPixel, final Typeface font) {
        Vector2 size = new Vector2();
        int fontSize = heightPixel + 5;
        Paint paint = new Paint();
        paint.setTypeface(font);
        paint.setAntiAlias(true);
        while (calcTextSize(text, paint, fontSize, size).y > heightPixel && heightPixel > 1) {
            //                AndroidUtil.log(String.format("TextSize(%d x %d)", size.x, size.y));
            --fontSize;
        }
        return fontSize;
    }

    /**
     * テキストを描画した場合のエリアを取得する
     *
     * @param text
     * @return
     */
    public Vector2 calcTextArea(final String text) {
        Paint paint = new Paint();
        paint.setTypeface(font);
        paint.setTextSize(fontSize);

        Vector2 result = new Vector2();
        return calcTextSize(text, paint, fontSize, result);
    }

    /**
     * 特定サイズに収めることができる文字を生成する。
     * height/widthの値は変更せず、オーバーする場合はテキストの末尾を削ってfooderTextに置き換える。
     *
     * @param baseText    描画したい文字列
     * @param fooderText  もし文字列が指定範囲に収まらない場合に指定するフッダーテキスト
     * @param forceFooder 強制的にフッダを付与する場合はtrue
     * @param widthPixel  幅のピクセル数
     * @return
     */
    public String calcTextInRect(final String baseText, final String fooderText, boolean forceFooder, int widthPixel) {
        String tempText = forceFooder ? (baseText + fooderText) : baseText;
        Vector2 tempTextSize = new Vector2();

        int index = baseText.length();
        Paint paint = new Paint();
        paint.setTypeface(font);
        paint.setTextSize(fontSize);

        while (tempText.length() > 0 && calcTextSize(tempText, paint, fontSize, tempTextSize).x > widthPixel) {
            tempText = baseText.substring(0, --index) + fooderText;
        }

        return tempText;
    }

    /**
     * 特定サイズに収めることができる文字を生成する。
     * <br>
     * height/widthの値は変更せず、オーバーする場合は折り返しを行う
     *
     * @param baseText
     * @param widthPixel 1行の幅
     * @return
     */
    public String calcTextInRectNewLine(final String baseText, int widthPixel) {
        String allText = baseText;
        Vector2 tempTextSize = new Vector2();

        List<String> result = new ArrayList<String>();

        Paint paint = new Paint();
        paint.setTypeface(font);
        paint.setTextSize(fontSize);

        while (!allText.isEmpty()) {
            int index = allText.length();
            String tempText = allText;
            while (tempText.length() > 0 && calcTextSize(tempText, paint, fontSize, tempTextSize).x > widthPixel) {
                tempText = allText.substring(0, --index);
            }

            result.add(tempText);
            allText = allText.substring(tempText.length());
        }

        String tempResult = "";
        int index = 0;
        for (String txt : result) {
            if (index > 0) {
                tempResult += "\n";
            }
            tempResult += txt;
            ++index;
        }

        return tempResult;
    }
}
