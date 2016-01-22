package com.eaglesakura.lib.android.game.graphics.gl11;

import java.nio.Buffer;

/**
 * glext.h内部が本来呼び出すべきメソッドの呼出を代行する。<BR>
 * GL11Ext は強制定期に未対応の例外を投げるため、このクラスにNDKを介した呼出を行わせる。<BR>
 * 主にスキンメッシュ実現に必要な関数を実装してある。<BR>
 * 端末が対応しているかは不明。
 *
 * @author TAKESHI YAMASHITA
 */
public class GL11Extension {
    static {
        System.loadLibrary("gl11extension");
    }

    /**
     *
     */
    public static final int GL_MATRIX_PALETTE_OES = 0x8840;

    /**
     *
     */
    public static final int GL_MATRIX_INDEX_ARRAY_OES = 0x8844;

    /**
     *
     */
    public static final int GL_WEIGHT_ARRAY_OES = 0x86AD;

    /**
     * ネイティブのglMatrixModeを呼び出す。
     */
    public native void glMatrixMode(int mode);

    /**
     * ネイティブのglCurrentPaletteMatrixOESを呼び出す。<BR>
     * Xperiaは20、HT-03Aは48の行列パレットを使用可能。
     */
    public native void glCurrentPaletteMatrixOES(int index);

    /**
     * ネイティブのglLoadMatrixxを呼び出す。
     */
    public native void glLoadMatrixx(Buffer buffer);

    /**
     * ネイティブのglLoadMatrixfを呼び出す。
     */
    public native void glLoadMatrixf(Buffer buffer);

    /**
     * ネイティブのglEnableを呼び出す。
     *
     * @see #GL_MATRIX_PALETTE_OES
     */
    public native void glEnable(int flag);

    /**
     * ネイティブのglEnableClientStateを呼び出す。
     *
     * @see #GL_MATRIX_INDEX_ARRAY_OES
     */
    public native void glEnableClientState(int flag);

    /**
     * ネイティブのglWeightPointerOESを呼び出す。<BR>
     * bufferは描画までポインタが有効である必要がある。
     */
    public native void glWeightPointerOES(int num, int type, int stride, Buffer buffer);

    /**
     * ネイティブのglMatrixIndexPointerOESを呼び出す。<BR>
     * Xperia/HT-03A共に頂点ブレンドの最大数は４である。
     */
    public native void glMatrixIndexPointerOES(int num, int type, int stride, Buffer buffer);

    /**
     * ネイティブのglLoadPaletteFromModelViewMatrixOESを呼び出す。
     */
    public native void glLoadPaletteFromModelViewMatrixOES();
}
