package com.github.mimo31.thedecomposegame;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.Visibility;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlayActivity extends AppCompatActivity implements Runnable {

    private GameView plane;
    private GameDesk gameDesk;
    private RelativeLayout finishedLayout;
    private int level;
    private final int goodColor = Color.BLUE;
    private final int badColor = Color.RED;
    private float finishedLayoutState;
    private boolean showingFinished;
    private boolean hidingFinished;
    private Handler updateHandler = new Handler();
    private long levelStartTime;
    private int millisTaken = 0;
    private boolean isBest = false;
    private boolean showNextLevelButton = false;
    private boolean paused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.plane = new GameView(this);
        this.finishedLayout = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.finished_appearance, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(this.plane, layoutParams);
        this.addContentView(this.finishedLayout, layoutParams);
        if (savedInstanceState == null) {
            this.level = this.getIntent().getIntExtra("level", 0);
            this.gameDesk = Level.levels[this.level].getFreeDesk();
            ClickField.availableClickFields = Level.levels[this.level].allowedClickFields;
            ClickField.selectedClickField = 0;
            this.levelStartTime = System.currentTimeMillis();
        }
        else {
            this.level = savedInstanceState.getInt("level");
            this.gameDesk = savedInstanceState.getParcelable("state");
            this.levelStartTime = savedInstanceState.getLong("startTime");
            this.finishedLayoutState = savedInstanceState.getFloat("finishedState");
            this.showingFinished = savedInstanceState.getBoolean("showingFinished");
            this.hidingFinished = savedInstanceState.getBoolean("hidingFinished");
            this.paused = savedInstanceState.getBoolean("paused");
            if (this.finishedLayoutState != 0) {
                if (this.paused) {
                    this.updatePauseLayout();
                }
                else {
                    this.millisTaken = savedInstanceState.getInt("timeTaken");
                    this.isBest = savedInstanceState.getBoolean("isBest");
                    this.showNextLevelButton = savedInstanceState.getBoolean("showNextButton");
                    this.updateFinishedData();
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.updateFinishedPosition();
        addMargins((TextView) this.finishedLayout.findViewById(R.id.timeText), this.finishedLayout.getWidth());
        addMargins((TextView) this.finishedLayout.findViewById(R.id.bestText), this.finishedLayout.getWidth());
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("level", this.level);
        state.putParcelable("state", this.gameDesk);
        state.putLong("startTime", this.levelStartTime);
        state.putFloat("finishedState", this.finishedLayoutState);
        state.putBoolean("showingFinished", this.showingFinished);
        state.putBoolean("hidingFinished", this.hidingFinished);
        state.putInt("timeTaken", this.millisTaken);
        state.putBoolean("isBest", this.isBest);
        state.putBoolean("showNextButton", this.showNextLevelButton);
        state.putBoolean("paused", this.paused);
    }

    private static void addMargins(TextView textView, int width) {
        textView.setWidth(width - width / 8);
        textView.setLeft(width / 16);
        textView.setRight(width / 16);
        textView.setGravity(Gravity.CENTER);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.plane.getCloseReady();
        IO.saveData(this.getApplicationContext());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.finishedLayoutState == 0) {
            this.plane.gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void updateFinishedPosition() {
        float positionFraction = getMovableViewPosition(this.finishedLayoutState, 0.0f);
        this.finishedLayout.setX((int) (-(1 - positionFraction) * this.finishedLayout.getWidth()));
    }

    public static float getMovableViewPosition(float state, float initialSpeed) {
        return (float) ((2 * initialSpeed - 2) * Math.pow(state, 3) + (3 - 3 * initialSpeed) * Math.pow(state, 2) + initialSpeed * state);
    }

    @Override
    public void onBackPressed() {
        if (this.paused) {
            this.hideFinished();
            this.paused = false;
        }
        else {
            if (this.finishedLayoutState == 0) {
                this.paused = true;
                this.updatePauseLayout();
                this.showingFinished = true;
                this.updateHandler.postDelayed(this, 17);
            }
        }
    }

    private void showFinished() {
        this.showingFinished = true;
        this.updateHandler.postDelayed(this, 17);
        this.showNextLevelButton = !(this.level == Level.levels.length - 1);
        this.millisTaken = (int) (System.currentTimeMillis() - this.levelStartTime);
        if (this.level == Level.maxLevel) {
            Level.maxLevel++;
        }
        int currentBestTime = Level.bestTimes[this.level];
        if (currentBestTime == 0 || this.millisTaken < currentBestTime) {
            Level.bestTimes[this.level] = this.millisTaken;
            this.isBest = true;
        }
        else {
            this.isBest = false;
        }
        this.updateFinishedData();
    }

    private void updateFinishedData() {
        ((TextView) this.finishedLayout.findViewById(R.id.congratulationsText)).setText("Congratulations!");
        TextView timeTakenText = (TextView) this.finishedLayout.findViewById(R.id.timeText);
        timeTakenText.setText("You've finished this level in " + formatTime(this.millisTaken) + " seconds.");
        timeTakenText.setVisibility(View.VISIBLE);
        TextView bestTimeText = (TextView) this.finishedLayout.findViewById(R.id.bestText);
        bestTimeText.setVisibility(View.VISIBLE);
        if (this.isBest) {
            bestTimeText.setText("That's a new best time!");
        }
        else {
            bestTimeText.setText("The best time is " + formatTime(Level.bestTimes[this.level]) + " seconds.");
        }
        Button nextLevelButton = (Button) this.finishedLayout.findViewById(R.id.nextLevelButton);
        nextLevelButton.setText("NEXT LEVEL");
        if (!this.showNextLevelButton) {
            this.finishedLayout.findViewById(R.id.nextLevelButton).setVisibility(View.INVISIBLE);
        }
    }

    private void updatePauseLayout() {
        Button resumeButton = (Button) this.finishedLayout.findViewById(R.id.nextLevelButton);
        resumeButton.setText("RESUME");
        resumeButton.setVisibility(View.VISIBLE);
        ((TextView) this.finishedLayout.findViewById(R.id.congratulationsText)).setText("Paused");
        this.finishedLayout.findViewById(R.id.timeText).setVisibility(View.INVISIBLE);
        this.finishedLayout.findViewById(R.id.bestText).setVisibility(View.INVISIBLE);
    }

    public static String formatTime(int time) {
        return String.valueOf(time / 1000) + "." + String.format("%03d", time % 1000);
    }

    private void hideFinished() {
        this.hidingFinished = true;
        this.updateHandler.postDelayed(this, 17);
    }

    public void goToNextLevel(View v) {
        if (v.getId() == R.id.nextLevelButton) {
            if (this.paused) {
                this.paused = false;
                this.hideFinished();
            }
            else {
                this.level++;
                this.gameDesk = Level.levels[this.level].getFreeDesk();
                ClickField.availableClickFields = Level.levels[this.level].allowedClickFields;
                ClickField.selectedClickField = 0;
                this.plane.postInvalidate();
                this.hideFinished();
                this.levelStartTime = System.currentTimeMillis();
            }
        }
    }

    public void exitTheGame(View v) {
        if (v.getId() == R.id.exitButton) {
            this.startActivity(new Intent(this, StartActivity.class));
        }
    }

    public void replay(View v) {
        if (v.getId() == R.id.replayButton) {
            this.paused = false;
            this.gameDesk = Level.levels[this.level].getFreeDesk();
            this.plane.postInvalidate();
            this.hideFinished();
            this.levelStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void run() {
        if (this.showingFinished) {
            this.finishedLayoutState += 0.05;
            if (this.finishedLayoutState >= 1) {
                this.finishedLayoutState = 1;
                this.showingFinished = false;
            }
            else {
                this.updateHandler.postDelayed(this, 17);
            }
            this.updateFinishedPosition();
        }
        else if (this.hidingFinished) {
            this.finishedLayoutState -= 0.05;
            if (this.finishedLayoutState <= 0) {
                this.finishedLayoutState = 0;
                this.hidingFinished = false;
            }
            else {
                this.updateHandler.postDelayed(this, 17);
            }
            this.updateFinishedPosition();
        }
    }

    private class GameView extends View implements Runnable {

        private PlayActivity attachedActivity;
        private Point drawingOrigin;
        private float fieldSize;
        private float clickFieldListPosition = 0.5f;
        private float clickFieldListVelocity;
        private GestureDetectorCompat gestureDetector;
        private boolean keepUpdating = true;
        private Handler updateHandler = new Handler();

        public GameView(PlayActivity playActivity) {
            super(playActivity.getApplicationContext());
            this.attachedActivity = playActivity;
            this.setBackgroundColor(Color.WHITE);
            this.gestureDetector = new GestureDetectorCompat(this.attachedActivity.getApplicationContext(), new GestureListener(this));
            this.updateHandler.postDelayed(this, 17);
        }

        public void run() {
            this.update();
            if (keepUpdating) {
                this.updateHandler.postDelayed(this, 17);
            }
        }

        public void getCloseReady() {
            this.keepUpdating = false;
        }

        private void update() {
            if (this.clickFieldListVelocity != 0) {
                this.clickFieldListPosition += this.clickFieldListVelocity;
                if (this.clickFieldListPosition < 0) {
                    this.clickFieldListPosition = 0;
                    this.clickFieldListVelocity = 0;
                }
                else if (this.clickFieldListPosition > ClickField.availableClickFields.length) {
                    this.clickFieldListPosition = ClickField.availableClickFields.length;
                    this.clickFieldListVelocity = 0;
                }
                this.clickFieldListVelocity *= 0.97;
                if (this.clickFieldListVelocity < 0.01 && this.clickFieldListVelocity > -0.01) {
                    this.clickFieldListVelocity = 0;
                }
                this.invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //Drawing the grid
            int playWidth = this.getWidth();
            int playHeight = this.getHeight() * 4 / 5;
            Paint p = new Paint();
            int rowSize = this.attachedActivity.gameDesk.rowSize();
            int columnSize = this.attachedActivity.gameDesk.columnSize();
            float rowColumnRatio = rowSize / (float) columnSize;
            if (rowColumnRatio < playWidth / (float) playHeight) {
                this.fieldSize = playHeight / (float) columnSize;
                this.drawingOrigin = new Point((int) ((playWidth - rowSize * this.fieldSize) / 2), 0);
            }
            else {
                this.fieldSize = playWidth / (float) rowSize;
                this.drawingOrigin = new Point(0, (int) ((playHeight - columnSize * this.fieldSize) / 2));
            }
            for (int i = 0; i < columnSize; i++) {
                for (int j = 0; j < rowSize; j++) {
                    p.setColor(this.attachedActivity.gameDesk.states[i][j] ? badColor : goodColor);
                    int rectStartX = (int) (this.drawingOrigin.x + j * this.fieldSize);
                    int rectStartY = (int) (this.drawingOrigin.y + i * this.fieldSize);
                    Rect rect = new Rect(rectStartX, rectStartY, rectStartX + (int) this.fieldSize, rectStartY + (int) this.fieldSize);
                    canvas.drawRect(rect, p);
                }
            }
            p.setColor(Color.WHITE);
            p.setStrokeWidth(this.fieldSize / 32);
            for (int i = 0; i < rowSize + 1; i++) {
                canvas.drawLine(this.drawingOrigin.x + i * this.fieldSize, this.drawingOrigin.y, this.drawingOrigin.x + i * this.fieldSize, this.drawingOrigin.y + columnSize * this.fieldSize, p);
            }
            for (int i = 0; i < columnSize + 1; i++) {
                canvas.drawLine(this.drawingOrigin.x, this.drawingOrigin.y + i * this.fieldSize, this.drawingOrigin.x + rowSize * this.fieldSize, this.drawingOrigin.y + i * this.fieldSize, p);
            }

            //Drawing the field list
            float listDistanceToEnd = this.getWidth() / (float) (this.getHeight() / 5);
            float visionFieldStart = this.clickFieldListPosition - listDistanceToEnd;
            float visionFieldEnd = this.clickFieldListPosition + listDistanceToEnd;
            int firstIndexToDraw = (int) visionFieldStart;
            int lastIndexToDraw = (int) visionFieldEnd;
            if (firstIndexToDraw < 0) {
                firstIndexToDraw = 0;
            }
            if (lastIndexToDraw >= ClickField.availableClickFields.length) {
                lastIndexToDraw = ClickField.availableClickFields.length - 1;
            }
            int borderSize = this.getHeight() / 40;
            for (int i = firstIndexToDraw; i <= lastIndexToDraw; i++) {
                int left = (int) ((i - this.clickFieldListPosition) * this.getHeight() / 5 + this.getWidth() / 2);

                drawClickField(ClickField.availableClickFields[i], new Rect(left + borderSize, this.getHeight() * 4 / 5 + borderSize, left + this.getHeight() / 5 - borderSize, this.getHeight() - borderSize), canvas);
                if (i == ClickField.selectedClickField) {
                    p.setColor(Color.YELLOW);
                    Point p1 = new Point(left + this.getHeight() / 5 - this.getHeight() / 20, this.getHeight());
                    Point p2 = new Point(left + this.getHeight() / 5, this.getHeight());
                    Point p3 = new Point(left + this.getHeight() / 5, this.getHeight() - this.getHeight() / 20);
                    Path path = new Path();
                    path.moveTo(p1.x, p1.y);
                    path.lineTo(p2.x, p2.y);
                    path.lineTo(p3.x, p3.y);
                    canvas.drawPath(path, p);
                    p.setColor(Color.RED);
                }
            }
        }
    }

    private static void drawClickField(ClickField clickField, Rect bounds, Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.RED);
        Point origin;
        float fieldSize;
        if (bounds.width() / (float) bounds.height() > clickField.getTotalWidth() / (float) clickField.getTotalHeight()) {
            fieldSize = bounds.height() / (float) clickField.getTotalHeight();
            origin = new Point((int) ((bounds.width() - clickField.getTotalWidth() * fieldSize) / 2) + bounds.left, bounds.top);
        }
        else {
            fieldSize = bounds.width() / (float) clickField.getTotalWidth();
            origin = new Point(bounds.left, (int) ((bounds.height() - clickField.getTotalHeight() * fieldSize) / 2) + bounds.top);
        }
        for (int i = 0; i < clickField.getTotalWidth(); i++) {
            for (int j = 0; j < clickField.getTotalHeight(); j++) {
                boolean isClickPosition = clickField.getClickX() == i && clickField.getClickY() == j;
                boolean isChangedOnClick = clickField.getInAbsoluteCoordinates(i, j);
                if (isChangedOnClick || isClickPosition) {
                    int rectStartX = (int) (origin.x + fieldSize * i);
                    int rectStartY = (int) (origin.y + fieldSize * j);
                    if (isChangedOnClick) {
                        Rect rect = new Rect(rectStartX, rectStartY, rectStartX + (int) fieldSize, rectStartY + (int) fieldSize);
                        canvas.drawRect(rect, p);
                    }
                    if (isClickPosition) {
                        rectStartX += fieldSize / 4;
                        rectStartY += fieldSize / 4;
                        Rect rect = new Rect(rectStartX, rectStartY, rectStartX + (int) (fieldSize / 2), rectStartY + (int) (fieldSize / 2));
                        p.setColor(Color.GREEN);
                        canvas.drawRect(rect, p);
                        p.setColor(Color.RED);
                    }
                }
            }
        }
        p.setStrokeWidth(fieldSize / 32);
        p.setColor(Color.WHITE);
        for (int i = 1; i < clickField.getTotalWidth(); i++) {
            canvas.drawLine(origin.x + i * fieldSize, origin.y, origin.x + i * fieldSize, origin.y + clickField.getTotalHeight() * fieldSize, p);
        }
        for (int i = 1; i < clickField.getTotalHeight(); i++) {
            canvas.drawLine(origin.x, origin.y + fieldSize * i, origin.x + clickField.getTotalWidth() * fieldSize, origin.y + fieldSize * i, p);
        }
    }

    private class GestureListener implements GestureDetector.OnGestureListener {

        GameView attachedView;
        public GestureListener(GameView attachedView) {
            super();
            this.attachedView = attachedView;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            int[] tapLocation = new int[2];
            this.attachedView.getLocationInWindow(tapLocation);
            float tapX = event.getX() - tapLocation[0];
            float tapY = event.getY() - tapLocation[1];
            float minX = this.attachedView.drawingOrigin.x;
            float maxX = minX + this.attachedView.attachedActivity.gameDesk.rowSize() * this.attachedView.fieldSize;
            float minY = this.attachedView.drawingOrigin.y;
            float maxY = minY + this.attachedView.attachedActivity.gameDesk.columnSize() * this.attachedView.fieldSize;
            if (tapX >= minX && tapY >= minY && tapX < maxX && tapY < maxY) {
                int column = (int) Math.floor((tapX - this.attachedView.drawingOrigin.x) / this.attachedView.fieldSize);
                int row = (int) Math.floor((tapY - this.attachedView.drawingOrigin.y) / this.attachedView.fieldSize);
                if (this.attachedView.attachedActivity.gameDesk.doAttempt(ClickField.getSelectedClickField(), row, column)) {
                    if (this.attachedView.attachedActivity.gameDesk.isCleared()) {
                        this.attachedView.attachedActivity.showFinished();
                    }
                    this.attachedView.invalidate();
                }
            }
            else if (tapY >= this.attachedView.getHeight() * 4 / 5) {
                if (this.attachedView.clickFieldListVelocity == 0) {
                    float listXPosition = this.attachedView.clickFieldListPosition + (tapX - this.attachedView.getWidth() / 2) / (float) (this.attachedView.getHeight() / 5);
                    if (listXPosition >= 0 && listXPosition < ClickField.availableClickFields.length) {
                        ClickField.selectedClickField = (int) listXPosition;
                        this.attachedView.postInvalidate();
                    }
                }
                else {
                    this.attachedView.clickFieldListVelocity = 0;
                }
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (this.isInClickFieldList(e1) && this.isInClickFieldList(e2)) {
                if ((this.attachedView.clickFieldListPosition == 0 && distanceX < 0) || (this.attachedView.clickFieldListPosition == ClickField.availableClickFields.length && distanceX > 0)) {
                    return true;
                }
                this.attachedView.clickFieldListPosition += distanceX / (this.attachedView.getHeight() / 5);
                if (this.attachedView.clickFieldListPosition < 0) {
                    this.attachedView.clickFieldListPosition = 0;
                }
                else if (this.attachedView.clickFieldListPosition > ClickField.availableClickFields.length) {
                    this.attachedView.clickFieldListPosition = ClickField.availableClickFields.length;
                }
                this.attachedView.postInvalidate();
                return true;
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (this.isInClickFieldList(e1) && this.isInClickFieldList(e2)) {
                this.attachedView.clickFieldListVelocity = -velocityX / 131072;
                return true;
            }
            return false;
        }

        private boolean isInClickFieldList(MotionEvent e) {
            int[] location = new int[2];
            this.attachedView.getLocationInWindow(location);
            float y = e.getY() - location[1];
            return y >= this.attachedView.getHeight() * 4 / 5;
        }
    }
}
