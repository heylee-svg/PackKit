package com.dhg.packkit.widget.wheel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhg.packkit.R;

/**
 * Created by hebin on 2015/7/23.
 */
public class MyWheelViewAdapter extends ArrayWheelAdapter {

    private String[] mData;
    private Context mContext;
    private int current;

    public MyWheelViewAdapter(Context ctx, String[] data) {
        super(ctx, data);
        this.mContext = ctx;
        this.mData = data;
    }
    public MyWheelViewAdapter(Context ctx, String[] data,int current) {
        super(ctx, data);
        this.mContext = ctx;
        this.mData = data;
        this.current = current;
    }

    public void setData(String[] data) {
        this.mData = data;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    @Override
    public int getItemsCount() {
        return mData.length;
    }

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        TextView mTv = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_wheel, null);
            mTv = (TextView) convertView;
            convertView.setTag(mTv);
        } else {
            mTv = (TextView) convertView.getTag();
        }
        mTv.setText(getItemByIndex(index));
        if (index == current) {
            mTv.setTextColor(mContext.getResources().getColor(R.color.current_wheel_color));
        } else {
            mTv.setTextColor(mContext.getResources().getColor(R.color.default_wheel_color));
        }
        return convertView;
    }

    protected String getItemByIndex(int index) {
        return mData[index];
    }


    public void notifyDataSetChanged() {
        notifyDataChangedEvent();
    }
}
