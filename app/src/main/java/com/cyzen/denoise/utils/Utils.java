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

    public static int getMinBufferSize(boolean isOutput) {
        if (isOutput) {
            return AudioTrack.getMinBufferSize(RecordInfo.OUTPUT_SAMPLE_RATE, RecordInfo.OUTPUT_CHANNEL, RecordInfo.AUDIO_ENCODING);
        } else
            return AudioRecord.getMinBufferSize(RecordInfo.INPUT_SAMPLE_RATE, RecordInfo.INPUT_CHANNEL, RecordInfo.AUDIO_ENCODING);
    }


    /**
     * 根据值获取在数组中的下标
     *
     * @param type  数组类型
     * @param value 值
     * @return 下标
     */
    public static int getPosFromArray(RecordInfo.TYPE type, int value) {
        switch (type) {
            case AUDIO_SOURCE:
                for (int i = 0; i < RecordInfo.AUDIO_SOURCES.length; i++) {
                    if (RecordInfo.AUDIO_SOURCES[i] == value) {
                        return i;
                    }
                }
                return 0;
            case AUDIO_SAMPLE_RATE:
                for (int i = 0; i < RecordInfo.AUDIO_SAMPLE_RATES.length; i++) {
                    if (RecordInfo.AUDIO_SAMPLE_RATES[i] == value) {
                        return i;
                    }
                }
                return 7;
            case AUDIO_CHANNEL:
                for (int i = 0; i < RecordInfo.AUDIO_CHANNELS.length; i++) {
                    if (RecordInfo.AUDIO_CHANNELS[i] == value) {
                        if (i > 2) {//输出声道
                            return i - 3;
                        } else return i;
                    }
                }
                return 0;
            case AUDIO_ENCODING:
                for (int i = 0; i < RecordInfo.AUDIO_ENCODINGS.length; i++) {
                    if (RecordInfo.AUDIO_ENCODINGS[i] == value) {
                        return i;
                    }
                }
                return 0;
            default:
                return 0;
        }
    }

    /**
     * 获取合理的值
     *
     * @param type 数组类型
     * @param pos  值
     * @return 合理的值
     */
    public static int getValueFromArray(RecordInfo.TYPE type, int pos) {
        switch (type) {
            case AUDIO_SOURCE:
                for (int i : RecordInfo.AUDIO_SOURCES) {
                    if (i == pos) {
                        return i;
                    }
                }
                return RecordInfo.AUDIO_SOURCE;
            case AUDIO_SAMPLE_RATE:
                for (int i : RecordInfo.AUDIO_SAMPLE_RATES) {
                    if (i == pos) {
                        return i;
                    }
                }
                return RecordInfo.OUTPUT_SAMPLE_RATE;
            case AUDIO_CHANNEL:
                for (int i : RecordInfo.AUDIO_CHANNELS) {
                    if (i == pos) {
                        return i;
                    }
                }
                return RecordInfo.INPUT_CHANNEL;
            case AUDIO_ENCODING:
                for (int i : RecordInfo.AUDIO_ENCODINGS) {
                    if (i == pos) {
                        return i;
                    }
                }
                return RecordInfo.AUDIO_ENCODING;
            default:
                return 0;
        }
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
    //安卓7.1判断
    public static final boolean IS_NOUGAT_MR1 = Build.VERSION.SDK_INT >= 25;
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
