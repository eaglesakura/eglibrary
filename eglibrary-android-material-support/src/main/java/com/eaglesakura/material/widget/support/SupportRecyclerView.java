package com.eaglesakura.material.widget.support;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.eaglesakura.material.R;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

/**
 * List Support
 */
public class SupportRecyclerView extends FrameLayout {
    RecyclerView recyclerView;

    FrameLayout emptyViewRoot;

    View progress;

    public SupportRecyclerView(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public SupportRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public SupportRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public SupportRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (isInEditMode()) {
            return;
        }

        View view = View.inflate(context, R.layout.esm_support_recyclerview, null);

        recyclerView = (RecyclerView) view.findViewById(R.id.EsMaterial_SupportRecyclerView_Content);
        emptyViewRoot = (FrameLayout) view.findViewById(R.id.EsMaterial_SupportRecyclerView_Empty);
        progress = view.findViewById(R.id.EsMaterial_SupportRecyclerView_Loading);

        FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, layoutParams);

        if (attrs != null) {
            LogUtil.log("has attribute");
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SupportRecyclerView);
            String emptyText = typedArray.getString(R.styleable.SupportRecyclerView_emptyText);
            LogUtil.log("SupportRecyclerView_emptyText(%s)", emptyText);

            if (!StringUtil.isEmpty(emptyText)) {
                // empty
                TextView tv = new TextView(context, null, R.style.EsMaterial_Font_Normal);
                tv.setText(emptyText);
                tv.setGravity(Gravity.CENTER);
                setEmptyView(tv);
            }
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setEmptyView(@LayoutRes int layoutId) {
        setEmptyView(View.inflate(getContext(), layoutId, null));
    }

    public void setEmptyView(View view) {
        if (emptyViewRoot == null) {
            return;
        }

        if (emptyViewRoot.getChildCount() != 0) {
            // 子を殺す
            emptyViewRoot.removeAllViews();
        }

        FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        emptyViewRoot.addView(view, layoutParams);
    }

    /**
     * 空Viewを取得する
     *
     * @param <T>
     * @return
     */
    public <T extends View> T getEmptyView(Class<T> clazz) {
        if (emptyViewRoot.getChildCount() == 0) {
            return null;
        }
        return (T) emptyViewRoot.getChildAt(0);
    }

    /**
     * プログレスバーの可視状態を設定する
     *
     * @param visible
     */
    public void setProgressVisibly(boolean visible, int recyclerViewItemNum) {
        if (visible) {
            progress.setVisibility(VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            emptyViewRoot.setVisibility(View.INVISIBLE);
        } else {
            progress.setVisibility(INVISIBLE);
            recyclerView.setVisibility(VISIBLE);
            if (recyclerViewItemNum > 0) {
                emptyViewRoot.setVisibility(INVISIBLE);
            } else {
                emptyViewRoot.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * Adapterの選択位置を取得する
     *
     * @param view
     * @return
     */
    public static int getSelectedAdapterPosition(RecyclerView view) {
        if (view == null || view.getChildCount() <= 0) {
            return -1;
        }

        return view.getChildAdapterPosition(view.getChildAt(0));
    }
}
