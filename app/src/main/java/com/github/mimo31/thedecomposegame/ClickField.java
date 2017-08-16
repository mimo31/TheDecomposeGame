package com.github.mimo31.thedecomposegame;

/**
 * Created by Viktor on 1/23/2016.
 * <p>
 * Contains data about the shape of neighboring tiles that can be changed at once.
 */
public class ClickField
{

    /**
     * All the ClickField available to the player in the level currently played.
     */
    public static ClickField[] availableClickFields;

    /**
     * The index of the availableClickFields array of which ClickField is now selected.
     */
    public static int selectedClickField;

    /**
     * All the ClickFields. Used for creating levels.
     */
    public static ClickField[] allClickFields;

    // an array specifying which tiles will be changed when the ClickField is used
    private final boolean[] changeField;

    /**
     * The number of tiles in the x direction.
     */
    public final int width;

    /**
     * The number of tiles in the y direction.
     */
    public final int height;

    /**
     * The x coordinate of the click tile when origin is placed at the top left tile.
     */
    public final int clickX;

    /**
     * The y coordinate of the click tile when origin is placed at the top left tile.
     */
    public final int clickY;

    public ClickField(boolean[] changeField, int width, int height, int clickX, int clickY)
    {
        this.changeField = changeField;
        this.width = width;
        this.height = height;
        this.clickX = clickX;
        this.clickY = clickY;
    }

    /**
     * @param x x coordinate of the tile when origin is placed at the top left tile
     * @param y y coordinate of the tile when origin is placed at the top left tile
     * @return value at that tile - whether that tile should be changed when this ClickField is used
     */
    public boolean getInAbsoluteCoordinates(int x, int y)
    {
        return this.changeField[x + this.width * y];
    }

    /**
     * @param x x coordinate of the tile when origin is placed at the click tile
     * @param y y coordinate of the tile when origin is placed at the click tile
     * @return value at that tile - whether that tile should be changed when this ClickField is used
     */
    public boolean getInClickCoordinates(int x, int y)
    {
        return this.changeField[this.clickX + x + this.width * (this.clickY + y)];
    }

    /**
     * @return the lowest value of the x coordinate in click coordinates (see getInClickCoordinates) where this ClickField is defined
     */
    public int getMinX()
    {
        return -this.clickX;
    }

    /**
     * @return the highest value of the x coordinate in click coordinates (see getInClickCoordinates) where this ClickField is defined
     */
    public int getMaxX()
    {
        return this.width - this.clickX - 1;
    }

    /**
     * @return the lowest value of the y coordinate in click coordinates (see getInClickCoordinates) where this ClickField is defined
     */
    public int getMinY()
    {
        return -this.clickY;
    }

    /**
     * @return the highest value of the y coordinate in click coordinates (see getInClickCoordinates) where this ClickField is defined
     */
    public int getMaxY()
    {
        return this.height - this.clickY - 1;
    }

    /**
     * @return the currently selected ClickField
     */
    public static ClickField getSelectedClickField()
    {
        return availableClickFields[selectedClickField];
    }

    /**
     * Creates all the ClickFields - initializes the allClickFields array.
     * Should be called statically at the startup of the application.
     */
    public static void initializeClickFields()
    {
        allClickFields = new ClickField[12];

        allClickFields[0] = new ClickField(new boolean[]{
                true, true,
                true, true
        }, 2, 2, 0, 0);

        allClickFields[1] = new ClickField(new boolean[]{true, true}, 2, 1, 0, 0);
        allClickFields[2] = new ClickField(new boolean[]{true, false, false, true}, 2, 2, 0, 0);
        allClickFields[3] = new ClickField(new boolean[]{true, true, true, true, false, true, true, true, true}, 3, 3, 1, 1);
        allClickFields[4] = new ClickField(new boolean[]{true, true, true, false, true, false, false, true, false}, 3, 3, 1, 1);
        allClickFields[5] = new ClickField(new boolean[]{true, false, true, false, true, false, true, false, true}, 3, 3, 1, 1);
        allClickFields[6] = new ClickField(new boolean[]{true, false, false, false, false, true, false, false, false, false, true, false, false, false, false, true}, 4, 4, 1, 1);
        allClickFields[7] = new ClickField(new boolean[]{false, false, true, false, true, true, true, false, false, true, true, true, false, true, false, false}, 4, 4, 1, 1);
        allClickFields[8] = new ClickField(new boolean[]{false, false, false, true, false, false, true, true, false, true, true, true, true, true, true, true}, 4, 4, 1, 1);
        allClickFields[9] = new ClickField(new boolean[]{false, false, true, true, false, false, true, false, false}, 3, 3, 1, 1);
        allClickFields[10] = new ClickField(new boolean[]{true, true, false, true, false, false, false, false, false, false, false, false, true, false, false, true}, 4, 4, 1, 1);
        allClickFields[11] = new ClickField(new boolean[]{false, true, false, false, true, false, true, true, true}, 3, 3, 1, 1);
    }
}
