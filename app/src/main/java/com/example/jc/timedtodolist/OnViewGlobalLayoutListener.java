package com.example.jc.timedtodolist;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

public class OnViewGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private final static int maxHeight = 1200;
    private View view;

    float density;
    int deviceWidth;
    int deviceHeight;
    //float px = someDpValue * density;

    public OnViewGlobalLayoutListener(View view) {
        this.view = view;
        density = (int) view.getResources().getDisplayMetrics().density;

    }
   /* public OnViewGlobalLayoutListener(Context context, AttributeSet attrs) {

        super(context, attrs);

    }*/
    public OnViewGlobalLayoutListener(View view, Context context) {
        this.view = view;
        density = (int) view.getResources().getDisplayMetrics().density;
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

     //   context.obtainStyledAttributes()
    }

    public void setContext(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth= displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;
        density = displayMetrics.density;
    }
    @Override
    public void onGlobalLayout() {
        double tmp = .58;
        if(deviceHeight>=700 && deviceHeight<800)
            tmp = .60;
        if(deviceHeight>=800)
            tmp = .63;

        int dp ;
        dp = (int) (deviceHeight * tmp);

        Log.d("mainActivity", "onGlobalLayout: px " + dp);
        if (view.getHeight() + 250 >= dp)

            view.getLayoutParams().height = dp;
    }
}

