package com.cyzen.denoise.record;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
    public static final int[] AUDIO_SAMPLE_RATES = {
            //Input
            8000,
            11025,
            12000,
            16000,
            22050,
            24000,
            32000,
            44100,
            48000
    };
    public static final int[] AUDIO_CHANNELS = {
            AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.CHANNEL_IN_STEREO,
            //Output
            AudioFormat.CHANNEL_OUT_DEFAULT,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.CHANNEL_OUT_STEREO,
    };
    public static final int[] AUDIO_ENCODINGS = {
            AudioFormat.ENCODING_PCM_16BIT,
            AudioFormat.ENCODING_PCM_8BIT,
            AudioFormat.ENCODING_PCM_FLOAT
    };

    public enum TYPE {
        AUDIO_SOURCE,
        AUDIO_SAMPLE_RATE,
        AUDIO_CHANNEL,
        AUDIO_ENCODING
    }

    //声音来源
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;

    //输入采样频率
    public static int INPUT_SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

    //输出采样频率
    public static int OUTPUT_SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

    //输入声道
    public static int INPUT_CHANNEL = AudioFormat.CHANNEL_IN_DEFAULT;

    //输出声道
    public static int OUTPUT_CHANNEL = AudioFormat.CHANNEL_OUT_DEFAULT;

    //编码
    public static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //输入缓冲区字节大小
    public static int INPUT_BUFFER_SIZE = Utils.getMinBufferSize(false);

    //输出缓冲区字节大小
    public static int OUTPUT_BUFFER_SIZE = Utils.getMinBufferSize(true);

    //帧缓冲区大小
    public static int FRAMES_PER_BUFFER;

    static {
        loadInfo();
        AudioManager audioManager = (AudioManager) App.getContext().getSystemService(Context.AUDIO_SERVICE);
        FRAMES_PER_BUFFER = Utils.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER), 192);

    }

    public static void loadInfo() {
        Context context = App.getContext();
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        AUDIO_SOURCE = Utils.getValueFromArray(TYPE.AUDIO_SOURCE, preferences.getInt(Constants.AUDIO_SOURCE, AUDIO_SOURCE));
        INPUT_SAMPLE_RATE = Utils.getValueFromArray(TYPE.AUDIO_SAMPLE_RATE, preferences.getInt(Constants.AUDIO_SAMPLE_RATE, INPUT_SAMPLE_RATE));
        INPUT_CHANNEL = Utils.getValueFromArray(TYPE.AUDIO_CHANNEL, preferences.getInt(Constants.INPUT_CHANNEL, INPUT_CHANNEL));
        AUDIO_ENCODING = Utils.getValueFromArray(TYPE.AUDIO_ENCODING, preferences.getInt(Constants.AUDIO_ENCODING, AUDIO_ENCODING));
        INPUT_BUFFER_SIZE = Utils.getMinBufferSize(false);

        OUTPUT_CHANNEL = Utils.getValueFromArray(TYPE.AUDIO_CHANNEL, preferences.getInt(Constants.OUTPUT_CHANNEL, OUTPUT_CHANNEL));
        OUTPUT_BUFFER_SIZE = Utils.getMinBufferSize(true);
    }

}
