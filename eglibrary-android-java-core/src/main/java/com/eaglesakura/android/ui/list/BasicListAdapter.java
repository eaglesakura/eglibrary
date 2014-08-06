package com.eaglesakura.android.ui.list;

import android.content.Context;
import android.widget.ArrayAdapter;

public class BasicListAdapter extends ArrayAdapter<String> {

    public BasicListAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
    }
}
