package com.eaglesakura.lib.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * XMLの要素を定義する。
 * 全てTextで保持して、内容については別途パースする。
 * @author TAKESHI YAMASHITA
 *
 */
public class XmlElement {
    /**
     * 親エレメント
     */
    XmlElement parent = null;

    /**
     * タグ名を取得する
     */
    String tag = null;

    /**
     * ネームスペース
     */
    String nspace = null;

    /**
     * メインコンテンツ
     * 
     * <tag>コンテンツ</tag>を保持する
     */
    String content = null;

    /**
     * 属性情報
     * <tag key=value key=value></tag>を保持する
     */
    Map<String, String> attributes = new HashMap<String, String>();

    /**
     * 子要素を定義する
     */
    List<XmlElement> childs = new ArrayList<XmlElement>();

    public XmlElement() {

    }

    /**
     * XMLタグを取得する
     * @return
     */
    public String getTag() {
        return tag;
    }

    /**
     * メインコンテンツを取得する
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * ネームスペースを取得する
     * @return
     */
    public String getNamespace() {
        return nspace;
    }

    /**
     * サブコンテンツを取得する
     * @param key
     * @return
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 子を追加する
     * @param element
     */
    private void addChild(XmlElement element) {
        childs.add(element);
        element.parent = this;
    }

    /**
     * 親属性を取得する
     * @return
     */
    public XmlElement getParent() {
        return parent;
    }

    public static XmlElement parse(String xml) throws XmlPullParserException, IOException {
        XmlPullParser parser = android.util.Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");

        int event = 0;

        // 戻り値用の属性
        XmlElement root = null;

        // 現在設定中の属性
        XmlElement current = null;

        while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    XmlElement nextElement = new XmlElement();
                    // タグ名を取得する
                    nextElement.tag = parser.getName();
                    nextElement.nspace = parser.getNamespace();

                    // 現在のタグの子に設定
                    if (current != null) {
                        current.addChild(nextElement);
                    } else {
                        root = nextElement;
                    }

                    current = nextElement;
                    // 属性を取得する
                    for (int i = 0; i < parser.getAttributeCount(); ++i) {
                        current.attributes.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    // 親に戻る
                    current = current.getParent();
                    break;
                case XmlPullParser.TEXT:
                    // テキストを格納する
                    current.content = parser.getText();
                    break;
                case XmlPullParser.END_DOCUMENT:
                    break;
            }
        }

        return root;
    }
}
