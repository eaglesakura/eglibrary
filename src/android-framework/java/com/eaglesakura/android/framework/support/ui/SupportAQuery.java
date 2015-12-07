package com.eaglesakura.android.framework.support.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.androidquery.AbstractAQuery;
import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.widget.SupportNetworkImageView;
import com.eaglesakura.material.widget.support.SupportRecyclerView;

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

    public SupportAQuery checkedChange(final CompoundButton.OnCheckedChangeListener listener) {
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(listener);
        }
        return this;
    }

    public <T extends View> T getView(Class<T> clazz) {
        return (T) getView();
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
     *
     * @param url
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public SupportAQuery imageUrl(String url, int maxWidth, int maxHeight) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setImageFromNetwork(url, new NetworkConnector.ScaledImageParser(maxWidth, maxHeight));
        }
        return this;
    }

    /**
     * {@link SupportNetworkImageView}
     *
     * @param url
     * @param parser
     * @return
     */
    public SupportAQuery imageUrl(String url, NetworkConnector.RequestParser<Bitmap> parser) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setImageFromNetwork(url, parser);
        }
        return this;
    }

    /**
     * {@link SupportNetworkImageView}
     *
     * @param drawableRes
     * @return
     */
    public SupportAQuery errorImage(int drawableRes) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setErrorImage(getContext().getResources().getDrawable(drawableRes));
        }
        return this;
    }

    /**
     * {@link SupportNetworkImageView}
     *
     * @param image
     * @return
     */
    public SupportAQuery errorImage(Bitmap image) {
        if (view instanceof SupportNetworkImageView) {
            ((SupportNetworkImageView) view).setErrorImage(new BitmapDrawable(getContext().getResources(), image));
        }
        return this;
    }
}
