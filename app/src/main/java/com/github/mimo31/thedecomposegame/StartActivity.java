package com.github.mimo31.thedecomposegame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_start);
        ClickField.availableClickFields = new ClickField[2];
        ClickField.availableClickFields[0] = new ClickField(new boolean[][] { new boolean[] { true, false, true }, new boolean[] { true, false, true }, new boolean[] { true, false, true }}, 1, 1);
        ClickField.availableClickFields[1] = new ClickField(new boolean[][] { new boolean[] { true, true, true }, new boolean[] { false, true, false }, new boolean[] { false, true, false }}, 1, 1);
    }

    public void goToChooseLevel(View view) {
        if (view.getId() == R.id.playButton) {
            Intent intent = new Intent(this, ChooseLevelActivity.class);
            this.startActivity(intent);
        }
    }

    public void goToHelp(View v) {
        if (v.getId() == R.id.howToButton) {
            this.startActivity(new Intent(this, HelpActivity.class));
        }
    }
}
