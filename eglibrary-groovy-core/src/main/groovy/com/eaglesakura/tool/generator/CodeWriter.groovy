package com.eaglesakura.tool.generator

import com.eaglesakura.io.IOUtil
import com.eaglesakura.tool.log.Logger

/**
 * コード出力を行う
 *
 * 一旦一時ファイルへ出力し、commitされた時点で最新版に更新する
 */
public class CodeWriter {
    int indent = 0;

    /**
     * 1インデントで出力するスペース数
     */
    int indentSpaces = 4;

    /**
     * 最終出力ファイル
     */
    File outFile;

    /**
     * 一時出力ファイル
     */
    File tempFile;

    public CodeWriter(File file) {
        this.outFile = file.absoluteFile;
        this.tempFile = new File(file.absolutePath + ".txt");

        // 経路確保
        this.outFile.parentFile.mkdirs();
        tempFile.delete();
    }

    /**
     * 文字を書き込む
     * @param text
     */
    public CodeWriter write(String text) {
        tempFile.append(text);
        return this;
    }

    /**
     * 文字を書き込み、改行する
     * @param text
     */
    public CodeWriter writeLine(String text) {
        tempFile.append(text);
        return newLine();
    }

    /**
     * 改行を行う
     * @return
     */
    public CodeWriter newLine() {
        String temp = "\n";
        for (int i = 0; i < (indentSpaces * indent); ++i) {
            temp += " ";
        }
        return write(temp);
    }

    /**
     * インデントを追加する
     */
    public CodeWriter pushIndent(boolean writeIndent) {
        ++indent;
        if (writeIndent) {
            String temp = "";
            for (int i = 0; i < (indentSpaces * indent); ++i) {
                temp += " ";
            }
            write(temp);
        }
        return this;
    }

    /**
     * インデントを減らす
     * @return
     */
    public CodeWriter popIndent(boolean withNewLine) {
        --indent;
        if (withNewLine) {
            return newLine();
        }
        return this;
    }

    /**
     * 最終コミットを行う
     */
    public void commit() {
        if (!outFile.isFile()) {
            // まだファイルがないからすぐコミットできる
            tempFile.renameTo(outFile);
            return;
        }

        // 上書きチェックを行う
        String oldSha1 = IOUtil.genSHA1(outFile);
        String newSha1 = IOUtil.genSHA1(tempFile);

        if (!oldSha1.equals(newSha1)) {
            // 差分があるため上書きする
            outFile.delete();
            tempFile.renameTo(outFile);
            Logger.out("[${tempFile.name}] commit -> [${outFile.name}]")
        } else {
            // 一致するため、tempは不要となる
            Logger.out("[${tempFile.name}] equals [${outFile.name}]")
            tempFile.delete();
        }
    }
}