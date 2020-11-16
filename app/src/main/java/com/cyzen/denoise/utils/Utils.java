package com.cyzen.denoise.utils;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;

import com.cyzen.denoise.record.RecordInfo;

public class Utils {


    public static void stopThread(Thread thread) {
        try {
            thread.interrupt();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取输入/输出最小缓冲区大小
    public static int getMinBufferSize(boolean isOutput) {
        if (isOutput) {
            return AudioTrack.getMinBufferSize(RecordInfo.AUDIO_SAMPLE_RATE, RecordInfo.OUTPUT_CHANNEL, RecordInfo.AUDIO_ENCODING);
        } else
            return AudioRecord.getMinBufferSize(RecordInfo.AUDIO_SAMPLE_RATE, RecordInfo.INPUT_CHANNEL, RecordInfo.AUDIO_ENCODING);
    }


    /**
     * 根据值获取音频来源数组的下标
     *
     * @param value 值
     * @return 下标
     */
    public static int getSourcePos(int value) {
        for (int i = 0; i < RecordInfo.AUDIO_SOURCES.length; i++) {
            if (RecordInfo.AUDIO_SOURCES[i] == value) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 获取合理的音频来源值
     *
     * @param pos 值
     * @return 合理的值
     */
    public static int getSourceValueFromArray(int pos) {
        for (int i : RecordInfo.AUDIO_SOURCES) {
            if (i == pos) {
                return i;
            }
        }
        return RecordInfo.AUDIO_SOURCE;
    }


    //安卓11判断
    public static final boolean IS_R = Build.VERSION.SDK_INT >= 30;
    //安卓10判断
    public static final boolean IS_Q = Build.VERSION.SDK_INT >= 29;
    //安卓9判断
    public static final boolean IS_PIE = Build.VERSION.SDK_INT >= 28;
    //安卓8.0判断
    public static final boolean IS_OREO = Build.VERSION.SDK_INT >= 26;
    //安卓7.0判断
    public static final boolean IS_NOUGAT = Build.VERSION.SDK_INT >= 24;
    //安卓6判断
    public static final boolean IS_MARSHMALLOW = Build.VERSION.SDK_INT >= 23;


    //String to Integer
    public static int parseInt(String str) {
        return parseInt(str, 0);
    }

    public static int parseInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
