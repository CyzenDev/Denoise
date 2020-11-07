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

    private int sourcePos = 0, sampleRatePos = 0, channelPos1 = 0, encodingPos = 0;
    private int channelPos2 = 0;

    private ListView listView;
    private MyListAdapter adapter;

    private AlertDialog alertDialog;

    private void initList() {
        if (!list.isEmpty()) {
            list.clear();
        }
        Map<String, Object> map;

        //Input
        map = new HashMap<>();
        map.put(Constants.category, R.string.input);
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_source);
        map.put(Constants.val, sourceList.get((sourcePos = Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_SOURCE, preferences.getInt(Constants.AUDIO_SOURCE, 0)))));
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_sample_rate);
        map.put(Constants.val, sampleRateList.get((sampleRatePos = Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_SAMPLE_RATE, preferences.getInt(Constants.AUDIO_SAMPLE_RATE, 0)))));
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_channel);
        map.put(Constants.val, getResources().getStringArray(R.array.audio_channel)[(channelPos1 = Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_CHANNEL, preferences.getInt(Constants.INPUT_CHANNEL, 0)))]);
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_encoding);
        map.put(Constants.val, encodingList.get((encodingPos = Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_ENCODING, preferences.getInt(Constants.AUDIO_ENCODING, 0)))));
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.buffer_size);
        map.put(Constants.val, Utils.getMinBufferSize(false) + getString(R.string.bytes));
        list.add(map);


        //Output
        map = new HashMap<>();
        map.put(Constants.category, R.string.output);
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.audio_channel);
        map.put(Constants.val, channelList.get((channelPos2 = Utils.getPosFromArray(RecordInfo.TYPE.AUDIO_CHANNEL, preferences.getInt(Constants.OUTPUT_CHANNEL, 0)))));
        list.add(map);

        map = new HashMap<>();
        map.put(Constants.key, R.string.buffer_size);
        map.put(Constants.val, Utils.getMinBufferSize(true) + getString(R.string.bytes));
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
                        .setSingleChoiceItems(sampleRateList.toArray(new String[0]), (dialogPos = sampleRatePos), (dialog, which) -> dialogPos = which)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            preferences.edit().putInt(Constants.AUDIO_SAMPLE_RATE, Utils.parseInt(sampleRateList.get(dialogPos).split("Hz")[0])).apply();

                            initList();
                            RecordInfo.loadInfo();
                        }).setNegativeButton(R.string.cancel, null).show();
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            } else if (key == R.string.audio_channel) {
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(key)
                        .setSingleChoiceItems(channelList.toArray(new String[0]), (dialogPos = (position == 3 ? channelPos1 : channelPos2)), (dialog, which) -> dialogPos = which)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            if (position == 3) {
                                preferences.edit().putInt(Constants.INPUT_CHANNEL, RecordInfo.AUDIO_CHANNELS[dialogPos]).apply();
                            } else {
                                preferences.edit().putInt(Constants.OUTPUT_CHANNEL, RecordInfo.AUDIO_CHANNELS[dialogPos + 3]).apply();
                            }

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

    //获取设备支持的采样率
    private static List<String> getChannelList() {
        String[] array = App.getContext().getResources().getStringArray(R.array.audio_channel);
        return new ArrayList<>(Arrays.asList(array));
    }

    //获取设备支持的编码
    private static List<String> getEncodingList() {
        List<String> list = new ArrayList<>();
        String[] array = App.getContext().getResources().getStringArray(R.array.audio_encoding);
        for (int i = 0; i < 2; i++) {
            list.add(array[i]);
        }
        if (Utils.IS_MARSHMALLOW) {
            list.add(array[2]);
        }

        return list;
    }

}
