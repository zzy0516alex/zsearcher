package com.Z.NovelReader.Global;

import com.Z.NovelReader.Objects.NovelChap;

public interface OnReadingListener {
    /**
     * 控制顶部和底部的工具栏是否弹出
     * @param intercept 是否拦截 若为true，则表示不考虑点击事件直接关闭
     * @param x 用户点击的x轴位置
     * @param y 用户点击的y轴位置
     * 若需要弹出，还需判断用户点击的位置是否在
     */
    void toolbarControl(boolean intercept, float x, float y);

    //改变目录突出显示章节用
    void onReadNewChap(int chapIndex);
    void onJumpToNewChap(boolean success, NovelChap newChap);
    void waitDialogControl(boolean isShow);
}
