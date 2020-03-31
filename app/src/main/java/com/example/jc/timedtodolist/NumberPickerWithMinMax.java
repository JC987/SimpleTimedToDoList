package com.example.jc.timedtodolist;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.NumberPicker;

public class NumberPickerWithMinMax extends NumberPicker {
    public NumberPickerWithMinMax(Context context){
        super(context);
    }

    public NumberPickerWithMinMax(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributeSet(context, attrs,0, 0);
    }

    public NumberPickerWithMinMax(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs);
        setAttributeSet(context, attrs, defStyleAttr, 0);
    }

    public NumberPickerWithMinMax(Context context, AttributeSet attrs, int defStyleAttr, int defStyle) {
        super(context, attrs, defStyle);
        setAttributeSet(context, attrs, defStyleAttr, defStyle);
    }

    private void setAttributeSet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberPickerWithMinMax, defStyleAttr, defStyleRes);

       // try {
            this.setMinValue(attributes.getInt(R.styleable.NumberPickerWithMinMax_min, 0));
            this.setMaxValue(attributes.getInt(R.styleable.NumberPickerWithMinMax_max, 0));
       // } finally {
            attributes.recycle();
       // }

    }
}
