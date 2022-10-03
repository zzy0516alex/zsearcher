package com.Z.NovelReader.Fragments;

import android.widget.RadioButton;
import android.widget.Toast;

import com.Z.NovelReader.R;

public class NovelViewFragmentFactory {
    public enum ViewMode{VERTICAL,HORIZONTAL,VIVID}//竖直阅读模式、水平阅读模式

    //根据阅读模式选择需要的fragment
    public static NovelViewBasicFragment getNovelViewFragment(ViewMode mode){
        switch(mode){
            case VERTICAL:
                return new NovelViewVerticalFragment();
            case HORIZONTAL:
                return new NovelViewHorizontalFragment();
            default:
                return null;
        }
    }

    public static String getViewModeInString(ViewMode mode){
        switch(mode){
            case VERTICAL:
                return  "vertical";
            case HORIZONTAL:
                return  "horizon";
            case VIVID:
                return "vivid";
            default:
                return "";
        }
    }

    public static ViewMode parseViewMode(String s_mode){
        switch(s_mode){
            case "vertical":
                return ViewMode.VERTICAL;
            case "horizon":
                return ViewMode.HORIZONTAL;
            case "vivid":
                return ViewMode.VIVID;
            default:
                return null;
        }
    }
}
