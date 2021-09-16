package com.Z.NovelReader.Global;

import com.Z.NovelReader.Adapters.NovelViewAdapter.DNMod;
import com.Z.NovelReader.myObjects.beans.NovelCatalog;

public interface OnSettingChangeListener {
    /**@see com.Z.NovelReader.Fragments.NovelViewFragment*/

    //switch to *mod*
    void onDNModChange(DNMod mod);

    void onCatalogItemClick(int position);

    //adapter setTextSize
    void onTextSizeChange(int size);

    //update catalog
    void onCatalogUpdate(NovelCatalog new_catalog);

//    void onToolBarVisibilityChange(boolean isVisible);
}
