package com.github.mimo31.thedecomposegame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * The activity containing the main play area. Contains the tile grid, a ClickField selection dialog.
 * Includes the pause and finished dialogs.
 */
public class PlayActivity extends AppCompatActivity implements Runnable
{
    // the view with the tile grid, ClickField selection etc.
    private GameView plane;

    // the GameDesk with the current grid
    private GameDesk gameDesk;

    // the view - layout with serving as a pause and level finished dialog
    private RelativeLayout dialogLayout;

    // the number of the level that is currently being played
    private int level;

    // the colors of the tiles on the grid
    public static final int goodColor = Color.BLUE;
    public static final int badColor = Color.RED;

    // handles the updating of animation in the Activity
    private Handler updateHandler = new Handler();

    // if we are currently counting the time to the time taken by the user for the level, this value is the time when we started the counting
    // the total time taken up to this point is then System.currentTimeMillis() - timerLastStart + millisTaken
    // if we are currently not counting the time, this value is set to 0
    private long timerLastStart;

    // the total time taken by the user for this level not including the time after the last pause (see also the timerLastStart field)
    private int millisTaken = 0;

    // indicates whether this is the new best time for this level, only relevant when showing the finished dialog
    private boolean isBest = false;

    // indicates whether a next level is available and thus we should show the next level button, only relevant when showing the finished dialog
    private boolean showNextLevelButton = false;

    // indicates the current state of the Activity
    private PlayState state;

    // the state of showing or hiding the dialog
    // 0.0 - animation start
    // 1.0 - animation end
    private float animationState;

    // indicates whether the current dialog is the paused dialog
    // should be false when state == PlayState.PLAYING
    private boolean paused = false;

    // indicates whether the current dialog is the finished dialog
    // should be false when state == PlayState.PLAYING
    private boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.plane = new GameView(this);
        this.dialogLayout = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.finished_and_pause, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(this.plane, layoutParams);
        this.addContentView(this.dialogLayout, layoutParams);

        // if there is no saved state, get the level from intent and start playing
        // otherwise, the data are being initialized in the onRestoreInstanceState method
        if (savedInstanceState == null)
        {
            this.level = this.getIntent().getIntExtra("level", 0);
            this.gameDesk = Level.levels[this.level].getNewDesk();
            ClickField.availableClickFields = Level.levels[this.level].allowedClickFields;
            ClickField.selectedClickField = 0;
            this.state = PlayState.PLAYING;
        }

