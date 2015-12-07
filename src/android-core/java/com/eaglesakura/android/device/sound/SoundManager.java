package com.eaglesakura.android.device.sound;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.eaglesakura.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SE管理クラス
 */
public class SoundManager {
    Map<Object, MediaPlayer> medias = new HashMap<Object, MediaPlayer>();
    Context context = null;

    /**
     * 再生ストリームタイプを設定する
     */
    int streamType = AudioManager.STREAM_MUSIC;

    public SoundManager(Context context) {
        this.context = context;
    }

    /**
     * {@link AudioManager#STREAM_MUSIC}がデフォルトで設定されている。
     *
     * @param streamType
     */
    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    private MediaPlayer init(MediaPlayer player) {
        player.setAudioStreamType(streamType);
        return player;
    }

    /**
     * 効果音のローディングを行う。
     *
     * @param id     再生に利用する効果音ID
     * @param source 音源URI
     *
     * @return 成功した場合true
     */
    public boolean load(Object id, Uri source) {
        unload(id);
        try {
            MediaPlayer player = init(MediaPlayer.create(context, source));
            medias.put(id, player);
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    public boolean loadFromRaw(Object id, int rawId) {
        unload(id);
        try {
            MediaPlayer player = init(MediaPlayer.create(context, rawId));
            medias.put(id, player);
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    public boolean loadFromAssets(Object id, String path) {
        unload(id);
        try {
            MediaPlayer player = new MediaPlayer();
            AssetFileDescriptor fd = context.getAssets().openFd(path);
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            player.prepare();
            init(player);
            medias.put(id, player);
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    /**
     * 効果音を再生する。
     *
     * @param id {@link #load(Object, Uri)}で指定した効果音ID
     */
    public void play(Object id) {
        try {
            MediaPlayer player = medias.get(id);
            if (player != null) {
                if (player.isPlaying()) {
                    player.seekTo(0);
                } else {
                    player.start();
                }
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * 指定したIDの効果音を解放する。
     *
     * @param id
     */
    public void unload(Object id) {
        MediaPlayer player = medias.get(id);
        if (player != null) {
            release(player);
            medias.remove(id);
        }
    }

    /**
     * ロード済みだったらtrueを返す。
     *
     * @param id
     *
     * @return
     */
    public boolean isLoaded(Object id) {
        return medias.get(id) != null;
    }

    void release(MediaPlayer player) {
        try {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * 握っているリソースを全て解放する
     */
    public void dispose() {
        Iterator<Entry<Object, MediaPlayer>> iterator = medias.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, MediaPlayer> entry = iterator.next();
            try {
                release(entry.getValue());
            } catch (Exception e) {
                LogUtil.log(e);
            }
            iterator.remove();
        }
    }
}
