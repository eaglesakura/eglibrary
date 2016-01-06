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
import android.view.View;

public class AQuery extends AbstractAQuery<AQuery> {

    public static final int TAG_URL = 0x40FF0001;
    public static final int TAG_SCROLL_LISTENER = 0x40FF0002;
    public static final int TAG_LAYOUT = 0x40FF0003;
    public static final int TAG_NUM = 0x40FF0004;


    public AQuery(Activity act) {
        super(act);
    }

    public AQuery(View view) {
        super(view);
    }

    public AQuery(Context context) {
        super(context);
    }

    public AQuery(Activity act, View root) {
        super(act, root);
    }

}