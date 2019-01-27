package com.example.jc.timedtodolist;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import java.util.logging.LogManager;

public class MyScrollView extends View {



    public static int WITHOUT_MAX_HEIGHT_VALUE = -1;



    private int maxHeight = WITHOUT_MAX_HEIGHT_VALUE;



    public MyScrollView(Context context) {

        super(context);

    }



    public MyScrollView(Context context, AttributeSet attrs) {

        super(context, attrs);

    }



    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

    }



    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        try {

            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (maxHeight != WITHOUT_MAX_HEIGHT_VALUE

                    && heightSize > maxHeight) {

                heightSize = maxHeight;

            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);

            getLayoutParams().height = heightSize;

        } catch (Exception e) {

            Log.d("onMeasure", "Error forcing height", e);

        } finally {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        }

    }



    public void setMaxHeight(int maxHeight) {

        this.maxHeight = maxHeight;

    }

}