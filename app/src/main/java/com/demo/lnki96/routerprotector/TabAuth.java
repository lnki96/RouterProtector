package com.demo.lnki96.routerprotector;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by lnki9 on 2016/9/7 0007.
 */

public class TabAuth extends Fragment implements View.OnClickListener {

    public int WifiPasswordStrengthLevel;
    public int WifiAuthMethod;

    private Button scan_button;
    private Button btnReconn;

    private TextView wifi_result_textview;

    private WifiManager wifiManager;

    private WifiInfo currentWifiInfo;// 当前所连接的wifi

    private List<ScanResult> wifiList;// wifi列表

    private String[] str;

    private int wifiIndex;

    private ProgressDialog progressDialog;
    
    //--------------------------------------------
    public static final int VERIFY = 0,SECURE_SAFETY=1;
    private View root,backgroundTint;
    private ImageView status;
    private AnimatorSet popupShow, popupHide, popupHideSet;
    private ObjectAnimator secureSafe,secureDanger;
    private PopupWindow popupWindow;
    private String macAddr = "";
    private Button statusBtn;
    private boolean isVerified = false;

    private Message msg;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VERIFY:
                    if (msg.obj.equals(false)) {
                        TabAuth.this.msg = ((MainActivity)getActivity()).getHandler().obtainMessage();
                        TabAuth.this.msg.what = MainActivity.SNACKBAR;
                        TabAuth.this.msg.obj = "Login failed!";
                        ((MainActivity)getActivity()).getHandler().sendMessage(TabAuth.this.msg);
                    } else {
                        isVerified = (Boolean) msg.obj;
                        statusBtn.setClickable(false);
                        status.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_lock_open));
                    }
                    break;
                case SECURE_SAFETY:
                    if(msg.getData().getBoolean("isThereAAttack")&&secureDanger!=null)secureDanger.start();
                    else if(secureSafe!=null)secureSafe.start();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.tab_auth, container, false);
        
        //----------------------------------------
        //wifiManager = myWifiManager.getWifiManagerInstance(getActivity().getApplicationContext());
        wifiManager=(WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        setupViews();
        
        
        
        ((TextView) root.findViewById(R.id.mac_addr)).setText(macAddr);

        status = (ImageView) root.findViewById(R.id.auth_status);

        //-----------------------
        statusBtn= (Button) root.findViewById(R.id.auth_status_btn);


        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getActivity(),WifiActivity.class));