        // set the position of the dialog layout but after the dialog is loaded
        this.dialogLayout.post(new Runnable()
        {

            @Override
            public void run()
            {
                if (state == PlayState.PLAYING)
                {
                    updateDialogPosition(0.0f);
                }
                else if (state == PlayState.DIALOG)
                {
                    updateDialogPosition(1.0f);
                }
            }

        });
    }

    @Override
    protected void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);
        state.putInt("level", this.level);
        state.putSerializable("state", this.state);
        state.putParcelable("desk", this.gameDesk);
        state.putInt("millisTaken", this.millisTaken);
        if (this.state != PlayState.PLAYING)
        {
            state.putBoolean("paused", this.paused);
            state.putBoolean("finished", this.finished);
            if (this.state != PlayState.DIALOG)
            {
                state.putFloat("animationState", this.animationState);
            }
            if (this.finished)
            {
                state.putBoolean("isBest", this.isBest);
                state.putBoolean("showNextLevelButton", this.showNextLevelButton);
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (this.state == PlayState.PLAYING)
        {
            // pause the time
            this.millisTaken += System.currentTimeMillis() - this.timerLastStart;
            this.timerLastStart = 0;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (this.state == PlayState.PLAYING)
        {
            // resume the timing
            this.timerLastStart = System.currentTimeMillis();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        this.plane.getCloseReady();
        IO.saveData(this.getApplicationContext());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        this.level = savedInstanceState.getInt("level");
        this.state = (PlayState) savedInstanceState.getSerializable("state");
        this.gameDesk = savedInstanceState.getParcelable("desk");
        this.millisTaken = savedInstanceState.getInt("millisTaken");
        if (this.state != PlayState.PLAYING)
        {
            this.paused = savedInstanceState.getBoolean("paused");
            this.finished = savedInstanceState.getBoolean("finished");
            if (this.state != PlayState.DIALOG)
            {
                this.animationState = savedInstanceState.getFloat("animationState");
                this.updateHandler.postDelayed(this, 17);
            }
            if (this.finished)
            {
                this.isBest = savedInstanceState.getBoolean("isBest");
                this.showNextLevelButton = savedInstanceState.getBoolean("showNextLevelButton");
                this.prepareDialogForFinished();
            }
            else
            {
                this.prepareDialogForPause();
            }
        }
    }

    /**
     * Sets the position of the dialog layout. Used for showing and hiding the dialog view.
     *
     * @param position the position of the dialog to set (from 0.0 to 1.0)
     */
    private void updateDialogPosition(float position)
    {
        float positionFraction = getMovableViewPosition(position, 0.0f);
        this.dialogLayout.setX((int) (-(1 - positionFraction) * this.dialogLayout.getWidth()));
    }

    /**
     * @param state        the state of the animation from 0.0 to 1.0
     * @param initialSpeed a value corresponding to the rate of change at the beginning of the animation
     * @return the current position calculated using a cubic curve with values between 0.0 and 1.0
     */
    public static float getMovableViewPosition(float state, float initialSpeed)
    {
        return (float) ((2 * initialSpeed - 2) * Math.pow(state, 3) + (3 - 3 * initialSpeed) * Math.pow(state, 2) + initialSpeed * state);
    }

    @Override
    public void onBackPressed()
    {
        // unpause if paused
        if (this.state == PlayState.DIALOG && this.paused)
        {
            this.state = PlayState.HIDING_DIALOG;
            this.animationState = 0;
            this.updateHandler.postDelayed(this, 17);
        }
        // close if finished
        else if (this.state == PlayState.DIALOG && this.finished)
        {
            this.finish();
        }
        // pause if playing
        else if (this.state == PlayState.PLAYING)
        {
            this.millisTaken += System.currentTimeMillis() - this.timerLastStart;
            this.timerLastStart = 0;
            this.state = PlayState.SHOWING_DIALOG;
            this.paused = true;
            this.animationState = 0;
            this.prepareDialogForPause();
            this.updateHandler.postDelayed(this, 17);
        }
    }

    /**
     * Sets the values in the dialog layout to look like a pause screen.
     */
    private void prepareDialogForPause()
    {
        Button resumeButton = (Button) this.dialogLayout.findViewById(R.id.nextLevelOrResumeButton);
        resumeButton.setText("RESUME");
        resumeButton.setVisibility(View.VISIBLE);
        ((TextView) this.dialogLayout.findViewById(R.id.congratulationsText)).setText("Paused");
        this.dialogLayout.findViewById(R.id.timeText).setVisibility(View.INVISIBLE);
        this.dialogLayout.findViewById(R.id.bestText).setVisibility(View.INVISIBLE);
    }

    /**
     * Starts showing a dialog of a finished level.
     * Should be called when the user makes the last move and the GameDesk goes cleared.
     */
    private void finishLevel()
    {
        this.state = PlayState.SHOWING_DIALOG;
        this.finished = true;

        // add the time from the last period
        this.millisTaken += (int) (System.currentTimeMillis() - this.timerLastStart);
        this.timerLastStart = 0;

        // decide whether a next level is available
        this.showNextLevelButton = !(this.level == Level.levels.length - 1);

        // push the maxlevel if appropriate
        if (this.level == Level.maxLevel && this.level != Level.levels.length - 1)
        {
            Level.maxLevel++;
        }

        // change the best time
        int currentBestTime = Level.bestTimes[this.level];
        if (currentBestTime == 0 || this.millisTaken < currentBestTime)
        {
            Level.bestTimes[this.level] = this.millisTaken;
            this.isBest = true;
        }
        else
        {
            this.isBest = false;
        }

        this.prepareDialogForFinished();
        this.updateHandler.postDelayed(this, 17);
    }

    /**
     * Sets the values in the dialog layout to look like a pause level finished screen.
     */
    private void prepareDialogForFinished()
    {
        ((TextView) this.dialogLayout.findViewById(R.id.congratulationsText)).setText("Congratulations!");
        TextView timeTakenText = (TextView) this.dialogLayout.findViewById(R.id.timeText);
        timeTakenText.setText("You've finished this level in " + formatTime(this.millisTaken) + " seconds.");
        timeTakenText.setVisibility(View.VISIBLE);
        TextView bestTimeText = (TextView) this.dialogLayout.findViewById(R.id.bestText);
        bestTimeText.setVisibility(View.VISIBLE);
        if (this.isBest)
        {
            bestTimeText.setText("That's a new best time!");
        }
        else
        {
            bestTimeText.setText("The best time is " + formatTime(Level.bestTimes[this.level]) + " seconds.");
        }
        Button nextLevelButton = (Button) this.dialogLayout.findViewById(R.id.nextLevelOrResumeButton);
        nextLevelButton.setText("NEXT LEVEL");
        if (!this.showNextLevelButton)
        {
            this.dialogLayout.findViewById(R.id.nextLevelOrResumeButton).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * @param time the number of milliseconds
     * @return time string in format #.### s
     */
    public static String formatTime(int time)
    {
        return String.valueOf(time / 1000) + "." + String.format("%03d", time % 1000);
    }

    /**
     * Handles the press of the next level button or the resume button (depending on whether the dialog is pause or finished).
     *
     * @param v the view that caused the call
     */
    public void nextLevelOrResumePress(View v)
    {
        if (v.getId() == R.id.nextLevelOrResumeButton && this.state == PlayState.DIALOG)
        {
            if (this.paused)
            {
                // hide the pause dialog
                this.state = PlayState.HIDING_DIALOG;
                this.paused = false;
                this.animationState = 0;
                this.updateHandler.postDelayed(this, 17);
            }
            else if (this.finished)
            {
                // advance the level and hide the finished dialog
                this.level++;
                this.gameDesk = Level.levels[this.level].getNewDesk();
                ClickField.availableClickFields = Level.levels[this.level].allowedClickFields;
                ClickField.selectedClickField = 0;
                this.state = PlayState.HIDING_DIALOG;
                this.animationState = 0;
                this.updateHandler.postDelayed(this, 17);
                this.plane.updateGameDeskComponentSizes();
                this.millisTaken = 0;
            }
        }
    }

    /**
     * Handles the press of the exit button.
     *
     * @param v the view that caused the call
     */
    public void exitTheGame(View v)
    {
        if (v.getId() == R.id.exitButton && this.state == PlayState.DIALOG)
        {
            this.finish();
        }
    }

    /**
     * Handles the press of the replay button.
     *
     * @param v the view that caused the call
     */
    public void replay(View v)
    {
        if (v.getId() == R.id.replayButton && this.state == PlayState.DIALOG)
        {
            this.gameDesk = Level.levels[this.level].getNewDesk();
            this.state = PlayState.HIDING_DIALOG;
            this.animationState = 0;
            this.millisTaken = 0;
            this.updateHandler.postDelayed(this, 17);
        }
    }

    /**
     * Updates the Activity.
     */
    @Override
    public void run()
    {
        if (this.state == PlayState.HIDING_DIALOG || this.state == PlayState.SHOWING_DIALOG)
        {
            // update the animation state
            this.animationState += 0.05;
            if (this.animationState > 1)
            {
                this.animationState = 1;
            }

            // calculate the dialog position from the animation state depending of whether we are showing or hiding
            float dialogPosition = this.state == PlayState.SHOWING_DIALOG ? this.animationState : 1 - this.animationState;

            // update the dialog position using to the calculated one
            this.updateDialogPosition(dialogPosition);

            // do appropriate actions when the animation has finished
            if (this.animationState == 1)
            {
                this.animationState = 0;

                if (this.state == PlayState.HIDING_DIALOG)
                {
                    this.timerLastStart = System.currentTimeMillis();
                    this.paused = false;
                    this.finished = false;
                    this.state = PlayState.PLAYING;
                }
                else
                {
                    this.state = PlayState.DIALOG;
                }
            }
            else
            {
                this.updateHandler.postDelayed(this, 17);
            }
        }
    }

    private class GameView extends View implements Runnable
    {

        private PlayActivity attachedActivity;
        private GestureDetectorCompat gestureDetector;
        private boolean keepUpdating = true;
        private Handler updateHandler = new Handler();
        private final int backgroundColor = Color.rgb(220, 220, 220);
        private final int selectionDialogSelectedColor = Color.rgb(100, 0, 180);
        private final int selectionDialogNotSelectedColor = Color.rgb(200, 0, 255);

        private boolean animatingFieldChoice = false;
        private float choiceState = 0;
        private boolean choiceFromPrev;

        private Paint p = new Paint();

        // width and height of the whole view
        private int width;
        private int height;

        // the top left corner of the tile grid
        private int gridCornerX;
        private int gridCornerY;

        // size of the area where the grid is drawn
        private int gridSpaceWidth;
        private int gridSpaceHeight;

        private float tileSize;

        // the number of columns of the GameDesk
        private int tilesInWidth;

        // the number of rows of the GameDesk
        private int tilesInHeight;

        private Rect levelInfoBounds;
        private Rect timeInfoBounds;

        // coordinates of the ClickField selection dialog squares
        private int prevSelectionStartX;
        private int prevSelectionStartY;
        private int curSelectionStartX;
        private int curSelectionStartY;
        private int nextSelectionStartX;
        private int nextSelectionStartY;

        private int selectionSquareSize;

        // a path object used for the tile grid change animations
        private Path gridAnimationPath;

        /**
         * Indicates whether the initializeComponentSizes method has been already called and therefore the component sizes are initialized.
         * The initializeComponentSizes method actually can't be called right in the beginning (in the constructor or so)
         * because the size of the View and similar UI related quantities are not yet known at that time.
         */
        private boolean initialized = false;

        private StringDraw.StringDrawData timeInfoDraw = null;
        private int calibrationLimit = 0;

        private StringDraw.StringDrawData levelInfoDraw;

        /**
         * Precomputes the sizes and locations of various components, so that it does not need to be recalculated in every redraw or tap.
         */
        private void initializeComponentSizes()
        {
            this.width = this.getWidth();
            this.height = this.getHeight();

            // the top of the area where grid and the field selection dialog are drawn
            // the level and time information is drawn above that
            int playYStart = this.height / 5;

            // set the sizes of the level and time info bounds
            this.levelInfoBounds = new Rect(0, 0, this.width, playYStart * 2 / 3);
            StringDraw.applyBordersChange(this.levelInfoBounds, playYStart / 10);

            this.timeInfoBounds = new Rect(0, playYStart * 2 / 3, this.width, playYStart);
            StringDraw.applyBordersChange(this.timeInfoBounds, playYStart / 10);

            // calculates the locations of the selection dialog and the size of the grid space
            if (this.width < this.height - playYStart)
            {
                // the selection dialog will be in the left-right direction

                int selectionSpaceWidth = this.width;
                int selectionSpaceHeight = (this.height - playYStart) / 5;
                if (selectionSpaceWidth / (float) selectionSpaceHeight > 3)
                {
                    this.selectionSquareSize = selectionSpaceHeight;
                    this.curSelectionStartX = width / 2 - this.selectionSquareSize / 2;
                    this.prevSelectionStartX = this.curSelectionStartX - this.selectionSquareSize;
                    this.nextSelectionStartX = this.curSelectionStartX + this.selectionSquareSize;
                    this.prevSelectionStartY = this.curSelectionStartY = this.nextSelectionStartY = this.height - selectionSpaceHeight;
                }
                else
                {
                    this.selectionSquareSize = selectionSpaceWidth / 3;
                    this.prevSelectionStartX = 0;
                    this.curSelectionStartX = this.selectionSquareSize;
                    this.nextSelectionStartX = 2 * this.selectionSquareSize;
                    this.prevSelectionStartY = this.curSelectionStartY = this.nextSelectionStartY = this.height - selectionSpaceHeight / 2 - this.selectionSquareSize / 2;
                }
                this.gridSpaceWidth = this.width;
                this.gridSpaceHeight = this.height - playYStart - selectionSpaceHeight;
            }
            else
            {
                // the selection dialog will be in the top-bottom direction

                int selectionSpaceWidth = this.width / 5;
                int selectionSpaceHeight = this.height - playYStart;
                if (selectionSpaceHeight / (float) selectionSpaceWidth > 3)
                {
                    this.selectionSquareSize = selectionSpaceWidth;
                    this.prevSelectionStartX = this.curSelectionStartX = this.nextSelectionStartX = this.width - selectionSpaceWidth;
                    this.curSelectionStartY = playYStart + selectionSpaceHeight / 2 - this.selectionSquareSize / 2;
                    this.prevSelectionStartY = this.curSelectionStartY - this.selectionSquareSize;
                    this.nextSelectionStartY = this.curSelectionStartY + this.selectionSquareSize;
                }
                else
                {
                    this.selectionSquareSize = selectionSpaceHeight / 3;
                    this.prevSelectionStartX = this.curSelectionStartX = this.nextSelectionStartX = this.width - selectionSpaceWidth / 2 - this.selectionSquareSize / 2;
                    this.prevSelectionStartY = playYStart;
                    this.curSelectionStartY = playYStart + this.selectionSquareSize;
                    this.nextSelectionStartY = playYStart + 2 * this.selectionSquareSize;
                }

                this.gridSpaceWidth = this.width - selectionSpaceWidth;
                this.gridSpaceHeight = selectionSpaceHeight;
            }

            // calculates the size of one tile on the GameDesk and the location of the GameDesk
            this.tilesInWidth = this.attachedActivity.gameDesk.width;
            this.tilesInHeight = this.attachedActivity.gameDesk.height;
            float gridWidthToHeight = this.tilesInWidth / (float) this.tilesInHeight;
            if (gridWidthToHeight > this.gridSpaceWidth / (float) this.gridSpaceHeight)
            {
                this.tileSize = this.gridSpaceWidth / (float) this.tilesInWidth;
                this.gridCornerX = 0;
                this.gridCornerY = playYStart + this.gridSpaceHeight / 2 - (int) (this.tileSize * this.tilesInHeight / 2);
            }
            else
            {
                this.tileSize = this.gridSpaceHeight / (float) this.tilesInHeight;
                this.gridCornerX = this.gridSpaceWidth / 2 - (int) (this.tileSize * this.tilesInWidth / 2);
                this.gridCornerY = playYStart;
            }

            this.p.setTypeface(Typeface.DEFAULT);
            this.levelInfoDraw = StringDraw.getMaxStringData("Level " + (this.attachedActivity.level + 1), this.levelInfoBounds, StringDraw.TextAlign.MIDDLE, this.p);

            this.gridAnimationPath = new Path();
            this.gridAnimationPath.setFillType(Path.FillType.EVEN_ODD);

            this.initialized = true;
        }

        /**
         * Updates the sizes of components related to the size of the GameDesk. Should be called when the next level is started.
         */
        public void updateGameDeskComponentSizes()
        {
            // the top of the area where grid and the field selection dialog are drawn
            // the level and time information is drawn above that
            int playYStart = this.height / 5;

            // calculates the size of one tile on the GameDesk and the location of the GameDesk
            this.tilesInWidth = this.attachedActivity.gameDesk.width;
            this.tilesInHeight = this.attachedActivity.gameDesk.height;
            float gridWidthToHeight = this.tilesInWidth / (float) this.tilesInHeight;
            if (gridWidthToHeight > gridSpaceWidth / (float) gridSpaceHeight)
            {
                this.tileSize = gridSpaceWidth / (float) this.tilesInWidth;
                this.gridCornerX = 0;
                this.gridCornerY = playYStart + gridSpaceHeight / 2 - (int) (this.tileSize * this.tilesInHeight / 2);
            }
            else
            {
                this.tileSize = gridSpaceHeight / (float) this.tilesInHeight;
                this.gridCornerX = gridSpaceWidth / 2 - (int) (this.tileSize * this.tilesInWidth / 2);
                this.gridCornerY = playYStart;
            }

            this.p.setTypeface(Typeface.DEFAULT);
            this.levelInfoDraw = StringDraw.getMaxStringData("Level " + (this.attachedActivity.level + 1), this.levelInfoBounds, StringDraw.TextAlign.MIDDLE, this.p);
        }

        public GameView(PlayActivity playActivity)
        {
            super(playActivity.getApplicationContext());
            this.attachedActivity = playActivity;
            this.setBackgroundColor(backgroundColor);
            this.gestureDetector = new GestureDetectorCompat(this.attachedActivity.getApplicationContext(), new GestureListener(this));
            this.updateHandler.postDelayed(this, 17);
        }

        /**
         * Implementation of the Runnable interface - calls the update method.
         */
        public void run()
        {
            this.update();
            if (keepUpdating)
            {
                this.updateHandler.postDelayed(this, 17);
            }
        }

        /**
         * Unbinds any resources, gets ready for destroying - stops updating.
         */
        public void getCloseReady()
        {
            this.keepUpdating = false;
        }

        /**
         * Updates the animations and invalidates the view.
         */
        private void update()
        {
            if (this.animatingFieldChoice)
            {
                this.choiceState += 0.1;
                if (this.choiceState >= 1)
                {
                    this.animatingFieldChoice = false;
                }
            }
            this.invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            if (!initialized)
                this.initializeComponentSizes();

            // draw the level and time info
            this.p.setColor(Color.BLACK);
            this.p.setTypeface(Typeface.DEFAULT);
            StringDraw.drawMaxString("Level " + (this.attachedActivity.level + 1), this.levelInfoDraw, canvas, this.p);
            int millisecondsTaken = this.attachedActivity.millisTaken;
            if (this.attachedActivity.state == PlayState.PLAYING)
            {
                millisecondsTaken += System.currentTimeMillis() - this.attachedActivity.timerLastStart;
            }
            this.p.setTypeface(Typeface.MONOSPACE);
            if (this.timeInfoDraw == null || this.calibrationLimit <= millisecondsTaken / 1000)
            {
                int digCount = millisecondsTaken < 1000 ? 1 : (int) Math.floor(Math.log10(millisecondsTaken / 1000)) + 1;
                this.timeInfoDraw = StringDraw.getMaxStringData(millisecondsTaken / 1000 + " s", this.timeInfoBounds, StringDraw.TextAlign.MIDDLE, this.p);
                this.calibrationLimit = 1;
                for (int i = 0; i < digCount; i++)
                {
                    this.calibrationLimit *= 10;
                }
            }
            StringDraw.drawMaxString(millisecondsTaken / 1000 + " s", this.timeInfoDraw, canvas, this.p);

            float animationFraction = 0;

            // update the animation of the grid
            if (this.attachedActivity.gameDesk.isAnimating)
            {
                float animState = (System.currentTimeMillis() - this.attachedActivity.gameDesk.animationBegin) / (float) GameDesk.animationLength;
                if (animState > 1)
                {
                    this.attachedActivity.gameDesk.isAnimating = false;
                }
                animationFraction = getMovableViewPosition(animState, 0);
            }

            boolean isAnimating = this.attachedActivity.gameDesk.isAnimating;

            // drawing the grid
            for (int i = 0; i < this.tilesInWidth; i++)
            {
                for (int j = 0; j < this.tilesInHeight; j++)
                {
                    boolean animated = isAnimating && this.attachedActivity.gameDesk.animating[i + j * this.attachedActivity.gameDesk.width];
                    p.setColor(this.attachedActivity.gameDesk.state(i, j) ^ animated ? badColor : goodColor);
                    int rectStartX = (int) (this.gridCornerX + i * this.tileSize);
                    int rectStartY = (int) (this.gridCornerY + j * this.tileSize);
                    canvas.drawRect(rectStartX, rectStartY, rectStartX + this.tileSize, rectStartY + this.tileSize, this.p);
                    if (animated)
                    {
                        // the portion of the bottom triangle of the cover
                        float bottomFraction = 2 * Math.min(animationFraction, 0.5f);

                        float p1x = rectStartX + this.tileSize * (1 - bottomFraction);
                        float p1y = rectStartY + this.tileSize;
                        float p2x = rectStartX + this.tileSize;
                        float p2y = rectStartY + this.tileSize;
                        float p3x = rectStartX + this.tileSize;
                        float p3y = rectStartY + this.tileSize * (1 - bottomFraction);
                        if (animationFraction > 0.3)
                        {
                            p.setTextSize(30);
                        }
                        p.setColor(this.attachedActivity.gameDesk.state(i, j) ? badColor : goodColor);
                        this.gridAnimationPath.reset();
                        this.gridAnimationPath.moveTo(p1x, p1y);
                        this.gridAnimationPath.lineTo(p2x, p2y);
                        this.gridAnimationPath.lineTo(p3x, p3y);

                        // if the animation is more than half the way through, the top triangle should be drawn also
                        if (animationFraction > 0.5)
                        {
                            // the portion of the top triangle of the cover
                            float topFraction = 2 * (animationFraction - 0.5f);

                            this.gridAnimationPath.lineTo(rectStartX + this.tileSize * (1 - topFraction), rectStartY);
                            this.gridAnimationPath.lineTo(rectStartX, rectStartY + this.tileSize * (1 - topFraction));
                        }
                        this.gridAnimationPath.close();
                        this.p.setColor(this.attachedActivity.gameDesk.state(i, j) ? badColor : goodColor);
                        canvas.drawPath(this.gridAnimationPath, this.p);
                    }
                }
            }

            // drawing the empty lines in the grid
            p.setColor(this.backgroundColor);
            p.setStrokeWidth(this.tileSize / 32);
            for (int i = 0; i < this.tilesInWidth + 1; i++)
            {
                canvas.drawLine(this.gridCornerX + i * this.tileSize, this.gridCornerY, this.gridCornerX + i * this.tileSize, this.gridCornerY + this.tilesInHeight * this.tileSize, p);
            }
            for (int i = 0; i < this.tilesInHeight + 1; i++)
            {
                canvas.drawLine(this.gridCornerX, this.gridCornerY + i * this.tileSize, this.gridCornerX + this.tilesInWidth * this.tileSize, this.gridCornerY + i * this.tileSize, p);
            }

            // drawing the field list
            this.p.setColor(this.selectionDialogSelectedColor);
            canvas.drawRect(this.curSelectionStartX, this.curSelectionStartY, this.curSelectionStartX + this.selectionSquareSize, this.curSelectionStartY + this.curSelectionStartY, this.p);
            this.p.setColor(this.selectionDialogNotSelectedColor);
            canvas.drawRect(this.prevSelectionStartX, this.prevSelectionStartY, this.prevSelectionStartX + this.selectionSquareSize, this.prevSelectionStartY + this.selectionSquareSize, this.p);
            canvas.drawRect(this.nextSelectionStartX, this.nextSelectionStartY, this.nextSelectionStartX + this.selectionSquareSize, this.nextSelectionStartY + this.selectionSquareSize, this.p);
            int border = this.selectionSquareSize / 10;
            if (this.animatingFieldChoice)
            {
                // draw the ClickFields animated
                if (this.choiceFromPrev)
                {
                    if (ClickField.selectedClickField != 0)
                    {
                        int size = this.selectionSquareSize - 4 * border;
                        size *= this.choiceState * this.choiceState;
                        int centerX = this.prevSelectionStartX + this.selectionSquareSize / 2;
                        int centerY = this.prevSelectionStartY + this.selectionSquareSize / 2;
                        drawClickField(ClickField.availableClickFields[ClickField.selectedClickField - 1], centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2, canvas, this.p);
                    }
                    int movInCenterX = this.prevSelectionStartX + this.selectionSquareSize / 2 + (int) ((this.curSelectionStartX - this.prevSelectionStartX) * this.choiceState * this.choiceState);
                    int movInCenterY = this.prevSelectionStartY + this.selectionSquareSize / 2 + (int) ((this.curSelectionStartY - this.prevSelectionStartY) * this.choiceState * this.choiceState);
                    int movInSize = this.selectionSquareSize - 4 * border + (int) (2 * border * this.choiceState * this.choiceState);
                    drawClickField(ClickField.availableClickFields[ClickField.selectedClickField], movInCenterX - movInSize / 2, movInCenterY - movInSize / 2, movInCenterX + movInSize / 2, movInCenterY + movInSize / 2, canvas, this.p);
                    if (ClickField.selectedClickField != ClickField.availableClickFields.length - 1)
                    {
                        int movOutCenterX = movInCenterX + this.curSelectionStartX - this.prevSelectionStartX;
                        int movOutCenterY = movInCenterY + this.curSelectionStartY - this.prevSelectionStartY;
                        int movOutSize = this.selectionSquareSize - 2 * border - (int) (2 * border * this.choiceState * this.choiceState);
                        drawClickField(ClickField.availableClickFields[ClickField.selectedClickField + 1], movOutCenterX - movOutSize / 2, movOutCenterY - movOutSize / 2, movOutCenterX + movOutSize / 2, movOutCenterY + movOutSize / 2, canvas, this.p);
                        if (ClickField.selectedClickField != ClickField.availableClickFields.length - 2)
                        {
                            int popOutSize = (int) ((this.selectionSquareSize - 4 * border) * (1 - this.choiceState * this.choiceState));
                            int popOutRad = popOutSize / 2;
                            int popOutCenterX = this.nextSelectionStartX + this.selectionSquareSize / 2;
                            int popOutCenterY = this.nextSelectionStartY + this.selectionSquareSize / 2;
                            drawClickField(ClickField.availableClickFields[ClickField.selectedClickField + 2], popOutCenterX - popOutRad, popOutCenterY - popOutRad, popOutCenterX + popOutRad, popOutCenterY + popOutRad, canvas, this.p);
                        }
                    }
                }
                else
                {
                    if (ClickField.selectedClickField != ClickField.availableClickFields.length - 1)
                    {
                        int size = this.selectionSquareSize - 4 * border;
                        size *= this.choiceState * this.choiceState;
                        int centerX = this.nextSelectionStartX + this.selectionSquareSize / 2;
                        int centerY = this.nextSelectionStartY + this.selectionSquareSize / 2;
                        drawClickField(ClickField.availableClickFields[ClickField.selectedClickField + 1], centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2, canvas, this.p);
                    }
                    int movInCenterX = this.nextSelectionStartX + this.selectionSquareSize / 2 - (int) ((this.nextSelectionStartX - this.curSelectionStartX) * this.choiceState * this.choiceState);
                    int movInCenterY = this.nextSelectionStartY + this.selectionSquareSize / 2 - (int) ((this.nextSelectionStartY - this.curSelectionStartY) * this.choiceState * this.choiceState);
                    int movInSize = this.selectionSquareSize - 4 * border + (int) (2 * border * this.choiceState * this.choiceState);
                    drawClickField(ClickField.availableClickFields[ClickField.selectedClickField], movInCenterX - movInSize / 2, movInCenterY - movInSize / 2, movInCenterX + movInSize / 2, movInCenterY + movInSize / 2, canvas, this.p);
                    if (ClickField.selectedClickField != 0)
                    {
                        int movOutCenterX = movInCenterX - (this.curSelectionStartX - this.prevSelectionStartX);
                        int movOutCenterY = movInCenterY - (this.curSelectionStartY - this.prevSelectionStartY);
                        int movOutSize = this.selectionSquareSize - 2 * border - (int) (2 * border * this.choiceState * this.choiceState);
                        drawClickField(ClickField.availableClickFields[ClickField.selectedClickField - 1], movOutCenterX - movOutSize / 2, movOutCenterY - movOutSize / 2, movOutCenterX + movOutSize / 2, movOutCenterY + movOutSize / 2, canvas, this.p);
                        if (ClickField.selectedClickField != 1)
                        {
                            int popOutSize = (int) ((this.selectionSquareSize - 4 * border) * (1 - this.choiceState * this.choiceState));
                            int popOutRad = popOutSize / 2;
                            int popOutCenterX = this.prevSelectionStartX + this.selectionSquareSize / 2;
                            int popOutCenterY = this.prevSelectionStartY + this.selectionSquareSize / 2;
                            drawClickField(ClickField.availableClickFields[ClickField.selectedClickField - 2], popOutCenterX - popOutRad, popOutCenterY - popOutRad, popOutCenterX + popOutRad, popOutCenterY + popOutRad, canvas, this.p);
                        }
                    }
                }
            }
            else
            {
                // draw the ClickField static (not animated)
                drawClickField(ClickField.availableClickFields[ClickField.selectedClickField], this.curSelectionStartX + border, this.curSelectionStartY + border,
                        this.curSelectionStartX + this.selectionSquareSize - border, this.curSelectionStartY + this.selectionSquareSize - border, canvas, this.p);
                if (ClickField.selectedClickField != 0)
                {
                    drawClickField(ClickField.availableClickFields[ClickField.selectedClickField - 1], this.prevSelectionStartX + 2 * border, this.prevSelectionStartY + 2 * border,
                            this.prevSelectionStartX + this.selectionSquareSize - 2 * border, this.prevSelectionStartY + this.selectionSquareSize - 2 * border, canvas, this.p);
                }
                if (ClickField.selectedClickField != ClickField.availableClickFields.length - 1)
                {
                    drawClickField(ClickField.availableClickFields[ClickField.selectedClickField + 1], this.nextSelectionStartX + 2 * border, this.nextSelectionStartY + 2 * border,
                            this.nextSelectionStartX + this.selectionSquareSize - 2 * border, this.nextSelectionStartY + this.selectionSquareSize - 2 * border, canvas, this.p);
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            super.onTouchEvent(event);

            if (this.attachedActivity.state == PlayState.PLAYING)
            {
                this.gestureDetector.onTouchEvent(event);
            }
            return true;
        }


        private class GestureListener extends GestureDetector.SimpleOnGestureListener
        {

            GameView attachedView;

            public GestureListener(GameView attachedView)
            {
                super();
                this.attachedView = attachedView;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event)
            {
                float tapX = event.getX();
                float tapY = event.getY();
                float minX = this.attachedView.gridCornerX;
                float maxX = minX + this.attachedView.tilesInWidth * this.attachedView.tileSize;
                float minY = this.attachedView.gridCornerY;
                float maxY = minY + this.attachedView.tilesInHeight * this.attachedView.tileSize;
                if (tapX >= minX && tapY >= minY && tapX < maxX && tapY < maxY)
                {
                    int x = (int) Math.floor((tapX - this.attachedView.gridCornerX) / this.attachedView.tileSize);
                    int y = (int) Math.floor((tapY - this.attachedView.gridCornerY) / this.attachedView.tileSize);
                    if (this.attachedView.attachedActivity.gameDesk.doAttempt(ClickField.getSelectedClickField(), x, y))
                    {
                        if (this.attachedView.attachedActivity.gameDesk.isCleared())
                        {
                            this.attachedView.attachedActivity.finishLevel();
                        }
                        this.attachedView.invalidate();
                    }
                }
                // click on the prev ClickField
                else if (tapX >= this.attachedView.prevSelectionStartX && tapX < this.attachedView.prevSelectionStartX + this.attachedView.selectionSquareSize
                        && tapY >= this.attachedView.prevSelectionStartY && tapY < this.attachedView.prevSelectionStartY + this.attachedView.selectionSquareSize)
                {
                    if (ClickField.selectedClickField != 0)
                    {
                        ClickField.selectedClickField--;
                        this.attachedView.animatingFieldChoice = true;
                        this.attachedView.choiceState = 0;
                        this.attachedView.choiceFromPrev = true;
                    }
                }
                // click of the next ClickField
                else if (tapX >= this.attachedView.nextSelectionStartX && tapX < this.attachedView.nextSelectionStartX + this.attachedView.selectionSquareSize
                        && tapY >= this.attachedView.nextSelectionStartY && tapY < this.attachedView.nextSelectionStartY + this.attachedView.selectionSquareSize)
                {
                    if (ClickField.selectedClickField != ClickField.availableClickFields.length - 1)
                    {
                        ClickField.selectedClickField++;
                        this.attachedView.animatingFieldChoice = true;
                        this.attachedView.choiceState = 0;
                        this.attachedView.choiceFromPrev = false;
                    }
                }
                return true;
            }
        }
    }

    /**
     * Draws a ClickField as a maximized rectangle in the specified rectangle.
     *
     * @param clickField the ClickField to draw
     * @param left       left of the rectangle
     * @param top        top of the rectangle
     * @param right      right of the rectangle
     * @param bottom     bottom of the rectangle
     * @param canvas     the canvas to draw on
     * @param p          the paint to draw with
     */
    private static void drawClickField(ClickField clickField, int left, int top, int right, int bottom, Canvas canvas, Paint p)
    {
        p.setColor(Color.RED);

        // the top left corner of drawing the ClickField
        int originX;
        int originY;

        int spaceWidth = right - left;
        int spaceHeight = bottom - top;
        int tileWidth = clickField.getTotalWidth();
        int tileHeight = clickField.getTotalHeight();

        // the size of one drawn tile of the ClickField
        float fieldSize;

        if (spaceWidth / (float) spaceHeight > tileWidth / (float) tileHeight)
        {
            fieldSize = spaceHeight / (float) tileHeight;
            originX = (int) ((spaceWidth - clickField.getTotalWidth() * fieldSize) / 2) + left;
            originY = top;
        }
        else
        {
            fieldSize = spaceWidth / (float) tileWidth;
            originX = left;
            originY = (int) ((spaceHeight - tileHeight * fieldSize) / 2) + top;
        }

        // border between the tiles
        int border = (int) fieldSize / 10;

        for (int i = 0; i < tileWidth; i++)
        {
            for (int j = 0; j < tileHeight; j++)
            {
                // whether this is the click position - the green square should be drawn
                boolean isClickPosition = clickField.getClickX() == i && clickField.getClickY() == j;

                // whether this is a part of the ClickField - it should be drawn red
                boolean isChangedOnClick = clickField.getInAbsoluteCoordinates(i, j);

                if (isChangedOnClick || isClickPosition)
                {
                    int rectStartX = (int) (originX + fieldSize * i);
                    int rectStartY = (int) (originY + fieldSize * j);
                    if (isChangedOnClick)
                    {
                        Rect rect = new Rect(rectStartX + border, rectStartY + border, rectStartX + (int) fieldSize - border, rectStartY + (int) fieldSize - border);
                        canvas.drawRect(rect, p);
                    }
                    if (isClickPosition)
                    {
                        rectStartX += fieldSize / 4;
                        rectStartY += fieldSize / 4;
                        p.setColor(Color.GREEN);
                        canvas.drawRect(rectStartX, rectStartY, rectStartX + fieldSize / 2, rectStartY + fieldSize / 2, p);
                        p.setColor(Color.RED);
                    }
                }
            }
        }
    }
}
