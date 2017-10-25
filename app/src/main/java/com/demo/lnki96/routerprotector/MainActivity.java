package com.demo.lnki96.routerprotector;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lnki9 on 2016/9/5 0005.
 */

public class MainActivity extends FragmentActivity {
    public final static int UPDATE_NET_SPEED = 0,
            SNACKBAR = 1,
            SECURE_SAFETY = 2,
            CONNECT_DETECTED=3,
            UNBIND_ARP_DETECTION=4;

    private final int TAB_MAIN = 0;
    private final int TAB_SECURE = 1;
    private final int TAB_AUTH = 2;
    private static boolean isSafe = true;
    private TextView upload;
    private TextView download;
    private View root;
    private ViewPager pager;
    private TabMain tabMain = new TabMain();
    private TabSecure tabSecure = new TabSecure();
    private TabAuth tabAuth = new TabAuth();
    private int tabPos, tabPosPre;
    View.OnClickListener bottomBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.left_btn:
                    switch (tabPos) {
                        case TAB_MAIN:
                            break;
                        case TAB_SECURE:
                            break;
                        case TAB_AUTH:
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.right_btn:
                    switch (tabPos) {
                        case TAB_MAIN:
                            break;
                        case TAB_SECURE:
                            break;
                        case TAB_AUTH:
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private ServiceNewConnectionControl.Binder newConnectionControlBinder;
    private ServiceArpDetection.Binder arpDetectionBinder;
    private ImageButton leftBtn, rightBtn;
    private FloatingActionButton fabMenuBtn;
    private PopupWindow popupWindow;
    private ObjectAnimator fabPopShow, fabPopHide;
    private AnimatorSet popupHide;
    View.OnClickListener fabMenuBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (popupWindow == null || !popupWindow.isShowing()) {
                AnimatorSet popupShow;
                switch (tabPos) {
                    case TAB_MAIN:
                        popupWindow = tabMain.getPopupWindow();
                        popupShow = tabMain.getPopupShow();
                        popupHide = tabMain.getPopupHide();
                        break;
                    case TAB_SECURE:
                        popupWindow = tabSecure.getPopupWindow();
                        popupShow = tabSecure.getPopupShow();
                        popupHide = tabSecure.getPopupHide();
                        break;
                    case TAB_AUTH:
                        popupWindow = tabAuth.getPopupWindow();
                        popupShow = tabAuth.getPopupShow();
                        popupHide = tabAuth.getPopupHide();
                        break;
                    default:
                        return;
                }
                fabPopShow.setFloatValues(fabMenuBtn.getTranslationY(), fabMenuBtn.getTranslationY() - popupWindow.getContentView().getMeasuredHeight());
                fabPopShow.start();
                popupShow.start();
            }
        }
    };
    private NewConnectionControl newConnectionControl = new NewConnectionControl();
    private ArpDetection arpDetection = new ArpDetection();
    private NotificationManager notificationManager;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            Message msgMain = tabMain.getHandler().obtainMessage(),
                    msgSecure = tabSecure.getHandler().obtainMessage(),
                    msgAuth = tabAuth.getHandler().obtainMessage();
            switch (msg.what) {
                case UPDATE_NET_SPEED:
                    List<String> netSpeed = (ArrayList) msg.obj;
                    if (netSpeed!=null) {
                        upload.setText(netSpeed.get(0));
                        download.setText(netSpeed.get(1));
                    }
                    break;
                case SNACKBAR:
                    Snackbar.make(root, msg.obj.toString(), Snackbar.LENGTH_LONG);
                    break;
                case SECURE_SAFETY:
                    msg = mHandler.obtainMessage();
                    msg.what = SECURE_SAFETY;
                    if (bundle.getBoolean("isThereAAttack")&&!bundle.getString("data").equals("not find")) {
                        if (isSafe) {
                            addNotification("ARP spoofing! The MAC is " + bundle.getString("data"));
                            isSafe = false;
                            msgMain.what=TabMain.SECURE_SAFETY;
                            msgMain.setData(bundle);
                            tabMain.getHandler().sendMessage(msgMain);
                            msgSecure.what=TabSecure.SECURE_SAFETY;
                            msgSecure.setData(bundle);
                            tabSecure.getHandler().sendMessage(msgSecure);
                            msgAuth.what=TabAuth.SECURE_SAFETY;
                            msgAuth.setData(bundle);
                            tabAuth.getHandler().sendMessage(msgAuth);
                        }
                    } else if (!isSafe) {
                        isSafe = true;
                        msgMain.what=TabMain.SECURE_SAFETY;
                        msgMain.setData(bundle);
                        tabMain.getHandler().sendMessage(msgMain);
                        msgSecure.what=TabSecure.SECURE_SAFETY;
                        msgSecure.setData(bundle);
                        tabSecure.getHandler().sendMessage(msgSecure);
                        msgAuth.what=TabAuth.SECURE_SAFETY;
                        msgAuth.setData(bundle);
                        tabAuth.getHandler().sendMessage(msgAuth);
                    }
                    break;
                case CONNECT_DETECTED:
                    if(msg.getData().getBoolean("isFound")){
                        //发现新的Mac连入,这里应该谈个对话框,提示有新的MAC地址连入了
                        View layout = LayoutInflater.from(MainActivity.this).inflate(
                                R.layout.dialoag_show_new_mac, null);
                        final TextView newMacText = (TextView) layout
                                .findViewById(R.id.textviewOfDialoageShowNewMac);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setTitle("发现新设备接入").setView(layout);
                        newMacText.setText(msg.getData().getString("newMac"));
                        final String newMac=new String(msg.getData().getString("newMac"));
                        System.out.println(newMac);
                        builder.setPositiveButton("允许接入", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //允许接入的代码
                            }
                        }).setNegativeButton("拒绝接入", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //拒绝接入的代码
                                //将目标mac添加到过滤列表中
                                //readNet("http://192.168.1.1/userRpm/WlanMacFilterRpm.htm?Mac="+msg.getData().getString("newMac")+"&Desc=&entryEnabled=1&Changed=0&SelIndex=0&Page=1&Save=%B1%A3+%B4%E67","3");
                                TabMain.readNet("http://192.168.1.1/userRpm/WlanMacFilterRpm.htm?Mac="+newMac+"&Desc=&entryEnabled=1&Changed=0&SelIndex=0&Page=1&Save=%B1%A3+%B4%E6","3");
                                System.out.println("finally reject succeed!!");
                            }
                        }).show();
                    }
                    break;
                case UNBIND_ARP_DETECTION:
                    unbindService(arpDetection);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private AnimatorSet popupHideSet;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.main);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.settings);
        upload = (TextView) findViewById(R.id.upload);
        download = (TextView) findViewById(R.id.download);
        new NetSpeedMonitor(mHandler).start();

        final TabLayout tab = (TabLayout) findViewById(R.id.tab);
        pager = (ViewPager) findViewById(R.id.pager);
        final List<Fragment> tabList = new ArrayList<>();
        tabList.add(tabMain);
        tabList.add(tabSecure);
        tabList.add(tabAuth);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return tabList.get(position);
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }
        };
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPos = tab.getPosition();
                showBottomButtons();
                pager.setCurrentItem(tabPos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabMain.setWifiIpAddr(getWifiIpAddr());
        tabAuth.setMacAddr(getMacAddr());

        fabMenuBtn = (FloatingActionButton) findViewById(R.id.float_btn);
        fabMenuBtn.setOnClickListener(fabMenuBtnListener);
        leftBtn = (ImageButton) findViewById(R.id.left_btn);
        rightBtn = (ImageButton) findViewById(R.id.right_btn);
        leftBtn.setOnClickListener(bottomBtnListener);
        rightBtn.setOnClickListener(bottomBtnListener);

        fabPopShow = new ObjectAnimator().setDuration(400);
        fabPopShow.setTarget(fabMenuBtn);
        fabPopShow.setPropertyName("translationY");
        fabPopShow.setInterpolator(new OvershootInterpolator());
        fabPopShow.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fabPopHide.setFloatValues(fabMenuBtn.getTranslationY(), fabMenuBtn.getTranslationY() + popupWindow.getContentView().getMeasuredHeight());
                switch (tabPos) {
                    case TAB_MAIN:
                        tabMain.setPopHideSet(popupHideSet);
                        break;
                    case TAB_SECURE:
                        tabSecure.setPopHideSet(popupHideSet);
                        break;
                    case TAB_AUTH:
                        tabAuth.setPopHideSet(popupHideSet);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        fabPopHide = new ObjectAnimator().setDuration(400);
        fabPopHide.setTarget(fabMenuBtn);
        fabPopHide.setPropertyName("translationY");
        fabPopHide.setInterpolator(new AnticipateOvershootInterpolator());
        popupHideSet = new AnimatorSet();
        popupHideSet.playTogether(fabPopHide);

        int serviceRunning = 1;
        Intent intent = new Intent();
        ActivityManager activityManager = (ActivityManager) RouterProtectorApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfoList = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfoList) {
            if (serviceInfo.service.getClassName() == "ServiceNewConnectionControl") {
                serviceRunning *= 2;
                bindService(intent.setClass(this, ServiceNewConnectionControl.class), newConnectionControl, 0);
            } else if (serviceInfo.service.getClassName() == "ServiceArpDetection") {
                serviceRunning *= 3;
                bindService(intent.setClass(this, ServiceArpDetection.class), arpDetection, 0);
            }
            if (serviceRunning == 6) break;
        }
        if (serviceRunning % 2 != 0)
            bindService(new Intent(this, ServiceNewConnectionControl.class), newConnectionControl, Context.BIND_AUTO_CREATE);
        if (serviceRunning % 3 != 0)
            bindService(new Intent(this, ServiceArpDetection.class), arpDetection, Context.BIND_AUTO_CREATE);
    }

    public int addNotification(String content) {
        int id = content.hashCode();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
        Notification notify = new NotificationCompat.Builder(this)
                .setTicker(content)
                .setSmallIcon(R.drawable.ic_note)
                .setContentTitle(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi)
                .setColor(Color.BLACK)
                .build();
        notificationManager.notify(id, notify);
        return id;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public static boolean getIsSafe(){
        return isSafe;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (popupWindow != null && popupWindow.isShowing()) {
            onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && popupWindow.isShowing()) {
            fabPopHide.setFloatValues(fabMenuBtn.getTranslationY(), fabMenuBtn.getTranslationY() + popupWindow.getContentView().getMeasuredHeight());
            fabPopHide.start();
            popupHide.start();
        }
        return super.onTouchEvent(event);
    }

    private void showBottomButtons() {
        if (tabPos == tabPosPre) return;
        ObjectAnimator leftAnimator = new ObjectAnimator().setDuration(500);
        ObjectAnimator rightAnimator = new ObjectAnimator().setDuration(500);
        leftAnimator.setTarget(leftBtn);
        rightAnimator.setTarget(rightBtn);
        leftAnimator.setPropertyName("alpha");
        rightAnimator.setPropertyName("alpha");
        switch (tabPos) {
            case TAB_MAIN:
                leftBtn.setClickable(true);
                rightBtn.setClickable(true);
                leftAnimator.setFloatValues(leftBtn.getAlpha(), 0f, 1f);
                rightAnimator.setFloatValues(rightBtn.getAlpha(), 0f, 1f);
                break;
            case TAB_SECURE:
                leftBtn.setClickable(false);
                rightBtn.setClickable(true);
                leftAnimator.setFloatValues(leftBtn.getAlpha(), 0f, 0f);
                rightAnimator.setFloatValues(rightBtn.getAlpha(), 0f, 1f);
                break;
            case TAB_AUTH:
                leftBtn.setClickable(false);
                rightBtn.setClickable(false);
                leftAnimator.setFloatValues(leftBtn.getAlpha(), 0f, 0f);
                rightAnimator.setFloatValues(rightBtn.getAlpha(), 0f, 0f);
                break;
            default:
                break;
        }
        leftAnimator.start();
        rightAnimator.start();
        tabPosPre = tabPos;
    }

    private String getWifiIpAddr() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            int ipAddr = wifi.getConnectionInfo().getIpAddress();
            return (ipAddr & 0xFF) + "." + ((ipAddr >> 8) & 0xFF) + "." + ((ipAddr >> 16) & 0xFF) + "." + (ipAddr >> 24 & 0xFF);
        }
        return "0.0.0.0";
    }

    private String getMacAddr() {
        String macAddr = "00.00.00.00.00.00", str;
        try {
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ").getInputStream()));
            str = reader.readLine();
            if (str != null) macAddr = str.trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macAddr;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private class NewConnectionControl implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (newConnectionControlBinder == null) {
                newConnectionControlBinder = (ServiceNewConnectionControl.Binder) iBinder;
                newConnectionControlBinder.getServiceNewConnectionControl().setCallback(new ServiceNewConnectionControl.Callback() {
                    @Override
                    public void onDataChange(boolean isFound, String state, String checking, String newMac) {
                        Bundle b = new Bundle();
                        b.putBoolean("isFound", isFound);
                        b.putString("state", state);//
                        b.putString("newMac", newMac);
                        b.putString("checking", checking);
                        Message msg = mHandler.obtainMessage();
                        msg.what = CONNECT_DETECTED;
                        msg.setData(b);
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            newConnectionControlBinder = null;
            System.out.println("Service disconnected!");
        }
    }

    private class ArpDetection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //
            if (arpDetectionBinder == null) {
                arpDetectionBinder = (ServiceArpDetection.Binder) iBinder;
                arpDetectionBinder.getServiceArpDetection().setCallback(new ServiceArpDetection.Callback() {
                    @Override
                    public void onDataChange(String Data, String running, boolean isThereAAttack) {
                        Bundle b = new Bundle();
                        b.putString("data", Data);
                        b.putString("running", running);
                        b.putBoolean("isThereAAttack", isThereAAttack);
                        Message msg = mHandler.obtainMessage();
                        msg.what=SECURE_SAFETY;
                        msg.setData(b);
                        mHandler.sendMessage(msg);
                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            arpDetectionBinder = null;
            System.out.println("Service disconnected!");
        }
    }
}