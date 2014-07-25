package com.eaglesakura.google.spreadsheet.generic;

import com.eaglesakura.proguard.NonProguardModel;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Content extends NonProguardModel {
    @JsonProperty("$t")
    public String name;

    public String type;
}