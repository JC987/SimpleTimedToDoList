package com.example.jc.timedtodolist;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ScrollView;


public class MaxHeightScrollView extends ScrollView {

    private int  deviceHeight;
    private float scale;
    private final int defaultHeight = 200;

    public MaxHeightScrollView(Context context) {
        super(context);
        setContext(context);
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs);
        }
        setContext(context);
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs);
        }
        setContext(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaxHeightScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init(context, attrs);
        }
        setContext(context);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView);
           // maxHeight = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, defaultHeight);
           // scale = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightScrollView_scale,1);
            scale = styledAttrs.getFloat(R.styleable.MaxHeightScrollView_scale,0.5f);
            styledAttrs.recycle();
        }
    }
    public void setScale(float scale){
        this.scale = scale;
    }
    public float getScale(){
        return scale;
    }

    private void setContext(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        try {
            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        catch(NullPointerException e){
            ;
        }
       // deviceWidth= displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;

    }
    /*
    This will set the max height of the scroll view
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Scale is a float value between 0-1.
        //newHeight is going to be the max height of the scroll view in pxs.
        //So if scale is 0.5f and deviceHeight is 1000px then newHeight's value will be 500px.
        //This is like saying I want the scrollview to take up half the screen or whatever scale is.
        int newHeight = (int) (scale * deviceHeight);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
