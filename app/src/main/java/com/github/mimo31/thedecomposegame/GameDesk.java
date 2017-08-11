package com.github.mimo31.thedecomposegame;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Viktor on 1/22/2016.
 */
public class GameDesk implements Parcelable {

    public boolean[][] states;

    public GameDesk(int rowSize, int columnSize) {
        this.states = new boolean[columnSize][rowSize];
    }

    private GameDesk(boolean[][] states) {
        this.states = states;
    }

    public int rowSize() {
        return states[0].length;
    }

    public int columnSize() {
        return states.length;
    }

    public static final Parcelable.Creator<GameDesk> CREATOR
            = new Parcelable.Creator<GameDesk>() {
        public GameDesk createFromParcel(Parcel in) {
            return new GameDesk(in);
        }

        public GameDesk[] newArray(int size) {
            return new GameDesk[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.states.length);
        dest.writeInt(this.states[0].length);
        for (int i = 0; i < this.states.length; i++) {
            dest.writeBooleanArray(this.states[i]);
        }
    }

    public GameDesk(Parcel in) {
        this.states = new boolean[in.readInt()][];
        int length = in.readInt();
        for (int i = 0; i < this.states.length; i++) {
            this.states[i] = new boolean[length];
            in.readBooleanArray(this.states[i]);
        }
    }

    public GameDesk(boolean[] semiStates, int rowSize) {
        this.states = new boolean[semiStates.length / rowSize][rowSize];
        int row = 0;
        int column = 0;
        for (int i = 0; i < semiStates.length; i++) {
            this.states[row][column] = semiStates[i];
            column++;
            if (column == rowSize) {
                row++;
                column = 0;
            }
        }
    }

    public boolean[] asArray() {
        boolean[] array = new boolean[this.rowSize() * this.columnSize()];
        for (int i = 0; i < this.columnSize(); i++) {
            for (int j = 0; j < this.rowSize(); j++) {
                array[j + i * this.rowSize()] = this.states[i][j];
            }
        }
        return array;
    }

    public boolean doAttempt(ClickField field, int row, int column) {
        if (column + field.getMinX() < 0 || row + field.getMinY() < 0 || column + field.getMaxX() >= this.rowSize() || row + field.getMaxY() >= this.columnSize()) {
            return false;
        }
        for (int i = field.getMinX(); i <= field.getMaxX(); i++) {
            for (int j = field.getMinY(); j <= field.getMaxY(); j++) {
                if (field.getInClickCoordinates(i, j)) {
                    this.states[row + j][column + i] = !this.states[row + j][column + i];
                }
            }
        }
        return true;
    }

    public GameDesk clone() {
        boolean[][] newStates = new boolean[this.states.length][this.states[0].length];
        for (int i = 0; i < newStates.length; i++) {
            for (int j = 0; j < newStates[0].length; j++) {
                newStates[i][j] = this.states[i][j];
            }
        }
        return new GameDesk(newStates);
    }

    public boolean isCleared() {
        for (int i = 0; i < this.states.length; i++) {
            for (int j = 0; j < this.states[0].length; j++) {
                if (this.states[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
