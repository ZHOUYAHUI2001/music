package com.bistu.cs.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class BaseActivity extends AppCompatActivity implements Callback.CommonCallback<String>{

    public void loadData(String url){
        System.out.println(url);
        RequestParams params = new RequestParams(url);
        params.setConnectTimeout(10000);
        x.http().get(params,this);
    }
    @Override
    public void onSuccess(String result) {

    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {

    }

    @Override
    public void onCancelled(org.xutils.common.Callback.CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }
}

