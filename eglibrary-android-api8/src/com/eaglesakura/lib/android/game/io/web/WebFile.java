package com.eaglesakura.lib.android.game.io.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eaglesakura.lib.android.game.io.IFile;
import com.eaglesakura.lib.android.game.io.WebInputStream;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * Web上の1ファイルを扱う。
 * 
 * @author Takeshi
 * 
 */
public class WebFile implements IFile {
    String uri;
    WebShareStreage strate;
    Boolean exist = null;

    public WebFile(WebShareStreage strage, String uri) {
        this.uri = uri;
        this.strate = strage;
    }

    @Override
    public boolean isDirectory() {
        return uri.endsWith("/");
    }

    @Override
    public boolean isFile() {
        return !uri.endsWith("/");
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public boolean exists() {
        if (exist == null) {
            try {
                //! バッファが200ならtrue
                WebInputStream stream = (WebInputStream) openReadable();
                exist = stream.isStatusOK();
                stream.close();
            } catch (Exception e) {
                exist = false;
            }
        }
        return exist;
    }

    @Override
    public String getName() {
        return WebShareStreage.toName(uri);
    }

    @Override
    public List<IFile> list() {
        if (isFile()) {
            return new ArrayList<IFile>();
        }

        List<IFile> result = new ArrayList<IFile>();
        try {
            //! apacheの結果を利用して取得する。
            //! index.htmlだけは仕組み上メンドウに・・・
            String html = new String(WebInputStream.get(uri, strate.getConnectTimeout()).toByteArray());
            Matcher matcher = Pattern.compile("<td>.*?href=.*?</a>").matcher(html);
            while (matcher.find()) {
                String command = matcher.group();
                String path = command.substring(command.indexOf("=\"") + 2, command.lastIndexOf("\""));
                result.add(new WebFile(strate, uri + path));
            }

            if (result.size() > 0) {
                result.remove(0);
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return result;
    }

    @Override
    public InputStream openReadable() throws IOException {
        if (!isFile()) {
            throw new UnsupportedOperationException("is read only!!");
        }
        return WebInputStream.get(uri, strate != null ? strate.getConnectTimeout() : 1000 * 10);
    }

    @Override
    public OutputStream openWritable() throws IOException {
        throw new UnsupportedOperationException("is read only!!");
    }

}
