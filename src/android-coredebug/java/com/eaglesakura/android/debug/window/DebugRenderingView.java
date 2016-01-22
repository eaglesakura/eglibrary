package com.eaglesakura.android.debug.window;

import com.eaglesakura.android.debug.window.log.DebugItemGroup;
import com.eaglesakura.android.debug.window.log.DebugRenderingItem;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.math.Vector2;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.util.List;

class DebugRenderingView extends View {
    DebugItemGroup debugItemGroup = new DebugItemGroup();

    int debugBackgroundColor;

    public DebugRenderingView(Context context) {
        super(context);
    }

    public void setDebugItemGroup(DebugItemGroup debugItemGroup) {
        this.debugItemGroup = debugItemGroup;
    }

    public void setDebugBackgroundColor(int debugBackgroundColor) {
        this.debugBackgroundColor = debugBackgroundColor;
    }

    /**
     * レンダリングに必要な領域を計算する
     */
    Vector2 calcRenderSize(List<DebugRenderingItem> renderingItems, Vector2 result) {
        result.set(0, 0);
        Vector2 temp = new Vector2();
        for (DebugRenderingItem item : renderingItems) {
            item.getRenderingSize(temp);
            result.x = Math.max(temp.x, result.x);
            result.y += (temp.y + Math.max(2, temp.y * 0.05f));
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Graphics g = new Graphics(canvas);

        int margin = 10;
        int padding = 5;

        int[] renderFlags = {
                RealtimeDebugWindow.FLAG_RENDERING_POSITION_LEFT, RealtimeDebugWindow.FLAG_RENDERING_POSITION_RIGHT
        };

        for (int flags : renderFlags) {
            Vector2 area = new Vector2();
            List<DebugRenderingItem> renderingItems = debugItemGroup.listFlagItems(flags);
            calcRenderSize(renderingItems, area);


//            g.setColorRGBA((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255), 128);
            g.setColorARGB(debugBackgroundColor);

            int backgroundX = 0;
            int backgroundY = margin;
            if ((flags & RealtimeDebugWindow.FLAG_RENDERING_POSITION_RIGHT) == RealtimeDebugWindow.FLAG_RENDERING_POSITION_RIGHT) {
                // 右寄せ
                backgroundX = g.getWidth() - margin - (int) area.x;
            } else {
                // 左寄せ
                backgroundX = margin;
            }
            g.fillRect(backgroundX, backgroundY, (int) area.x + (padding * 2), (int) area.y + (padding * 2));


            Vector2 size = new Vector2();
            int renderPosY;

            renderPosY = backgroundY + padding;
            for (DebugRenderingItem item : renderingItems) {
                item.getRenderingSize(size);
                item.rendering(g, backgroundX + padding, renderPosY);

                renderPosY += (int) size.y;
            }
        }
    }
}
