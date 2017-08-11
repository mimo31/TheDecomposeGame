package com.github.mimo31.thedecomposegame;

/**
 * Created by Viktor on 1/23/2016.
 */
public class ClickField {

    public static ClickField[] availableClickFields;
    public static int selectedClickField;
    public static ClickField[] allClickFields;
    private final boolean[][] changeField;
    private final int distanceToLeftEnd;
    private final int distanceToTopEnd;

    public ClickField(boolean[][] changeField, int distanceToLeftEnd, int distanceToTopEnd) {
        this.changeField = changeField;
        this.distanceToLeftEnd = distanceToLeftEnd;
        this.distanceToTopEnd = distanceToTopEnd;
    }

    public int getTotalWidth() {
        return this.changeField.length;
    }

    public int getTotalHeight() {
        return this.changeField[0].length;
    }

    public boolean getInAbsoluteCoordinates(int x, int y) {
        return this.changeField[x][y];
    }

    public int getClickX() {
        return this.distanceToLeftEnd;
    }

    public int getClickY() {
        return this.distanceToTopEnd;
    }

    public boolean getInClickCoordinates(int x, int y) {
        return this.changeField[this.distanceToLeftEnd + x][this.distanceToTopEnd + y];
    }

    public int getMinX() {
        return -this.distanceToLeftEnd;
    }

    public int getMaxX() {
        return this.changeField.length - this.distanceToLeftEnd - 1;
    }

    public int getMinY() {
        return -this.distanceToTopEnd;
    }

    public int getMaxY() {
        return this.changeField[0].length - this.distanceToTopEnd - 1;
    }

    public static ClickField getSelectedClickField() {
        return availableClickFields[selectedClickField];
    }

    public static void selectRandomClickField() {
        selectedClickField = (int) Math.floor(Math.random() * availableClickFields.length);
    }

    public static void initializeClickFields() {
        allClickFields = new ClickField[9];
        allClickFields[0] = new ClickField(new boolean[][]{{true, true}, {true, true}}, 0, 0);
        allClickFields[1] = new ClickField(new boolean[][]{{true}, {true}}, 0, 0);
        allClickFields[2] = new ClickField(new boolean[][]{{true, false}, {false, true}}, 0, 0);
        allClickFields[3] = new ClickField(new boolean[][]{{true, true, true}, {true, false, true}, {true, true, true}}, 1, 1);
        allClickFields[4] = new ClickField(new boolean[][]{{true, false, false}, {true, true, true}, {true, false, false}}, 1, 1);
        allClickFields[5] = new ClickField(new boolean[][]{{true, false, true}, {false, true, false}, {true, false, true}}, 1, 1);
        allClickFields[6] = new ClickField(new boolean[][]{{true, false, false, false}, {false, true, false, false}, {false, false, true, false}, {false, false, false, true}}, 1, 1);
        allClickFields[7] = new ClickField(new boolean[][]{{false, true, false, false}, {false, true, true, true}, {true, true, true, false}, {false, false, true, false}}, 1, 1);
        allClickFields[8] = new ClickField(new boolean[][]{{false, false, false, true}, {false, false, true, true}, {false, true, true, true}, {true, true, true, true}}, 1, 1);
    }
}
