package com.eaglesakura.android.ui.spinner;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Spinner向けのAdapter
 */
public class BasicSpinnerAdapter extends ArrayAdapter<String> {

    public BasicSpinnerAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}
