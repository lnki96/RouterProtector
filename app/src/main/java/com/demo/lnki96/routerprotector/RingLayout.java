package com.demo.lnki96.routerprotector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lnki9 on 2016/9/7 0007.
 */

public class RingLayout extends ViewGroup {
    private final int STROKE_WIDTH = 16;
    private Paint paint = new Paint();
    private int height,radius,radiusLocked;
    private boolean sizeLock=false;

    public RingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View view;
        int n=getChildCount();
        int centerX=(r-l)/2,w,h=(b-t)/2-height,dh;
        for(int i=0;i<n;i++){
            view=getChildAt(i);
            w=view.getMeasuredWidth()/2;
            dh=view.getMeasuredHeight();
            view.layout(centerX-w,h,centerX+w,h+dh);
            h+=dh;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View view;
        final int RING_PADDING = 32;
        int width = 0;
        height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            view = getChildAt(i);
            measureChild(view,0,0);
            if (width < view.getMeasuredWidth()) width = view.getMeasuredWidth();
            height += view.getMeasuredHeight();
        }
        width /= 2;
        height /= 2;
        radius = 400;//(int) (Math.sqrt(width * width + height * height) + STROKE_WIDTH + RING_PADDING);
        setPadding(radius - width, radius - height, radius - width, radius - height);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
        else setMeasuredDimension(2 * radius, 2 * radius);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewWidth = getWidth(), viewHeight = getHeight();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.textColor));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        if(sizeLock)radius=radiusLocked;
        else radiusLocked=radius;
        canvas.drawCircle(viewWidth / 2, viewHeight / 2, radius - STROKE_WIDTH, paint);
        sizeLock=true;
    }

    public void setRadius(int radius){
        sizeLock=true;
        this.radiusLocked=radius;
    }

    public int getRadius(){
        return radius;
    }

    public void unLockRadius(){
        this.sizeLock=false;
    }
}