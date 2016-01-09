package com.eaglesakura.android.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * XMLの要素を定義する。
 * 全てTextで保持して、内容については別途パースする。
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
     * <p/>
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
     *
     * @return
     */
    public String getTag() {
        return tag;
    }

    /**
     * メインコンテンツを取得する
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * ネームスペースを取得する
     *
     * @return
     */
    public String getNamespace() {
        return nspace;
    }

    /**
     * サブコンテンツを取得する
     *
     * @param key
     * @return
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 子を追加する
     *
     * @param element
     */
    private void addChild(XmlElement element) {
        childs.add(element);
        element.parent = this;
    }

    /**
     * 子要素の持つコンテンツを文字列として取得する
     *
     * @param tag
     * @return
     */
    public String childToString(String tag) {
        Iterator<XmlElement> iterator = childs.iterator();
        while (iterator.hasNext()) {
            XmlElement child = iterator.next();
            if (child.getTag().equals(tag)) {
                return child.getContent();
            }
        }

        // 要素が見つからなかった
        return null;
    }

    /**
     * 子要素の持つコンテンツを整数として取得する
     *
     * @param tag
     * @param def
     * @return
     */
    public int childToInt(String tag, int def) {
        String value = childToString(tag);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 子要素の持つコンテンツを実数として取得する
     *
     * @param tag
     * @param def
     * @return
     */
    public double childToDouble(String tag, double def) {
        String value = childToString(tag);
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 指定子要素の指定属性を取得する
     * 失敗したらnullを返す
     *
     * @param tag
     * @param attribute
     * @return
     */
    public String childAttributeToString(String tag, String attribute) {
        XmlElement child = getChild(tag);
        if (child != null) {
            return child.getAttribute(attribute);
        } else {
            return null;
        }
    }

    /**
     * 一致するタグの子エレメントを列挙する
     *
     * @param tag
     * @return
     */
    public List<XmlElement> listChilds(String tag) {
        List<XmlElement> result = new ArrayList<XmlElement>();
        {
            Iterator<XmlElement> iterator = childs.iterator();
            while (iterator.hasNext()) {
                XmlElement element = iterator.next();
                // タグが一致したから返す
                if (element.getTag().equals(tag)) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    public interface EnumlateCallback {
        void onFoundElement(XmlElement element);
    }

    /**
     * 指定した名前の子要素を列挙してコールバックする
     *
     * @param tag
     * @param callback
     */
    public void listChilds(String tag, EnumlateCallback callback) {
        Iterator<XmlElement> iterator = childs.iterator();
        while (iterator.hasNext()) {
            XmlElement element = iterator.next();
            // タグが一致したから返す
            if (element.getTag().equals(tag)) {
                callback.onFoundElement(element);
            }
        }
    }

    /**
     * 指定した子要素を取得する。
     *
     * @param tag
     * @return
     */
    public XmlElement getChild(String tag) {
        Iterator<XmlElement> iterator = childs.iterator();
        while (iterator.hasNext()) {
            XmlElement element = iterator.next();
            // タグが一致したから返す
            if (element.getTag().equals(tag)) {
                return element;
            }
        }

        return null;
    }

    /**
     * 親属性を取得する
     *
     * @return
     */
    public XmlElement getParent() {
        return parent;
    }

    public static XmlElement parse(InputStream is) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(is, "UTF-8");
        //        parser.setFeature(XmlPullParser, true);

        int event = 0;

        // 戻り値用の属性
        XmlElement root = null;

        // 現在設定中の属性
        XmlElement current = null;

        while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
            switch (event) {
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
                default:
                    break;
            }
        }

        return root;
    }

    public static XmlElement parse(String xml) throws XmlPullParserException, IOException {
        return parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
