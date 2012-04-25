package com.eaglesakura.lib.android.game.graphics;

import java.util.ArrayList;
import java.util.List;

import com.eaglesakura.lib.android.game.resource.IRawResource;

public class ImageStub extends ImageBase {
    int width;
    int height;

    public ImageStub(int width, int height) {
        super(null);
        this.width = width;
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public List<IRawResource> getRawResources() {
        return new ArrayList<IRawResource>();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    protected void onDispose() {

    }
}
