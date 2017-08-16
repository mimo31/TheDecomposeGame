package com.github.mimo31.thedecomposegame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Viktor on 3/11/2016.
 * <p>
 * Class for drawing strings into rectangular bounds.
 */
public class StringDraw
{

    /**
     * Specifies the align of text for the StringDraw methods.
     */
    public enum TextAlign
    {
        MIDDLE, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT
    }

    /**
     * Draws the string with the maximal possible size so that it still fits in the rectangle aligned in the middle.
     *
     * @param s          the string to draw
     * @param bounds     the rectangle to draw into
     * @param borderSize the size of the border to remove from the rectangle
     * @param canvas     the canvas to draw on
     * @param p          the paint object to draw with
     */
    public static void drawMaxString(String s, Rect bounds, int borderSize, Canvas canvas, Paint p)
    {
        drawMaxString(s, bounds, borderSize, TextAlign.MIDDLE, canvas, p);
    }

    /**
     * Draws the string with the maximal possible size so that it still fits in the rectangle.
     *
     * @param s          the string to draw
     * @param bounds     the rectangle to draw into
     * @param borderSize the size of the border to remove from the rectangle
     * @param align      the text align
     * @param canvas     the canvas to draw on
     * @param p          the paint object to draw with
     */
    public static void drawMaxString(String s, Rect bounds, int borderSize, TextAlign align, Canvas canvas, Paint p)
    {
        bounds = applyBorders(bounds, borderSize);
        p.setTextSize(getMaxTextSize(s, bounds, p));
        fitString(s, bounds, align, canvas, p);
    }

    /**
     * Draws the string with the maximal possible size so that it still fits in the rectangle.
     *
     * @param s      the string to draw
     * @param bounds the rectangle to draw into
     * @param align  the text align
     * @param canvas the canvas to draw on
     * @param p      the paint object to draw with
     */
    public static void drawMaxString(String s, Rect bounds, TextAlign align, Canvas canvas, Paint p)
    {
        p.setTextSize(getMaxTextSize(s, bounds, p));
        fitString(s, bounds, align, canvas, p);
    }

    /**
     * Draws the string with the provided text size properly aligned within a rectangle.
     *
     * @param s        the string to draw
     * @param bounds   the rectangle to draw into
     * @param align    the text align
     * @param canvas   the canvas to draw on
     * @param p        the paint object to use
     * @param textSize the text size to draw the string with
     */
    public static void drawMaxString(String s, Rect bounds, TextAlign align, Canvas canvas, Paint p, float textSize)
    {
        p.setTextSize(textSize);
        fitString(s, bounds, align, canvas, p);
    }

    /**
     * Draws the string with the provided text size properly aligned within a rectangle.
     *
     * @param s        the string to draw
     * @param left     the left of the rectangle to draw into
     * @param top      the top of the rectangle to draw into
     * @param right    the right of the rectangle to draw into
     * @param bottom   the bottom of the rectangle to draw into
     * @param align    the text align
     * @param canvas   the canvas to draw on
     * @param p        the paint object to use
     * @param textSize the text size to draw the string with
     */
    public static void drawMaxString(String s, int left, int top, int right, int bottom, TextAlign align, Canvas canvas, Paint p, float textSize)
    {
        p.setTextSize(textSize);
        fitString(s, left, top, right, bottom, align, canvas, p);
    }

    /**
     * Draws the string with the maximal possible size so that it still fits in the rectangle.
     *
     * @param s      the string to draw
     * @param data   the StringDrawData object to use
     * @param canvas the canvas to draw on
     * @param p      the paint object to draw with
     */
    public static void drawMaxString(String s, StringDrawData data, Canvas canvas, Paint p)
    {
        p.setTextSize(data.size);
        canvas.drawText(s, data.x, data.y, p);
    }

    /**
     * Returns the StringDrawData object for drawing with the specified parameters.
     *
     * @param s      the string to draw
     * @param bounds the rectangle to draw into
     * @param align  the text align
     * @param p      the paint object to use
     * @return the corresponding StringDrawData object
     */
    public static StringDrawData getMaxStringData(String s, Rect bounds, TextAlign align, Paint p)
    {
        float size = getMaxTextSize(s, bounds, p);
        p.setTextSize(size);
        return getMaxStringData(s, bounds, align, p, size);
    }

