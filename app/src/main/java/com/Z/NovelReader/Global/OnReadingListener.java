package com.Z.NovelReader.Global;

import com.Z.NovelReader.Objects.NovelChap;

public interface OnReadingListener {
    void onScreenTouch(float x,float y);
    void onScroll(boolean isStop);
    void onReadNewChap(int chapIndex);
    void onJumpToNewChap(NovelChap newChap);
}
