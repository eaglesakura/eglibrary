package com.eaglesakura.lib.android.game.resource;

/**
 * 複数箇所から参照を取れるように、リソースを共有する。
 * @author TAKESHI YAMASHITA
 *
 */
public class SharedRawResource implements IRawResource {

    protected IRawResource resource;

    protected int ref = 0;

    public SharedRawResource(IRawResource resource) {
        this.resource = resource;
    }

    /**
     * 参照カウントを追加する
     */
    public void addRef() {
        ++ref;
    }

    /**
     * 参照カウントが0になったら解放する。
     */
    @Override
    public void dispose() {
        if (resource == null) {
            return;
        }
        --ref;
        if (ref <= 0) {
            resource.dispose();
            resource = null;
        }
    }

    /**
     * 管理対象のリソースを取得する。
     * @return
     */
    public IRawResource getResource() {
        return resource;
    }
}
