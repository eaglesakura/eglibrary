package com.eaglesakura.spreadsheet.generic;

import com.eaglesakura.proguard.NonProguardModel;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class Title extends NonProguardModel {
    @JsonProperty("$t")
    public String name;

    public String type;
}