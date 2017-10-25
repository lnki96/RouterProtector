package com.demo.lnki96.routerprotector;

import android.app.Application;
import android.content.Context;

/**
 * Created by lnki9 on 2016/9/19 0019.
 */

public class RouterProtectorApplication extends Application {
    private static Context context;

    public static Context getContext(){
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
    }
}
