package com.go.driver.util;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import android.widget.SimpleAdapter;

import com.go.driver.R;

public class CustomPopupWindow {

    private Context             mContext;
    private View                anchorView;
    private SimpleAdapter       mAdapter;
    private OnItemClickListener mOnItemClickListener;

    public CustomPopupWindow(Context mContext, View anchorView, SimpleAdapter mAdapter) {
        this.mContext = mContext;
        this.anchorView = anchorView;
        this.mAdapter = mAdapter;
        show();
    }

    public void show(){
        final ListPopupWindow popupWindow = new ListPopupWindow(mContext);
        popupWindow.setAdapter(mAdapter);
        popupWindow.setAnchorView(anchorView);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            if(mOnItemClickListener != null) mOnItemClickListener.onItemClick(popupWindow, parent, view, position, id);
        });
        popupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_popup));
        popupWindow.setModal(true);
        popupWindow.show();
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ListPopupWindow popupWindow, AdapterView<?> parent, View view, int position, long id);
    }
}
