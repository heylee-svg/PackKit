package com.dhg.packkit.widget.wheel.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhg.packkit.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hebin on 2015/7/23.
 */
public class MyNumercWheelAdapter extends NumericWheelAdapter {

    private Context mContext;
    private int current;
    private NumericWheelAdapter mNumericWheelAdapter;

    List<Integer> day31 = Arrays.asList(1, 3, 5, 7, 8, 10, 12);
    List<Integer> day30 = Arrays.asList(4, 6, 9, 11);

    public MyNumercWheelAdapter(Context context, int minValue, int maxValue, String format) {
        super(context, minValue, maxValue, format);
        this.mContext = context;
        setItemResource(R.layout.item_wheel);
    }

    public MyNumercWheelAdapter(Context context, int minValue, int maxValue) {
        super(context, minValue, maxValue);
        this.mContext = context;
        setItemResource(R.layout.item_wheel);
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        convertView = super.getItem(index, convertView, parent);
        if (index == current) {
            ((TextView) convertView).setTextColor(mContext.getResources().getColor(R.color.current_wheel_color));
        } else {
            ((TextView) convertView).setTextColor(mContext.getResources().getColor(R.color.default_wheel_color));
        }
        return convertView;
    }

    private Boolean isAverageYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }
    public int getCurrentMonthCount(int year, int month) {
        int maxValue = 30;
        int actualMonth = month + 1;
        if (2 == actualMonth) {
            if (isAverageYear(year)) {
                maxValue = 29;
            } else {
                maxValue = 28;
            }
        } else if (day30.contains(actualMonth)) {
            maxValue = 30;
        } else if (day31.contains(actualMonth)) {
            maxValue = 31;
        }

        return maxValue;

    }

    public void notifyDataSetChanged() {
        notifyDataChangedEvent();
    }

    public void notifyDataInvalidated() {
        notifyDataInvalidatedEvent();
    }
}
