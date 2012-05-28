package com.eaglesakura.lib.list;

/**
 * 逐次アクセスを行うためのリスト
 * @author TAKESHI YAMASHITA
 *
 * @param <T>
 */
public class OrderAccessList<T> {

    Object lock = new Object();

    /**
     * リストの最初
     */
    Element<T> first = null;

    /**
     * リストの最後
     */
    Element<T> last = null;

    /**
     * 要素に追加された数。
     */
    int size = 0;

    /**
     * 最初の要素を取得する。
     * @param obj
     * @return
     */
    private synchronized Element<T> getFirstElement(T obj) {
        if (first == null) {
            first = new Element<T>(obj, null);
            last = first;
            ++size;
        }

        return first;
    }

    /**
     * 末尾のオブジェクトを取得する。
     * @param obj
     * @return
     */
    private synchronized Element<T> getLastElement(T obj) {
        if (last == null) {
            return getFirstElement(obj);
        }
        return last;
    }

    /**
     * 要素を追加する
     * @param object
     * @return
     */
    public boolean add(T object) {
        synchronized (lock) {
            if (size == 0) {
                getLastElement(object);
                return true;
            }
            Element<T> lastElement = getLastElement(object);
            Element<T> newElement = new Element<T>(object, lastElement);
            last = newElement;
            ++size;
            return true;
        }
    }

    private Element<T> getElementAt(int index) {
        synchronized (lock) {
            // インデックスチェック
            if (index < (size - 1) || index < 0) {
                throw new IndexOutOfBoundsException();
            }
            Element<T> element = getFirstElement(null);

            for (int i = 0; i < index; ++i) {
                element = element.next;
            }
            return element;
        }
    }

    /**
     * 要素を指定箇所に追加する
     * @param index
     * @param object
     * @return
     */
    public boolean add(int index, T object) {
        synchronized (lock) {
            if (size == 0 && index == 0) {
                // まだ要素がなければ、最後に挿入するのと同じである
                return add(object);
            }

            Element<T> before = null;
            if (index > 0) {
                before = getElementAt(index - 1);
            }
            Element<T> current = new Element<T>(object, before);
            if (index == (size)) {
                last = current;
            } else if (index == 0) {
                current.next = first;
                first.before = current;
                first = current;
            }

            ++size;
            return true;
        }
    }

    public T getFirst() {
        synchronized (lock) {
            return first.obj;
        }
    }

    public T getLast() {
        synchronized (lock) {
            return last.obj;
        }
    }

    /**
     * サイズを取得する
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * オブジェクトが含まれている場合はtrue
     * @param object
     * @return
     */
    public boolean contains(T object) {
        return indexOf(object) >= 0;
    }

    /**
     * 保持しているリストを全てクリアする。
     */
    public void clear() {
        synchronized (lock) {
            first = null;
            last = null;
            size = 0;
        }
    }

    /**
     * 指定したオブジェクトを排除する
     * @param obj
     */
    public void remove(T obj) {
        synchronized (lock) {
            Iterator<T> iterator = iterator();
            while (iterator.hasNext()) {
                T t = iterator.next();
                if (t.equals(obj)) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    /**
     * 指定したオブジェクトを全て削除する
     * @param obj
     */
    public void removeAll(T obj) {
        synchronized (lock) {
            Iterator<T> iterator = iterator();
            while (iterator.hasNext()) {
                T t = iterator.next();
                if (t.equals(obj)) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    /**
     * オブジェクトのインデックスを取得する。
     * 
     * @param object
     * @return
     */
    public int indexOf(T object) {
        synchronized (lock) {
            Iterator<T> iterator = iterator();
            int index = 0;
            while (iterator.hasNext()) {
                T t = iterator.next();
                if (t.equals(object)) {
                    return index;
                }
                ++index;
            }
            return -1;
        }
    }

    /**
     * 要素が空ならtrue
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 末尾から先頭へ向かってアクセスするイテレータを返す
     * @return
     */
    public Iterator<T> revertIterator() {
        return new Iterator<T>(last, this) {

            @Override
            protected void moveNext() {
                current = current.before;
            }

            @Override
            public boolean hasNext() {
                if (before != null) {
                    if (before.before != null) {
                        current = before.before;
                    }
                }
                return super.hasNext();
            }
        };
    }

    public Iterator<T> iterator() {
        return new Iterator<T>(first, this) {

            @Override
            protected void moveNext() {
                current = current.next;
            }

            @Override
            public boolean hasNext() {
                if (before != null) {
                    if (before.next != null) {
                        current = before.next;
                        return true;
                    }
                }
                return super.hasNext();
            }
        };
    }

    static class Element<T> {
        /*
         * 前の要素
         */
        Element<T> before = null;

        /**
         * 現在の要素
         */
        T obj;

        /**
         * 次の要素
         */
        Element<T> next;

        public Element(T obj, Element<T> before) {
            this.before = before;
            if (before != null) {
                // beforeをつなぎ替える
                Element<T> temp = before.next;
                before.next = this;

                // もともと入っていた要素を自分に付け替える
                if (temp != null) {
                    this.next = temp;
                    temp.before = this;
                }
            }
            this.obj = obj;
        }
    }

    /**
     * 連続操作用のiterator
     * @author TAKESHI YAMASHITA
     *
     */
    public static abstract class Iterator<T> {
        Element<T> current = null;
        Element<T> removeTarget = null;
        Element<T> before = null;
        OrderAccessList<T> list = null;

        public Iterator(Element<T> current, OrderAccessList<T> list) {
            this.current = current;
            this.list = list;
        }

        public boolean hasNext() {
            return current != null;
        }

        /**
         * 次の要素を取得する
         * @return
         */
        public T next() {
            synchronized (list.lock) {
                before = current;
                removeTarget = current;
                T result = current.obj;
                moveNext();
                return result;
            }
        }

        /**
         * 現在の要素を削除する
         */
        public void remove() {
            synchronized (list.lock) {
                Element<T> before = removeTarget.before;
                Element<T> next = removeTarget.next;

                // チェインを切断する
                {
                    if (before != null) {
                        before.next = null;
                    }
                    if (next != null) {
                        next.before = null;
                    }
                }

                // チェインを再構築する
                {
                    if (before != null && next != null) {
                        before.next = next;
                        next.before = before;
                    }
                }

                // last/firstを再構築する
                if (before == null) {
                    list.first = next;
                }
                if (next == null) {
                    list.last = before;
                }

                // サイズを減らす
                --list.size;
            }
        }

        /**
         * 次の要素に移動する
         */
        protected abstract void moveNext();
    }
}
