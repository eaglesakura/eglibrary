package com.eaglesakura.lib.android.game.io.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.eaglesakura.lib.android.game.io.IFile;

/**
 * ファイルシステムを扱ったシステム
 * 
 * @author TAKESHI YAMASHITA
 * 
 */
public class FSFile implements IFile {
    File file;

    public FSFile(File file) {
        this.file = file;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public List<IFile> list() {
        List<IFile> result = new ArrayList<IFile>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File sub : files) {
                result.add(new FSFile(sub));
            }
        }
        return result;
    }

    @Override
    public InputStream openReadable() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream openWritable() throws IOException {
        return new FileOutputStream(file);
    }

}
