package com.example.helloworld;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.helloworld.Adapters.ResultBoxAdapter;
import com.example.helloworld.Dialog.MagnetDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class ResultFragment extends Fragment {
    private ListView resultList;
    private ArrayList<String> title_list;
    private ArrayList<String> type_list;
    private ArrayList<String> size_list;
    private ArrayList<String> magnet_list;
    private HashMap<String,ArrayList<String>> resultBox;
    ResultBoxAdapter adapter;
    Context context;

    public void setResultBox(HashMap<String, ArrayList<String>> resultBox) {
        this.resultBox = resultBox;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView=inflater.inflate(R.layout.fragment_result,container,false);
        context=getContext();
        resultList=myView.findViewById(R.id.resultList);
        title_list = resultBox.get("TitleList");
        type_list = resultBox.get("TypeList");
        size_list = resultBox.get("SizeList");
        magnet_list = resultBox.get("MagnetList");
        adapter=new ResultBoxAdapter(title_list,size_list,type_list,context);
        resultList.setAdapter(adapter);
        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MagnetDialog dialog=new MagnetDialog(context);
                dialog.setMyTitle(title_list.get(position)).setMyMagnet(magnet_list.get(position))
                        .setCopyListener(new MagnetDialog.OnCopyListener() {
                            @Override
                            public void OnCopy(MagnetDialog dialog) {
                                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .setOpenListener(new MagnetDialog.OnOpenListener() {
                            @Override
                            public void OnOpen(MagnetDialog dialog) {

                            }
                        }).show();
            }
        });
        return myView;
    }
}
