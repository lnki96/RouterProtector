package com.demo.lnki96.routerprotector;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lnki9 on 2016/9/17 0017.
 */

public class NetSpeedMonitor extends TrafficStats {
    private Handler mhandler;
    private Message msg;
    private long uploadNow,uploadBefore,downloadNow,downloadBefore,timeNow,timeBefore;
    private float upload,download;
    private List<String> netSpeed=new ArrayList<>();
    public final static int UPDATE_NET_SPEED=0;

    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
            uploadNow=getTotalTxBytes()-getMobileTxBytes();
            downloadNow=getTotalRxBytes()-getMobileRxBytes();
            timeNow=System.currentTimeMillis();
            upload=((float) uploadNow-uploadBefore)/(timeNow-timeBefore)/1024*1000;
            download=((float) downloadNow-downloadBefore)/(timeNow-timeBefore)/1024*1000;
            uploadBefore=uploadNow;
            downloadBefore=downloadNow;
            timeBefore=timeNow;
            netSpeed=new ArrayList<>();
            netSpeed.add(String.format(Locale.CHINA,"%.3f kB/s",upload));
            netSpeed.add(String.format(Locale.CHINA,"%.3f kB/s",download));
            msg=mhandler.obtainMessage();
            msg.what=UPDATE_NET_SPEED;
            msg.obj=netSpeed;
            mhandler.sendMessage(msg);
        }
    };

    public NetSpeedMonitor(Handler mhandler){
        this.mhandler=mhandler;
    }

    public void start(){
        uploadBefore=getTotalTxBytes()-getMobileTxBytes();
        downloadBefore=getTotalRxBytes()-getMobileRxBytes();
        timeBefore=System.currentTimeMillis();
        new Timer().schedule(timerTask,1000,1000);
    }
}
