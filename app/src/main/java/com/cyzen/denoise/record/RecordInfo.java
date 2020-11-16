package com.cyzen.denoise.record;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;

import com.cyzen.denoise.App;
import com.cyzen.denoise.Constants;
import com.cyzen.denoise.utils.Utils;

public class RecordInfo {

    public static final int[] AUDIO_SOURCES = {
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.UNPROCESSED,//24
            MediaRecorder.AudioSource.VOICE_PERFORMANCE//29
    };
    public static final int[] AUDIO_CHANNELS = {
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.CHANNEL_IN_STEREO,
            //Output
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.CHANNEL_OUT_STEREO
    };

    //声音来源
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;

    //采样频率，输入频率必须与输出相同(避免重采样延迟)
    public static int AUDIO_SAMPLE_RATE;

    //输入声道
    public static int INPUT_CHANNEL = AudioFormat.CHANNEL_IN_MONO;

    //输出声道
    public static int OUTPUT_CHANNEL = AudioFormat.CHANNEL_OUT_MONO;

    //编码
    public static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //输入缓冲区字节大小
    public static int INPUT_BUFFER_SIZE = 0;

    //输出缓冲区字节大小
    public static int OUTPUT_BUFFER_SIZE = 0;

    //帧缓冲区大小
    public static int FRAMES_PER_BUFFER;

    static {
        AudioManager audioManager = (AudioManager) App.getContext().getSystemService(Context.AUDIO_SERVICE);
        AUDIO_SAMPLE_RATE = Utils.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE), 48000);
        FRAMES_PER_BUFFER = Utils.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER), 192);
        loadInfo();
    }

    public static void loadInfo() {
        Context context = App.getContext();
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        AUDIO_SOURCE = Utils.getSourceValueFromArray(preferences.getInt(Constants.AUDIO_SOURCE, AUDIO_SOURCE));
        INPUT_CHANNEL = preferences.getInt(Constants.AUDIO_CHANNEL, 1) == 1 ? AUDIO_CHANNELS[0] : AUDIO_CHANNELS[1];
        INPUT_BUFFER_SIZE = Utils.getMinBufferSize(false);

        OUTPUT_CHANNEL = preferences.getInt(Constants.AUDIO_CHANNEL, 1) == 1 ? AUDIO_CHANNELS[2] : AUDIO_CHANNELS[3];
        OUTPUT_BUFFER_SIZE = Utils.getMinBufferSize(true);
    }

}
