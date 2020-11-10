package com.cyzen.denoise;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final String[] PERMISSION_ALL = {
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission(this);
    }

    public void checkPermission(Activity activity) {
        boolean granted = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
        if (!granted) {
            ActivityCompat.requestPermissions(activity, PERMISSION_ALL, REQUEST_CODE);
        }
    }

    public Toolbar initToolBar(boolean homeAsUp) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUp);
        }
        return toolbar;
    }

}
