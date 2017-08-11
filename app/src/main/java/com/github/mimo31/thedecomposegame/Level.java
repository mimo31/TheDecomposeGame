package com.github.mimo31.thedecomposegame;

import android.graphics.Point;
import android.util.Pair;

/**
 * Created by Viktor on 1/27/2016.
 */
public class Level {

    public static Level[] levels;
    public static int[] bestTimes;
    public static int maxLevel = 0;

    private final GameDesk desk;
    public ClickField[] allowedClickFields;

    public Level(int numberOfRows, int numberOfColumns, ClickToApply[] clicks, ClickField[] clickFields) {
        this.desk = new GameDesk(numberOfColumns, numberOfRows);
        this.allowedClickFields = clickFields;
        for (int i = 0; i < clicks.length; i++) {
            clicks[i].apply(this.desk);
        }
    }

    public Level(int numberOfRows, int numberOfColumns, int[][] positionsWithClickFieldIndexes, ClickField[] clickFields) {
        this.desk = new GameDesk(numberOfColumns, numberOfRows);
        this.allowedClickFields = clickFields;
        for (int i = 0; i < positionsWithClickFieldIndexes.length; i++) {
            int[] currentData = positionsWithClickFieldIndexes[i];
            this.desk.doAttempt(clickFields[currentData[2]], currentData[1], currentData[0]);
        }
    }

    public GameDesk getFreeDesk() {
        return this.desk.clone();
    }

