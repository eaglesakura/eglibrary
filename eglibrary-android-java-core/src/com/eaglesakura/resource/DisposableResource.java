package com.eaglesakura.resource;

/**
 * 資源開放を行う必要があるリソースを示す。<BR>
 * <BR>
 * dispose()は2回以上呼ばれた場合、2回目以降は何もしないよう挙動を調整すること。<BR>
 * dispose()はfinalize()でも念のため呼ばれるが、明示的に呼ぶことが望ましい。<BR>
 */
public abstract class DisposableResource {
    /**
     * テクスチャ識別のためのタグ
     */
    protected Object tag = null;

    /**
     * 管理タグを指定する
     * @param tag
     */
    public void setTag(Object tag) {
        this.tag = tag;
    }

    /**
     * 管理タグを取得する
     * @return
     */
    public Object getTag() {
        return tag;
    }

    /**
     * 管理している画像資源を解放する。
     */
    public abstract void dispose();

    @Override
    public String toString() {
        if (tag == null) {
            return super.toString();
        } else {
            return tag.toString();
        }
    }
}
