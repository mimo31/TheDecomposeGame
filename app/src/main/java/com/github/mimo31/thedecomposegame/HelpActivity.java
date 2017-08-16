package com.github.mimo31.thedecomposegame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Viktor on 1/31/2016.
 *
 * Shows a few lines of text describing what is the game about.
 */
public class HelpActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_help);
    }

}
