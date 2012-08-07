package com.eaglesakura.lib.net.google.drive;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.net.WebAPIConnectorBase;
import com.eaglesakura.lib.net.WebAPIException;
import com.eaglesakura.lib.net.google.drive.GoogleDriveAPIHelper.DriveItem;

/**
 * GDriveのファイルを利用してツリーを構築する。
 * ファイル検索 -> フォルダとのタグ付を後から行う場合に利用する
 * @author TAKESHI YAMASHITA
 *
 */
public class DriveFileTree {
    /**
     * 扱うディレクトリ
     */
    DriveFile directory = null;

    /**
     * 子として扱うファイル一覧
     */
    List<DriveFile> files = new LinkedList<DriveFile>();

    /**
     * 子ディレクトリ
     */
    List<DriveFileTree> children = new LinkedList<DriveFileTree>();

    public DriveFileTree(DriveFile directory) {
        this.directory = directory;
    }

    /**
     * ディレクトリを取得する
     * @return
     */
    public DriveFile getDirectory() {
        return directory;
    }

    /**
     * 登録されているファイル一覧を取得する
     * @return
     */
    public List<DriveFile> getFiles() {
        return files;
    }

    /**
     * 子ディレクトリを取得する
     * @return
     */
    public List<DriveFileTree> getChildren() {
        return children;
    }

    /**
     * 子階層を含めて保持しているファイル数を取得する
     * @return
     */
    public int getFileCount() {
        int result = files.size();

        // 子フォルダからも集める
        for (DriveFileTree tree : children) {
            result += tree.getFileCount();
        }

        return result;
    }

    /**
     * 不要なディレクトリを削除する
     */
    public void compact() {
        Iterator<DriveFileTree> iterator = children.iterator();
        while (iterator.hasNext()) {
            DriveFileTree tree = iterator.next();
            if (tree.getFileCount() == 0) {
                // ファイルが存在しないから削除する
                iterator.remove();
            } else {
                // 子階層も探索して切り離させる
                tree.compact();
            }
        }
    }

    /**
     * 子も含めた全ファイルのリストを作成する
     * @return
     */
    public List<DriveFile> listFiles(List<DriveFile> result) {
        result.addAll(files);

        for (DriveFileTree child : children) {
            child.listFiles(result);
        }

        return result;
    }

    /**
     * ファイルのツリー構造を構築する
     * @param files
     * @param connector
     * @return
     */
    public static DriveFileTree build(List<DriveFile> files, WebAPIConnectorBase connector) throws WebAPIException {
        // フォルダのマッピング
        Map<String, DriveFile> dirMap = new HashMap<String, DriveFile>();

        // 戻りにするマップ
        Map<String, DriveFileTree> treeMap = new HashMap<String, DriveFileTree>();

        DriveFileTree root = null;
        {
            DriveFile rootFile = DriveFile.root(connector);
            if (rootFile == null) {
                LogUtil.log("drive is empty...");
                return null;
            }

            root = new DriveFileTree(rootFile);
            // rootを追加する
            treeMap.put(rootFile.getId(), root);
        }

        // ディレクトリ一覧を取得する
        {
            List<DriveItem> directories = GoogleDriveAPIHelper.listDirectories(connector);
            for (DriveItem item : directories) {
                DriveFile file = new DriveFile(item);
                dirMap.put(file.getId(), file);
            }
        }

        // ディレクトリの親情報を登録していく
        {
            Iterator<Entry<String, DriveFile>> iterator = dirMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, DriveFile> entry = iterator.next();
                DriveFile dir = entry.getValue();
                // 親を問い合わせる
                DriveFile parent = dirMap.get(dir.getParentId());

                if (parent != null) {
                    dir.setParent(parent);
                } else {
                    // 親を強制的に問い合わせる
                    dir.getParent(connector);
                }

                // マップに登録する
                treeMap.put(dir.getId(), new DriveFileTree(dir));
            }
        }

        // ツリーマップを構築する
        {
            Iterator<Entry<String, DriveFileTree>> iterator = treeMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, DriveFileTree> entry = iterator.next();

                DriveFileTree current = entry.getValue();
                // 親に登録していく
                if (!current.directory.isRoot()) {

                    // IDから親を取得する
                    String parentId = current.directory.getParentId();
                    DriveFileTree parentTree = treeMap.get(parentId);
                    if (root == parentTree) {
                        LogUtil.log("isRoot");
                    }
                    if (parentTree != null) {
                        // 親ディレクトリに登録する
                        parentTree.children.add(current);
                    }
                }
            }
        }

        // 各階層にファイルを追加する
        {
            for (DriveFile file : files) {
                if (file.isFile()) {
                    DriveFileTree parent = treeMap.get(file.getParentId());
                    if (parent != null) {
                        parent.files.add(file);
                        file.setParent(parent.getDirectory());
                    }
                }
            }
        }

        // 不要な階層を削除する
        root.compact();

        // ROOT階層を返す
        return root;
    }
}
