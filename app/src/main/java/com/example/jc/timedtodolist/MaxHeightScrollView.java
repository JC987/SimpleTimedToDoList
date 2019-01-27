package com.example.jc.timedtodolist;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ScrollView;

public class MaxHeightScrollView extends ScrollView {

    private int maxHeight, deviceHeight, deviceWidth;
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
            //200 is a defualt value
            maxHeight = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, defaultHeight);
           // scale = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightScrollView_scale,1);
            scale = styledAttrs.getFloat(R.styleable.MaxHeightScrollView_scale,0.5f);
            styledAttrs.recycle();
        }
    }

    private void setContext(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        try {
            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        catch(NullPointerException e){
            Log.d("ViewGlobal", "OnViewGlobalLayoutListener: null");
        }
        deviceWidth= displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newHeight = (int) (scale * deviceHeight);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
