package com.Z.NovelReader.views.Dialog;

import static com.Z.NovelReader.Utils.ScreenUtils.dip2px;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Adapters.SwitchSourceAdapter;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.BackupSourceBean;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Service.AlterSourceService;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileOperateUtils;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.views.FlexibleRectDrawable;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SwitchSourceDialog extends Dialog {

    private ListView lv_backupSource;
    private Button btn_confirm;
    private Button btn_cancel;
    private TextView tv_source_brief;
    private ArrayList<BackupSourceBean> currentSourceList;
    private ArrayList<BackupSourceBean> updateSourceList;
    private Map<Integer,NovelRequire> novelSourceMap;
    private Map<Integer,Novels> novelBackupMap;
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
        currentSourceList = new ArrayList<>();
        updateSourceList = new ArrayList<>();
        adapter = new SwitchSourceAdapter(getContext(), currentSourceList);
    }

    public void setListener(SwitchSourceListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.switch_source_dialog);
        btn_cancel = findViewById(R.id.ssd_cancel);
        btn_confirm = findViewById(R.id.ssd_confirm);
        tv_source_brief = findViewById(R.id.novel_source_brief);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setDragRate(0.6f);//显示下拉高度/手指真实下拉高度=阻尼效果
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            updateSourceList.clear();
            getContext().startService(service_intent);
        });
        initAlterSourceService();
        getContext().bindService(service_intent,serviceConnection,getContext().BIND_AUTO_CREATE);

        String source_info=String.format(getContext().getString(R.string.novel_source_info), currentSourceList.size());
        tv_source_brief.setText(source_info);
        FlexibleRectDrawable btn_confirm_background = FlexibleRectDrawable.Builder.create()
                .setSolidFill(getContext().getColor(R.color.DoderBlue))
                .setCorners(dip2px(getContext(),50), FlexibleRectDrawable.CORNER_HALF_LEFT)
                .setRipple(getContext().getColor(R.color.white_smoke),300)
                .build();
        FlexibleRectDrawable btn_cancel_background = FlexibleRectDrawable.Builder.create()
                .setStroke(2,getContext().getColor(R.color.DoderBlue))
                .setCorners(dip2px(getContext(),50), FlexibleRectDrawable.CORNER_HALF_RIGHT)
                .setRipple(getContext().getColor(R.color.white_smoke),300)
                .build();
        btn_cancel.setBackground(btn_cancel_background);
        btn_cancel.setOnClickListener(view -> {
            dismiss();
        });
        btn_confirm.setBackground(btn_confirm_background);
        btn_confirm.setOnClickListener(view -> {
            ArrayList<BackupSourceBean> sources = adapter.getSources();
            if (sources.size() == 0)return;
            for (BackupSourceBean bean: sources) {
                if (bean.isChosen()){
                    if (listener!=null)listener.onSwitchConfirmed(bean);
                    this.dismiss();
                    break;
                }
            }
        });
    }

    public void setCurrentChap(NovelChap currentChap) {
        this.currentChap = currentChap;
    }

    public void setNovelSourceMap(Map<Integer, NovelRequire> novelSourceMap) {
        this.novelSourceMap = novelSourceMap;
    }

    public void setNovelBackupMap(Map<Integer, Novels> novelBackupMap) {
        this.novelBackupMap = novelBackupMap;
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
        lp.height = (int) (screenHeight *(3.0/4.0));
        window.setAttributes(lp);
        setBackupSourceFromFile();
        submitUpdateSourceList();
    }

    private void initAlterSourceService(){
        service_intent = new Intent(getContext(), AlterSourceService.class);
        service_intent.putExtra("Novel",currentChap);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                AlterSourceService.AlterSourceServiceBinder binder = (AlterSourceService.AlterSourceServiceBinder) iBinder;
                AlterSourceService service = binder.getService();
                if (service!=null && service.isAllProcessDone()) {
                    //运行过，并且运行完毕了
                    Map<NovelRequire, NovelCatalog> backupSourceMap = service.getBackupSourceMap();
                    setBackupSourceFromMap(backupSourceMap);
                    getOwnerActivity().runOnUiThread(()->{
                        submitUpdateSourceList();
                        if(refreshLayout.getState()== RefreshState.Refreshing)
                            refreshLayout.finishRefresh();
                    });
                }
                else if(service!=null) {
                    service.setListener(backupSourceMap -> {
                        setBackupSourceFromMap(backupSourceMap);
                        getOwnerActivity().runOnUiThread(()->{
                            submitUpdateSourceList();
                            if(refreshLayout.getState()== RefreshState.Refreshing)
                                refreshLayout.finishRefresh();
                        });
                    });
                    if(!service.isServiceRunning()){
                        //未运行过
                        setBackupSourceFromFile();
                        submitUpdateSourceList();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }

    private void setBackupSourceFromFile() {
        this.updateSourceList.clear();
        List<String> dirs = FileOperateUtils.getAllDirNames(
                StorageUtils.getBackupDir(currentChap.getBookName(), currentChap.getWriter()));
        for (String dir : dirs) {
            try {
                int id = Integer.parseInt(dir);
                NovelRequire novelRequire = novelSourceMap.get(id);
                if (novelRequire == null) continue;
                NovelCatalog novelCatalog = FileIOUtils.readCatalog(
                        StorageUtils.getBackupSourceCatalogPath(currentChap.getBookName(), currentChap.getWriter(),id));
                Bitmap cover = FileIOUtils.readBitmap(
                        StorageUtils.getBackupSourceCoverPath(currentChap.getBookName(), currentChap.getWriter(),id));
                if (novelCatalog.getSize()!=0){
                    Novels backupNovel = novelBackupMap.get(id);
                    BackupSourceBean backupSourceBean = new BackupSourceBean(backupNovel,novelRequire.getId(),novelRequire.getBookSourceName(),novelCatalog,(novelRequire.getId() == currentChap.getSource()));
                    backupSourceBean.setCoverBrief(cover);
                    this.updateSourceList.add(backupSourceBean);
                }
            }catch (NumberFormatException e){
                Log.e("SwitchSourceDialog","backup source file read error:dir name not int");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("SwitchSourceDialog","backup source file read error:catalog load fail");
            }
        }
    }

    private void setBackupSourceFromMap(Map<NovelRequire, NovelCatalog> backupSourceMap) {
        this.updateSourceList.clear();
        Iterator<Map.Entry<NovelRequire, NovelCatalog>> iterator = backupSourceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<NovelRequire, NovelCatalog> entry = iterator.next();
            NovelRequire novelRequire = entry.getKey();
            NovelCatalog novelCatalog = entry.getValue();
            Bitmap cover = FileIOUtils.readBitmap(StorageUtils.getBackupSourceCoverPath(currentChap.getBookName(), currentChap.getWriter(),novelRequire.getId()));
            Novels backupNovel = novelBackupMap.get(novelRequire.getId());
            BackupSourceBean backupSourceBean = new BackupSourceBean(backupNovel, novelRequire.getId(), novelRequire.getBookSourceName(), novelCatalog, (novelRequire.getId() == currentChap.getSource()));
            backupSourceBean.setCoverBrief(cover);
            this.updateSourceList.add(backupSourceBean);
        }
    }

    private void submitUpdateSourceList(){
        ArrayList<BackupSourceBean> diff = new ArrayList<>();
        for (BackupSourceBean backup_source : updateSourceList) {
            boolean match = currentSourceList.stream().anyMatch(current -> current.getSourceID() == backup_source.getSourceID());
            if(!match)diff.add(backup_source);
        }
        if(!diff.isEmpty()){
            if(!this.currentSourceList.isEmpty()){
                String info = String.format(Locale.CHINA,"新增%d个书源",this.updateSourceList.size());
                Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            }
            this.currentSourceList.addAll(this.updateSourceList);
            adapter.updateSources(currentSourceList);
        }
        String source_info=String.format(getContext().getString(R.string.novel_source_info), currentSourceList.size());
        tv_source_brief.setText(source_info);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("SwitchSourceDialog","on stop");
    }
}
