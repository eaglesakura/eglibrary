package com.eaglesakura.gradle.android.googleplay

import com.eaglesakura.io.IOUtil
import com.eaglesakura.tool.log.Logger
import com.eaglesakura.util.StringUtil
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Images
import com.google.api.services.androidpublisher.model.ApkListing
import com.google.api.services.androidpublisher.model.AppEdit
import com.google.api.services.androidpublisher.model.Listing
import com.google.api.services.androidpublisher.model.Track
import com.google.play.developerapi.samples.AndroidPublisherHelper

/**
 * Google Playのコンソールで行えることをTaskから実行するためのラッパー
 *
 * https://github.com/googlesamples/android-play-publisher-api/tree/master/v2/java
 */
public class GooglePlayConsoleManager {
    File p12;

    File apk;

    String applicationId;

    String serviceAccountEmail;

    private AndroidPublisher service;


    private Listing newListing(File lang) {
        def title = new File(lang, "title.txt");
        def shortDescription = new File(lang, "shortDescription.txt");
        def fullDescription = new File(lang, "fullDescription.txt");
        def video = new File(lang, "video.txt");

        def langListing = new Listing();
        if (IOUtil.isFile(title)) {
            langListing.setTitle(title.text);
        }
        if (IOUtil.isFile(shortDescription)) {
            langListing.setShortDescription(shortDescription.text);
        }
        if (IOUtil.isFile(fullDescription)) {
            langListing.setFullDescription(fullDescription.text);
        }
        if (IOUtil.isFile(video)) {
            langListing.setVideo(video.text);
        }

        return langListing;
    }

    /**
     * 画像を一枚アップロードする
     */
    private void uploadImages(File dir, String editId, String lang, String imageType, Images images) {
        // 最大10枚アップロード可能
        for (int i = 0; i < 8; ++i) {
            def png = new File(dir, String.format("%d.png", i));
            if (IOUtil.isFile(png)) {
                // アップロード
                images.upload(applicationId, editId, lang, imageType, new FileContent(AndroidPublisherHelper.MIME_TYPE_PNG, png)).execute();
            }
        }
    }

    /**
     * アプリの内容を更新させる
     */
    public void updateListings(File listings) {
        if (!IOUtil.isDirectory(listings)) {
            throw new IllegalArgumentException("task.listings Error");
        }

        final AndroidPublisher.Edits edits = service.edits();

        // Create a new edit to make changes to your listing.
        AndroidPublisher.Edits.Insert editRequest = edits.insert(applicationId, null /** no content */);
        AppEdit edit = editRequest.execute();
        final String editId = edit.getId();
        Logger.out("Created edit with id: %s", editId);


        for (def lang : listings.listFiles()) {
            Logger.out("dir : ${lang.absolutePath}")

            def updatedListing = edits.listings().update(applicationId, editId, lang.name, newListing(lang)).execute();

            // スクリーンショットをチェックして、必要に応じてアップロードする
            def IMAGE_TABLE = [
                    ["phone", "phoneScreenshots"],
                    ["tablet-7", "sevenInchScreenshots"],
                    ["tablet-10", "tenInchScreenshots"],
                    ["tv", "tvScreenshots"],
            ]

            for (int i = 0; i < IMAGE_TABLE.size(); ++i) {
                def dirName = IMAGE_TABLE.get(i).get(0);
                def imageType = IMAGE_TABLE.get(i).get(1);
                def directory = new File(lang, dirName);
                if (IOUtil.isDirectory(directory)) {
                    Logger.out("upload type(${imageType})")
                    edits.images().deleteall(applicationId, editId, lang.name, imageType).execute();
                    uploadImages(directory, editId, lang.name, imageType, edits.images());
                }
            }

            // その他の単体アイコンをチェックする
            def IMAGE_SINGLE_TABLE = [
                    [new File(lang, "icon.png"), "icon"],
                    [new File(lang, "banner-tv.png"), "tvBanner"],
                    [new File(lang, "promo.png"), "promoGraphic"],
                    [new File(lang, "feature.png"), "featureGraphic"],
            ]
            for (int i = 0; i < IMAGE_SINGLE_TABLE.size(); ++i) {
                def png = (File) IMAGE_SINGLE_TABLE.get(i).get(0);
                def imageType = IMAGE_SINGLE_TABLE.get(i).get(1).toString();
                if (IOUtil.isFile(png)) {
                    Logger.out("upload type(${imageType})")
                    edits.images().deleteall(applicationId, editId, lang.name, imageType).execute();
                    edits.images().upload(applicationId, editId, lang.name, imageType, new FileContent(AndroidPublisherHelper.MIME_TYPE_PNG, png)).execute();
                }
            }


            Logger.out("Update ${lang.name} listing with title: %s", updatedListing.getTitle());
        }

        // Commit changes for edit.
        def commitRequest = edits.commit(applicationId, editId);
        def appEdit = commitRequest.execute();
        Logger.out("App edit with id %s has been comitted", appEdit.getId());
    }