    /**
     * Calculates the maximal text size used to draw a string so that it can still fit in the specified rectangle.
     * Does not draw the string
     *
     * @param s      the string to calculate with
     * @param bounds the enclosing rectangle
     * @param p      the paint object to use
     * @return the calculated maximal text size for drawing the string
     */
    public static float getMaxTextSize(String s, Rect bounds, Paint p)
    {
        p.setTextSize(100);
        Rect bounds100 = new Rect();
        p.getTextBounds(s, 0, s.length(), bounds100);
        if ((bounds100.bottom - bounds100.top) / (float) (bounds100.right - bounds100.left) < (bounds.bottom - bounds.top) / (float) (bounds.right - bounds.left))
        {
            return 100 * (bounds.right - bounds.left) / (float) (bounds100.right - bounds100.left);
        }
        else
        {
            return 100 * (bounds.bottom - bounds.top) / (float) (bounds100.bottom - bounds100.top);
        }
    }

    /**
     * Draws a string properly aligned in the specified rectangle with the text size set in the paint object.
     *
     * @param s      the string to draw
     * @param left   the left of the rectangle to draw into
     * @param top    the top of the rectangle to draw into
     * @param right  the right of the rectangle to draw into
     * @param bottom the bottom of the rectangle to draw into
     * @param align  the align of the string
     * @param canvas the canvas to draw on
     * @param p      the paint object to draw with
     */
    private static void fitString(String s, int left, int top, int right, int bottom, TextAlign align, Canvas canvas, Paint p)
    {
        Rect textSize = new Rect();
        p.getTextBounds(s, 0, s.length(), textSize);
        int leftX = left - textSize.left;
        int middleX = (left + right - textSize.left - textSize.right) / 2;
        int rightX = right - textSize.right;
        int upY = top - textSize.top;
        int middleY = (top + bottom - textSize.top - textSize.bottom) / 2;
        int downY = bottom - textSize.bottom;
        int x;
        int y;
        switch (align)
        {
            case DOWN_LEFT:
                x = leftX;
                y = downY;
                break;
            case DOWN:
                x = middleX;
                y = downY;
                break;
            case DOWN_RIGHT:
                x = rightX;
                y = downY;
                break;
            case LEFT:
                x = leftX;
                y = middleY;
                break;
            case RIGHT:
                x = rightX;
                y = middleY;
                break;
            case UP:
                x = middleX;
                y = upY;
                break;
            case UP_LEFT:
                x = leftX;
                y = upY;
                break;
            case UP_RIGHT:
                x = rightX;
                y = upY;
                break;
            default:
                x = middleX;
                y = middleY;

        }
        canvas.drawText(s, x, y, p);
    }

    /**
     * Draws a string using properly aligned in the specified rectangle.
     *
     * @param s      the string to draw
     * @param bounds the rectangle to draw into
     * @param align  the align of the string
     * @param canvas the canvas to draw on
     * @param p      the paint object to draw with
     */
    private static void fitString(String s, Rect bounds, TextAlign align, Canvas canvas, Paint p)
    {
        Rect textSize = new Rect();
        p.getTextBounds(s, 0, s.length(), textSize);
        int leftX = bounds.left - textSize.left;
        int middleX = (bounds.left + bounds.right - textSize.left - textSize.right) / 2;
        int rightX = bounds.right - textSize.right;
        int upY = bounds.top - textSize.top;
        int middleY = (bounds.top + bounds.bottom - textSize.top - textSize.bottom) / 2;
        int downY = bounds.bottom - textSize.bottom;
        int x;
        int y;
        switch (align)
        {
            case DOWN_LEFT:
                x = leftX;
                y = downY;
                break;
            case DOWN:
                x = middleX;
                y = downY;
                break;
            case DOWN_RIGHT:
                x = rightX;
                y = downY;
                break;
            case LEFT:
                x = leftX;
                y = middleY;
                break;
            case RIGHT:
                x = rightX;
                y = middleY;
                break;
            case UP:
                x = middleX;
                y = upY;
                break;
            case UP_LEFT:
                x = leftX;
                y = upY;
                break;
            case UP_RIGHT:
                x = rightX;
                y = upY;
                break;
            default:
                x = middleX;
                y = middleY;

        }
        canvas.drawText(s, x, y, p);
    }

