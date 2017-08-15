package com.github.mimo31.thedecomposegame;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * The activity where the user can choose the level to play.
 */
public class ChooseLevelActivity extends AppCompatActivity
{
    /**
     * The view to do the drawing.
     */
    private LevelListView plane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.plane = new LevelListView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(this.plane, layoutParams);
    }

    /**
     * Creates an intent starting the PlayActivity with a specified level.
     * @param level the level number to send to the PlayActivity
     */
    private void goToLevel(int level)
    {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("level", level);
        this.startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putFloat("listPosition", this.plane.listPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        this.plane.listPosition = savedInstanceState.getFloat("listPosition");
        this.plane.checkOnInitialize = true;
    }

    private static class LevelListView extends View
    {

        ChooseLevelActivity attachedActivity;
        GestureDetectorCompat gestureDetector;

        // position of the list with levels
        // this value is the distance from the top of the list to the top of the current view (in level heights)
        private float listPosition = 0;

        // size of the whole view
        private int width;
        private int height;

        // y locations of the parts of the level rectangle
        private int levelTop;
        private int middleTop;
        private int timeTop;
        private int bottomBorderTop;

        // the size of one level rectangle in the y direction
        private int levelHeight;

        // text size used to draw the level texts
        private float levelTextSize;

        // text size used to draw the time texts
        private float timeTextSize;

        // the left and rights bounds of the space that is used for drawing, the stripes at the edges are left intact
        private int left;
        private int right;

        // the objects used for painting everything
        private Paint p;

        // indicates whether the sizes are already initialized - whether the initializeComponentSizes has been called
        private boolean initialized = false;

        // indicates whether the listPosition should be check on initializing because it could be outside the valid bounds
        private boolean checkOnInitialize = false;

        private LevelListView(ChooseLevelActivity attachedActivity)
        {
            super(attachedActivity);
            this.attachedActivity = attachedActivity;
            this.setBackgroundColor(Color.WHITE);
            this.gestureDetector = new GestureDetectorCompat(this.attachedActivity.getApplicationContext(), new GestureListener(this));
        }

        /**
         * Initializes the variables related to the drawing (sizes, positions, ...) of the view.
         */
        private void initializeComponentSizes()
        {
            this.p = new Paint();

            this.width = this.getWidth();
            this.height = this.getHeight();

            int borderSize = this.width / 4 / 16;
            int stripSize = (this.width / 4 - 3 * borderSize) / 2;

            this.levelTop = borderSize;
            this.middleTop = this.levelTop + stripSize;
            this.timeTop = this.middleTop + borderSize;
            this.bottomBorderTop = this.timeTop + stripSize;

            this.levelHeight = this.bottomBorderTop + borderSize;

            this.left = 2 * borderSize;
            this.right = this.width - 2 * borderSize;

            int rectsWidth = this.width - 4 * borderSize;

            this.levelTextSize = StringDraw.getMaxTextSize("Level 99", new Rect(0, 0, rectsWidth * 3 / 4, stripSize * 3 / 4), this.p);
            this.timeTextSize = StringDraw.getMaxTextSize("999.999 s", new Rect(0, 0, rectsWidth * 3 / 4, stripSize / 2), this.p);

            this.initialized = true;

            if (this.checkOnInitialize)
            {
                this.checkListPositionOverflow();
            }
        }

        @Override
        public void onDraw(Canvas canvas)
        {
            if (!initialized)
                this.initializeComponentSizes();

            // the next level to draw
            int nextLevel = Level.maxLevel - (int) this.listPosition;

            // the top of the next level to draw
            int nextY = (int) (-this.levelHeight * (this.listPosition - (int) this.listPosition));

            // draw light gray as background
            this.p.setColor(Color.LTGRAY);
            canvas.drawRect(0, 0, this.width, this.height, this.p);

            while (nextY < this.height && nextLevel >= 0)
            {
                // draw the level rectangles
                this.p.setColor(PlayActivity.badColor);
                canvas.drawRect(this.left, nextY + this.levelTop, this.right, nextY + this.middleTop, this.p);
                canvas.drawRect(this.left, nextY + this.middleTop, this.right, nextY + this.timeTop, this.p);
                this.p.setColor(PlayActivity.goodColor);
                canvas.drawRect(this.left, nextY + this.timeTop, this.right, nextY + this.bottomBorderTop, this.p);

                this.p.setColor(Color.WHITE);

                // draw the level text
                StringDraw.drawMaxString("Level " + (nextLevel + 1), 0, nextY + this.levelTop, this.width, nextY + this.middleTop, StringDraw.TextAlign.MIDDLE, canvas, this.p, this.levelTextSize);

                // draw the time text
                String timeText;
                if (Level.bestTimes[nextLevel] != 0)
                {
                    timeText = PlayActivity.formatTime(Level.bestTimes[nextLevel]) + " s";
                }
                else
                {
                    timeText = "to do";
                }
                StringDraw.drawMaxString(timeText, 0, nextY + this.timeTop, this.width, nextY + this.bottomBorderTop, StringDraw.TextAlign.MIDDLE, canvas, this.p, this.timeTextSize);

                nextLevel--;
                nextY += this.levelHeight;
            }
        }

        /**
         * Checks that the listPosition is within the bounds of the number of levels shown.
         * If it is, changes the listPosition to the nearest proper value.
         */
        private void checkListPositionOverflow()
        {
            float maxListPosition = Level.maxLevel + 1 - this.height / (float) this.levelHeight;
            if (maxListPosition < 0)
            {
                this.listPosition = 0;
            }
            else if (this.listPosition > maxListPosition)
            {
                this.listPosition = maxListPosition;
            }
            else if (this.listPosition < 0)
            {
                this.listPosition = 0;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            super.onTouchEvent(event);
            this.gestureDetector.onTouchEvent(event);
            return true;
        }
    }

    private static class GestureListener extends GestureDetector.SimpleOnGestureListener
    {

        private LevelListView attachedView;

        private GestureListener(LevelListView attachedView)
        {
            this.attachedView = attachedView;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            // move the list
            this.attachedView.listPosition += distanceY / this.attachedView.levelHeight;

            // check for getting too far
            this.attachedView.checkListPositionOverflow();

            // redraw the list
            this.attachedView.postInvalidate();

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // the location of the tap
            float tapX = e.getX();
            float tapY = e.getY();

            // return in the border on the left or the right
            if (tapX < this.attachedView.left || tapX >= this.attachedView.right)
                return true;

            // the distance of the click location from the top of the list (in level heights)
            float listClickPosition = this.attachedView.listPosition + tapY / this.attachedView.levelHeight;

            // the distance of the click location from the top of the clicked level (ranges from 0.0 to 1.0)
            float clickPositionInsideLevel = listClickPosition - (int) listClickPosition;

            // the limit for the clickPositionInsideLevel due to the borders between the levels
            float clickInsideLimit = this.attachedView.levelTop / (float) this.attachedView.levelHeight;

            // so check if the border between was not clicked
            if (clickPositionInsideLevel < clickInsideLimit || clickPositionInsideLevel >= 1 - clickInsideLimit)
                return true;

            // the number of the level that was has been clicked
            int levelClicked = Level.maxLevel - (int) listClickPosition;

            // go to that level unless its number is below zero
            if (Level.maxLevel - (int) listClickPosition >= 0)
            {
                this.attachedView.attachedActivity.goToLevel(levelClicked);
            }

            return true;
        }
    }
}
