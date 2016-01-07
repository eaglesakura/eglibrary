/*
 * このクラスはAQueryから必要な機能のみを抽出した軽量なクラス。
 * 非同期処理等は基本的に使用しないため、View関連の機能のみを取り出す。
 *
 * Copyright 2011 - AndroidQuery.com (tinyeeliu@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.eaglesakura.android.aquery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * The core class of AQuery. Contains all the methods available from an AQuery object.
 *
 * @param <T> the generic type
 */
public abstract class AbstractAQuery<T extends AbstractAQuery<T>> {

    private View root;
    private Activity act;
    private Context context;

    protected View view;

    /**
     * Instantiates a new AQuery object.
     *
     * @param act Activity that's the parent of the to-be-operated views.
     */
    public AbstractAQuery(Activity act) {
        this.act = act;
    }

    /**
     * Instantiates a new AQuery object.
     *
     * @param root View container that's the parent of the to-be-operated views.
     */
    public AbstractAQuery(View root) {
        this.root = root;
        this.view = root;
    }

    /**
     * Instantiates a new AQuery object. This constructor should be used for Fragments.
     *
     * @param act  Activity
     * @param root View container that's the parent of the to-be-operated views.
     */
    public AbstractAQuery(Activity act, View root) {
        this.root = root;
        this.view = root;
        this.act = act;
    }


    /**
     * Instantiates a new AQuery object.
     *
     * @param context Context that will be used in async operations.
     */

    public AbstractAQuery(Context context) {
        this.context = context;
    }

    private View findView(int id) {
        View result = null;
        if (root != null) {
            result = root.findViewById(id);
        } else if (act != null) {
            result = act.findViewById(id);
        }
        return result;
    }

    private View findView(String tag) {

        //((ViewGroup)findViewById(android.R.id.content)).getChildAt(0)
        View result = null;
        if (root != null) {
            result = root.findViewWithTag(tag);
        } else if (act != null) {
            //result = act.findViewById(id);
            View top = ((ViewGroup) act.findViewById(android.R.id.content)).getChildAt(0);
            if (top != null) {
                result = top.findViewWithTag(tag);
            }
        }
        return result;

    }

    private View findView(int... path) {

        View result = findView(path[0]);

        for (int i = 1; i < path.length && result != null; i++) {
            result = result.findViewById(path[i]);
        }

        return result;

    }

    /**
     * Recycle this AQuery object.
     * <p/>
     * The method is designed to avoid recreating an AQuery object repeatedly, such as when in list adapter getView method.
     *
     * @param root The new root of the recycled AQuery.
     * @return self
     */
    public T recycle(View root) {
        this.root = root;
        this.view = root;
        reset();
        this.context = null;
        return self();
    }


    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Return the current operating view.
     *
     * @return the view
     */
    public View getView() {
        return view;
    }

    /**
     * Points the current operating view to the first view found with the id under the root.
     *
     * @param id the id
     * @return self
     */
    public T id(int id) {

        return id(findView(id));
    }

    /**
     * Points the current operating view to the specified view.
     *
     * @param view
     * @return self
     */
    public T id(View view) {
        this.view = view;
        reset();
        return self();
    }


    /**
     * Points the current operating view to the specified view with tag.
     *
     * @param tag
     * @return self
     */

    public T id(String tag) {
        return id(findView(tag));
    }

    /**
     * Find the first view with first id, under that view, find again with 2nd id, etc...
     *
     * @param path The id path.
     * @return self
     */
    public T id(int... path) {

        return id(findView(path));
    }

    /**
     * Set the rating of a RatingBar.
     *
     * @param rating the rating
     * @return self
     */
    public T rating(float rating) {

        if (view instanceof RatingBar) {
            RatingBar rb = (RatingBar) view;
            rb.setRating(rating);
        }
        return self();
    }


