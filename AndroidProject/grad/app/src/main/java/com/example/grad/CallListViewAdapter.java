package com.example.grad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CallListViewAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<CallListItem> array_calls;

    private ViewHolder mViewHolder;

    public CallListViewAdapter(Context mContext, ArrayList<CallListItem> array_calls) {
        this.mContext = mContext;
        this.array_calls = array_calls;
    }

    @Override
    public int getCount() {
        return array_calls.size();
    }

    @Override
    public Object getItem(int position) {
        return array_calls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHoldr 패턴
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_call_item, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        // View에 Data 세팅
        mViewHolder.tv_addr.setText(array_calls.get(position).addr); //해야함
        mViewHolder.tv_time.setText(array_calls.get(position).time); //해야함

        return convertView;
    }

    public class ViewHolder {
        private TextView tv_addr;
        private TextView tv_time;

        public ViewHolder(View convertView){
            tv_addr = (TextView)convertView.findViewById(R.id.tv_addr);
            tv_time = (TextView)convertView.findViewById(R.id.tv_time);
        }
    }

}