    /**
     * Returns the StringDrawData object for drawing with the specified parameters.
     *
     * @param s      the string to draw
     * @param bounds the to draw into
     * @param align  the align of text
     * @param p      the paint object to draw with
     * @param size   the text size to draw with (should be first calculated by the getMaxTextSize method)
     * @return the corresponding StringDrawData object.
     */
    private static StringDrawData getMaxStringData(String s, Rect bounds, TextAlign align, Paint p, float size)
    {
        Rect textSize = new Rect();
        p.getTextBounds(s, 0, s.length(), textSize);
        int leftX = bounds.left - textSize.left;
        int middleX = (bounds.left + bounds.right - textSize.left - textSize.right) / 2;
        int rightX = bounds.right - textSize.right;
        int upY = bounds.top - textSize.top;
        int middleY = (bounds.top + bounds.bottom - textSize.top - textSize.bottom) / 2;
        int downY = bounds.bottom - textSize.bottom;
        int x;
        int y;
        switch (align)
        {
            case DOWN_LEFT:
                x = leftX;
                y = downY;
                break;
            case DOWN:
                x = middleX;
                y = downY;
                break;
            case DOWN_RIGHT:
                x = rightX;
                y = downY;
                break;
            case LEFT:
                x = leftX;
                y = middleY;
                break;
            case RIGHT:
                x = rightX;
                y = middleY;
                break;
            case UP:
                x = middleX;
                y = upY;
                break;
            case UP_LEFT:
                x = leftX;
                y = upY;
                break;
            case UP_RIGHT:
                x = rightX;
                y = upY;
                break;
            default:
                x = middleX;
                y = middleY;

        }
        return new StringDrawData(x, y, size);
    }

    /**
     * Draws maximum strings into multiple rectangles. All the strings are drawn with the same size.
     * That means that the size of the drawing with the smallest size is selected.
     *
     * @param strings    the array of strings to draw; corresponding one-to-one to the bounds array
     * @param bounds     the array of rectangle to draw into; corresponding one-to-ont to the strings array
     * @param borderSize the size of the border; is applied to all the rectangles
     * @param align      the align of the text; is applied to all the strings
     * @param canvas     the canvas to draw on
     * @param p          the paint object to draw with
     */
    public static void drawMaxStrings(String[] strings, Rect[] bounds, int borderSize, TextAlign align, Canvas canvas, Paint p)
    {
        for (int i = 0; i < bounds.length; i++)
        {
            bounds[i] = applyBorders(bounds[i], borderSize);
        }
        float smallestTextSize = getMaxTextSize(strings[0], bounds[0], p);
        for (int i = 1; i < strings.length; i++)
        {
            float currentTextSize = getMaxTextSize(strings[i], bounds[i], p);
            if (currentTextSize < smallestTextSize)
            {
                smallestTextSize = currentTextSize;
            }
        }
        p.setTextSize(smallestTextSize);
        for (int i = 0; i < strings.length; i++)
        {
            fitString(strings[i], bounds[i], align, canvas, p);
        }
    }

    /**
     * Creates a new rectangle that is smaller by the borderSize argument.
     *
     * @param original   the original rectangle (will be left intact)
     * @param borderSize the size of the border to take away form the rectangle
     * @return a new, reduced rectangle
     */
    public static Rect applyBorders(Rect original, int borderSize)
    {
        return new Rect(original.left + borderSize, original.top + borderSize, original.right - borderSize, original.bottom - borderSize);
    }

    /**
     * Shrinks the r argument by the size of the argument borderSize.
     *
     * @param r          the rectangle to shrink
     * @param borderSize the size of the border to remove from the rectangle
     */
    public static void applyBordersChange(Rect r, int borderSize)
    {
        r.left += borderSize;
        r.top += borderSize;
        r.right -= borderSize;
        r.bottom -= borderSize;
    }

    /**
     * Contains precalculated data about drawing a specific string.
     */
    static class StringDrawData
    {
        private final int x;
        private final int y;
        private final float size;

        private StringDrawData(int x, int y, float size)
        {
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }
}
