package com.Z.NovelReader.Fragments;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class NovelViewBasicFragment extends Fragment {
    private String tag;
    private SharedPreferences recoverInfo;

    public String getFragmentTag() {
        return tag;
    }

    public void setFragmentTag(String tag) {
        this.tag = tag;
    }

    public void saveRecoverFile(String BookName){
        recoverInfo= Objects.requireNonNull(getActivity()).getSharedPreferences("recoverInfo", Context.MODE_PRIVATE);
        recoverInfo.edit().putString("BookName",BookName)
                .putBoolean("onFront",true)
                .apply();
    }
}
