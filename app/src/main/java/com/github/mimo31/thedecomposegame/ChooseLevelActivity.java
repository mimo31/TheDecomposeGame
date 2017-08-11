package com.github.mimo31.thedecomposegame;

import android.content.Intent;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class ChooseLevelActivity extends AppCompatActivity {

    private LevelListView plane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.plane = new LevelListView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(this.plane, layoutParams);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.plane.getCloseReady();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.plane.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void goToLevel(int level) {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("level", level);
        this.startActivity(intent);
    }

    private class LevelListView extends View implements Runnable{

        ChooseLevelActivity attachedActivity;
        GestureDetectorCompat gestureDetector;
        private float listPosition = 0;
        private float listVelocity;
        private boolean keepUpdating = true;
        private Handler updateHandler = new Handler();

        private LevelListView(ChooseLevelActivity attachedActivity) {
            super(attachedActivity);
            this.attachedActivity = attachedActivity;
            this.setBackgroundColor(Color.WHITE);
            this.gestureDetector = new GestureDetectorCompat(this.attachedActivity.getApplicationContext(), new GestureListener(this));
            this.updateHandler.postDelayed(this, 17);
        }

        @Override
        public void run() {
            this.update();
            if (this.keepUpdating) {
                this.updateHandler.postDelayed(this, 17);
            }
        }

        public void getCloseReady() {
            this.keepUpdating = false;
        }

        public void update() {
            if (this.listVelocity != 0) {
                this.listPosition += this.listVelocity;
                this.checkListPositionOverflow();
                this.listVelocity *= .99;
                if (this.listVelocity < 0.01 && this.listVelocity > -0.01) {
                    this.listVelocity = 0;
                }
                this.postInvalidate();
            }
        }

        @Override
        public void onDraw(Canvas canvas) {
            int minLevelToDraw = (int) this.listPosition;
            int maxLevelToDraw = (int) (this.listPosition + this.getLevelsOnScreen() + 1);
            if (maxLevelToDraw > Level.maxLevel) {
                maxLevelToDraw = Level.maxLevel;
            }
            for (int i = minLevelToDraw; i <= maxLevelToDraw; i++) {
                this.drawLevel(i, canvas);
            }
        }

        private void drawLevel(int levelNumber, Canvas canvas) {
            float yToStart = (levelNumber - this.listPosition) * this.getWidth() / 4;
            Paint p = new Paint();
            p.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
            p.setStrokeWidth(this.getWidth() / 128);
            canvas.drawLine(0, (int) yToStart, this.getWidth(), (int) yToStart, p);
            canvas.drawLine(0, (int) yToStart + this.getWidth() / 4, this.getWidth(), (int) yToStart + this.getWidth() / 4, p);
            p.setTextSize(this.getWidth() / (float) 16);
            canvas.drawText("Level " + String.valueOf(levelNumber + 1), this.getWidth() / 64, yToStart + this.getWidth() / 8 + this.getWidth() / 32, p);
            p.setTextSize(this.getWidth() / (float) 32);
            int levelBestTime = Level.bestTimes[levelNumber];
            if (levelBestTime != 0) {
                canvas.drawText(PlayActivity.formatTime(levelBestTime) + " s", this.getWidth() - this.getWidth() / 6, yToStart + this.getWidth() / 8 + this.getWidth() / 64, p);
            }
        }

        private float getLevelsOnScreen() {
            return this.getHeight() / (float) (this.getWidth() / 4);
        }

        private void checkListPositionOverflow() {
            float maxListPosition = (Level.maxLevel < Level.levels.length ? Level.maxLevel : Level.levels.length - 1) - this.getLevelsOnScreen() + 1;
            if (maxListPosition < 0) {
                this.listPosition = 0;
                this.listVelocity = 0;
            }
            else if (this.listPosition > maxListPosition) {
                this.listPosition = maxListPosition;
                this.listVelocity = -this.listVelocity / 2;
            }
            else if (this.listPosition < 0) {
                this.listPosition = 0;
                this.listVelocity = -this.listVelocity / 2;
            }
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private LevelListView attachedView;

        private GestureListener(LevelListView attachedView) {
            this.attachedView = attachedView;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            this.attachedView.listVelocity = -velocityY / 131072;
            this.attachedView.postInvalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            this.attachedView.listPosition += distanceY / (this.attachedView.getWidth() / 4);
            this.attachedView.checkListPositionOverflow();
            this.attachedView.postInvalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            this.attachedView.listVelocity = 0;
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int[] tapLocation = new int[2];
            this.attachedView.getLocationInWindow(tapLocation);
            float tapY = e.getY() - tapLocation[1];
            int listClickPosition = (int) (this.attachedView.listPosition + tapY / (this.attachedView.getWidth() / 4));
            if (listClickPosition <= Level.maxLevel) {
                this.attachedView.attachedActivity.goToLevel(listClickPosition);
            }
            return true;
        }
    }
}
