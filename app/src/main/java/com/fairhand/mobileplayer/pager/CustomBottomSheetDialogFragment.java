package com.fairhand.mobileplayer.pager;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fairhand.mobileplayer.R;

import java.util.Objects;

/**
 * BottomSheet模态底部表
 */
public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment {
    
    private ArrayAdapter<String> adapter;
    
    String[] strings = { "张三", "李思思", "王五五", "刘顺" };
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getContext() == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        return new BottomSheetDialog(getContext(), R.style.TransparentBottomSheetStyle);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.bottom_dialog_layout, container, false);
        ListView listview = view.findViewById(R.id.listview);
        
        adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_list_item_1, strings);
        listview.setAdapter(adapter);
        
        return view;
    }
    
}
