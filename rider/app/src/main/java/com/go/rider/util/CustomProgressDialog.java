package com.go.rider.util;

import android.app.ProgressDialog;
import android.content.Context;
import com.go.rider.R;

public class CustomProgressDialog {

    private Context         mContext;
    private ProgressDialog  progressDialog;

    public CustomProgressDialog(Context mContext) {
        this.mContext = mContext;
    }

    public void show(int message, boolean isCancelable){
        progressDialog = new ProgressDialog(mContext, R.style.ProgressDialog);
        progressDialog.setMessage(mContext.getResources().getString(message));
        progressDialog.setCancelable(isCancelable);
        progressDialog.show();
    }

    public void show(int title, int message, boolean isCancelable){
        progressDialog = new ProgressDialog(mContext, R.style.ProgressDialog);
        progressDialog.setTitle(mContext.getResources().getString(title));
        progressDialog.setMessage(mContext.getResources().getString(message));
        progressDialog.setCancelable(isCancelable);
        progressDialog.show();
    }

    public void cancel(){
        progressDialog.dismiss();
    }
}
