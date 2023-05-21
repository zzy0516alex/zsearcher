package com.Z.NovelReader.views.Dialog;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Adapters.SwitchSourceAdapter;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.BackupSourceBean;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Service.AlterSourceService;
import com.Z.NovelReader.Utils.CollectionUtils;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.ServiceUtils;
import com.Z.NovelReader.Utils.StorageUtils;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SwitchSourceDialog extends Dialog {

    private ListView lv_backupSource;
    private Button btn_confirm;
    private Button btn_cancel;
    private TextView tv_source_brief;
    private ArrayList<BackupSourceBean> backupSourceList;
    private ArrayList<BackupSourceBean> refreshSourceList;
    private SwitchSourceAdapter adapter;
    private SwitchSourceListener listener;
    private NovelChap currentChap;
    private RefreshLayout refreshLayout;
    private Intent service_intent;
    private ServiceConnection serviceConnection;

    public interface SwitchSourceListener{
        void onSwitchConfirmed(BackupSourceBean backupSource);
    }

    public SwitchSourceDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        backupSourceList = new ArrayList<>();
        refreshSourceList = new ArrayList<>();
        adapter = new SwitchSourceAdapter(getContext(),backupSourceList);
    }

    public void setListener(SwitchSourceListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_source_dialog);
        btn_cancel = findViewById(R.id.ssd_cancel);
        btn_confirm = findViewById(R.id.ssd_confirm);
        tv_source_brief = findViewById(R.id.novel_source_brief);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setDragRate(0.6f);//显示下拉高度/手指真实下拉高度=阻尼效果
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            boolean serviceRunning = ServiceUtils.isServiceRunning(getContext(), AlterSourceService.class.getName());
            if (serviceRunning) {
                //刷新失败
                refreshlayout.finishRefresh(false);
                Toast.makeText(getContext(), "换源服务正在运行中，请稍后再试", Toast.LENGTH_SHORT).show();
                return;
            }
            refreshSourceList.clear();
            getContext().bindService(service_intent,serviceConnection,getContext().BIND_AUTO_CREATE);
            getContext().startService(service_intent);
        });
        initAlterSourceService();
        String source_info=String.format(getContext().getString(R.string.novel_source_info),backupSourceList.size());
        tv_source_brief.setText(source_info);

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

    public void setCurrentChap(NovelChap currentChap) {
        this.currentChap = currentChap;
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
        lp.height = (int) (screenHeight *(5.0/6.0));
        window.setAttributes(lp);
    }

    private void initAlterSourceService(){
        service_intent = new Intent(getContext(), AlterSourceService.class);
        service_intent.putExtra("Novel",currentChap);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                AlterSourceService.AlterSourceServiceBinder binder = (AlterSourceService.AlterSourceServiceBinder) iBinder;
                AlterSourceService service = binder.getService();
                if (service.isAllProcessDone()) {
                    Map<NovelRequire, NovelCatalog> backupSourceMap = service.getBackupSourceMap();
                    setBackupSourceFromMap(backupSourceMap);
                    getOwnerActivity().runOnUiThread(()->{
                        adapter.updateSources(backupSourceList);
                        refreshLayout.finishRefresh();
                    });
                } else {
                    service.setListener(backupSourceMap -> {
                        setBackupSourceFromMap(backupSourceMap);
                        getOwnerActivity().runOnUiThread(()->{
                            adapter.updateSources(backupSourceList);
                            refreshLayout.finishRefresh();
                        });
                    });
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }

    private void setBackupSourceFromMap(Map<NovelRequire, NovelCatalog> backupSourceMap) {
        Iterator<Map.Entry<NovelRequire, NovelCatalog>> iterator = backupSourceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<NovelRequire, NovelCatalog> entry = iterator.next();
            NovelRequire novelRequire = entry.getKey();
            NovelCatalog novelCatalog = entry.getValue();
            Bitmap cover = FileIOUtils.readBitmap(StorageUtils.getBackupSourceCoverPath(currentChap.getBookName(), currentChap.getWriter(),novelRequire.getId()));
            BackupSourceBean backupSourceBean = new BackupSourceBean(currentChap, novelRequire.getId(), novelRequire.getBookSourceName(), novelCatalog, (novelRequire.getId() == currentChap.getSource()));
            backupSourceBean.setCoverBrief(cover);
            this.refreshSourceList.add(backupSourceBean);
        }
        //CollectionUtils<BackupSourceBean> collectionUtil = new CollectionUtils<>();
        List<BackupSourceBean> novelSearchBeans = CollectionUtils.diffListByStringKey(backupSourceList, refreshSourceList);
        if(!novelSearchBeans.isEmpty()){
            this.backupSourceList.addAll(novelSearchBeans);
            String info = String.format(Locale.CHINA,"新增%d个书源",novelSearchBeans.size());
            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
        }else Toast.makeText(getContext(), "没有新增书源", Toast.LENGTH_SHORT).show();
    }

}
