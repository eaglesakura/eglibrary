package com.eaglesakura.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.eaglesakura.android.net_legacy.LegacyNetworkConnector;
import com.eaglesakura.android.net_legacy.LegacyNetworkResult;
import com.eaglesakura.android.R;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.LogUtil;

import java.io.File;

/**
 * Support Network ImageView
 * <br>
 * Volleyとキャッシュ領域が別にあるため、画像キャッシュで不必要なキャッシュ領域を食わずに済む。
 */
public class SupportNetworkImageView extends ImageView {
    protected String url;

    protected LegacyNetworkConnector connector;

    protected LegacyNetworkResult<Bitmap> imageResult;

    /**
     * ダウンロード失敗時に表示する画像
     */
    protected Drawable errorImage;

    /**
     * 標準では1時間キャッシュ
     */
    protected long cacheTimeoutMs;

    protected OnImageListener onImageListener;

    private File imageFile;

    public SupportNetworkImageView(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public SupportNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public SupportNetworkImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("NewApi")
    public SupportNetworkImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (isInEditMode()) {
            return;
        }

        connector = LegacyNetworkConnector.getDefaultConnector();

        if (attrs != null) {
            LogUtil.log("has attribute");
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SupportNetworkImageView);
            int cacheTimeSec = typedArray.getInteger(R.styleable.SupportNetworkImageView_cacheTimeSec, 0);
            int cacheTimeMin = typedArray.getInteger(R.styleable.SupportNetworkImageView_cacheTimeMin, 0);
            int cacheTimeHour = typedArray.getInteger(R.styleable.SupportNetworkImageView_cacheTimeHour, 0);
            int cacheTimeDay = typedArray.getInteger(R.styleable.SupportNetworkImageView_cacheTimeDay, 0);

            cacheTimeoutMs += (1000 * cacheTimeSec);
            cacheTimeoutMs += LegacyNetworkConnector.CACHE_ONE_MINUTE * cacheTimeMin;
            cacheTimeoutMs += LegacyNetworkConnector.CACHE_ONE_HOUR * cacheTimeHour;
            cacheTimeoutMs += LegacyNetworkConnector.CACHE_ONE_DAY * cacheTimeDay;

            errorImage = typedArray.getDrawable(R.styleable.SupportNetworkImageView_errorImage);
        }

        if (cacheTimeoutMs == 0) {
            cacheTimeoutMs = LegacyNetworkConnector.CACHE_ONE_HOUR;
        }

        LogUtil.log("Cache time(%.2f hour) ErrorImage (%s)", (double) cacheTimeoutMs / (double) LegacyNetworkConnector.CACHE_ONE_HOUR, "" + errorImage);
    }

    /**
     * ネットワークキャッシュが有効な時間を設定する
     *
     * @param cacheTimeoutMs
     */
    public void setCacheTimeoutMs(long cacheTimeoutMs) {
        this.cacheTimeoutMs = cacheTimeoutMs;
    }

    /**
     * 通信エラー時の画像を設定する
     *
     * @param errorImage エラー時に表示する画像
     */
    public void setErrorImage(Drawable errorImage) {
        this.errorImage = errorImage;
    }

    /**
     * ネットワーク経由でgetする
     *
     * @param getUrl
     * @param parser
     */
    public void setImageFromNetwork(final String getUrl, LegacyNetworkConnector.RequestParser<Bitmap> parser) {
        this.url = getUrl;
        this.imageFile = new File(getContext().getCacheDir(), "net-img/" + EncodeUtil.genSHA1(getUrl.getBytes()) + ".img");
        imageFile.getParentFile().mkdirs();
        imageResult = connector.get(getUrl, parser, cacheTimeoutMs, imageFile); // large file support
        imageResult.setListener(new LegacyNetworkResult.Listener<Bitmap>() {
            @Override
            public void onDataReceived(LegacyNetworkResult<Bitmap> sender) {
                if (getUrl.equals(url)) {
                    try {
                        onReceivedImage(sender.getReceivedData());
                    } catch (Exception e) {
                        onImageLoadError();
                    }
                }
                imageResult = null;
            }

            @Override
            public void onError(LegacyNetworkResult<Bitmap> sender) {
                imageResult = null;
                onImageLoadError();
            }


        });
    }

    public void setOnImageListener(OnImageListener onImageListener) {
        this.onImageListener = onImageListener;
    }

    protected void onReceivedImage(Bitmap image) {
        setImageBitmap(image);

        if (onImageListener != null) {
            onImageListener.onImageReceived(this, url, image);
        }
    }

    protected void onImageLoadError() {
        if (errorImage != null) {
            setImageDrawable(errorImage);
        }

        if (onImageListener != null) {
            onImageListener.onImageReceiveFailed(this, url);
        }
    }

    /**
     * 画像受信時のListener
     */
    public interface OnImageListener {
        /**
         * 正常に画像を受信した
         *
         * @param view  this
         * @param url   画像URL
         * @param image 画像
         */
        void onImageReceived(SupportNetworkImageView view, String url, Bitmap image);

        /**
         * 受信に失敗した
         *
         * @param view this
         * @param url  画像URL
         */
        void onImageReceiveFailed(SupportNetworkImageView view, String url);
    }
}
