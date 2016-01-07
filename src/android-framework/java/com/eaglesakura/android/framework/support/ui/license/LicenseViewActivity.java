package com.eaglesakura.android.framework.support.ui.license;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.eaglesakura.android.R;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.async.AsyncTaskController;
import com.eaglesakura.android.async.AsyncTaskResult;
import com.eaglesakura.android.async.IAsyncTask;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.support.ui.BaseActivity;
import com.eaglesakura.android.thread.AsyncAction;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.material.widget.MaterialLicenseDialog;
import com.eaglesakura.material.widget.support.SupportRecyclerView;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 各種ライブラリのLicenseを自動で表示するためのActivity
 * <br>
 * AndroidManifest.xmlに下記を追加する
 * <pre>
 * android:name="com.eaglesakura.android.framework.support.ui.license.LicenseViewActivity"
 * android:theme="@style/EsMaterial.Theme.Grey.NoActionBar"
 * android:screenOrientation="portrait"
 * </pre>
 */
public class LicenseViewActivity extends BaseActivity {
    /**
     * 読み込んだライセンス一覧
     */
    List<LicenseItem> licenseList = new ArrayList<>();

    List<String> ignoreFiles = new ArrayList<>();

    SupportRecyclerView listRoot;

    RecyclerView licenseListView;

    RecyclerView.Adapter<ItemViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_view);


        String[] ignoreFiles = getResources().getStringArray(R.array.eglibrary_Licence_IgnoreFiles);
        if (!Util.isEmpty(ignoreFiles)) {
            this.ignoreFiles = Util.convert(ignoreFiles);
        }
        setSupportActionBar((Toolbar) findViewById(R.id.EsMaterial_Toolbar));
        setTitle(R.string.eglibrary_License_Activity_Title);

        listRoot = (SupportRecyclerView) findViewById(R.id.eglibrary_License_List);
        licenseListView = listRoot.getRecyclerView();

        // Licenseが読み込まれて無ければ読み込む
        if (licenseList.isEmpty()) {
            loadLicenseList();
        }
    }

    /**
     * 全てのLicenseを読み込む
     */
    void loadLicenseList() {

        FrameworkCentral.getTaskController().pushBack(new IAsyncTask<List<LicenseItem>>() {
            @Override
            public List<LicenseItem> doInBackground(AsyncTaskResult<List<LicenseItem>> result) throws Exception {
                List<LicenseItem> licenses = new ArrayList<>();
                final String LICENSE_PATH = "license";
                String[] files = getAssets().list(LICENSE_PATH);
                for (String file : files) {
                    if (isFinishing()) {
                        return licenses;
                    }

                    // 拡張子が一致して、かつignoreリストに含まれていなければ登録する
                    if (file.endsWith(".license") && ignoreFiles.indexOf(file) < 0) {
                        LogUtil.log("load license(%s)", file);
                        // １行目にOSSの表示名が格納されている
                        final LicenseItem item = newLicense(LICENSE_PATH + "/" + file);
                        if (item != null) {
                            licenses.add(item);
                        }
                    }
                }
                return licenses;
            }
        }).setListener(new AsyncTaskResult.Listener<List<LicenseItem>>() {
            @Override
            public void onTaskCompleted(AsyncTaskResult<List<LicenseItem>> task, List<LicenseItem> licenses) {
                addLicenses(licenses);
            }

            @Override
            public void onTaskCanceled(AsyncTaskResult<List<LicenseItem>> task) {

            }

            @Override
            public void onTaskFailed(AsyncTaskResult<List<LicenseItem>> task, Exception error) {
                onLoadFailed();
            }

            @Override
            public void onTaskFinalize(AsyncTaskResult<List<LicenseItem>> task) {

            }
        });
    }

    void addLicenses(List<LicenseItem> newItems) {
        licenseList = newItems;

        // アダプタを作成
        adapter = new RecyclerView.Adapter<ItemViewHolder>() {
            @Override
            public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ItemViewHolder(parent);
            }

            @Override
            public void onBindViewHolder(final ItemViewHolder holder, final int position) {
                LogUtil.log("onBindViewHolder pos(%d) title(%s) bind(%s)", position, licenseList.get(position).title, holder.toString());
                UIHandler.postUI(new Runnable() {
                    @Override
                    public void run() {
                        holder.bind(position);
                    }
                });
                ViewUtil.matchCardWidth(holder.itemView);
            }

            @Override
            public int getItemCount() {
                return licenseList.size();
            }
        };
        licenseListView.setItemAnimator(new DefaultItemAnimator());
        licenseListView.setLayoutManager(new LinearLayoutManager(this));
        licenseListView.setHasFixedSize(false);
        licenseListView.setAdapter(adapter);

        listRoot.setProgressVisibly(false, licenseList.size());
    }

    /**
     * Licenseを追加する
     *
     * @param assetsPath
     */
    LicenseItem newLicense(String assetsPath) {
        InputStream is = null;
        try {
            is = getAssets().open(assetsPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            LogUtil.log("OSS(%s)", line);

            return new LicenseItem(line, assetsPath);
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        } finally {
            IOUtil.close(is);
        }
    }

    /**
     * 読み込みに失敗した
     */
    void onLoadFailed() {
        listRoot.setProgressVisibly(false, licenseList.size());
    }

    class LicenseItem {
        final String title;

        final String path;

        public LicenseItem(String title, String path) {
            this.title = title;
            this.path = path;
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        LicenseItem item;

        public ItemViewHolder(ViewGroup parent) {
//            super(getLayoutInflater().inflate(R.layout.card_license, parent, false));
            super(View.inflate(LicenseViewActivity.this, R.layout.card_license, null));
        }

        void bind(int position) {
            item = licenseList.get(position);
            ViewUtil.matchCardWidth(itemView);

            AQuery q = new AQuery(itemView);
            q.id(R.id.eglibrary_License_Item_Name).text("").text(item.title);
            q.id(R.id.eglibrary_License_Item_Root).clicked(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadLicense(item);
                }
            });
        }
    }

    /**
     * ライセンスの読み込みと表示を行う
     *
     * @param item
     */
    void loadLicense(final LicenseItem item) {
        {
            AQuery q = new AQuery(this);
            q.id(R.id.eglibrary_License_Loading).visible();
        }
        new AsyncAction("License Load") {
            @Override
            protected Object onBackgroundAction() throws Exception {
                InputStream is = null;
                try {
                    is = getAssets().open(item.path);
                    String text = IOUtil.toString(is, false);
                    text = text.substring(text.indexOf("\n") + 1);

                    return text;
                } finally {
                    IOUtil.close(is);
                }
            }

            @Override
            protected void onSuccess(Object object) {
                MaterialLicenseDialog dialog = new MaterialLicenseDialog(LicenseViewActivity.this);
                dialog.setTitle(item.title);
                dialog.setLicense(object.toString());
                dialog.show();
            }

            @Override
            protected void onFailure(Exception exception) {
                LogUtil.log(exception);
            }

            @Override
            protected void onFinalize() {
                super.onFinalize();

                AQuery q = new AQuery(LicenseViewActivity.this);
                q.id(R.id.eglibrary_License_Loading).gone();
            }
        }.start();
    }

    /**
     * 表示を開始する
     *
     * @param context
     */
    public static void startContent(Context context) {
        context.startActivity(new Intent(context, LicenseViewActivity.class));
    }

}
