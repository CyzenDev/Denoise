package com.cyzen.denoise.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.cyzen.denoise.Constants;
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
import java.util.Locale;

public class DenoiseActivity extends BaseActivity implements View.OnClickListener {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler timeHandler = new Handler(Looper.getMainLooper());

    public static int mElapsedTime = -1;

    public static int DATA_LENGTH = RecordInfo.FRAMES_PER_BUFFER * 4;
    public static int audioLatencyMs = -1;

    TextView info_tv;
    BarChart barChart;
    Button denoise_btn;
    CheckBox denoise_cb;
    TextView latency_tv;

    private AudioManager audioManager;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    private final List<BarEntry> barEntries = new ArrayList<>();
    private final BarDataSet barDataSet = new BarDataSet(barEntries, "");
    private final BarData barData = new BarData(barDataSet);

    private SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denoise);
        initView();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);


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
    private String infoFormat;

    @Override
    protected void onResume() {
        super.onResume();
        info = String.format(Locale.getDefault(), "%s  %dHz  %s\n" + getString(R.string.audio_low_latency) + "=%b",
                getResources().getStringArray(R.array.audio_source)[Utils.getSourcePos(RecordInfo.AUDIO_SOURCE)],
                RecordInfo.AUDIO_SAMPLE_RATE,
                getResources().getStringArray(R.array.audio_channel)[preferences.getInt(Constants.AUDIO_CHANNEL, 1) - 1],
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY));
        if (Utils.IS_MARSHMALLOW) {
            info += "  " + getString(R.string.audio_pro) + "=" + getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);
        }

        try {
            Method getOutputLatency = audioManager.getClass().getMethod("getOutputLatency", int.class);
            int latencyMs = (Integer) getOutputLatency.invoke(audioManager, AudioManager.STREAM_MUSIC);
            info += "\n" + getString(R.string.hardware_latency) + "=" + latencyMs + "ms";
        } catch (Exception e) {
            e.printStackTrace();
        }
        info_tv.setText(info = info.replace("true", getString(R.string.yes)).replace("false", getString(R.string.no)));

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

    public short[] audioData;

    //反向
    public static boolean reserve = true;

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
                recordThread = null;

                handler.post(() -> {
                    latency_tv.setText("");
                    denoise_btn.setText(R.string.start);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            //设置最高优先级
            setPriority(Thread.MAX_PRIORITY);
            try {
                //AudioRecord
                if (Utils.IS_MARSHMALLOW) {
                    audioRecord = new AudioRecord.Builder()
                            .setAudioSource(RecordInfo.AUDIO_SOURCE)
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(RecordInfo.AUDIO_ENCODING)
                                    .setSampleRate(RecordInfo.AUDIO_SAMPLE_RATE)
                                    .setChannelMask(RecordInfo.INPUT_CHANNEL)
                                    .build())
                            .setBufferSizeInBytes(RecordInfo.INPUT_BUFFER_SIZE)
                            .build();
                } else {
                    audioRecord = new AudioRecord(RecordInfo.AUDIO_SOURCE, RecordInfo.AUDIO_SAMPLE_RATE, RecordInfo.INPUT_CHANNEL, RecordInfo.AUDIO_ENCODING, RecordInfo.INPUT_BUFFER_SIZE);
                }

                //AudioFormat
                AudioFormat audioFormat = new AudioFormat.Builder()
                        .setEncoding(RecordInfo.AUDIO_ENCODING)
                        .setSampleRate(RecordInfo.AUDIO_SAMPLE_RATE)
                        .setChannelMask(RecordInfo.OUTPUT_CHANNEL)
                        .build();

                //AudioTrack
                if (Utils.IS_OREO) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build())
                            .setAudioFormat(audioFormat)
                            //.setBufferSizeInBytes(RecordInfo.OUTPUT_BUFFER_SIZE)
                            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                            .build();

                    audioTrack.setBufferSizeInFrames(DATA_LENGTH);

                } else if (Utils.IS_NOUGAT) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                                    .build())
                            .setAudioFormat(audioFormat)
                            //.setBufferSizeInBytes(RecordInfo.OUTPUT_BUFFER_SIZE)
                            .build();

                    audioTrack.setBufferSizeInFrames(DATA_LENGTH);

                } else if (Utils.IS_MARSHMALLOW) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build())
                            .setAudioFormat(audioFormat)
                            .setBufferSizeInBytes(RecordInfo.OUTPUT_BUFFER_SIZE)
                            .build();
                } else {
                    audioTrack = new AudioTrack(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build(),
                            audioFormat, RecordInfo.OUTPUT_BUFFER_SIZE, AudioTrack.MODE_STREAM, audioManager.generateAudioSessionId());
                }


                //获取延迟
                if (audioLatencyMs == -1) {
                    try {
                        if (Utils.IS_PIE) {
                            info += "   " + getString(R.string.input) + "=" + audioRecord.getMetrics().getInt(AudioRecord.MetricsConstants.LATENCY) + "ms";
                        }
                        Method getLatency = audioTrack.getClass().getMethod("getLatency");
                        audioLatencyMs = (Integer) getLatency.invoke(audioTrack);
                        info += "   " + getString(R.string.output) + "=" + audioLatencyMs + "ms";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                infoFormat = info + "\n" + getString(R.string.average) + ": %.02f " + getString(R.string.db) + "\n" + getString(R.string.count) + ": %d  " + getString(R.string.time) + ": %d s";


                //获取低延迟模式
                if (Utils.IS_OREO) {
                    handler.post(() -> latency_tv.setText(String.format(getString(R.string.low_latency_mode) + "=%s", ((audioTrack.getPerformanceMode() == AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) ? getString(R.string.yes) : getString(R.string.no)))));
                }


                try {
                    audioRecord.startRecording();
                    audioTrack.play();

                    audioData = new short[DATA_LENGTH];
                    while (!stop) {
                        try {
                            audioRecord.read(audioData, 0, DATA_LENGTH /*, AudioRecord.READ_NON_BLOCKING*/);

                            if (reserve) {
                                for (int i = 0; i < DATA_LENGTH; i++) {
                                    audioData[i] = (short) -audioData[i];
                                }
                            }

                            audioTrack.write(audioData, 0, DATA_LENGTH /*, AudioTrack.WRITE_NON_BLOCKING*/);
                            //画图
                            drawChart();
                            counts++;
                        } catch (Exception record) {
                            record.printStackTrace();
                            Stop();
                        }
                    }
                } catch (Exception start) {
                    start.printStackTrace();
                    Stop();
                    Snackbar.make(info_tv.getRootView(), R.string.record_failed, Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception create) {
                create.printStackTrace();
                Stop();
            }
        }
    }


    private boolean canDraw = true;

    private void drawChart() {
        handler.post(() -> {
            if (canDraw) {
                canDraw = false;

                try {
                    //计算声音分贝大小
                    long values = 0;
                    for (int i = 0; i < audioData.length; i++) {
                        values += audioData[i] * audioData[i];
                        barEntries.set(i, new BarEntry(i, audioData[i]));
                    }
                    //平方和除以数据总长度，得到音量大小
                    double volume = 10 * Math.log10((double) values / audioData.length);
                    setDbInfo(volume);


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
        info_tv.setText(String.format(infoFormat, volume, counts, mElapsedTime));
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

                denoise_btn.setText(R.string.stop);

                //计时
                mElapsedTime = -1;
                counts = 0;
                runnable.run();
            } else {
                recordThread.Stop();
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
        latency_tv = findViewById(R.id.latency_tv);
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
                Snackbar.make(info_tv.getRootView(), R.string.plz_stop_record, Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
