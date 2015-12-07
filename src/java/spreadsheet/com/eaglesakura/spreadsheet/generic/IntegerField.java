package com.eaglesakura.spreadsheet.generic;

import com.eaglesakura.proguard.NonProguardModel;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * int型のフィールドを指定する
 */
public class IntegerField extends NonProguardModel {
    @JsonProperty("$t")
    public int value;
}
