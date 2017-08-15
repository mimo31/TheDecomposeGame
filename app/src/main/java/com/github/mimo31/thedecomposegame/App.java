package com.github.mimo31.thedecomposegame;

import android.app.Application;

/**
 * Created by Viktor on 1/27/2016.
 * <p>
 * Extends the classical Application class to initialize levels and load data on startup.
 */
public class App extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();
        ClickField.initializeClickFields();
        Level.initializeLevels();
        IO.loadData(this.getApplicationContext());
    }

}
