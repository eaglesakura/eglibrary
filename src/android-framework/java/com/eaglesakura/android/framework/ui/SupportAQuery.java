package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.aquery.AbstractAQuery;
import com.eaglesakura.android.net.parser.BitmapParser;
import com.eaglesakura.android.net.parser.RequestParser;
import com.eaglesakura.android.widget.SupportNetworkImageView;
import com.eaglesakura.material.widget.support.SupportRecyclerView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

/**
 * eglibrary Framework用に拡張したAQuery
 */
public class SupportAQuery extends AbstractAQuery<SupportAQuery> {
    public SupportAQuery(Activity act) {
        super(act);
    }

    public SupportAQuery(View view) {
        super(view);
    }

    public SupportAQuery(Context context) {
        super(context);
    }

    public SupportAQuery(Activity act, View root) {
        super(act, root);
    }

    public SupportAQuery(Fragment fragment) {
        super(fragment.getView());
    }

    public SupportAQuery emptyText(int stringRes) {
        if (view instanceof SupportRecyclerView) {
            ((SupportRecyclerView) view).getEmptyView(TextView.class).setText(stringRes);
        }
        return this;
    }

    public SupportAQuery emptyText(String text) {
        if (view instanceof SupportRecyclerView) {
            ((SupportRecyclerView) view).getEmptyView(TextView.class).setText(text);
        }
        return this;
    }

    public SupportAQuery imageListener(SupportNetworkImageView.OnImageListener listener) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setOnImageListener(listener);
        }
        return this;
    }

    /**
     * {@link SupportNetworkImageView}
     */
    public SupportAQuery imageUrl(String url, int maxWidth, int maxHeight) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setImageFromNetwork(url, new BitmapParser(maxWidth, maxHeight));
        }
        return this;
    }

    /**
     * {@link SupportNetworkImageView}
     */
    public SupportAQuery imageUrl(String url, RequestParser<Bitmap> parser) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setImageFromNetwork(url, parser);
        }
        return this;
    }

    /**
     * {@link SupportNetworkImageView}
     */
    public SupportAQuery errorImage(int drawableRes) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setErrorImage(getContext().getResources().getDrawable(drawableRes));
        }
        return this;
    }

    /**
     * {@link SupportNetworkImageView}
     */
    public SupportAQuery errorImage(Bitmap image) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setErrorImage(new BitmapDrawable(getContext().getResources(), image));
        }
        return this;
    }
}
