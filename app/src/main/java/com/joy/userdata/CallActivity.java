package com.joy.userdata;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by huangyacong on 2018/8/24.
 */
public class CallActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        mContext = this;
        ListView listView = findViewById(R.id.call_lv);
        List<CallRecord> list = new ArrayList<>();
        CallAdapter adapter = new CallAdapter(list, mContext);
        listView.setAdapter(adapter);

        ContentResolver resolver = getContentResolver();
        //获取cursor对象
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, new String[]{
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,  //一开始没设置姓名的话,为null,不会自动更新
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
        }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    CallRecord record = new CallRecord();
                    record.number = cursor.getString(0);
                    record.name = cursor.getString(1);
                    record.type = getCallType(cursor.getInt(2));
                    record.date = cursor.getLong(3);
                    record.duration = cursor.getLong(4);
                    list.add(record);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();  //关闭cursor，避免内存泄露
            }
        }
        if (list != null && list.size() != 0) {
            adapter.notifyDataSetChanged();
        }
    }


    class CallRecord {
        long date;
        String number;
        String name;
        String type;
        long duration;

        @Override
        public String toString() {
            return "CallRecord{" +
                    "date=" + formatDate(date) +
                    ", number='" + number + '\'' +
                    ", name='" + name + '\'' +
                    ", type=" + type +
                    ", duration=" + formatDuration(duration) +
                    '}';
        }
    }

    private String getCallType(int anInt) {
        switch (anInt) {
            case CallLog.Calls.INCOMING_TYPE:
                return "呼入";
            case CallLog.Calls.OUTGOING_TYPE:
                return "呼出";
            case CallLog.Calls.MISSED_TYPE:
                return "未接";
            default:
                break;
        }
        return null;
    }

    public String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        return format.format(new Date(time));
    }

    public String formatDuration(long time) {
        long s = time % 60;
        long m = time / 60;
        long h = time / 60 / 60;
        StringBuilder sb = new StringBuilder();
        if (h > 0) {
            sb.append(h).append("小时");
        }
        if (m > 0) {
            sb.append(m).append("分");
        }
        sb.append(s).append("秒");
        return sb.toString();
    }

    public class CallAdapter extends BaseAdapter {

        List<CallRecord> mList;
        Context mContext;

        public CallAdapter(List<CallRecord> list, Context context) {
            mList = list;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_call, null);
                viewHolder = new ViewHolder();
                viewHolder.typeTv = convertView.findViewById(R.id.call_type);
                viewHolder.nameTv = convertView.findViewById(R.id.call_name);
                viewHolder.numberTv = convertView.findViewById(R.id.call_number);
                viewHolder.dateTv = convertView.findViewById(R.id.call_date);
                viewHolder.durationTv = convertView.findViewById(R.id.call_duration);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            CallRecord callRecord = mList.get(position);
            viewHolder.typeTv.setText(callRecord.type);
            viewHolder.nameTv.setText(callRecord.name);
            viewHolder.numberTv.setText(callRecord.number);
            viewHolder.dateTv.setText(formatDate(callRecord.date));
            viewHolder.durationTv.setText(formatDuration(callRecord.duration));
            return convertView;
        }

        private class ViewHolder {
            TextView typeTv;
            TextView nameTv;
            TextView numberTv;
            TextView dateTv;
            TextView durationTv;
        }
    }
}
