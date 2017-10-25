package com.demo.lnki96.routerprotector;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimatedVectorDrawable;
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
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by lnki9 on 2016/9/7 0007.
 */

public class TabSecure extends Fragment implements View.OnClickListener {
    public static final int SECURE_SAFETY = 0;
    private View root, backgroundTint;
    private Integer notificationId;
    private ImageButton stateIcon;
    private TextView state, attackMac;
    private AnimatedVectorDrawable switchToUnsafe, switchToSafe;

    private AnimatorSet popupShow, popupHide, popupHideSet;
    private ObjectAnimator secureSafe, secureDanger;
    private PopupWindow popupWindow;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case SECURE_SAFETY:
                    if (bundle.getBoolean("isThereAAttack")) {
                        secureDanger.start();
                        stateIcon.setImageDrawable(switchToUnsafe);
                        switchToUnsafe.start();
                        //stateIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close));
                        stateIcon.setClickable(true);
                        attackMac.setText("Attacker's MAC is " + bundle.getString("data"));
                        state.setText("ARP spoofing!");
                    } else {
                        secureSafe.start();
                        stateIcon.setImageDrawable(switchToSafe);
                        switchToSafe.start();
                        //stateIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_check));
                        stateIcon.setClickable(false);
                        attackMac.setText("");
                        state.setText(getString(R.string.secure_status));
                    }
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
        root = inflater.inflate(R.layout.tab_secure, container, false);
        stateIcon = (ImageButton) root.findViewById(R.id.secure_status);
        stateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isThereAAttack", false);
                ((MainActivity) getActivity()).getHandler().sendEmptyMessage(MainActivity.UNBIND_ARP_DETECTION);
                Message msg = ((MainActivity) getActivity()).getHandler().obtainMessage();
                msg.what = MainActivity.SECURE_SAFETY;
                msg.setData(bundle);
                ((MainActivity) getActivity()).getHandler().sendMessage(msg);
            }
        });
        switchToUnsafe = (AnimatedVectorDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.anim_vector);
        switchToSafe = (AnimatedVectorDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.to_safe);

        state = (TextView) root.findViewById(R.id.secure_status_text);
        attackMac = (TextView) root.findViewById(R.id.secure_mac_addr);

        ViewGroup popupView = (ViewGroup) inflater.inflate(R.layout.popup_secure, null);
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

        backgroundTint = root.findViewById(R.id.tab_secure_background_tint);
        secureSafe = ObjectAnimator.ofFloat(backgroundTint, "alpha", 1f, 0f).setDuration(3000);
        secureDanger = ObjectAnimator.ofFloat(backgroundTint, "alpha", 0f, 1f).setDuration(3000);
        if (MainActivity.getIsSafe()) stateIcon.setClickable(false);
        return root;
    }

    public Handler getHandler() {
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

    @Override
    public void onResume() {
        if (!MainActivity.getIsSafe()) backgroundTint.setAlpha(1);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.popup_secure_0:
//                break;
//            case R.id.popup_secure_1:
//                break;
            default:
                break;
        }
        popupHideSet.start();
    }
}
