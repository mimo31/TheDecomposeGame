package com.github.mimo31.thedecomposegame;

/**
 * Created by Viktor on 1/27/2016.
 */
public final class ClickToApply {

    final int x;
    final int y;
    final ClickField field;

    public ClickToApply(int x, int y, ClickField field) {
        this.x = x;
        this.y = y;
        this.field = field;
    }

    public void apply(GameDesk desk) {
        desk.doAttempt(this.field, y, x);
    }
}
