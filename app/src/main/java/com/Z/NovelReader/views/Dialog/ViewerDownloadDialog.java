package com.Z.NovelReader.views.Dialog;

import static com.Z.NovelReader.Utils.ScreenUtils.dip2px;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Adapters.DownloadSelectAdapter;
import com.Z.NovelReader.Global.DragSelectTouchListener;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Service.ChapDownloadService;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.views.FlexibleRectDrawable;

import java.util.ArrayList;
import java.util.List;

public class ViewerDownloadDialog extends DialogFragment implements ChapDownloadService.ChapDownloadListener {

    private DragSelectTouchListener touchListener;
    private DownloadSelectAdapter adapter;
    private NovelCatalog catalogPointer;
    private NovelChap chapPointer;

    private TextView btn_cancel_select;
    private Button btn_confirm_download;

    private Intent service_intent;
    private ServiceConnection serviceConnection;
    private ViewerDownloadDialog context;

    public ViewerDownloadDialog(NovelCatalog catalog_pointer, NovelChap chap_pointer) {
        super();
        setStyle(STYLE_NO_TITLE,R.style.NoDimDialog);
        this.catalogPointer = catalog_pointer;
        this.chapPointer = chap_pointer;
        this.context = this;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_selector_dialog, container, false);
        initDownloadService();

        RecyclerView selector_view = view.findViewById(R.id.drag_select_view);
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        selector_view.setLayoutManager(layoutManager);
        adapter = new DownloadSelectAdapter(getContext());
        for (int i = 0; i < catalogPointer.getSize(); i++) {
            NovelCatalog.CatalogItem item = catalogPointer.get(i);
            item.isSelected = false;
            adapter.addItem(item);
        }
        NovelCatalog.CatalogItem item = catalogPointer.get(chapPointer.getCurrentChap());
        adapter.setCurrentItem(item);
        selector_view.setAdapter(adapter);

        int index = chapPointer.getCurrentChap();
        int rows = index/4;
        int locate_row;
        if(rows<3)locate_row = rows;
        else locate_row = rows-3;
        int locate_index = locate_row*4;

        selector_view.scrollToPosition(locate_index);

        btn_cancel_select = view.findViewById(R.id.ds_cancel_select);
        btn_cancel_select.setOnClickListener(v -> {
            adapter.unSelectAll();
            btn_confirm_download.setText(String.format(getContext().getString(R.string.novel_chap_download),0));
        });

        FlexibleRectDrawable btn_confirm_background =
                FlexibleRectDrawable.Builder.create()
                .setSolidFill(getContext().getColor(R.color.DoderBlue))
                .setCorners(dip2px(getContext(),50), FlexibleRectDrawable.CORNER_ALL)
                .setRipple(getContext().getColor(R.color.white_smoke),300)
                .build();
        btn_confirm_download = view.findViewById(R.id.ds_confirm_download);
        btn_confirm_download.setText(String.format(getContext().getString(R.string.novel_chap_download),0));
        btn_confirm_download.setBackground(btn_confirm_background);
        btn_confirm_download.setOnClickListener(v -> {
            List<String> links = adapter.getAllSelectedLinks();
            startDownloadService((ArrayList<String>) links);
            adapter.setSelectedItemToDownloadMode();
            btn_confirm_download.setText(String.format(getContext().getString(R.string.novel_chap_download),0));
        });

        adapter.setClickListener(v -> {
            int position = selector_view.getChildAdapterPosition(v);
            adapter.reverseSelection(position);
            if(adapter.isSelected(position))
                touchListener.setStartSelectPosition(position);
            btn_confirm_download.setText(String.format(getContext().getString(R.string.novel_chap_download),adapter.getAllSelectedItem().size()));
        });

        adapter.setLongClickListener(v -> {
            int position = selector_view.getChildAdapterPosition(v);
            adapter.setSelected(position, true);
            touchListener.setStartSelectPosition(position);
            btn_confirm_download.setText(String.format(getContext().getString(R.string.novel_chap_download),adapter.getAllSelectedItem().size()));
            return false;
        });

        touchListener = new DragSelectTouchListener();

        //监听滑动选择
        selector_view.addOnItemTouchListener(touchListener);

        touchListener.setSelectListener((start, end, isSelected) -> {
            //选择的范围回调
            adapter.selectRangeChange(start, end, isSelected);
            btn_confirm_download.setText(String.format(getContext().getString(R.string.novel_chap_download),adapter.getAllSelectedItem().size()));
        });
        Log.d("DownloadDialog","create");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getDialog()==null||getContext()==null)return;
        Window window = getDialog().getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        int screenHeight = ScreenUtils.getScreenHeight(getContext());
        int matchParent = WindowManager.LayoutParams.MATCH_PARENT;//父布局的宽度
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = matchParent;
        lp.height = (int) (screenHeight *(4.0/5.0));
        window.setAttributes(lp);
    }

    private void initDownloadService(){
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder mBinder) {
                ChapDownloadService.ChapDownloadServiceBinder binder = (ChapDownloadService.ChapDownloadServiceBinder) mBinder;
                ChapDownloadService service = binder.getService();
                //更新目录显示
                service.setDownloadListener(context);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        service_intent = new Intent(getContext(), ChapDownloadService.class);
        getContext().bindService(service_intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startDownloadService(ArrayList<String> links){
        service_intent = new Intent(getContext(), ChapDownloadService.class);
        service_intent.putExtra("DownloadLinks",links);
        service_intent.putExtra("NovelChap", chapPointer);
        service_intent.putExtra("Catalog", catalogPointer);
        getContext().startService(service_intent);
    }

    @Override
    public void onChapDownloaded(int index) {
        if(adapter!=null)adapter.setDownloaded(index,true);
        this.catalogPointer.setDownloaded(index,true);
    }
}
