package com.eaglesakura.lib.android.game.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * File関連の便利メソッドを提供する
 * @author TAKESHI YAMASHITA
 *
 */
public class FileUtil {

    /**
     * 指定箇所へファイルをコピーする。
     * @param src
     * @param dst
     * @return
     */
    public static final void copy(File src, File dst) throws IOException {
        InputStream srcStream = new FileInputStream(src);
        OutputStream dstStream = new FileOutputStream(dst);

        //! 適当なバッファサイズを決める。
        byte[] buffer = new byte[128 * 1024];
        int readed = 0;

        //! 読めなくなるまで読み込みを続ける
        while ((readed = srcStream.read(buffer)) > 0) {
            dstStream.write(buffer, 0, readed);
        }

        srcStream.close();
        dstStream.close();

        //! 最終変更日を修正する
        dst.setLastModified(src.lastModified());
    }

    /**
     * ファイルからMD5を求める。
     * 
     * @param file
     * @return
     */
    public static String genMD5(File file) {
        try {

            FileInputStream is = new FileInputStream(file);
            final MessageDigest md = MessageDigest.getInstance("MD5");
            {
                byte[] buffer = new byte[128 * 1024];
                int readed = 0;

                while ((readed = is.read(buffer)) > 0) {
                    md.update(buffer, 0, readed);
                }
            }
            is.close();
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ファイルからMD5を求める。
     * 
     * @param file
     * @return
     */
    public static String genSHA1(File file) {
        try {

            FileInputStream is = new FileInputStream(file);
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            {
                byte[] buffer = new byte[128 * 1024];
                int readed = 0;

                while ((readed = is.read(buffer)) > 0) {
                    md.update(buffer, 0, readed);
                }
            }
            is.close();
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ディレクトリを再帰的に削除する。
     * 
     * @param root
     */
    public static void delete(File root) {
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                delete(file);
            }
        }
        root.delete();
    }

    /**
     * ファイル拡張子を取得する。
     *
     * 
     * @param path
     * @return
     * 
     */
    public static String getFileExt(String fileName) {
        if (fileName == null)
            return "";
        int point = fileName.lastIndexOf('.');
        if (point != -1) {
            return fileName.substring(point + 1);
        } else {
            // fileName = "";
            return "";
        }
    }

    /**
     * 拡張子付のファイル名からファイル名のみを抜き出す
     *
     * 
     * @param fileName
     *            ファイル名
     * @return 拡張子を取り除いたファイル名
     * 
     */
    public static String getFileName(final String fileName) {
        if (fileName == null)
            return "";
        int point = fileName.lastIndexOf('.');
        if (point != -1) {
            return fileName.substring(0, point);
        } else {
            // fileName = "";
            return "";
        }
    }

    /**
     * カレントディレクトリのパスを取得する。
     *
     * 
     * @return
     * 
     */
    public static String getCurrentDirectoryPath() {
        return (new File(".").getAbsoluteFile().getParent());
    }

    public static File getCurrentDirectory() {
        return (new File(".")).getAbsoluteFile().getParentFile();
    }

    /**
     * ファイルを名前順にソートする。
     * inの中身がソートされるため、戻り値は同じ参照となる。
     * @param files
     * @return
     */
    public static File[] sort(File[] files) {
        if (files == null) {
            return null;
        }
        List<File> temp = new ArrayList<File>();
        for (File f : files) {
            temp.add(f);
        }

        Collections.sort(temp, new Comparator<File>() {
            @Override
            public int compare(File object1, File object2) {
                String f0 = object1.getAbsolutePath();
                String f1 = object2.getAbsolutePath();
                return GameUtil.compareString(f0, f1);
            }
        });

        for (int i = 0; i < files.length; ++i) {
            files[i] = temp.get(i);
        }

        return files;
    }

}
