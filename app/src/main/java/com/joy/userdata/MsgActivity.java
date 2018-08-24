package com.joy.userdata;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by huangyacong on 2018/8/24.
 */
public class MsgActivity extends AppCompatActivity {

    private Context mContext;
    private Map<String, String> mPhoneMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);
        mContext = this;
        ListView listView = findViewById(R.id.msg_lv);
        List<SMSMessage> list = new ArrayList<>();
        MsgAdapter adapter = new MsgAdapter(list, mContext);
        listView.setAdapter(adapter);

        mPhoneMap = getContacts();
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://sms/"), new String[]{
                "address",
                "body",
                "date",
                "read",
                "status",
                "type",
                "person"
        }, null, null, "date DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SMSMessage message = new SMSMessage();
                message.address = cursor.getString(0);
                message.body = cursor.getString(1);
                message.date = cursor.getLong(2);
                message.read = getMessageRead(cursor.getInt(3));
                message.status = getMessageStatus(cursor.getInt(4));
                message.type = getMessageType(cursor.getInt(5));
                String id = cursor.getString(6);
                if ("已发出".equals(message.type)) {
                    message.person = "我";
                } else {
                    if (mPhoneMap.containsKey(id)) {
                        message.person = mPhoneMap.get(id);
                    }
                }
//                message.person = getPerson(message.address);
                list.add(message);
            }
            cursor.close();
        }
        if (list != null && list.size() != 0) {
            adapter.notifyDataSetChanged();
        }
    }

    private String getMessageRead(int anInt) {
        if (1 == anInt) {
            return "已读";
        }
        if (0 == anInt) {
            return "未读";
        }
        return null;
    }

    private String getMessageType(int anInt) {
        if (1 == anInt) {
            return "收到的";
        }
        if (2 == anInt) {
            return "已发出";
        }
        return "失败";
    }

    private String getMessageStatus(int anInt) {
        switch (anInt) {
            case -1:
                return "接收";
            case 0:
                return "complete";
            case 64:
                return "pending";
            case 128:
                return "failed";
            default:
                break;
        }
        return null;
    }

    private String getPerson(String address) {
        try {
            ContentResolver resolver = getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, address);
            Cursor cursor;

            cursor = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        String name = cursor.getString(0);
                        return name;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    class SMSMessage {
        long date;   //时间
        String address;  //手机号
        String body;   //内容
        String person;  //姓名
        String read;   //阅读状态
        String status;  //状态  暂时不用
        String type;   //类型

        @Override
        public String toString() {
            return "SMSMessage{" +
                    "date=" + formatDate(date) +
                    ", address='" + address + '\'' +
                    ", body='" + body + '\'' +
                    ", person='" + person + '\'' +
                    ", read='" + read + '\'' +
                    ", status='" + status + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        return format.format(new Date(time));
    }

    public class MsgAdapter extends BaseAdapter {

        List<SMSMessage> mList;
        Context mContext;

        public MsgAdapter(List<SMSMessage> list, Context context) {
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_msg, null);
                viewHolder = new ViewHolder();
                viewHolder.typeTv = convertView.findViewById(R.id.msg_type);
                viewHolder.nameTv = convertView.findViewById(R.id.msg_name);
                viewHolder.numberTv = convertView.findViewById(R.id.msg_number);
                viewHolder.dateTv = convertView.findViewById(R.id.msg_date);
                viewHolder.readTv = convertView.findViewById(R.id.msg_read);
                viewHolder.bodyTv = convertView.findViewById(R.id.msg_body);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SMSMessage msg = mList.get(position);
            viewHolder.typeTv.setText(msg.type);
            viewHolder.nameTv.setText(msg.person);
            viewHolder.numberTv.setText(msg.address);
            viewHolder.dateTv.setText(formatDate(msg.date));
            viewHolder.readTv.setText(msg.read);
            viewHolder.bodyTv.setText(msg.body);
            return convertView;
        }

        private class ViewHolder {
            TextView typeTv;
            TextView nameTv;
            TextView numberTv;
            TextView dateTv;
            TextView readTv;
            TextView bodyTv;
        }
    }

    public Map<String, String> getContacts() {
        ContentResolver resolver = getContentResolver();
        // 获得联系人手机号码
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{"name_raw_contact_id",
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},null, null, null);

        HashMap<String,String> phoneMap = new HashMap<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获得联系人姓名
                String name = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                // 获得联系人手机号
                String phoneNumber = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //获取联系人编号
                String id = String.valueOf(cursor.getLong(cursor
                        .getColumnIndex("name_raw_contact_id")));
                if(TextUtils.isEmpty(id)) continue;
                if (!phoneMap.containsKey(id)) {
                    phoneMap.put(id, name);
                }
            }
            cursor.close();
        }
        return phoneMap;
    }
}
