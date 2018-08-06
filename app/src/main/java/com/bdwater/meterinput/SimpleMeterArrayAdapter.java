package com.bdwater.meterinput;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bdwater.meterinput.model.Meter;

/**
 * Created by hegang on 16/6/25.
 */
public class SimpleMeterArrayAdapter extends ArrayAdapter<Meter> {
    LayoutInflater inflater;
    ViewHolder holder;
    int checkedPosition = -1;
    public SimpleMeterArrayAdapter(Context context, Meter[] meters) {
        super(context, R.layout.meter_list_item, meters);

        inflater = LayoutInflater.from(context);
    }

    public void setCheckedPosition(int position) {
        checkedPosition = position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            View view = inflater.inflate(R.layout.meter_list_item, null);
            holder = new ViewHolder();
            holder.ic_customer = view.findViewById(R.id.ic_customer);
            holder.ic_meter = view.findViewById(R.id.ic_meter);
            holder.tv1 = (TextView)view.findViewById(R.id.textView1);
            holder.tv2 = (TextView)view.findViewById(R.id.textView2);
            holder.tv3 = (TextView)view.findViewById(R.id.textView3);
            holder.tv4 = (TextView)view.findViewById(R.id.textView4);
            holder.tv5 = (TextView)view.findViewById(R.id.textView5);
            view.setTag(holder);
            convertView = view;
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        Meter meter = getItem(position);
        holder.ic_customer.setVisibility(meter.IsFake == false ? View.VISIBLE: View.GONE);
        holder.ic_meter.setVisibility(meter.IsFake ? View.VISIBLE: View.GONE);
        holder.tv1.setText(meter.CustomerNo);
        holder.tv2.setText(meter.Name);
        holder.tv3.setVisibility(meter.WaterStatus == 0 ? View.GONE: View.VISIBLE);
        holder.tv4.setVisibility(meter.HasCustomerBill ? View.VISIBLE: View.GONE);
        holder.tv5.setVisibility(meter.MeterTaskStatus > 0 ? View.VISIBLE: View.GONE);

        if(position == checkedPosition) {
            holder.tv2.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            convertView.setBackgroundResource(R.color.itemClickablePressedBackground);
        }
        else {
            convertView.setBackgroundResource(android.R.color.transparent);
            holder.tv2.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }

        return convertView;
    }

    class ViewHolder {
        View ic_customer;
        View ic_meter;
        TextView tv1;
        TextView tv2;
        TextView tv3;
        TextView tv4;
        TextView tv5;
    }
}