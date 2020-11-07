package com.cyzen.denoise.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cyzen.denoise.BaseActivity;
import com.cyzen.denoise.R;
import com.cyzen.denoise.record.RecordInfo;
import com.cyzen.denoise.utils.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DenoiseActivity extends BaseActivity implements View.OnClickListener {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler timeHandler = new Handler(Looper.getMainLooper());

    public static int mElapsedTime = -1;

    public static int DATA_LENGTH = RecordInfo.OUTPUT_BUFFER_SIZE / 4;
    public static int audioLatencyMs = -1;

    private boolean hasGetUnderrunCount = false;

    TextView info_tv;
    BarChart barChart;
    Button denoise_btn;
    CheckBox denoise_cb;

    private AudioManager audioManager;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    private final List<BarEntry> barEntries = new ArrayList<>();
    private final BarDataSet barDataSet = new BarDataSet(barEntries, "");
    private final BarData barData = new BarData(barDataSet);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denoise);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        initView();

        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(8);

        YAxis axisLeft = barChart.getAxisLeft();
        axisLeft.setLabelCount(8);
        axisLeft.setAxisMaximum(1000);
        axisLeft.setAxisMinimum(-1000);

        YAxis axisRight = barChart.getAxisRight();
        axisRight.setLabelCount(8);
        axisRight.setAxisMaximum(1000);
        axisRight.setAxisMinimum(-1000);

        //barChart.setPinchZoom(true);
        barChart.setFitBars(true);

        barDataSet.setColor(getResources().getColor(R.color.light_blue_500));
    }

    private String info;

    @Override
    protected void onResume() {
        super.onResume();
        info = getResources().getStringArray(R.array.audio_source)[Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_SOURCE, RecordInfo.AUDIO_SOURCE)] + "|" +
                RecordInfo.OUTPUT_SAMPLE_RATE + "Hz|" +
                getResources().getStringArray(R.array.audio_channel)[Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_CHANNEL, RecordInfo.INPUT_CHANNEL)] + "|" +
                getResources().getStringArray(R.array.audio_encoding)[Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_ENCODING, RecordInfo.AUDIO_ENCODING)];

        info += "\n低延迟音频=" + getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
        if (Utils.IS_MARSHMALLOW) {
            info += "  专业音频=" + getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);
        }

        try {
            Method getOutputLatency = audioManager.getClass().getMethod("getOutputLatency", int.class);
            int latencyMs = (Integer) getOutputLatency.invoke(audioManager, AudioManager.STREAM_MUSIC);
            info += "\n硬件延迟=" + latencyMs + "ms";
        } catch (Exception e) {
            e.printStackTrace();
        }

        info_tv.setText(info);

        audioLatencyMs = -1;

        //设置list大小
        barEntries.clear();
        for (int i = 0; i < DATA_LENGTH; i++) {
            barEntries.add(new BarEntry(i, 0));
        }
        barDataSet.notifyDataSetChanged();
        barData.notifyDataChanged();
        barChart.setData(barData);
    }

    public byte[] dataByte;
    public short[] dataShort;
    public float[] dataFloat;

    //反向
    public static boolean reserve = false;

    public RecordThread recordThread = null;

    public static int counts = 0;

    private class RecordThread extends Thread {

        private boolean stop = false;

        public synchronized void Stop() {
            this.stop = true;
            try {
                timeHandler.removeCallbacks(runnable);

                audioRecord.stop();
                audioTrack.stop();
                audioRecord.release();
                audioTrack.release();
                Utils.stopThread(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            //设置最高优先级
            setPriority(Thread.MAX_PRIORITY);
            try {
                audioRecord = new AudioRecord(RecordInfo.AUDIO_SOURCE, RecordInfo.INPUT_SAMPLE_RATE, RecordInfo.INPUT_CHANNEL, RecordInfo.AUDIO_ENCODING, RecordInfo.INPUT_BUFFER_SIZE);
                if (Utils.IS_OREO) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(RecordInfo.AUDIO_ENCODING)
                                    .setSampleRate(RecordInfo.OUTPUT_SAMPLE_RATE)
                                    .setChannelMask(RecordInfo.OUTPUT_CHANNEL)
                                    .build())
                            .setBufferSizeInBytes(RecordInfo.OUTPUT_BUFFER_SIZE)
                            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                            .build();
                    audioTrack.setBufferSizeInFrames(DATA_LENGTH);

                    hasGetUnderrunCount = false;
                } else if (Utils.IS_MARSHMALLOW) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(RecordInfo.AUDIO_ENCODING)
                                    .setSampleRate(RecordInfo.OUTPUT_SAMPLE_RATE)
                                    .setChannelMask(RecordInfo.OUTPUT_CHANNEL)
                                    .build())
                            .setBufferSizeInBytes(RecordInfo.OUTPUT_BUFFER_SIZE)
                            .build();
                } else {
                    audioTrack = new AudioTrack(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                            new AudioFormat.Builder()
                                    .setEncoding(RecordInfo.AUDIO_ENCODING)
                                    .setSampleRate(RecordInfo.OUTPUT_SAMPLE_RATE)
                                    .setChannelMask(RecordInfo.OUTPUT_CHANNEL)
                                    .build(),
                            RecordInfo.OUTPUT_BUFFER_SIZE, AudioTrack.MODE_STREAM, audioManager.generateAudioSessionId());
                }


                if (audioLatencyMs == -1) {
                    try {
                        Method getLatency = audioTrack.getClass().getMethod("getLatency");
                        audioLatencyMs = (Integer) getLatency.invoke(audioTrack);
                        info += "   Track=" + audioLatencyMs + "ms";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    audioRecord.startRecording();
                    audioTrack.play();

                    if (RecordInfo.AUDIO_ENCODING == AudioFormat.ENCODING_PCM_8BIT) {
                        dataByte = new byte[DATA_LENGTH];
                        recordByte();
                    } else if (RecordInfo.AUDIO_ENCODING == AudioFormat.ENCODING_PCM_16BIT) {
                        dataShort = new short[DATA_LENGTH];
                        recordShort();
                    } else {
                        dataFloat = new float[DATA_LENGTH];
                        recordFloat();
                    }

                } catch (Exception start) {
                    start.printStackTrace();
                    Stop();
                }
            } catch (Exception create) {
                create.printStackTrace();
                Stop();
            }
        }

        public void recordByte() {
            while (!stop) {
                try {
                    audioRecord.read(dataByte, 0, DATA_LENGTH/*, AudioRecord.READ_NON_BLOCKING*/);

                    if (reserve) {
                        for (int i = 0; i < dataByte.length; i++) {
                            dataByte[i] = (byte) (-((int) dataByte[i]) & 0xff);
                        }
                    }

                    audioTrack.write(dataByte, 0, DATA_LENGTH/*, AudioTrack.WRITE_NON_BLOCKING*/);

                    //画图
                    drawChart(1);
                    counts++;
                } catch (Exception record) {
                    record.printStackTrace();
                    Stop();
                }
            }
        }

        public void recordShort() {
            while (!stop && audioRecord.read(dataShort, 0, DATA_LENGTH /*, AudioRecord.READ_NON_BLOCKING*/) == DATA_LENGTH) {
                try {

                    if (reserve) {
                        for (int i = 0; i < dataShort.length; i++) {
                            dataShort[i] = (short) -dataShort[i];
                        }
                    }

                    audioTrack.write(dataShort, 0, DATA_LENGTH /*, AudioTrack.WRITE_NON_BLOCKING*/);
                    if (!hasGetUnderrunCount && Utils.IS_NOUGAT) {
                        hasGetUnderrunCount = true;
                        audioTrack.getUnderrunCount();
                    }
                    //画图
                    drawChart(2);
                    counts++;
                } catch (Exception record) {
                    record.printStackTrace();
                    Stop();
                }
            }
        }

        public void recordFloat() {
            while (!stop) {
                try {
                    audioRecord.read(dataFloat, 0, DATA_LENGTH, AudioRecord.READ_BLOCKING);

                    if (reserve) {
                        for (int i = 0; i < dataFloat.length; i++) {
                            dataFloat[i] = -dataFloat[i];
                        }
                    }

                    audioTrack.write(dataFloat, 0, DATA_LENGTH, AudioTrack.WRITE_BLOCKING);

                    //画图
                    drawChart(3);
                    counts++;
                } catch (Exception record) {
                    record.printStackTrace();
                    Stop();
                }
            }
        }

    }


    private boolean canDraw = true;

    private void drawChart(int type) {
        handler.post(() -> {
            if (canDraw) {
                canDraw = false;

                try {
                    if (type == 3) {
                        //计算声音分贝大小
                        double values = 0;
                        for (int i = 0; i < dataFloat.length; i++) {
                            values += dataFloat[i] * dataFloat[i];
                            barEntries.set(i, new BarEntry(i, dataFloat[i]));
                        }
                        //平方和除以数据总长度，得到音量大小
                        double volume = 10 * Math.log10((double) values / dataFloat.length);
                        setDbInfo(volume);
                    } else if (type == 2) {
                        //计算声音分贝大小
                        long values = 0;
                        for (int i = 0; i < dataShort.length; i++) {
                            values += dataShort[i] * dataShort[i];
                            barEntries.set(i, new BarEntry(i, dataShort[i]));
                        }
                        //平方和除以数据总长度，得到音量大小
                        double volume = 10 * Math.log10((double) values / dataShort.length);
                        setDbInfo(volume);
                    } else {
                        //计算声音分贝大小
                        int values = 0;
                        for (int i = 0; i < dataByte.length; i++) {
                            values += dataByte[i] * dataByte[i];
                            barEntries.set(i, new BarEntry(i, dataByte[i]));
                        }
                        //平方和除以数据总长度，得到音量大小
                        double volume = 10 * Math.log10((double) values / dataByte.length);
                        setDbInfo(volume);
                    }

                    //barDataSet.notifyDataSetChanged();
                    //barData.notifyDataChanged();
                    //barChart.notifyDataSetChanged();
                    barChart.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                canDraw = true;
            }
        });
    }

    public void setDbInfo(double volume) {
        info_tv.setText(String.format(info + "\n平均：%.02f 分贝\n次数：%d  时间：%d s", volume, counts, mElapsedTime));
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mElapsedTime++;
            timeHandler.postDelayed(runnable, 1000);
        }
    };


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.denoise_btn) {
            if (recordThread == null) {
                recordThread = new RecordThread();
                recordThread.start();

                denoise_btn.setText("停止");

                //计时
                mElapsedTime = -1;
                counts = 0;
                runnable.run();
            } else {
                recordThread.Stop();
                recordThread = null;
                denoise_btn.setText("开始");
            }
        } else if (id == R.id.denoise_cb) {
            denoise_cb.setChecked(reserve = !reserve);
        }
    }

    private void initView() {
        initToolBar(false);
        info_tv = findViewById(R.id.info_tv);
        barChart = findViewById(R.id.bar_chart);
        denoise_btn = findViewById(R.id.denoise_btn);
        denoise_btn.setOnClickListener(this);
        denoise_cb = findViewById(R.id.denoise_cb);
        denoise_cb.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            if (recordThread == null) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else {
                Snackbar.make(info_tv.getRootView(), "请先停止录音", Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
