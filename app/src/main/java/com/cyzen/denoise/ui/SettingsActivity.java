package com.cyzen.denoise.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.cyzen.denoise.App;
import com.cyzen.denoise.BaseActivity;
import com.cyzen.denoise.Constants;
import com.cyzen.denoise.R;
import com.cyzen.denoise.adapter.MyListAdapter;
import com.cyzen.denoise.record.RecordInfo;
import com.cyzen.denoise.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private final List<Map<String, Object>> list = new ArrayList<>();

    private static final List<String> sourceList = getSourceList();
    private static final List<String> sampleRateList = getSampleRateList();
    private static final List<String> channelList = getChannelList();
    private static final List<String> encodingList = getEncodingList();

    private int dialogPos;

    private SharedPreferences preferences;

    private int sourcePos = 0, channelPos = 0, encodingPos = 0;

    private ListView listView;
    private MyListAdapter adapter;

    private AlertDialog alertDialog;

    private void initList() {
        if (!list.isEmpty()) {
            list.clear();
        }
        Map<String, Object> map;

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_source);
        map.put(Constants.val, sourceList.get((sourcePos = Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_SOURCE, preferences.getInt(Constants.AUDIO_SOURCE, 0)))));
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_sample_rate);
        map.put(Constants.val, RecordInfo.AUDIO_SAMPLE_RATE + "Hz");
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_channel);
        map.put(Constants.val, channelList.get((channelPos = preferences.getInt(Constants.AUDIO_CHANNEL, 1) - 1)));
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_encoding);
        map.put(Constants.val, encodingList.get((encodingPos = Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_ENCODING, preferences.getInt(Constants.AUDIO_ENCODING, 0)))));
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.buffer_size);
        map.put(Constants.val, getString(R.string.input) + ": " + RecordInfo.INPUT_BUFFER_SIZE + getString(R.string.bytes) + "\n" +
                getString(R.string.output) + ": " + RecordInfo.OUTPUT_BUFFER_SIZE + getString(R.string.bytes));
        list.add(map);


        if (adapter == null) {
            adapter = new MyListAdapter(list, new String[]{Constants.key, Constants.val, Constants.bool});
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initToolBar(true).setNavigationOnClickListener(v -> finish());

        preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        listView = findViewById(R.id.listView);

        initList();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SwitchCompat sw = view.findViewById(R.id.Switch);
            int key = (int) list.get(position).get(Constants.key);
            if (key == R.string.audio_source) {
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(key)
                        .setSingleChoiceItems(sourceList.toArray(new String[0]), (dialogPos = sourcePos), (dialog, which) -> dialogPos = which)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            preferences.edit().putInt(Constants.AUDIO_SOURCE, RecordInfo.AUDIO_SOURCES[dialogPos]).apply();

                            initList();
                            RecordInfo.loadInfo();
                        }).setNegativeButton(R.string.cancel, null).create();
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            } else if (key == R.string.audio_sample_rate) {
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(key)
                        .setMessage(String.format(getString(R.string.audio_sample_rate_alert), RecordInfo.AUDIO_SAMPLE_RATE))
                        .setPositiveButton(R.string.ok, null)
                        .show();
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            } else if (key == R.string.audio_channel) {
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(key)
                        .setSingleChoiceItems(channelList.toArray(new String[0]), (dialogPos = channelPos), (dialog, which) -> dialogPos = which)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            preferences.edit().putInt(Constants.AUDIO_CHANNEL, dialogPos + 1).apply();

                            initList();
                            RecordInfo.loadInfo();
                        }).setNegativeButton(R.string.cancel, null).create();
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            } else if (key == R.string.audio_encoding) {
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(key)
                        .setSingleChoiceItems(encodingList.toArray(new String[0]), (dialogPos = encodingPos), (dialog, which) -> dialogPos = which)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            encodingPos = dialogPos;
                            preferences.edit().putInt(Constants.AUDIO_ENCODING, RecordInfo.AUDIO_ENCODINGS[dialogPos]).apply();

                            initList();
                            RecordInfo.loadInfo();
                        }).setNegativeButton(R.string.cancel, null).show();
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            }
        });
    }

    //获取设备支持的音频来源
    private static List<String> getSourceList() {
        List<String> list = new ArrayList<>();
        String[] array = App.getContext().getResources().getStringArray(R.array.audio_source);
        for (int i = 0; i < 5; i++) {
            list.add(array[i]);
        }
        if (Utils.IS_NOUGAT) {
            list.add(array[5]);
        }
        if (Utils.IS_Q) {
            list.add(array[6]);
        }
        return list;
    }

    //获取设备支持的采样率
    private static List<String> getSampleRateList() {
        List<String> list = new ArrayList<>();
        int[] ints = RecordInfo.AUDIO_SAMPLE_RATES;
        for (int anInt : ints) {
            list.add(anInt + "Hz");
        }

        return list;
    }

    //获取设备支持的声道
    private static List<String> getChannelList() {
        String[] array = App.getContext().getResources().getStringArray(R.array.audio_channel);
        return new ArrayList<>(Arrays.asList(array));
    }

    //获取设备支持的编码
    private static List<String> getEncodingList() {
        List<String> list = new ArrayList<>();
        String[] array = App.getContext().getResources().getStringArray(R.array.audio_encoding);
        list.add(array[0]);
        if (Utils.IS_MARSHMALLOW) {
            list.add(array[1]);
        }

        return list;
    }

}
