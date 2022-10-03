package com.Z.NovelReader.Objects.beans;

import com.Z.NovelReader.Fragments.NovelViewBasicFragment;

public class NovelViewTheme {
    private int textSize;//字体大小[15,35]
    private float lineSpaceMulti;//行距放大(缩小)倍率 [0.5,2.5]
    private NovelViewBasicFragment.DNMod DayNightMode;

    public static final int THEME_TYPE_TEXT_SIZE = 0x01;
    public static final int THEME_TYPE_LINE_SPACE_MULTI = 0x02;
    public static final int THEME_TYPE_DN_MODE = 0x03;

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public float getLineSpaceMulti() {
        return lineSpaceMulti;
    }

    public void setLineSpaceMulti(float lineSpaceMulti) {
        this.lineSpaceMulti = lineSpaceMulti;
    }

    public NovelViewBasicFragment.DNMod getDayNightMode() {
        return DayNightMode;
    }

    public void setDayNightMode(NovelViewBasicFragment.DNMod dayNightMode) {
        DayNightMode = dayNightMode;
    }
}
