package com.Z.NovelReader.views.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.Z.NovelReader.Adapters.SwitchSourceAdapter;
import com.Z.NovelReader.Objects.beans.BackupSourceBean;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.ScreenUtils;

import java.util.ArrayList;

public class SwitchSourceDialog extends Dialog {

    private ListView lv_backupSource;
    private Button btn_confirm;
    private Button btn_cancel;
    private ArrayList<BackupSourceBean> backupSourceList;
    private SwitchSourceAdapter adapter;
    private SwitchSourceListener listener;

    public interface SwitchSourceListener{
        void onSwitchConfirmed(BackupSourceBean backupSource);
    }

    public SwitchSourceDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        backupSourceList = new ArrayList<>();
        adapter = new SwitchSourceAdapter(getContext(),backupSourceList);
    }

    public void setListener(SwitchSourceListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_source_dialog);
        //lv_backupSource = findViewById(R.id.source_backup_list);
        btn_cancel = findViewById(R.id.ssd_cancel);
        btn_confirm = findViewById(R.id.ssd_confirm);

        btn_cancel.setOnClickListener(view -> {
            dismiss();
        });
        btn_confirm.setOnClickListener(view -> {
            ArrayList<BackupSourceBean> sources = adapter.getSources();
            if (sources.size() == 0)return;
            for (BackupSourceBean bean: sources) {
                if (bean.isChosen()){
                    if (listener!=null)listener.onSwitchConfirmed(bean);
                }
            }
        });
    }

    public void setBackupSourceList(ArrayList<BackupSourceBean> backupSourceList) {
        this.backupSourceList = backupSourceList;
        adapter.updateSources(backupSourceList);
    }

    @Override
    public void show() {
        super.show();
        lv_backupSource = findViewById(R.id.source_backup_list);
        lv_backupSource.setAdapter(adapter);
        lv_backupSource.setEmptyView(findViewById(R.id.empty));
        Window window = this.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        int screenHeight = ScreenUtils.getScreenHeight(getContext());
        int matchParent = WindowManager.LayoutParams.MATCH_PARENT;//父布局的宽度
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = matchParent;
        lp.height = screenHeight/2 + 200;
        window.setAttributes(lp);
    }

}
