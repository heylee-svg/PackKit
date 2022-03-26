package com.dhg.packkit.widget.wheel.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhg.packkit.R;

/**
 * Created by sanvar on 17-7-31.
 */

public class TimeWheelAdapter<T> extends ArrayWheelAdapter {

    private int current;
    private T[] items;

    public TimeWheelAdapter(Context context, T[] items) {
        super(context, items);
        this.items = items;
        setItemResource(R.layout.item_wheel);
    }

    public void setCurrent(int current) {
        this.current = current;
        this.notifyDataChangedEvent();
    }

    public T getCurrent() {
        return items[current];
    }


    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        convertView = super.getItem(index, convertView, parent);
        if (index == current) {
            ((TextView) convertView).setTextColor(this.context.getResources().getColor(R.color.current_wheel_color));
        } else {
            ((TextView) convertView).setTextColor(this.context.getResources().getColor(R.color.default_wheel_color));
        }
        return convertView;
    }


}
