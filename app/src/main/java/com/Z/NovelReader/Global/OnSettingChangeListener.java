package com.Z.NovelReader.Global;

import com.Z.NovelReader.Fragments.NovelViewBasicFragment;
import com.Z.NovelReader.Fragments.NovelViewVerticalFragment;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelViewTheme;

public interface OnSettingChangeListener {
    /**@see NovelViewVerticalFragment
     * @see com.Z.NovelReader.Fragments.NovelViewHorizontalFragment
     * */

    //switch to *mod*
    void onDNModChange(NovelViewBasicFragment.DNMod mod);

    void onCatalogItemClick(int position);

    //adapter setTextSize
//    void onTextSizeChange(int size);
//
//    void onLineSpaceChange(float multi);

    //change novel view font theme
    void onViewThemeChange(NovelViewTheme theme, int change_type);

    //update catalog
    void onCatalogUpdate(NovelCatalog new_catalog);

}
