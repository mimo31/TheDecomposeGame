package com.github.mimo31.thedecomposegame;

/**
 * Created by Viktor on 1/27/2016.
 *
 * Stores the initial state and allowed ClickFields for a level. Should be initialized statically at the start of the Application.
 */
public class Level
{

    /**
     * An array of all levels in the game.
     */
    public static Level[] levels;

    /**
     * Array of the same length as the levels array.
     * Contains the shortest times for
     */
    public static int[] bestTimes;

    /**
     * The max level that can be played. That's the last completed level + 1 (until we run out of levels).
     */
    public static int maxLevel = 0;

    public final int width;
    public final int height;
    public final boolean[] tiles;
    public final ClickField[] allowedClickFields;

    /**
     * Creates a level.
     * Meant to be called statically at the beginning of the execution.
     * @param width width of the tile grid
     * @param height height of the tile grid
     * @param positionsWithClickFieldIndexes an array specifying the applied ClickFields
     *                                       its length should be 3 * n
     *                                       [3 * i] should specify the x location of the applied click
     *                                       [3 * i + 1] should specify the y location of the applied click
     *                                       [3 * i + 2] should specify which ClickField to use as an index of the clickFields array argument
     * @param clickFields an array of the allowed ClickField for the level
     */
    public Level(int width, int height, int[] positionsWithClickFieldIndexes, ClickField[] clickFields)
    {
        this.width = width;
        this.height = height;
        this.allowedClickFields = clickFields;
        this.tiles = new boolean[width * height];

        // apply all the ClickFields as specified
        int clickCount = positionsWithClickFieldIndexes.length / 3;
        for (int i = 0; i < clickCount; i++)
        {
            int appX = positionsWithClickFieldIndexes[i * 3];
            int appY = positionsWithClickFieldIndexes[i * 3 + 1];
            int fieldInd = positionsWithClickFieldIndexes[i * 3 + 2];
            ClickField field = clickFields[fieldInd];
            int minX = field.getMinX();
            int minY = field.getMinY();
            int maxX = field.getMaxX();
            int maxY = field.getMaxY();
            for (int j = minX; j <= maxX; j++)
            {
                for (int k = minY; k <= maxY; k++)
                {
                    if (field.getInClickCoordinates(j, k))
                    {
                        int index = (j + appX) + (k + appY) * this.width;
                        this.tiles[index] = !this.tiles[index];
                    }
                }
            }
        }
    }

    /**
     * @return a new GameDesk for playing this level
     */
    public GameDesk getNewDesk()
    {
        return new GameDesk(this.width, this.height, this.tiles);
    }