    /**
     * Set the text of a TextView.
     *
     * @param resid the resid
     * @return self
     */
    public T text(int resid) {

        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setText(resid);
        }
        return self();
    }

    /**
     * Set the text of a TextView with localized formatted string
     * from application's package's default string table
     *
     * @param resid the resid
     * @return self
     * @see Context#getString(int, Object...)
     */
    public T text(int resid, Object... formatArgs) {
        Context context = getContext();
        if (context != null) {
            CharSequence text = context.getString(resid, formatArgs);
            text(text);
        }
        return self();
    }

    /**
     * Set the text of a TextView.
     *
     * @param text the text
     * @return self
     */
    public T text(CharSequence text) {

        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setText(text);
        }

        return self();
    }

    /**
     * Set the text of a TextView. Hide the view (gone) if text is empty.
     *
     * @param text        the text
     * @param goneIfEmpty hide if text is null or length is 0
     * @return self
     */

    public T text(CharSequence text, boolean goneIfEmpty) {

        if (goneIfEmpty && (text == null || text.length() == 0)) {
            return gone();
        } else {
            return text(text);
        }
    }


    /**
     * Set the text of a TextView.
     *
     * @param text the text
     * @return self
     */
    public T text(Spanned text) {


        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setText(text);
        }
        return self();
    }

    /**
     * Set the text color of a TextView. Note that it's not a color resource id.
     *
     * @param color color code in ARGB
     * @return self
     */
    public T textColor(int color) {

        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTextColor(color);
        }
        return self();
    }

    /**
     * Set the text color of a TextView from  a color resource id.
     *
     * @return self
     */
    public T textColorId(int id) {

        return textColor(getContext().getResources().getColor(id));
    }


    /**
     * Set the text typeface of a TextView.
     *
     * @return self
     */
    public T typeface(Typeface tf) {

        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTypeface(tf);
        }
        return self();
    }

    /**
     * Set the text size (in sp) of a TextView.
     *
     * @param size size
     * @return self
     */
    public T textSize(float size) {

        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTextSize(size);
        }
        return self();
    }


    /**
     * Set the adapter of an AdapterView.
     *
     * @param adapter adapter
     * @return self
     */

    @SuppressWarnings({"unchecked", "rawtypes"})
    public T adapter(Adapter adapter) {

        if (view instanceof AdapterView) {
            AdapterView av = (AdapterView) view;
            av.setAdapter(adapter);
        }

        return self();
    }

    /**
     * Set the adapter of an ExpandableListView.
     *
     * @param adapter adapter
     * @return self
     */
    public T adapter(ExpandableListAdapter adapter) {

        if (view instanceof ExpandableListView) {
            ExpandableListView av = (ExpandableListView) view;
            av.setAdapter(adapter);
        }

        return self();
    }

    /**
     * Set the image of an ImageView.
     *
     * @param resid the resource id
     * @return self
     */
    public T image(int resid) {

        if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.setTag(AQuery.TAG_URL, null);
            if (resid == 0) {
                iv.setImageBitmap(null);
            } else {
                iv.setImageResource(resid);
            }
        }

        return self();
    }

    /**
     * Set the image of an ImageView.
     *
     * @param drawable the drawable
     * @return self
     */
    public T image(Drawable drawable) {

        if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.setTag(AQuery.TAG_URL, null);
            iv.setImageDrawable(drawable);
        }

        return self();
    }

    /**
     * Set the image of an ImageView.
     *
     * @param bm Bitmap
     * @return self
     */
    public T image(Bitmap bm) {

        if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.setTag(AQuery.TAG_URL, null);
            iv.setImageBitmap(bm);
        }

        return self();
    }

    /**
     * Set tag object of a view.
     *
     * @param tag
     * @return self
     */
    public T tag(Object tag) {

        if (view != null) {
            view.setTag(tag);
        }

        return self();
    }

    /**
     * Set tag object of a view.
     *
     * @param key
     * @param tag
     * @return self
     */
    public T tag(int key, Object tag) {

        if (view != null) {
            view.setTag(key, tag);
        }

        return self();
    }

    /**
     * Enable a view.
     *
     * @param enabled state
     * @return self
     */
    public T enabled(boolean enabled) {

        if (view != null) {
            view.setEnabled(enabled);
        }

        return self();
    }

    /**
     * Set checked state of a compound button.
     *
     * @param checked state
     * @return self
     */
    public T checked(boolean checked) {

        if (view instanceof CompoundButton) {
            CompoundButton cb = (CompoundButton) view;
            cb.setChecked(checked);
        }

        return self();
    }

    /**
     * Get checked state of a compound button.
     *
     * @return checked
     */
    public boolean isChecked() {

        boolean checked = false;

        if (view instanceof CompoundButton) {
            CompoundButton cb = (CompoundButton) view;
            checked = cb.isChecked();
        }

        return checked;
    }

    /**
     * Set clickable for a view.
     *
     * @param clickable
     * @return self
     */
    public T clickable(boolean clickable) {

        if (view != null) {
            view.setClickable(clickable);
        }

        return self();
    }


    /**
     * Set view visibility to View.GONE.
     *
     * @return self
     */
    public T gone() {
        /*
        if(view != null && view.getVisibility() != View.GONE){
			view.setVisibility(View.GONE);
		}

		return self();
		*/
        return visibility(View.GONE);
    }

    /**
     * Set view visibility to View.INVISIBLE.
     *
     * @return self
     */
    public T invisible() {

		/*
        if(view != null && view.getVisibility() != View.INVISIBLE){
			view.setVisibility(View.INVISIBLE);
		}

		return self();
		*/
        return visibility(View.INVISIBLE);
    }

    /**
     * Set view visibility to View.VISIBLE.
     *
     * @return self
     */
    public T visible() {

		/*
        if(view != null && view.getVisibility() != View.VISIBLE){
			view.setVisibility(View.VISIBLE);
		}

		return self();
		*/
        return visibility(View.VISIBLE);
    }

    /**
     * Set view visibility, such as View.VISIBLE.
     *
     * @return self
     */
    public T visibility(int visibility) {

        if (view != null && view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }

        return self();
    }


    /**
     * Set view background.
     *
     * @param id the id
     * @return self
     */
    public T background(int id) {

        if (view != null) {

            if (id != 0) {
                view.setBackgroundResource(id);
            } else {
                view.setBackgroundDrawable(null);
            }

        }

        return self();
    }

    /**
     * Set view background color.
     *
     * @param color color code in ARGB
     * @return self
     */
    public T backgroundColor(int color) {

        if (view != null) {
            view.setBackgroundColor(color);
        }

        return self();
    }

    /**
     * Set view background color.
     *
     * @return self
     */
    public T backgroundColorId(int colorId) {

        if (view != null) {
            view.setBackgroundColor(getContext().getResources().getColor(colorId));
        }

        return self();
    }

    /**
     * Notify a ListView that the data of it's adapter is changed.
     *
     * @return self
     */
    public T dataChanged() {

        if (view instanceof AdapterView) {

            AdapterView<?> av = (AdapterView<?>) view;
            Adapter a = av.getAdapter();

            if (a instanceof BaseAdapter) {
                BaseAdapter ba = (BaseAdapter) a;
                ba.notifyDataSetChanged();
            }

        }


        return self();
    }


    /**
     * Checks if the current view exist.
     *
     * @return true, if is exist
     */
    public boolean isExist() {
        return view != null;
    }

    /**
     * Gets the tag of the view.
     *
     * @return tag
     */
    public Object getTag() {
        Object result = null;
        if (view != null) {
            result = view.getTag();
        }
        return result;
    }

    /**
     * Gets the tag of the view.
     *
     * @param id the id
     * @return tag
     */
    public Object getTag(int id) {
        Object result = null;
        if (view != null) {
            result = view.getTag(id);
        }
        return result;
    }

    /**
     * Gets the current view as an image view.
     *
     * @return ImageView
     */
    public ImageView getImageView() {
        return (ImageView) view;
    }

    /**
     * Gets the current view as an Gallery.
     *
     * @return Gallery
     */
    public Gallery getGallery() {
        return (Gallery) view;
    }


    /**
     * Gets the current view as a text view.
     *
     * @return TextView
     */
    public TextView getTextView() {
        return (TextView) view;
    }

    /**
     * Gets the current view as an edit text.
     *
     * @return EditText
     */
    public EditText getEditText() {
        return (EditText) view;
    }

    /**
     * Gets the current view as an progress bar.
     *
     * @return ProgressBar
     */
    public ProgressBar getProgressBar() {
        return (ProgressBar) view;
    }

    /**
     * Gets the current view as seek bar.
     *
     * @return SeekBar
     */

    public SeekBar getSeekBar() {
        return (SeekBar) view;
    }

    /**
     * Gets the current view as a button.
     *
     * @return Button
     */
    public Button getButton() {
        return (Button) view;
    }

    /**
     * Gets the current view as a checkbox.
     *
     * @return CheckBox
     */
    public CheckBox getCheckBox() {
        return (CheckBox) view;
    }

    /**
     * Gets the current view as a listview.
     *
     * @return ListView
     */
    public ListView getListView() {
        return (ListView) view;
    }

    /**
     * Gets the current view as a ExpandableListView.
     *
     * @return ExpandableListView
     */
    public ExpandableListView getExpandableListView() {
        return (ExpandableListView) view;
    }

    /**
     * Gets the current view as a gridview.
     *
     * @return GridView
     */
    public GridView getGridView() {
        return (GridView) view;
    }

    /**
     * Gets the current view as a RatingBar.
     *
     * @return RatingBar
     */
    public RatingBar getRatingBar() {
        return (RatingBar) view;
    }

    /**
     * Gets the current view as a webview.
     *
     * @return WebView
     */
    public WebView getWebView() {
        return (WebView) view;
    }

    /**
     * Gets the current view as a spinner.
     *
     * @return Spinner
     */
    public Spinner getSpinner() {
        return (Spinner) view;
    }

    /**
     * Gets the editable.
     *
     * @return the editable
     */
    public Editable getEditable() {

        Editable result = null;

        if (view instanceof EditText) {
            result = ((EditText) view).getEditableText();
        }

        return result;
    }

    /**
     * Gets the text of a TextView.
     *
     * @return the text
     */
    public CharSequence getText() {

        CharSequence result = null;

        if (view instanceof TextView) {
            result = ((TextView) view).getText();
        }

        return result;
    }

    /**
     * Gets the selected item if current view is an adapter view.
     *
     * @return selected
     */
    public Object getSelectedItem() {

        Object result = null;

        if (view instanceof AdapterView<?>) {
            result = ((AdapterView<?>) view).getSelectedItem();
        }

        return result;

    }


    /**
     * Gets the selected item position if current view is an adapter view.
     * <p/>
     * Returns AdapterView.INVALID_POSITION if not valid.
     *
     * @return selected position
     */
    public int getSelectedItemPosition() {

        int result = AdapterView.INVALID_POSITION;

        if (view instanceof AdapterView<?>) {
            result = ((AdapterView<?>) view).getSelectedItemPosition();
        }

        return result;

    }

    /**
     * Register a callback method for when the view is clicked.
     *
     * @param listener The callback method.
     * @return self
     */
    public T clicked(OnClickListener listener) {

        if (view != null) {
            view.setOnClickListener(listener);
        }

        return self();
    }


    /**
     * Register a callback method for when the view is long clicked.
     *
     * @param listener The callback method.
     * @return self
     */
    public T longClicked(OnLongClickListener listener) {

        if (view != null) {
            view.setOnLongClickListener(listener);
        }

        return self();
    }

    /**
     * Register a callback method for when an item is clicked in the ListView.
     *
     * @param listener The callback method.
     * @return self
     */
    public T itemClicked(OnItemClickListener listener) {

        if (view instanceof AdapterView) {

            AdapterView<?> alv = (AdapterView<?>) view;
            alv.setOnItemClickListener(listener);


        }

        return self();

    }


    /**
     * Register a callback method for when an item is long clicked in the ListView.
     *
     * @param listener The callback method.
     * @return self
     */
    public T itemLongClicked(OnItemLongClickListener listener) {

        if (view instanceof AdapterView) {

            AdapterView<?> alv = (AdapterView<?>) view;
            alv.setOnItemLongClickListener(listener);


        }

        return self();

    }


    /**
     * Register a callback method for when an item is selected.
     *
     * @param listener The item selected listener.
     * @return self
     */
    public T itemSelected(OnItemSelectedListener listener) {

        if (view instanceof AdapterView) {
            AdapterView<?> alv = (AdapterView<?>) view;
            alv.setOnItemSelectedListener(listener);
        }

        return self();

    }


    /**
     * Set selected item of an AdapterView.
     *
     * @param position The position of the item to be selected.
     * @return self
     */
    public T setSelection(int position) {

        if (view instanceof AdapterView) {
            AdapterView<?> alv = (AdapterView<?>) view;
            alv.setSelection(position);
        }

        return self();

    }


    /**
     * Set the activity to be hardware accelerated. Only applies when device API is 11+.
     *
     * @return self
     */
    public T hardwareAccelerated11() {

        if (act != null) {
            act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        return self();
    }


    /**
     * Clear a view. Applies to ImageView, WebView, and TextView.
     *
     * @return self
     */
    public T clear() {

        if (view != null) {

            if (view instanceof ImageView) {
                ImageView iv = ((ImageView) view);
                iv.setImageBitmap(null);
                iv.setTag(AQuery.TAG_URL, null);
            } else if (view instanceof WebView) {
                WebView wv = ((WebView) view);
                wv.stopLoading();
                wv.clearView();
                wv.setTag(AQuery.TAG_URL, null);
            } else if (view instanceof TextView) {
                TextView tv = ((TextView) view);
                tv.setText("");
            }


        }

        return self();
    }

    /**
     * Return the context of activity or view.
     *
     * @return Context
     */

    public Context getContext() {
        if (act != null) {
            return act;
        }
        if (root != null) {
            return root.getContext();
        }
        return context;
    }

    protected void reset() {
    }

    /**
     * Starts an animation on the view.
     * <p/>
     * <br>
     * contributed by: marcosbeirigo
     *
     * @param animId Id of the desired animation.
     * @return self
     */
    public T animate(int animId) {
        return animate(animId, null);
    }

    /**
     * Starts an animation on the view.
     * <p/>
     * <br>
     * contributed by: marcosbeirigo
     *
     * @param animId   Id of the desired animation.
     * @param listener The listener to recieve notifications from the animation on its events.
     * @return self
     */
    public T animate(int animId, AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(getContext(), animId);
        anim.setAnimationListener(listener);
        return animate(anim);
    }

    /**
     * Starts an animation on the view.
     * <p/>
     * <br>
     * contributed by: marcosbeirigo
     *
     * @param anim The desired animation.
     * @return self
     */
    public T animate(Animation anim) {
        if (view != null && anim != null) {
            view.startAnimation(anim);
        }
        return self();
    }

    /**
     * Trigger click event
     * <p/>
     * <br>
     * contributed by: neocoin
     *
     * @return self
     * @see View#performClick()
     */
    public T click() {
        if (view != null) {
            view.performClick();
        }
        return self();
    }

    /**
     * Trigger long click event
     * <p/>
     * <br>
     * contributed by: neocoin
     *
     * @return self
     * @see View#performClick()
     */
    public T longClick() {
        if (view != null) {
            view.performLongClick();
        }
        return self();
    }

    /**
     * Inflate a view from xml layout.
     * <p/>
     * This method is similar to LayoutInflater.inflate() but with sanity checks against the
     * layout type of the convert view.
     * <p/>
     * If the convertView is null or the convertView type doesn't matches layoutId type, a new view
     * is inflated. Otherwise the convertView will be returned for reuse.
     *
     * @param convertView the view to be reused
     * @param layoutId    the desired view type
     * @param root        the view root for layout params, can be null
     * @return self
     */
    public View inflate(View convertView, int layoutId, ViewGroup root) {

        if (convertView != null) {
            Integer layout = (Integer) convertView.getTag(AQuery.TAG_LAYOUT);
            if (layout != null && layout.intValue() == layoutId) {
                return convertView;
            }
        }

        LayoutInflater inflater = null;

        if (act != null) {
            inflater = act.getLayoutInflater();
        } else {
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View view = inflater.inflate(layoutId, root, false);
        view.setTag(AQuery.TAG_LAYOUT, layoutId);

        return view;

    }


    public T expand(int position, boolean expand) {

        if (view instanceof ExpandableListView) {

            ExpandableListView elv = (ExpandableListView) view;
            if (expand) {
                elv.expandGroup(position);
            } else {
                elv.collapseGroup(position);
            }
        }

        return self();
    }

    public T expand(boolean expand) {

        if (view instanceof ExpandableListView) {

            ExpandableListView elv = (ExpandableListView) view;
            ExpandableListAdapter ela = elv.getExpandableListAdapter();

            if (ela != null) {

                int count = ela.getGroupCount();

                for (int i = 0; i < count; i++) {
                    if (expand) {
                        elv.expandGroup(i);
                    } else {
                        elv.collapseGroup(i);
                    }
                }

            }


        }

        return self();
    }


    public T checkedChange(final CompoundButton.OnCheckedChangeListener listener) {
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(listener);
        }
        return self();
    }

    public <ViewType extends View> ViewType getView(Class<ViewType> clazz) {
        return (ViewType) getView();
    }

    /**
     * 指定Viewのセットアップを行う
     *
     * @param clazz
     * @param callback
     * @param <ViewType>
     * @return
     */
    public <ViewType extends View> T call(Class<ViewType> clazz, SettingCallback<ViewType> callback) {
        ViewType view = getView(clazz);
        if (view != null) {
            callback.run(view);
        }
        return self();
    }

    public interface SettingCallback<ViewType> {
        void run(ViewType view);
    }
}