    /**
     * 新しいAPKを指定されたトラックへアップロードする
     */
    public void uploadApk(String track, File apkListings) {
        if (StringUtil.isEmpty(track)) {
            throw new IllegalArgumentException("task.track Error");
        }

        if (!IOUtil.isFile(apk)) {
            throw new IllegalStateException("task.apk File Error");
        }

        final AndroidPublisher.Edits edits = service.edits();

        // Create a new edit to make changes to your listing.
        AndroidPublisher.Edits.Insert editRequest = edits.insert(applicationId, null /** no content */);
        AppEdit edit = editRequest.execute();
        final String editId = edit.getId();
        Logger.out("Created edit with id: %s", editId);

        // アップロードを行う
        final AbstractInputStreamContent apkFile = new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, apk);
        def uploadRequest = edits.apks().upload(applicationId, editId, apkFile);
        def apk = uploadRequest.execute();
        Logger.out("Version code %d has been uploaded", apk.getVersionCode());

        // 指定したトラック(alpha/beta/production/rollout)へ移行する
        AndroidPublisher.Edits.Tracks.Update updateTrackRequest = edits
                .tracks()
                .update(applicationId, editId, track, new Track().setVersionCodes(Arrays.asList(apk.getVersionCode()))
        );
        def updatedTrack = updateTrackRequest.execute();
        Logger.out("Track %s has been updated.", updatedTrack.getTrack());

        // upload listings
        if (apkListings != null) {
            for (def lang : apkListings.listFiles()) {
                File apkText = new File(lang, "apk.txt");
                if (IOUtil.isFile(apkText)) {
                    // テキストをアップロード
                    def listing = new ApkListing();
                    listing.setLanguage(lang.name);
                    listing.setRecentChanges(apkText.text);

                    edits.apklistings().update(applicationId, editId, apk.getVersionCode(), lang.name, listing).execute();
                }
            }
        }

        // Commit changes for edit.
        def commitRequest = edits.commit(applicationId, editId);
        def appEdit = commitRequest.execute();
        Logger.out("App edit with id %s has been comitted", appEdit.getId());
    }

    /**
     * 認証を行う
     */
    public void autholize() {
        validate();

        service = AndroidPublisherHelper.init(applicationId, serviceAccountEmail, p12);
        assert service != null;
    }

    /**
     * 検証を行い、問題があれば例外を投げて実行を止める
     */
    private void validate() {
        if (!IOUtil.isFile(p12)) {
            throw new IllegalStateException("task.p12 File Error");
        }

        if (StringUtil.isEmpty(applicationId)) {
            throw new IllegalStateException("task.applicationId Error");
        }

        if (StringUtil.isEmpty(serviceAccountEmail)) {
            throw new IllegalStateException("task.serviceAccountEmail Error");
        }
    }

}
