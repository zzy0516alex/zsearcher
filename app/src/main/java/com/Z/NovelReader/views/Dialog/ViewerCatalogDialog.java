package com.Z.NovelReader.views.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.Z.NovelReader.Adapters.CatalogListAdapter;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Threads.NovelUpdateThread;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StorageUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewerCatalogDialog extends DialogFragment {

    private final NovelChap chapPointer;//引用传递，同步外部章节变化
    private NovelCatalog catalogPointer;//引用传递，同步目录变化
    private List<String> catalog_items;
    private CatalogListAdapter adapter;
    private ListView catalogList;
    private int current_item = -1;
    private CatalogCommandListener listener;
    private NovelUpdateThread.NovelUpdaterHandler catalog_update_handler;

    public ViewerCatalogDialog(NovelChap chap_pointer, NovelCatalog catalog_pointer) {
        super();
        setStyle(STYLE_NO_TITLE,R.style.NoDimDialog);
        this.chapPointer = chap_pointer;
        this.catalogPointer = catalog_pointer;
        //loadCatalog();
        if(catalogPointer !=null)
            this.catalog_items = catalogPointer.getTitleList();
        initCatalogRefreshHandler();
    }

    public void setCurrentItem(int current_item) {
        //this.current_item = current_item;
    }

    public void setListener(CatalogCommandListener listener) {
        this.listener = listener;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewer_catalog_dialog, container, false);
        TextView title = view.findViewById(R.id.catalog_book_title);
        title.setText(chapPointer.getBookName());

        TextView total_size = view.findViewById(R.id.catalog_total_size);
        total_size.setText(String.format(getContext().getString(R.string.novel_catalog_size), catalogPointer.getSize()));

        catalogList = view.findViewById(R.id.catalog_body);
        adapter = new CatalogListAdapter(catalog_items,getContext());
        adapter.setTextSize(18);
        adapter.setBasicItemColor(getContext().getColor(R.color.white));
        catalogList.setAdapter(adapter);
        catalogList.setOnItemClickListener((parent, v, position, id) -> listener.onCatalogItemClick(position));

        Button refresh = view.findViewById(R.id.catalog_refresh);
        refresh.setOnClickListener(v -> {
            NovelUpdateThread updateThread = new NovelUpdateThread(chapPointer.getNovelRequire(),
                    chapPointer);
            updateThread.setHandler(catalog_update_handler);
            updateThread.start();
        });
        System.out.println("DialogFragment-oncreateview");
        return view;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DialogFragment-oncreate");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        System.out.println("DialogFragment-ondialogcreate");
        return super.onCreateDialog(savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getDialog()==null||getContext()==null)return;
        Window window = this.getDialog().getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        int screenHeight = ScreenUtils.getScreenHeight(getContext());
        int matchParent = WindowManager.LayoutParams.MATCH_PARENT;//父布局的宽度
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = matchParent;
        lp.height = (int) (screenHeight *(9.0/10.0));
        window.setAttributes(lp);

        paintDownloadedItems();
        paintCurrentItem();

        System.out.println("DialogFragment-onactivitycreated");
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable @org.jetbrains.annotations.Nullable String tag) {
        System.out.println("DialogFragment-show");
        super.show(manager, tag);
    }

    private void loadCatalog() {
        try {
            catalogPointer = FileIOUtils.readCatalog(
                    StorageUtils.getBookCatalogPath(chapPointer.getBookName(), chapPointer.getWriter()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initCatalogRefreshHandler(){
        catalog_update_handler =new NovelUpdateThread.NovelUpdaterHandler();
        catalog_update_handler.setOverride(new NovelUpdateThread.NovelUpdaterHandler.NovelUpdateListener() {
            @Override
            public void handle(Message msg, int Success, int Fail) {
                if (Success==1){
                    loadCatalog();
                    catalog_items = catalogPointer.getTitleList();
                    ArrayList<Boolean> downloadStateList = catalogPointer.getIsDownload();
                    adapter.updateList(catalog_items,downloadStateList);
                    listener.onRefreshDone(true,"");
                }else{
                    listener.onRefreshDone(false,"章节同步出错");
                }
            }

            @Override
            public void needRecoverAll(Novels novel) {
                listener.onRefreshDone(false,"目录文件损坏");
            }
        });
    }

    private void paintCurrentItem() {
        if(adapter==null)return;
        int currentChapIndex = chapPointer.getCurrentChap();
        adapter.setHighlightTitle(getContext().getColor(R.color.DoderBlue),
                catalog_items.get(currentChapIndex));
        if(catalogList==null) return;
        int firstVisiblePosition = catalogList.getFirstVisiblePosition();
        int lastVisiblePosition = catalogList.getLastVisiblePosition();
        int visible_item_size = lastVisiblePosition - firstVisiblePosition + 1;
        visible_item_size = (visible_item_size==0) ? 14:visible_item_size;//用于处理初次加载时，列表可见长度未知的情况
        if(currentChapIndex < (visible_item_size/2))catalogList.setSelection(0);//如果已经在顶部，无需移动
        else {
            //使当前item移动到列表中央
            catalogList.setSelection(currentChapIndex - visible_item_size/2 + 1);
        }
    }

    private void paintDownloadedItems(){
        if(adapter==null)return;
        adapter.setDownloadStateList(catalogPointer.getIsDownload());
    }

    public NovelCatalog syncCatalog(){
        return this.catalogPointer;
    }

    public interface CatalogCommandListener{
        void onCatalogItemClick(int index);
        void onRefreshDone(boolean success, String info);
    }
}
