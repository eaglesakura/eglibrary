package com.eaglesakura.android.device.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.eaglesakura.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class SoundPoolManager {

    /**
     * サウンド
     */
    Map<Object, Integer> soundEffects = new HashMap<Object, Integer>();

    SoundPool soundPool;

    /**
     * サウンドの最大数
     */
    int maxSoundStreams = 30;

    int streamType = AudioManager.STREAM_NOTIFICATION;

    private final Context context;

    public SoundPoolManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setMaxSoundStreams(int maxSoundStreams) {
        this.maxSoundStreams = maxSoundStreams;
    }

    /**
     * 初期化を行う
     */
    public void initialize() {
        soundPool = new SoundPool(maxSoundStreams, streamType, 0);
    }

    /**
     * 読み込みを行う
     *
     * @param rawId
     */
    public void load(Object id, int rawId) {
        int soundId = soundPool.load(context, rawId, 0);
        soundEffects.put(id, soundId);
    }

    /**
     * 読み込みを行う
     *
     * @param rawId
     */
    public void load(int rawId) {
        load(rawId, rawId);
    }

    /**
     * 読み込みを行う
     *
     * @param id
     */
    public void play(Object id, boolean loop) {
        Integer soundId = soundEffects.get(id);
        if (soundId == null) {
            return;
        }

        try {
            soundPool.play((int) soundId, 1.0f, 1.0f, 0, loop ? -1 : 0, 1.0f);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    public synchronized void dispose() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
