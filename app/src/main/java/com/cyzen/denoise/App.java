package com.cyzen.denoise;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class App extends Application {

    private static Handler mainHandler;
    private static Context AppContext;
    private static App instance;

    public static synchronized App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext = this;
        instance = this;
        mainHandler = new Handler(Looper.myLooper());
    }

    public static Context getContext() {
        return AppContext;
    }

    public static Handler getHandler() {
        return mainHandler;
    }

}
