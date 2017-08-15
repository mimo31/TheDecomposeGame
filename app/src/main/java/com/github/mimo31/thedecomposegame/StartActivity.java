package com.github.mimo31.thedecomposegame;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * The first Activity shown to the user.
 * Has buttons to go to the ChooseLevelActivity or the HelpActivity.
 */
public class StartActivity extends AppCompatActivity
{
    // the view where everything is drawn
    private WelcomeView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.drawView = new WelcomeView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(this.drawView, layoutParams);
    }

    /**
     * Starts the ChooseLevelActivity.
     */
    public void goToChooseLevel()
    {
        Intent intent = new Intent(this, ChooseLevelActivity.class);
        this.startActivity(intent);
    }

    /**
     * Starts the HelpActivity.
     */
    public void goToHelp()
    {
        this.startActivity(new Intent(this, HelpActivity.class));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // start the updating in the view
        this.drawView.keepUpdating = true;
        this.drawView.updateHandler.postDelayed(this.drawView, 17);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // stop the updating in the view
        this.drawView.keepUpdating = false;
    }

    private static class WelcomeView extends View implements Runnable
    {
        private StartActivity activity;
        private GestureDetectorCompat gestureDetector;

        private Handler updateHandler = new Handler();

        private boolean keepUpdating = true;

        // the blue-red state of the grid on the background
        // the grid has three rows and a variable number of columns
        // this array is accessed as gridState[x + gridWidth * y] where x is non-negative and less that gridWidth and y is also non-negative and less than 3
        private boolean[] gridState;

        // the coordinates of the grid tile that is currently being animated
        // if no tile in currently being animated, these values should be ignored
        private int animatingX;
        private int animatingY;

        // the number of update-cycles between two changes of a tile state
        // the default value for the toNextAnimation variable
        private static final int BETWEEN_ANIMATIONS_LENGTH = 200;

        // the number of update-cycles an animation of a tile state change should last
        private static final int ANIMATION_LENGTH = 30;

        // the number of update-cycles remaining to the next change of a tile state
        // this value should be set to its default (BETWEEN_ANIMATIONS_LENGTH) when some animation is being performed
        private int toNextAnimation = BETWEEN_ANIMATIONS_LENGTH;

        // the number of update-cycles remaining to the end of a tile state change animation
        // when no animation is being performed, this value is 0
        private int toAnimationEnd = 0;

        // the number of tiles of the background grid in the x direction
        private int gridWidth;

        // the x coordinate of the leftmost tile on the tile grid
        // a negative value
        private int gridStartX;

        // the size of the whole view
        private int width;
        private int height;

        // the size of a tile on the tile grid (including the border)
        private int tileSize;

        // the x index in the gridState array of the tile that is instead of blue or red working as a button navigating to the HelpActivity
        private int helpTileX;

        // the size of the border around a tile
        private int borderSize;

        // a Path object used for tile change animations
        private Path animationPath;

        // the Paint object used for drawing
        private Paint p;

        // the StringDrawData objects for the texts on the play and help buttons
        private StringDraw.StringDrawData playDrawData;
        private StringDraw.StringDrawData helpDrawData;

        // indicates whether the draw variables (sizes, positions,...) have already been initialized - whether the initializeComponentSizes method has been called
        private boolean initialized = false;

        // rectangles of the play and help buttons
        private Rect playButton;
        private Rect helpButton;

        private WelcomeView(StartActivity activity)
        {
            super(activity.getApplicationContext());
            this.activity = activity;
            this.gestureDetector = new GestureDetectorCompat(activity.getApplicationContext(), new GestureListener(this));
        }

        private void initializeComponentSizes()
        {
            this.width = this.getWidth();
            this.height = this.getHeight();

            this.tileSize = this.height / 4;

            this.gridWidth = 2 * (int) Math.ceil(this.width / 2 / (float) this.tileSize);

            this.gridStartX = this.width / 2 - this.tileSize * this.gridWidth / 2;

            this.gridState = new boolean[3 * this.gridWidth];

            this.helpTileX = this.gridWidth / 2;

            this.borderSize = this.tileSize / 16;

            this.p = new Paint();

            this.animationPath = new Path();
            this.animationPath.setFillType(Path.FillType.EVEN_ODD);

            this.playButton = new Rect(0, this.tileSize + this.borderSize, this.width, this.tileSize * 2 - this.borderSize);
            this.helpButton = new Rect(this.width / 2 + this.borderSize, this.tileSize * 3 + this.borderSize, this.width / 2 + this.tileSize - this.borderSize, this.tileSize * 4 - this.borderSize);

            Rect borderedPlayButton = StringDraw.applyBorders(this.playButton, this.tileSize / 6);
            Rect borderedHelpButton = StringDraw.applyBorders(this.helpButton, this.tileSize / 6);

            this.playDrawData = StringDraw.getMaxStringData("PLAY!", borderedPlayButton, StringDraw.TextAlign.MIDDLE, this.p);
            this.helpDrawData = StringDraw.getMaxStringData("help?", borderedHelpButton, StringDraw.TextAlign.MIDDLE, this.p);

            this.initialized = true;
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            if (!this.initialized)
                this.initializeComponentSizes();

            // draw a gray background
            this.p.setColor(Color.LTGRAY);
            canvas.drawRect(0, 0, this.width, this.height, this.p);

            // draw the tile grid on the background
            int nextX = this.gridStartX;
            for (int i = 0; i < this.gridWidth; i++)
            {
                this.drawTile(nextX, 0, this.gridState[i + 0], animatingX == i && animatingY == 0 ? this.toAnimationEnd : 0, canvas);
                this.drawTile(nextX, this.tileSize * 2, this.gridState[i + 1 * this.gridWidth], animatingX == i && animatingY == 1 ? this.toAnimationEnd : 0, canvas);
                if (i != this.helpTileX)
                {
                    this.drawTile(nextX, this.tileSize * 3, this.gridState[i + 2 * this.gridWidth], animatingX == i && animatingY == 2 ? this.toAnimationEnd : 0, canvas);
                }
                nextX += this.tileSize;
            }

            // draw the play button
            this.p.setColor(Color.BLACK);
            canvas.drawRect(this.playButton, this.p);
            this.p.setColor(System.currentTimeMillis() % 6000 < 2000 ? PlayActivity.badColor : PlayActivity.goodColor);
            StringDraw.drawMaxString("PLAY!", this.playDrawData, canvas, this.p);

            // draw the help button
            this.p.setColor(Color.DKGRAY);
            canvas.drawRect(this.helpButton, this.p);
            this.p.setColor(Color.WHITE);
            StringDraw.drawMaxString("help?", this.helpDrawData, canvas, this.p);
        }

        /**
         * Draws a tile including the state change animation if appropriate.
         *
         * @param screenX        the x location of the tile in the view coordinates
         * @param screenY        the y location of the tile in the view coordinates
         * @param state          the state of the tile
         * @param toAnimationEnd the time remaining to the end of the animation of the tile; set to 0 if the tile should not be animated
         * @param canvas         the canvas to draw on
         */
        private void drawTile(int screenX, int screenY, boolean state, int toAnimationEnd, Canvas canvas)
        {
            boolean animating = toAnimationEnd != 0;

            this.p.setColor(state ^ animating ? PlayActivity.badColor : PlayActivity.goodColor);

            int left = screenX + this.borderSize;
            int top = screenY + this.borderSize;
            int right = screenX + this.tileSize - this.borderSize;
            int bottom = screenY + this.tileSize - this.borderSize;
            int drawSize = this.tileSize - 2 * this.borderSize;

            // draw the tile (ignoring any animation)
            canvas.drawRect(left, top, right, bottom, this.p);

            if (animating)
            {
                // draw the polygon representing the animation over the drawn tile

                this.animationPath.reset();

                float animationFraction = PlayActivity.getMovableViewPosition((ANIMATION_LENGTH - toAnimationEnd) / (float) ANIMATION_LENGTH, 0.5f);

                float bottomFraction = 2 * Math.min(animationFraction, 0.5f);

                this.animationPath.moveTo(right - bottomFraction * drawSize, bottom);

                if (animationFraction >= 0.5f)
                {
                    float topFraction = 2 * (animationFraction - 0.5f);

                    this.animationPath.lineTo(left, bottom - topFraction * drawSize);
                    this.animationPath.lineTo(right - topFraction * drawSize, top);
                }

                this.animationPath.lineTo(right, bottom - bottomFraction * drawSize);
                this.animationPath.lineTo(right, bottom);
                this.animationPath.close();

                this.p.setColor(state ? PlayActivity.badColor : PlayActivity.goodColor);

                canvas.drawPath(this.animationPath, this.p);
            }
        }

        /**
         * Updates the view and requests a repaint.
         */
        @Override
        public void run()
        {
            if (this.keepUpdating)
            {
                // if not animating
                if (this.toAnimationEnd == 0)
                {
                    // decrease the time remaining to the next animation
                    this.toNextAnimation--;

                    // a new animation should start
                    if (this.toNextAnimation == 0)
                    {
                        this.toNextAnimation = BETWEEN_ANIMATIONS_LENGTH;
                        this.toAnimationEnd = ANIMATION_LENGTH;

                        // choose a random tile (any tile except the one replaced by the help button)
                        do
                        {
                            this.animatingX = (int) (Math.random() * this.gridWidth);
                            this.animatingY = (int) (Math.random() * 3);
                        } while (this.animatingX == this.helpTileX && this.animatingY == 2);

                        // change the state of the selected tile
                        int index = this.animatingX + this.gridWidth * this.animatingY;
                        this.gridState[index] = !this.gridState[index];
                    }
                }
                // if animating
                else
                {
                    // proceed with the animation
                    this.toAnimationEnd--;
                }

                this.postInvalidate();
                this.updateHandler.postDelayed(this, 17);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            super.onTouchEvent(event);
            this.gestureDetector.onTouchEvent(event);
            return true;
        }

        private static class GestureListener extends GestureDetector.SimpleOnGestureListener
        {

            private WelcomeView attachedView;

            private GestureListener(WelcomeView attachedView)
            {
                this.attachedView = attachedView;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                int tapX = (int) e.getX();
                int tapY = (int) e.getY();

                // play button tapped
                if (this.attachedView.playButton.contains(tapX, tapY))
                {
                    this.attachedView.activity.goToChooseLevel();
                }
                // help button tapped
                else if (this.attachedView.helpButton.contains(tapX, tapY))
                {
                    this.attachedView.activity.goToHelp();
                }

                return true;
            }
        }
    }
}
