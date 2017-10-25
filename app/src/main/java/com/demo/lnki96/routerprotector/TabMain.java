package com.demo.lnki96.routerprotector;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by lnki9 on 2016/9/7 0007.
 */

public class TabMain extends Fragment implements OnClickListener {
    public static final int TIMING_FINISH_NORMAL = 0, REFRESH_ICON = 1, SERVICE_REBIND = 2, CONNECT_STATUS_UPDATE = 3, SECURE_SAFETY = 4;
    private final int COUNT_DOWN_INTERVAL = 1000;
    private final int[] deviceCountNumbers = {R.drawable.ic_expand_more};
    private final String defaultHour = "2", defaultMinute = "00", defaultSecond = "00";
    Bundle bundle = new Bundle();
    private TextWatcher pwChecker = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            isPwEdit = true;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private View root, backgroundTint;
    private CountDownTimer timer;
    private EditText timerSecond;
    private TextView timerHour;
    private TextView timerMinute;
    private ObjectAnimator secureSafe, secureDanger;
    private AnimatorSet popupShow, popupHide, popupHideSet;
    private ImageButton refreshBtn;
    private ImageView deviceCount;
    private boolean isTiming = false, isPaused = false, isTimeEdit = false, isPwEdit = false, isLaunching = true;
    private int hour, minute, second;
    private KeyListener keyListener;
    private String wifiIpAddr = "";
    private Checker checker = new Checker();
    private String hourText, hourSet, minuteText, minuteSet, secondText, secondSet, ssid, password;
    private PopupWindow popupWindow;
    private TextInputLayout pwEdit;
    OnClickListener refresh = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isTiming) {
                timer.cancel();
                if (isPwEdit) {
                    password = pwEdit.getEditText().getText().toString();
                    isPwEdit = false;
                } else {
                    password = cipher();
                    changeText(pwEdit.getEditText(), password, pwChecker);
                }
                long millisInFuture = toMillis();
                toTime(millisInFuture);
                timer = new Timer(millisInFuture, COUNT_DOWN_INTERVAL).start();
                changeWifiPassword(ssid, password);
            } else {
                // if ((!timerSecond.getText().toString().equals("00") || !timerMinute.getText().equals("00") || !timerHour.getText().equals("0"))) {
                if (isPwEdit) {
                    password = pwEdit.getEditText().getText().toString();
                    isPwEdit = false;
                } else {
                    password = cipher();
                    changeText(pwEdit.getEditText(), password, pwChecker);
                }
                if (timerSecond.getText().toString().equals("00") || !timerMinute.getText().equals("00") || !timerHour.getText().equals("0")) {
                    hourSet = defaultHour;
                    minuteSet = defaultMinute;
                    secondSet = defaultSecond;
                }
                if (isTimeEdit) {
                    hourSet = timerHour.getText().toString();
                    minuteSet = timerMinute.getText().toString();
                    secondSet = timerSecond.getText().toString();
                    isTimeEdit = false;
                }
                timerSecond.setKeyListener(null);
                long millisInFuture = toMillis();
                toTime(millisInFuture);
                timer = new Timer(millisInFuture, COUNT_DOWN_INTERVAL).start();
                isTiming = true;
                refreshBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_refresh));
                changeWifiPassword(ssid, password);
            }
            deviceCount = (ImageView) getActivity().findViewById(R.id.device_count);
        }
    };
    private WifiManager wifiManager;
    private WifiInfo currentWifiInfo;
    private List<WifiConfiguration> wifiConfigurationList;
    private Message msg;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case TIMING_FINISH_NORMAL:
                    changeWifiPassword(ssid, password);
                    long millisInFuture = toMillis();
                    toTime(millisInFuture);
                    timer = new Timer(millisInFuture, COUNT_DOWN_INTERVAL).start();
                    break;
                case REFRESH_ICON:
                    refreshBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play));
                    break;
                case SERVICE_REBIND:
                    changeText(pwEdit.getEditText(), bundle.getString("password"), pwChecker);
                    if (!bundle.getBoolean("isTiming")) {
                        millisInFuture = toMillis();
                        toTime(millisInFuture);
                        hourSet = String.valueOf(hour);
                        minuteSet = String.valueOf(minute);
                        secondSet = String.valueOf(second);
                        timer = new Timer(bundle.getLong("millisInFuture"), COUNT_DOWN_INTERVAL).start();
                        isTiming = true;
                        refreshBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_refresh));
                    }
                    break;
                case CONNECT_STATUS_UPDATE:
                    if (deviceCount != null)
                        deviceCount.setImageDrawable(ContextCompat.getDrawable(getActivity(), deviceCountNumbers[Integer.parseInt(bundle.getString("state")) % 1]));
                    if (bundle.getBoolean("isFound")) {
                        Snackbar.make(getActivity().findViewById(R.id.main), "New device connected\nMAC: " + bundle.getString("newMac"), Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case SECURE_SAFETY:
                    if (bundle.getBoolean("isThereAAttack") && secureDanger != null)
                        secureDanger.start();
                    else if (secureSafe != null) secureSafe.start();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static String cipher() {
        String cipher = new String();
        String temp;
        for (int i = 0; i < 16; i++) {
            while (true) {
                int x = (int) (Math.random() * 100);
                if (x > 33 && x < 126) {
                    temp = String.valueOf((char) (x));
                    break;
                }
                //String temp=Integer.toString(x);
            }

            cipher += temp;
        }
        return cipher;
    }

    public static void readNet(String address, String step) {
        new AsyncTask<String, String, String>() {
            String result = null;

            @Override
            protected String doInBackground(String... strings) {

                try {
                    URL url = new URL(strings[0]);
                    try {
                        result = null;
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("Cookie", "Authorization=Basic%20YWRtaW46YWRtaW4xMjM%3D; ChgPwdSubTag=");
                        String choose = strings[1];

                        //1-修改密码:
                        if (choose.equals("1")) {
                            connection.setRequestProperty("Referer", "http://192.168.1.1/userRpm/WlanSecurityRpm.htm");
                        }
                        //2-重启路由器:
                        if (choose.equals("2")) {
                            connection.setRequestProperty("Referer", "http://192.168.1.1/userRpm/SysRebootRpm.htm");
                        }
                        //添加mac
                        if (choose.equals("3")) {
                            connection.setRequestProperty("Referer", "http://192.168.1.1/userRpm/WlanMacFilterRpm.htm?Add=Add&Page=1");
                        }


                        //由于并没有POST数据,这里先注释掉
//                        connection.setDoOutput(true);
//                        connection.setRequestMethod("POST");
//                        connection.setChunkedStreamingMode(0);
//                        OutputStreamWriter osw=new OutputStreamWriter(connection.getOutputStream(),"utf-8");
//                        BufferedWriter bw=new BufferedWriter(osw);
//                        bw.write("");
//                        bw.flush();

                        InputStream is = connection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is, "gb2312");
                        BufferedReader br = new BufferedReader(isr);
                        result = "result:";
                        while (br.readLine() != null) {
                            result = result + br.readLine();
                        }
//                        byte[] b=new byte[100];
//                        is.read(b,0,50);
//                        String sss=new String(b,"utf-8");

                        publishProgress(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return result;

            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }.execute(address, step);//这里注意不要少传参数了
    }//第二个参数："1"修改密码，"2"重启路由器，"3"添加MAC

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.tab_main, container, false);

        pwEdit = ((TextInputLayout) root.findViewById(R.id.password));
        pwEdit.setHint(getString(R.string.pw_hint));
        pwEdit.getEditText().addTextChangedListener(pwChecker);
        ((TextView) root.findViewById(R.id.ip_addr)).setText(wifiIpAddr);

        timerHour = (TextView) root.findViewById(R.id.count_down_hour);
        timerMinute = (TextView) root.findViewById(R.id.count_down_minute);
        timerSecond = (EditText) root.findViewById(R.id.count_down_second);
        timerSecond.setFocusable(true);
        timerSecond.setSelection(timerSecond.length());
        keyListener = timerSecond.getKeyListener();
        timerSecond.addTextChangedListener(checker);
        timerSecond.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timerSecond.setSelection(timerSecond.length());
            }
        });
        timerSecond.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) timerSecond.setSelection(timerSecond.length());
            }
        });

        refreshBtn = (ImageButton) root.findViewById(R.id.main_refresh);
        refreshBtn.setOnClickListener(refresh);

        refreshBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isTiming) {
                    timer.cancel();
                    timerSecond.setKeyListener(keyListener);
                    refreshBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play));
                    isTiming = false;
                } else {
                    refreshBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_refresh));
                    new java.util.Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(REFRESH_ICON);
                            cancel();
                        }
                    }, 1000);
                    timerHour.setText(getString(R.string.time_hour));
                    timerMinute.setText(getString(R.string.time_minute));
                    changeText(timerSecond, getString(R.string.time_second), checker);
                    changeText(pwEdit.getEditText(), "", pwChecker);
                }
                return true;
            }
        });

        ViewGroup popupView = (ViewGroup) inflater.inflate(R.layout.popup_main, null);
        for (int i = 0; i < popupView.getChildCount(); i++)
            for (int j = 0; j < ((ViewGroup) popupView.getChildAt(i)).getChildCount(); j++)
                ((ViewGroup) popupView.getChildAt(i)).getChildAt(j).setOnClickListener(this);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.getContentView().measure(0, 0);
        popupShow = new AnimatorSet().setDuration(200);
        popupShow.playTogether(ObjectAnimator.ofFloat(popupWindow.getContentView(), "translationY",
                popupWindow.getContentView().getTranslationY() + popupWindow.getContentView().getMeasuredHeight(),
                popupWindow.getContentView().getTranslationY()),
                ObjectAnimator.ofFloat(popupWindow, "alpha", 0f, 1f));
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
                popupWindow.getContentView().getTranslationY() + popupWindow.getContentView().getMeasuredHeight()),
                ObjectAnimator.ofFloat(popupWindow, "alpha", 1f, 0f));
        popupHide.setInterpolator(new AccelerateInterpolator());
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
        backgroundTint = root.findViewById(R.id.tab_main_background_tint);
        secureSafe = ObjectAnimator.ofFloat(backgroundTint, "alpha", 1f, 0f).setDuration(3000);
        secureDanger = ObjectAnimator.ofFloat(backgroundTint, "alpha", 0f, 1f).setDuration(3000);

        wifiManager = myWifiManager.getWifiManagerInstance(getActivity().getApplicationContext());

        return root;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setWifiIpAddr(String wifiIpAddr) {
        this.wifiIpAddr = wifiIpAddr;
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

    @Override
    public void onResume() {
        if (isLaunching) {
            refresh.onClick(refreshBtn);
            isLaunching = false;
        }
        if (!MainActivity.getIsSafe()) backgroundTint.setAlpha(1);
        if (isPaused) {
            if (isTiming) {
                timerHour.setText(String.valueOf(hour));
                timerMinute.setText((minute < 10) ? "0" + String.valueOf(minute) : String.valueOf(minute));
                changeText(timerSecond, (second < 10) ? "0" + String.valueOf(second) : String.valueOf(second), checker);
                refreshBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_refresh));
            } else {
                timerHour.setText(hourText);
                timerMinute.setText(minuteText);
                changeText(timerSecond, secondText, checker);
            }
        }

        currentWifiInfo = wifiManager.getConnectionInfo();
        wifiConfigurationList = wifiManager.getConfiguredNetworks();

        super.onResume();
    }

    @Override
    public void onPause() {
        hourText = timerHour.getText().toString();
        minuteText = timerMinute.getText().toString();
        secondText = timerSecond.getText().toString();
        if (!isPaused) isPaused = true;
        super.onPause();
    }

    private long toMillis() {
        long millis;
        millis = Integer.parseInt(secondSet) + 1;
        millis += Integer.parseInt(minuteSet) * 60;
        millis += Integer.parseInt(hourSet) * 3600;
        return ++millis * 1000;
    }

    private void toTime(long millis) {
        millis /= 1000;
        hour = (int) (--millis / 3600);
        millis %= 3600;
        minute = (int) (millis / 60);
        second = (int) (millis % 60);
    }

    private void changeText(EditText text, String string, TextWatcher textWatcher) {
        text.removeTextChangedListener(textWatcher);
        text.setText(string);
        text.setSelection(text.length());
        text.addTextChangedListener(textWatcher);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popup_main_0:

                break;
            case R.id.popup_main_1:
                ssid = currentWifiInfo.getSSID();
                //重启路由器
                readNet("http://192.168.1.1/userRpm/SysRebootRpm.htm?Reboot=%D6%D8%C6%F4%C2%B7%D3%C9%C6%F7", "2");
                //更新路由器连接信息并自动重连
                break;
            default:
                break;
        }
        popupHideSet.start();
    }

    private void changeWifiPassword(String ssid, String password) {
        //readNet("http://192.168.1.1/userRpm/WlanSecurityRpm.htm?secType=3&pskSecOpt=2&pskCipher=3&pskSecret="+password+"&interval=3600&Save=%B1%A3+%B4%E6","1");
        //readNet("http://192.168.1.1/userRpm/SysRebootRpm.htm?Reboot=%D6%D8%C6%F4%C2%B7%D3%C9%C6%F7","2");
        //new UpdateWifiConfiguration().execute(ssid,password);
    }

    class Checker implements TextWatcher {
        private String before, after, hour, minute;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            before = s.toString();
            hour = timerHour.getText().toString();
            minute = timerMinute.getText().toString();
            isTimeEdit = true;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int length = s.length();
            if (length == 1) {
                after = minute.substring(1) + this.before.charAt(0);
                minute = hour.substring(hour.length() - 1) + minute.charAt(0);
                hour = (hour.length() == 1) ? "0" : hour.substring(0, 1);
            } else if (length == 3 && Integer.parseInt(hour) < 10) {
                hour = (hour.equals("0")) ? minute.substring(0, 1) : hour + minute.charAt(0);
                minute = minute.substring(1) + s.charAt(0);
                after = s.toString().substring(1);
            } else after = this.before;
        }

        @Override
        public void afterTextChanged(Editable s) {
            timerHour.setText(hour);
            timerMinute.setText(minute);
            changeText(timerSecond, after, checker);
        }
    }

    class Timer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }


        @Override
        public void onTick(long millisUntilFinished) {
            if (hour > 0 || minute > 0) {
                if (hour > 0) {
                    second = (second == 0) ? 59 : --second;
                    minute = (second == 59) ? (minute == 0) ? 59 : --minute : minute;
                    if (minute == 59 && second == 59) --hour;
                } else {
                    second = (second == 0) ? 59 : --second;
                    minute = (second == 59) ? (minute == 0) ? 0 : --minute : minute;
                }
            } else {
                second = (second == 0) ? 59 : --second;
            }
            timerHour.setText(String.valueOf(hour));
            timerMinute.setText((minute < 10) ? "0" + String.valueOf(minute) : String.valueOf(minute));
            changeText(timerSecond, (second < 10) ? "0" + String.valueOf(second) : String.valueOf(second), checker);
        }

        @Override
        public void onFinish() {
            List<String> timeSet = new ArrayList<>();
            if (isPwEdit) {
                password = pwEdit.getEditText().getText().toString();
                isPwEdit = false;
            } else {
                password = cipher();
                changeText(pwEdit.getEditText(), password, pwChecker);
            }
            hour = Integer.parseInt(hourSet);
            minute = Integer.parseInt(minuteSet);
            second = Integer.parseInt(secondSet) + 1;
            msg = handler.obtainMessage();
            msg.what = TIMING_FINISH_NORMAL;
            handler.handleMessage(msg);
            cancel();
        }
    }

    class UpdateWifiConfiguration extends AsyncTask<String, WifiConfiguration, String> {
        @Override
        protected String doInBackground(String... strings) {
            wifiManager.startScan();//扫描
            // 获取扫描结果SSID到字符串数组中
            int index = 0;
            String ssid = strings[0];//获取当前网络的ssid


            // 连接配置好指定ID的网络
            WifiConfiguration config = WifiUtil.createWifiInfo(
                    ssid, strings[1], 3, wifiManager, false);
            wifiManager.startScan();//扫描

            String result = null;
            wifiConfigurationList = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : wifiConfigurationList) {
                if (existingConfig.SSID.equals(ssid)) {
                    result = existingConfig.SSID;
                    //config.networkId=existingConfig.networkId;
                    break;
                }
            }


            //config.networkId=wifiConfigurationlist.get(index).networkId;
            publishProgress(config);

            int networkId = wifiManager.addNetwork(config);//这条语句执行完后,就重新连接上了
            //publishProgress(networkId);

            if (null != config) {
                for (WifiConfiguration existingConfig : wifiConfigurationList) {
                    if (existingConfig.SSID.equals(ssid)) {
                        result = existingConfig.SSID;
                        //networkId=existingConfig.networkId;
                        break;
                    }
                }
                boolean ifSucceed = wifiManager.enableNetwork(networkId, true);

                System.out.println(result);
                return ssid;
            }
            return null;

        }

        @Override
        protected void onProgressUpdate(WifiConfiguration... values) {
            super.onProgressUpdate(values);
        }
    }
}
