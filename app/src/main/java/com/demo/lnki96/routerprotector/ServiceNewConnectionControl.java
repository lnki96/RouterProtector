package com.demo.lnki96.routerprotector;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eric on 16/9/16.
 */
public class ServiceNewConnectionControl extends Service {

    public Callback callback=null;
    public boolean isRun=true;

    public boolean isTheFirstTime=true;

    public Object lock=new Object();

    public String state="0";
    public String newMac="there is no new mac.";
    public String checking="the connection detection is up";
    public ArrayList macsWeGetNew=null;
    public ArrayList macsWeGetOld=null;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    public Callback getCallback() {
        return callback;
    }

    public static interface Callback{
        void onDataChange(boolean isFound,String state,String checking,String NewMac);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isRun=false;
        return super.onUnbind(intent);
    }

    public class Binder extends android.os.Binder{
        public ServiceNewConnectionControl getServiceNewConnectionControl(){return ServiceNewConnectionControl.this;}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(){
            @Override
            public void run() {
                super.run();
                int i=0;
                while (isRun){
                    for(;i<3;i++){
                        try {
                            //休眠5s并输出一个个点点~
                            if(!isRun){
                                break;
                            }
                            sleep(1000);

                            System.out.println(i);//测试服务是否被销毁

                            checking+='.';
                            if(callback!=null){
                                callback.onDataChange(false,state,checking,newMac);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                    //获取当前在线的主机mac信息
                    circuleGetCurrentMacInfo("http://192.168.1.1/userRpm/WlanStationRpm.htm?Page=1","http://192.168.1.1/userRpm/WlanStationRpm.htm?Page=1");

                    //这里需要添加一个进程同步
                    synchronized (lock){
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        state= String.valueOf(macsWeGetNew.size());
                        //先同步下这里
                        if(isTheFirstTime){macsWeGetOld=macsWeGetNew;isTheFirstTime=false;}
                    if(macsWeGetOld.size()<macsWeGetNew.size()){
                        //当检测到有新的设备连接时
                        for(int j=0;j<macsWeGetNew.size();j++){
                            if(!macsWeGetOld.contains(macsWeGetNew.get(j))){
                                String theNewMacWeFinallyFound=macsWeGetNew.get(j).toString();
                                if(callback!=null){
                                    callback.onDataChange(true,state,checking,theNewMacWeFinallyFound);
                                }
                            }
                        }
                    }
                    }
                    checking="the connection detection is up";
                    i=0;
                    macsWeGetOld=macsWeGetNew;

                }
                callback.onDataChange(false,"","the service is stopped.","");

            }

        }.start();
    }
    public void circuleGetCurrentMacInfo(String urlAddress, String referer) {
        //用ArrayList会比较方便
        new AsyncTask<String,ArrayList,ArrayList>(){

            @Override
            protected ArrayList doInBackground(String... strings) {

                ArrayList resultArrayList=new ArrayList();
                URL url = null;
                HttpURLConnection connection = null;
                try {
                    url = new URL(strings[0]);
                    connection = null;
                    connection = (HttpURLConnection) url.openConnection();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.setRequestProperty("Cookie", "Authorization=Basic%20YWRtaW46YWRtaW4xMjM%3D; ChgPwdSubTag=");
                connection.setRequestProperty("Referer", strings[1]);
                InputStream is = null;
                try {
                    is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "gb2312");
                    BufferedReader br = new BufferedReader(isr);
                    String line = null;
                    String s="\n";
                    Matcher matMac=null;
                    while ((line = br.readLine()) != null) {
                        s += line + "\n";}

                        //使用正则表达式从结果字符串中抓取得我们想要的信息
                        String regExMac = "([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})"; //匹配Mac地址
                        Pattern patMac = Pattern.compile(regExMac);
                        matMac = patMac.matcher(s);
                        int i=0;

                    //下面这里一定要用while,用if的话就只能匹配一个
                    while(matMac.find()){
                        System.out.println("we find the mac:"
                                    +matMac.group()+"--7778\n");
                        resultArrayList.add(matMac.group());
                        }
                    publishProgress(resultArrayList);


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return resultArrayList;
            }

            @Override
            protected void onProgressUpdate(ArrayList... values) {
                super.onProgressUpdate(values);
                synchronized (lock){
                    macsWeGetNew = values[0];
                    lock.notify();
                }
            }
        }.execute(urlAddress,referer);
    }
}