    public static void initializeLevels() {
        levels = new Level[16];
        ClickField fullTwoField = ClickField.allClickFields[0];
        levels[0] = new Level(4, 4, new ClickToApply[] { new ClickToApply(1, 1, fullTwoField) }, new ClickField[] { fullTwoField } );
        levels[1] = new Level(4, 4, new ClickToApply[] { new ClickToApply(1, 2, fullTwoField), new ClickToApply(2, 1, fullTwoField) }, new ClickField[] { fullTwoField } );
        ClickField twoClickField = ClickField.allClickFields[1];
        levels[2] = new Level(4, 4, new ClickToApply[] { new ClickToApply(2, 1, twoClickField) }, new ClickField[] { twoClickField } );
        levels[3] = new Level(4, 4, new ClickToApply[] {
                new ClickToApply(1, 0, twoClickField),
                new ClickToApply(1, 1, twoClickField),
                new ClickToApply(1, 2, twoClickField),
                new ClickToApply(1, 3, twoClickField),
                new ClickToApply(0, 2, twoClickField),
                new ClickToApply(2, 2, twoClickField)
        }, new ClickField[] { twoClickField } );
        levels[4] = new Level(4, 4, new ClickToApply[] { new ClickToApply(0, 1, fullTwoField), new ClickToApply(2, 1, fullTwoField), new ClickToApply(1, 1, twoClickField) }, new ClickField[] { twoClickField, fullTwoField } );
        ClickField twoCrossField = ClickField.allClickFields[2];
        levels[5] = new Level(4, 4, new ClickToApply[] { new ClickToApply(1, 1, fullTwoField), new ClickToApply(1, 1, twoCrossField) }, new ClickField[] { fullTwoField, twoCrossField } );
        levels[6] = new Level(6, 6, new ClickToApply[] {
                new ClickToApply(1, 4, fullTwoField),
                new ClickToApply(4, 4, fullTwoField),
                new ClickToApply(3, 2, fullTwoField),
                new ClickToApply(0, 3, twoCrossField),
                new ClickToApply(3, 2, twoCrossField),
                new ClickToApply(0, 0, twoCrossField)
        }, new ClickField[] { fullTwoField, twoCrossField } );
        levels[7] = new Level(8, 8, new ClickToApply[] {
                new ClickToApply(6, 4, fullTwoField),
                new ClickToApply(1, 2, fullTwoField),
                new ClickToApply(5, 6, fullTwoField),
                new ClickToApply(1, 4, fullTwoField),
                new ClickToApply(3, 5, fullTwoField),
                new ClickToApply(6, 2, twoCrossField),
                new ClickToApply(3, 4, twoCrossField),
                new ClickToApply(5, 1, twoCrossField),
                new ClickToApply(2, 6, twoCrossField),
                new ClickToApply(6, 3, twoCrossField)
        }, new ClickField[] { fullTwoField, twoCrossField } );
        levels[8] = new Level(8, 8, new ClickToApply[] {
                new ClickToApply(2, 1, fullTwoField),
                new ClickToApply(5, 6, fullTwoField),
                new ClickToApply(5, 3, fullTwoField),
                new ClickToApply(3, 2, fullTwoField),
                new ClickToApply(1, 2, fullTwoField),
                new ClickToApply(2, 5, fullTwoField),
                new ClickToApply(6, 6, twoCrossField),
                new ClickToApply(2, 5, twoCrossField),
                new ClickToApply(1, 5, twoCrossField),
                new ClickToApply(2, 4, twoCrossField),
                new ClickToApply(4, 4, twoCrossField),
                new ClickToApply(2, 2, twoCrossField)
        }, new ClickField[] { fullTwoField, twoCrossField } );
        ClickField holeField = ClickField.allClickFields[3];
        ClickField downIcicle = ClickField.allClickFields[4];
        levels[9] = new Level(8, 8, new ClickToApply[] {
                new ClickToApply(3, 1, holeField),
                new ClickToApply(5, 1, holeField),
                new ClickToApply(1, 1, holeField),
                new ClickToApply(2, 5, holeField),
                new ClickToApply(1, 6, holeField),
                new ClickToApply(4, 2, holeField),
                new ClickToApply(3, 3, downIcicle),
                new ClickToApply(2, 1, downIcicle),
                new ClickToApply(4, 2, downIcicle),
                new ClickToApply(6, 2, downIcicle),
                new ClickToApply(1, 5, downIcicle),
                new ClickToApply(4, 5, downIcicle)
        }, new ClickField[] { holeField, downIcicle } );
        levels[10] = new Level(8, 8, new int[][] {
                {6, 2, 0},
                {5, 4, 0},
                {3, 3, 0},
                {5, 5, 0},
                {5, 4, 0},
                {3, 2, 0},
                {4, 6, 1},
                {5, 2, 1},
                {4, 5, 1},
                {5, 2, 1},
                {4, 2, 1},
                {3, 3, 1}
        }, new ClickField[] { holeField, downIcicle } );
        ClickField chess3x3 = ClickField.allClickFields[5];
        levels[11] = new Level(8, 8, new int[][] {
                {1, 2, 0},
                {1, 4, 0},
                {1, 2, 0},
                {3, 6, 0},
                {2, 4, 0},
                {4, 5, 0},
                {2, 4, 1},
                {2, 6, 1},
                {5, 6, 1},
                {3, 6, 1},
                {2, 4, 1},
                {3, 6, 1}
        }, new ClickField[] { chess3x3, downIcicle } );
        levels[12] = new Level(8, 8, new int[][] {
                {1, 3, 0},
                {1, 4, 0},
                {2, 5, 0},
                {2, 6, 0},
                {4, 5, 0},
                {4, 5, 0},
                {3, 6, 0},
                {5, 6, 0},
                {2, 3, 1},
                {1, 2, 1},
                {4, 5, 1},
                {2, 6, 1},
                {1, 2, 1},
                {1, 2, 1},
                {3, 4, 1},
                {4, 5, 1}
        }, new ClickField[] { chess3x3, downIcicle } );
        ClickField diagonal4 = ClickField.allClickFields[6];
        levels[13] = new Level(12, 12, new int[][] {
                {8, 10, 0},
                {2, 6, 0},
                {3, 8, 0},
                {1, 2, 0},
                {1, 5, 0},
                {4, 7, 0},
                {8, 9, 0},
                {2, 6, 0},
                {1, 2, 1},
                {7, 9, 1},
                {5, 9, 1},
                {2, 4, 1},
                {2, 9, 1},
                {1, 3, 1},
                {1, 4, 1},
                {2, 3, 1}
        }, new ClickField[] { holeField, diagonal4 } );
        ClickField hook4Field = ClickField.allClickFields[7];
        levels[14] = new Level(12, 12, new int[][] {
                {5, 6, 0},
                {2, 6, 0},
                {1, 3, 0},
                {3, 6, 0},
                {4, 7, 0},
                {5, 8, 0},
                {1, 5, 0},
                {4, 9, 0},
                {2, 5, 1},
                {4, 7, 1},
                {6, 9, 1},
                {3, 5, 1},
                {1, 3, 1},
                {2, 8, 1},
                {1, 9, 1},
                {2, 7, 1}
        }, new ClickField[] { hook4Field, diagonal4 } );
        levels[15] = new Level(12, 12, new int[][] {
                {6, 9, 0},
                {5, 7, 0},
                {4, 6, 0},
                {1, 2, 0},
                {1, 5, 0},
                {5, 7, 0},
                {6, 9, 0},
                {1, 8, 0},
                {4, 8, 1},
                {7, 8, 1},
                {2, 7, 1},
                {1, 2, 1},
                {5, 7, 1},
                {1, 7, 1},
                {1, 9, 1},
                {7, 9, 1}
        }, new ClickField[] { hook4Field, diagonal4 } );
        bestTimes = new int[levels.length];
    }
}