//                lookUpScan();
//                if (!isVerified) {
//                    //登录路由器
//                }

            }
        });


        root.findViewById(R.id.secure_status);



        ViewGroup popupView = (ViewGroup) inflater.inflate(R.layout.popup_auth, null);
        for (int i = 0; i < popupView.getChildCount(); i++)
            for (int j = 0; j < ((ViewGroup) popupView.getChildAt(i)).getChildCount(); j++)
                ((ViewGroup) popupView.getChildAt(i)).getChildAt(j).setOnClickListener(this);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.getContentView().measure(0, 0);
        popupShow = new AnimatorSet().setDuration(200);
        popupShow.playTogether(ObjectAnimator.ofFloat(popupWindow.getContentView(), "translationY",
                popupWindow.getContentView().getTranslationY() + popupWindow.getContentView().getMeasuredHeight(),
                popupWindow.getContentView().getTranslationY()));
        popupShow.setInterpolator(new DecelerateInterpolator());
        popupShow.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                popupWindow.showAtLocation(root, Gravity.BOTTOM, 0, 0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        popupHide = new AnimatorSet().setDuration(200);
        popupHide.playTogether(ObjectAnimator.ofFloat(popupWindow.getContentView(), "translationY",
                popupWindow.getContentView().getTranslationY(),
                popupWindow.getContentView().getTranslationY() + popupWindow.getContentView().getMeasuredHeight()));
        popupHide.setInterpolator(new DecelerateInterpolator());
        popupHide.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                popupWindow.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        backgroundTint=root.findViewById(R.id.tab_auth_background_tint);
        secureSafe = ObjectAnimator.ofFloat(backgroundTint, "alpha", 1f, 0f).setDuration(3000);
        secureDanger = ObjectAnimator.ofFloat(backgroundTint, "alpha", 0f, 1f).setDuration(3000);
        return root;
    }
    
    

    public Handler getHandler(){
        return handler;
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    public AnimatorSet getPopupShow() {
        return popupShow;
    }

    public AnimatorSet getPopupHide() {
        return popupHide;
    }

    public void setPopHideSet(AnimatorSet popupHideSet) {
        if (this.popupHideSet == null) {
            this.popupHideSet = new AnimatorSet();
            this.popupHideSet.playTogether(popupHideSet, popupHide);
            this.popupHideSet.setStartDelay(300);
        }
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    @Override
    public void onResume() {
        if(!MainActivity.getIsSafe())backgroundTint.setAlpha(1);


        currentWifiInfo = wifiManager.getConnectionInfo();
        ((TextView) root.findViewById(R.id.mac_addr)).setText(" 当前网络：" + currentWifiInfo.getSSID()//增加了显示的信息
                        + "\n ip:" + WifiUtil.intToIp(currentWifiInfo.getIpAddress())
                        + "\n Frequency:" + currentWifiInfo.getFrequency() + "MHz"
                        + "\n linkspeed:" + currentWifiInfo.getLinkSpeed() + "MBps"
                        + "\n MAC:" + currentWifiInfo.getMacAddress()
                        + "\n SupplicantState:" + currentWifiInfo.getSupplicantState().toString()
                        + "\n DNS1:"+ WifiUtil.intToIp(wifiManager.getDhcpInfo().dns1)
                        + "\n DNS2:"+ WifiUtil.intToIp(wifiManager.getDhcpInfo().dns2)
                        + "\n gateway:"+ WifiUtil.intToIp(wifiManager.getDhcpInfo().gateway)
                        + "\n netmask:"+ WifiUtil.intToIp(wifiManager.getDhcpInfo().netmask)
                        + "\n server address:"+ WifiUtil.intToIp(wifiManager.getDhcpInfo().serverAddress));

        openWifi();//打开了wifi,从而让后面的wifiManager获取到信息
        currentWifiInfo = wifiManager.getConnectionInfo();
        new ScanWifiThread().start();//
        //==============================
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popup_auth_0:
                break;
            case R.id.popup_auth_1:
                break;
            default:
                break;
        }
        popupHideSet.start();
    }
    
    //============================================================
    public void setupViews() {

        wifi_result_textview = (TextView) root.findViewById(R.id.wifi_result_textview);
        //super.setupViews();
    }

    public void initListener() {
        // super.initListener();
        //----------------------takecare-----------------
    }
    /**
     * 打开wifi
     */
    public void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 扫描wifi线程
     *
     * @author passing
     */
    class ScanWifiThread extends Thread {

        @Override
        public void run() {
            while (true) {
                currentWifiInfo = wifiManager.getConnectionInfo();
                startScan();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * 扫描wifi
     */
    public void startScan() {
        boolean check=wifiManager.startScan();//扫描
        // 获取扫描结果SSID到字符串数组中
        wifiList = wifiManager.getScanResults();
        str = new String[wifiList.size()];
        String tempStr = null;
        for (int i = 0; i < wifiList.size(); i++) {
            tempStr = wifiList.get(i).SSID;//获取ssid信息
            if (null != currentWifiInfo
                    && tempStr.equals(currentWifiInfo.getSSID()))//判断是否是当前所连接的wifi
            {
                tempStr = tempStr + "(已连接)";
            }
            str[i] = tempStr;//装入数据到字符串数组
        }
    }

    /**
     * 弹出框 查看扫描结果
     */
    public void lookUpScan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Availible Wifi List");
        builder.setItems(str, new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                wifiIndex = which;
                wifiHandler.sendEmptyMessage(3);
            }
        });
        builder.show();
    }

    /**
     * 获取网络ip地址
     *
     * @author passing
     */
    class RefreshSsidThread extends Thread {

        @Override
        public void run() {
            boolean flag = true;
            while (flag) {
                currentWifiInfo = wifiManager.getConnectionInfo();
                if (null != currentWifiInfo.getSSID()
                        && 0 != currentWifiInfo.getIpAddress()) {
                    flag = false;

                }
            }
            //已经成功连接上了,发送成功的消息
            wifiHandler.sendEmptyMessage(4);
            super.run();
        }
    }

    /**
     * 连接网络
     *
     * @param index
     * @param password
     */
    public void connetionConfiguration(int index, String password) {
        progressDialog = ProgressDialog.show(getActivity(), "正在连接...",
                "请稍候...");
        new ConnectWifiThread().execute(index + "", password);
    }

    /**
     * 连接wifi
     *
     * @author passing
     */
    class ConnectWifiThread extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            int index = Integer.parseInt(params[0]);
            if (index > wifiList.size()) {
                return null;
            }
            // 建立一个新的config,连接配置好指定ID的网络
            WifiConfiguration config = WifiUtil.createWifiInfo(
                    wifiList.get(index).SSID, params[1], 3, wifiManager,true);//3为wpa
            //int networkId = wifiManager.updateNetwork(config);
            int networkId = wifiManager.addNetwork(config);
            if (null != config) {
                wifiManager.enableNetwork(networkId, true);
                //enableNetwork第二个参数为true时,将会禁止其他网络的连接而只连接这个网络
                return wifiList.get(index).SSID;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (null != progressDialog) {
                progressDialog.dismiss();
            }
            if (null != result) {
                wifiHandler.sendEmptyMessage(0);
            } else {
                wifiHandler.sendEmptyMessage(1);//连接失败
            }
            super.onPostExecute(result);
        }

    }

    Handler wifiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    wifi_result_textview.setText("正在获取ip地址...");
                    new RefreshSsidThread().start();
                    break;
                case 1:
                    Toast.makeText(getActivity(), "连接失败！", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 3:
                    View layout = LayoutInflater.from(getActivity()).inflate(
                            R.layout.custom_dialog_layout, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("请输入密码").setView(layout);
                    final EditText passowrdText = (EditText) layout
                            .findViewById(R.id.password_edittext);
                    //这里是原来的代码
//                    builder.setPositiveButton("连接",
//                            new DialogInterface.OnClickListener()
//
//                            {
//
//                                @Override
//                                public void onClick(DialogInterface dialog,
//                                                    int which) {
//                                    connetionConfiguration(wifiIndex, passowrdText
//                                            .getText().toString());
//                                }
//                            }).show();
                    builder.setPositiveButton("连接",
                            new DialogInterface.OnClickListener()

                            {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    View layout2 = LayoutInflater.from(getActivity()).inflate(
                                            R.layout.password_strength_masurement_dialog, null);
                                    final TextView passwordStrengthText = (TextView) layout2
                                            .findViewById(R.id.passwordStrengthMasurement);
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                                    builder2.setTitle("密码强度检测").setView(layout2);
                                    passwordStrengthMeasure(passwordStrengthText,passowrdText.getText().toString());
                                    builder2.setPositiveButton("确认连接", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            connetionConfiguration(wifiIndex, passowrdText.getText().toString());
                                        }
                                    }).show();
                                }
                            });
                    builder.show();
                    break;
                case 4:
                    Toast.makeText(getActivity(), "连接成功！", Toast.LENGTH_SHORT)
                            .show();
                    wifi_result_textview.setText("当前网络："
                            + currentWifiInfo.getSSID() + " ip:"
                            + WifiUtil.intToIp(currentWifiInfo.getIpAddress()));
                    break;
            }
            super.handleMessage(msg);
        }

    };

    public static final int WEAK_PASSWORD=0;
    public static final int MIDDLE_PASSWORD=1;
    public static final int NULL_PASSWORD=2;
    public static final int STRONG_PASSWORD=3;

    //判断密码强度等级,最终体现到wifi安全度评级上
    public void passwordStrengthMeasure(TextView tv,String password){
        if(isNumeric(password)){
            tv.setText("this newwork has a weak password,please be care.");
            WifiPasswordStrengthLevel=WEAK_PASSWORD;
        }
        else if(isNumericAndCharacter(password)){
            tv.setText("the password is made of number and letters,which is of middle-safey");
            WifiPasswordStrengthLevel=MIDDLE_PASSWORD;
        }
        else if(password.equals("")){
            tv.setText("this network has no password! it's not safe! ");
            WifiPasswordStrengthLevel=NULL_PASSWORD;
        }
        else {
            tv.setText("this newwork has a strong password,it's safe.");
            WifiPasswordStrengthLevel=STRONG_PASSWORD;
        }
    }
    public   static   boolean  isNumeric(String str){
        for  ( int  i  =   str.length(); --i>=0; ){
            if  ( ! Character.isDigit(str.charAt(i))){
                return   false ;
            }
        }
        return   true ;
    }
    public static boolean isNumericAndCharacter(String str){
        Pattern pattern = Pattern.compile("[0-9a-zA-Z]*");
        return pattern.matcher(str).matches();
    }
}
