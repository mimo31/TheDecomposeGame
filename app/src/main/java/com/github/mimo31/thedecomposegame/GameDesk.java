package com.github.mimo31.thedecomposegame;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Viktor on 1/22/2016.
 *
 * Handles the data about the tile grid shown to the player including the animations.
 */
public class GameDesk implements Parcelable
{
    /**
     * Describes the state of the tiles.
     * Accessed by states[x + width * y].
     */
    public final boolean[] states;

    /**
     * Describes whether a tile is being animated.
     * Accessed by animating[x + width * y]
     */
    public final boolean[] animating;

    /**
     * The width of the desk - the number of tiles in the x direction.
     */
    public final int width;

    /**
     * The height of the desk - the number of tiles in the y direction.
     */
    public final int height;

    /**
     * Indicates whether some tiles are currently animated.
     * Should be set back to false when the animationLength is over while drawing.
     */
    public boolean isAnimating;

    /**
     * The time (from System.currentTimeMillis()) of when the animation began.
     */
    public long animationBegin;

    /**
     * The number of milliseconds the animation should last.
     */
    public static final int animationLength = 200;

    public GameDesk(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.states = new boolean[width * height];
        this.animating = new boolean[width * height];
    }

    /**
     * Constructs a new GameDesk with the specified tile states.
     * Copies the states into a new array, so the passed array remains unchanged.
     * @param width width of the tile grid
     * @param height height of the tile grid
     * @param states the states for the new GameDesk
     */
    public GameDesk(int width, int height, boolean[] states)
    {
        this.width = width;
        this.height = height;
        this.states = new boolean[this.width * this.height];
        System.arraycopy(states, 0, this.states, 0, width * height);
        this.animating = new boolean[this.width * this.height];
    }

    /**
     * @param x the x coordinate of the tile
     * @param y the y coordinate of the tile
     * @return the state of the tile at the specified coordinates
     */
    public boolean state(int x, int y)
    {
        return this.states[x + this.width * y];
    }

    /**
     * Checks whether applying a ClickField at a specified location is valid.
     * If yes, applies the ClickField which results in changing the state of the corresponding tiles and starting an animation.
     * In no, does nothing.
     * @param field the ClickField to apply
     * @param x the x coordinate of the tile to apply the field on
     * @param y the y coordinate of the tile to apply the field on
     * @return whether the application was valid - whether the ClickField was applied
     */
    public boolean doAttempt(ClickField field, int x, int y)
    {
        // get the size of the ClickField
        int fieldMinX = field.getMinX();
        int fieldMinY = field.getMinY();
        int fieldMaxX = field.getMaxX();
        int fieldMaxY = field.getMaxY();

        // check whether the application is valid
        if (x + fieldMinX < 0 || y + fieldMinY < 0 || x + fieldMaxX >= this.width || y + fieldMaxY >= this.height)
        {
            return false;
        }

        // begin the animation
        this.isAnimating = true;
        this.animationBegin = System.currentTimeMillis();

        // clear any old animations
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.animating[i] = false;
        }

        // apply the ClickField - change the state and animation state of the corresponding tiles
        for (int i = fieldMinX; i <= fieldMaxX; i++)
        {
            for (int j = fieldMinY; j <= fieldMaxY; j++)
            {
                if (field.getInClickCoordinates(i, j))
                {
                    int index = (i + x) + this.width * (j + y);
                    this.states[index] = !this.states[index];
                    this.animating[index] = true;
                }
            }
        }
        return true;
    }

    /**
     * @return whether all fields are cleared - the level is complete
     */
    public boolean isCleared()
    {
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            if (this.states[i])
            {
                return false;
            }
        }
        return true;
    }

    // parcelling implementation here and under

    protected GameDesk(Parcel in)
    {
        this.states = in.createBooleanArray();
        this.width = in.readInt();
        this.height = in.readInt();
        this.isAnimating = in.readByte() != 0;
        if (this.isAnimating)
        {
            this.animationBegin = in.readLong();
            this.animating = in.createBooleanArray();
        }
        else
        {
            this.animating = new boolean[this.width * this.height];
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeBooleanArray(this.states);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeByte((byte) (this.isAnimating ? 1 : 0));
        if (this.isAnimating)
        {
            dest.writeLong(this.animationBegin);
            dest.writeBooleanArray(this.animating);
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<GameDesk> CREATOR = new Creator<GameDesk>()
    {
        @Override
        public GameDesk createFromParcel(Parcel in)
        {
            return new GameDesk(in);
        }

        @Override
        public GameDesk[] newArray(int size)
        {
            return new GameDesk[size];
        }
    };
}
