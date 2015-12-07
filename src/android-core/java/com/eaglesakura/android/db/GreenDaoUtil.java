package com.eaglesakura.android.db;

import java.io.IOException;

import de.greenrobot.dao.query.CloseableListIterator;

public class GreenDaoUtil {
    public static <T> void close(CloseableListIterator<T> itr) {
        try {
            if (itr == null) {
                return;
            }

            itr.close();
        } catch (IOException e) {

        }
    }
}
