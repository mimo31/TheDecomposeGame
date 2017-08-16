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
        levels = new Level[30];

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

        levels[16] = new Level(12, 12, new int[] {
                6, 5, 0,
                7, 1, 0,
                3, 8, 0,
                9, 4, 0,
                4, 3, 0,
                1, 2, 0,
                5, 8, 0,
                6, 7, 0,
                6, 4, 0,
                2, 5, 0,
                3, 9, 0,
                1, 7, 1,
                2, 1, 1,
                6, 7, 1,
                5, 9, 1,
                8, 4, 1,
                2, 5, 1,
                6, 8, 1,
                1, 4, 1,
                3, 9, 1,
                5, 1, 1,
                2, 4, 1
        }, new ClickField[] { hook4Field, diagonal4 } );

        levels[17] = new Level(12, 12, new int[] {
                1, 5, 0,
                1, 6, 0,
                2, 3, 0,
                2, 6, 0,
                2, 7, 0,
                3, 3, 0,
                5, 4, 0,
                5, 5, 0,
                6, 8, 0,
                8, 2, 0,
                9, 4, 0,
                2, 3, 1,
                3, 1, 1,
                3, 3, 1,
                4, 1, 1,
                4, 4, 1,
                4, 6, 1,
                5, 4, 1,
                6, 7, 1,
                7, 1, 1,
                9, 6, 1
        }, new ClickField[] { hook4Field, diagonal4 } );

        ClickField stairsField = ClickField.allClickFields[8];

        levels[18] = new Level(12, 12, new int[] {
                1, 1, 0,
                1, 4, 0,
                1, 9, 0,
                2, 8, 0,
                5, 3, 0,
                5, 4, 0,
                5, 8, 0,
                6, 4, 0,
                6, 6, 0,
                7, 4, 0,
                9, 7, 0,
                1, 8, 1,
                1, 9, 1,
                3, 9, 1,
                4, 4, 1,
                4, 5, 1,
                5, 7, 1,
                6, 5, 1,
                7, 2, 1,
                7, 8, 1,
                8, 5, 1,
                8, 9, 1
        }, new ClickField[] { hook4Field, stairsField } );

        levels[19] = new Level(12, 12, new int[] {
                1, 2, 0,
                3, 7, 0,
                4, 2, 0,
                4, 8, 0,
                5, 1, 0,
                5, 3, 0,
                5, 7, 0,
                6, 5, 0,
                7, 9, 0,
                8, 9, 0,
                4, 1, 1,
                6, 3, 1,
                6, 4, 1,
                6, 5, 1,
                7, 5, 1,
                7, 7, 1,
                7, 8, 1,
                8, 8, 1,
                9, 5, 1,
                9, 7, 1
        }, new ClickField[] { hook4Field, stairsField } );

        ClickField twoAndOneApart = ClickField.allClickFields[9];

        levels[20] = new Level(12, 12, new int[] {
                1, 2, 0,
                2, 5, 0,
                3, 1, 0,
                5, 4, 0,
                6, 2, 0,
                6, 6, 0,
                7, 5, 0,
                8, 7, 0,
                9, 5, 0,
                9, 6, 0,
                1, 3, 1,
                1, 5, 1,
                2, 4, 1,
                3, 1, 1,
                4, 1, 1,
                4, 2, 1,
                5, 7, 1,
                7, 9, 1,
                8, 10, 1,
                9, 1, 1,
                10, 7, 1
        }, new ClickField[] { stairsField, twoAndOneApart } );

        levels[21] = new Level(12, 12, new int[] {
                1, 6, 0,
                4, 3, 0,
                4, 9, 0,
                5, 5, 0,
                5, 7, 0,
                6, 2, 0,
                7, 5, 0,
                7, 6, 0,
                8, 4, 0,
                8, 6, 0,
                9, 8, 0,
                1, 1, 1,
                1, 6, 1,
                3, 3, 1,
                4, 9, 1,
                6, 2, 1,
                6, 8, 1,
                7, 2, 1,
                8, 1, 1,
                8, 4, 1,
                10, 4, 1
        }, new ClickField[] { stairsField, twoAndOneApart } );

        levels[22] = new Level(12, 12, new int[] {
                1, 5, 0,
                2, 9, 0,
                3, 7, 0,
                3, 8, 0,
                5, 2, 0,
                5, 5, 0,
                7, 8, 0,
                8, 8, 0,
                8, 9, 0,
                9, 7, 0,
                4, 7, 1,
                4, 9, 1,
                6, 1, 1,
                6, 2, 1,
                7, 2, 1,
                7, 7, 1,
                8, 1, 1,
                8, 5, 1,
                9, 5, 1,
                10, 5, 1,
                10, 9, 1
        }, new ClickField[] { stairsField, twoAndOneApart } );

        ClickField threeOnesAndTwo = ClickField.allClickFields[10];

        levels[22] = new Level(12, 12, new int[] {
                1, 7, 0,
                2, 8, 0,
                4, 2, 0,
                4, 4, 0,
                5, 6, 0,
                6, 2, 0,
                7, 1, 0,
                7, 7, 0,
                8, 7, 0,
                8, 8, 0,
                9, 1, 0,
                1, 8, 1,
                3, 6, 1,
                5, 1, 1,
                5, 5, 1,
                5, 9, 1,
                6, 9, 1,
                7, 6, 1,
                8, 4, 1,
                9, 8, 1,
                9, 9, 1,
                9, 10, 1
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart } );

        levels[23] = new Level(12, 12, new int[] {
                1, 1, 0,
                1, 9, 0,
                2, 2, 0,
                2, 4, 0,
                3, 6, 0,
                5, 6, 0,
                5, 7, 0,
                8, 1, 0,
                8, 9, 0,
                1, 9, 1,
                2, 1, 1,
                2, 2, 1,
                2, 4, 1,
                3, 9, 1,
                5, 1, 1,
                5, 3, 1,
                5, 9, 1,
                8, 1, 1,
                8, 2, 1,
                8, 5, 1
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart } );

        levels[24] = new Level(12, 12, new int[] {
                1, 7, 0,
                2, 1, 0,
                4, 3, 0,
                5, 1, 0,
                5, 2, 0,
                5, 3, 0,
                5, 7, 0,
                6, 8, 0,
                7, 6, 0,
                8, 4, 0,
                1, 1, 1,
                1, 3, 1,
                2, 9, 1,
                4, 7, 1,
                4, 10, 1,
                5, 2, 1,
                6, 1, 1,
                6, 5, 1,
                6, 9, 1,
                7, 9, 1,
                9, 3, 1
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart } );

        ClickField upIcicle = ClickField.allClickFields[11];

        levels[25] = new Level(12, 12, new int[] {
                2, 5, 0,
                3, 4, 0,
                3, 9, 0,
                4, 5, 0,
                5, 1, 0,
                7, 2, 0,
                7, 3, 0,
                7, 4, 0,
                8, 1, 0,
                8, 8, 0,
                9, 1, 0,
                1, 3, 1,
                3, 1, 1,
                3, 3, 1,
                3, 9, 1,
                3, 10, 1,
                5, 1, 1,
                5, 3, 1,
                6, 7, 1,
                7, 1, 1,
                9, 3, 1,
                10, 7, 1,
                1, 5, 2,
                1, 9, 2,
                3, 3, 2,
                4, 2, 2,
                4, 5, 2,
                5, 1, 2,
                7, 3, 2,
                8, 2, 2,
                8, 9, 2,
                9, 3, 2,
                10, 6, 2
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart, upIcicle } );

        levels[26] = new Level(12, 12, new int[] {
                1, 2, 0,
                1, 4, 0,
                1, 5, 0,
                2, 6, 0,
                3, 6, 0,
                4, 3, 0,
                7, 3, 0,
                7, 8, 0,
                9, 5, 0,
                9, 6, 0,
                2, 3, 1,
                2, 4, 1,
                3, 2, 1,
                3, 4, 1,
                3, 7, 1,
                4, 1, 1,
                6, 7, 1,
                8, 1, 1,
                8, 7, 1,
                9, 3, 1,
                9, 5, 1,
                1, 9, 2,
                2, 2, 2,
                2, 3, 2,
                4, 10, 2,
                5, 5, 2,
                5, 7, 2,
                6, 3, 2,
                10, 1, 2,
                10, 2, 2,
                10, 5, 2,
                10, 8, 2
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart, upIcicle } );

        levels[27] = new Level(12, 12, new int[] {
                1, 7, 0,
                2, 8, 0,
                3, 5, 0,
                4, 3, 0,
                4, 4, 0,
                5, 2, 0,
                6, 4, 0,
                6, 7, 0,
                7, 5, 0,
                9, 5, 0,
                1, 6, 1,
                1, 7, 1,
                1, 10, 1,
                2, 6, 1,
                2, 10, 1,
                3, 4, 1,
                4, 5, 1,
                4, 7, 1,
                7, 4, 1,
                9, 8, 1,
                10, 2, 1,
                1, 5, 2,
                1, 8, 2,
                1, 10, 2,
                2, 4, 2,
                3, 7, 2,
                6, 3, 2,
                7, 8, 2,
                8, 3, 2,
                10, 10, 2
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart, upIcicle } );

        levels[28] = new Level(12, 12, new int[] {
                2, 4, 0,
                3, 4, 0,
                3, 7, 0,
                4, 5, 0,
                4, 8, 0,
                5, 1, 0,
                5, 6, 0,
                6, 4, 0,
                7, 7, 0,
                8, 2, 0,
                1, 9, 1,
                2, 3, 1,
                4, 6, 1,
                4, 7, 1,
                5, 1, 1,
                5, 9, 1,
                7, 6, 1,
                7, 7, 1,
                9, 2, 1,
                10, 7, 1,
                1, 1, 2,
                1, 7, 2,
                2, 5, 2,
                2, 6, 2,
                3, 6, 2,
                3, 8, 2,
                4, 9, 2,
                5, 7, 2,
                6, 4, 2,
                6, 7, 2,
                7, 2, 2
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart, hook4Field } );

        levels[29] = new Level(12, 12, new int[] {
                1, 7, 0,
                2, 3, 0,
                2, 8, 0,
                3, 1, 0,
                3, 4, 0,
                3, 8, 0,
                4, 7, 0,
                7, 1, 0,
                8, 6, 0,
                9, 6, 0,
                2, 10, 1,
                3, 1, 1,
                3, 4, 1,
                3, 6, 1,
                5, 5, 1,
                6, 10, 1,
                9, 7, 1,
                10, 6, 1,
                10, 10, 1,
                1, 1, 2,
                1, 3, 2,
                1, 9, 2,
                2, 4, 2,
                2, 9, 2,
                3, 7, 2,
                4, 5, 2,
                4, 7, 2,
                8, 2, 2,
                8, 5, 2,
                9, 9, 2
        }, new ClickField[] { threeOnesAndTwo, twoAndOneApart, hook4Field } );

        bestTimes = new int[levels.length];
    }
}