    public static void initializeLevels()
    {
        levels = new Level[16];

        ClickField fullTwoField = ClickField.allClickFields[0];

        levels[0] = new Level(4, 4, new int[] {
                1, 1, 0
        }, new ClickField[] { fullTwoField } );

        levels[1] = new Level(4, 4, new int[] {
                1, 2, 0,
                2, 1, 0
        }, new ClickField[] { fullTwoField } );

        ClickField twoClickField = ClickField.allClickFields[1];

        levels[2] = new Level(4, 4, new int[] {
                2, 1, 0
        }, new ClickField[] { twoClickField } );

        levels[3] = new Level(4, 4, new int[] {
                1, 0, 0,
                1, 1, 0,
                1, 2, 0,
                1, 3, 0,
                0, 2, 0,
                2, 2, 0
        }, new ClickField[] { twoClickField } );

        levels[4] = new Level(4, 4, new int[] {
                0, 1, 0,
                2, 1, 0,
                1, 1, 1
        }, new ClickField[] { fullTwoField, twoClickField } );

        ClickField twoCrossField = ClickField.allClickFields[2];
        
        levels[5] = new Level(4, 4, new int[] {
                1, 1, 0, 
                1, 1, 1
        }, new ClickField[] { fullTwoField, twoCrossField } );
        
        levels[6] = new Level(6, 6, new int[] {
                1, 4, 0,
                4, 4, 0,
                3, 2, 0,
                0, 3, 1,
                3, 2, 1,
                0, 0, 1
        }, new ClickField[] { fullTwoField, twoCrossField } );
        
        levels[7] = new Level(8, 8, new int[] {
                6, 4, 0,
                1, 2, 0,
                5, 6, 0,
                1, 4, 0,
                3, 5, 0,
                6, 2, 1,
                3, 4, 1,
                5, 1, 1,
                2, 6, 1,
                6, 3, 1
        }, new ClickField[] { fullTwoField, twoCrossField } );
        
        levels[8] = new Level(8, 8, new int[] {
                2, 1, 0,
                5, 6, 0,
                5, 3, 0,
                3, 2, 0,
                1, 2, 0,
                2, 5, 0,
                6, 6, 1,
                2, 5, 1,
                1, 5, 1,
                2, 4, 1,
                4, 4, 1,
                2, 2, 1
        }, new ClickField[] { fullTwoField, twoCrossField } );
        
        ClickField holeField = ClickField.allClickFields[3];
        ClickField downIcicle = ClickField.allClickFields[4];
        
        levels[9] = new Level(8, 8, new int[] {
                3, 1, 0,
                5, 1, 0,
                1, 1, 0,
                2, 5, 0,
                1, 6, 0,
                4, 2, 0,
                3, 3, 1,
                2, 1, 1,
                4, 2, 1,
                6, 2, 1,
                1, 5, 1,
                4, 5, 1
        }, new ClickField[] { holeField, downIcicle } );

        levels[10] = new Level(8, 8, new int[] {
                6, 2, 0,
                5, 4, 0,
                3, 3, 0,
                5, 5, 0,
                5, 4, 0,
                3, 2, 0,
                4, 6, 1,
                5, 2, 1,
                4, 5, 1,
                5, 2, 1,
                4, 2, 1,
                3, 3, 1
        }, new ClickField[] { holeField, downIcicle } );

        ClickField chess3x3 = ClickField.allClickFields[5];

        levels[11] = new Level(8, 8, new int[] {
                1, 2, 0,
                1, 4, 0,
                1, 2, 0,
                3, 6, 0,
                2, 4, 0,
                4, 5, 0,
                2, 4, 1,
                2, 6, 1,
                5, 6, 1,
                3, 6, 1,
                2, 4, 1,
                3, 6, 1
        }, new ClickField[] { chess3x3, downIcicle } );

        levels[12] = new Level(8, 8, new int[] {
                1, 3, 0,
                1, 4, 0,
                2, 5, 0,
                2, 6, 0,
                4, 5, 0,
                4, 5, 0,
                3, 6, 0,
                5, 6, 0,
                2, 3, 1,
                1, 2, 1,
                4, 5, 1,
                2, 6, 1,
                1, 2, 1,
                1, 2, 1,
                3, 4, 1,
                4, 5, 1
        }, new ClickField[] { chess3x3, downIcicle } );

        ClickField diagonal4 = ClickField.allClickFields[6];

        levels[13] = new Level(12, 12, new int[] {
                8, 10, 0,
                2, 6, 0,
                3, 8, 0,
                1, 2, 0,
                1, 5, 0,
                4, 7, 0,
                8, 9, 0,
                2, 6, 0,
                1, 2, 1,
                7, 9, 1,
                5, 9, 1,
                2, 4, 1,
                2, 9, 1,
                1, 3, 1,
                1, 4, 1,
                2, 3, 1
        }, new ClickField[] { holeField, diagonal4 } );

        ClickField hook4Field = ClickField.allClickFields[7];

        levels[14] = new Level(12, 12, new int[] {
                5, 6, 0,
                2, 6, 0,
                1, 3, 0,
                3, 6, 0,
                4, 7, 0,
                5, 8, 0,
                1, 5, 0,
                4, 9, 0,
                2, 5, 1,
                4, 7, 1,
                6, 9, 1,
                3, 5, 1,
                1, 3, 1,
                2, 8, 1,
                1, 9, 1,
                2, 7, 1
        }, new ClickField[] { hook4Field, diagonal4 } );

        levels[15] = new Level(12, 12, new int[] {
                6, 9, 0,
                5, 7, 0,
                4, 6, 0,
                1, 2, 0,
                1, 5, 0,
                5, 7, 0,
                6, 9, 0,
                1, 8, 0,
                4, 8, 1,
                7, 8, 1,
                2, 7, 1,
                1, 2, 1,
                5, 7, 1,
                1, 7, 1,
                1, 9, 1,
                7, 9, 1
        }, new ClickField[] { hook4Field, diagonal4 } );

        bestTimes = new int[levels.length];
    }
}
