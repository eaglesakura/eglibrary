package com.eaglesakura.android.ui.spinner;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Spinner向けのAdapter
 */
public class BasicSpinnerAdapter extends ArrayAdapter<String> {
    boolean textRight = false;

    public BasicSpinnerAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public void setTextRight(boolean textRight) {
        this.textRight = textRight;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View result = super.getDropDownView(position, convertView, parent);
        if (textRight) {
            ((TextView) result.findViewById(android.R.id.text1)).setGravity(Gravity.RIGHT);
        }
        return result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = super.getView(position, convertView, parent);
        if (textRight) {
            ((TextView) result.findViewById(android.R.id.text1)).setGravity(Gravity.RIGHT);
        }
        return result;
    }
}
