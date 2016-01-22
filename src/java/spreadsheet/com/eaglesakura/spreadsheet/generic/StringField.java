package com.eaglesakura.spreadsheet.generic;

import com.eaglesakura.proguard.NonProguardModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class StringField extends NonProguardModel {
    @JsonProperty("$t")
    public String value;

    /**
     * 日付として読み込む
     */
    public Date toDate() {
        return StringField.toDate(value);
    }

    public static Date toDate(String gdataDate) {
        try {
            SimpleDateFormat simpleDataFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
            return simpleDataFormat.parse(gdataDate);
        } catch (Exception e) {
            return null;
        }
    }
}
