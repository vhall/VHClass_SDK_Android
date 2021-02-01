package com.vhall.classsdk.demo.widget;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.demo.R;
import com.vhall.classsdk.demo.utils.emoji.EmojiUtils;
import com.vhall.classsdk.service.ChatServer;
import com.vhall.classsdk.service.IConnectService;
import com.vhall.classsdk.utils.Constant;

import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private List<ChatServer.ChatInfo> dataSourceList;
    public static final int CLASS_EVENT_CHAT = 0x00;

    public ChatAdapter(Context context, List<ChatServer.ChatInfo> dataSourceList) {
        this.dataSourceList = dataSourceList;
        this.context = context;
    }

    public void updateDataSourceList(List<ChatServer.ChatInfo> newData, boolean isClear) {
        if (isClear)
            dataSourceList.clear();
        dataSourceList.addAll(0, newData);
        notifyDataSetChanged();
    }

    public void updateDataSource(ChatServer.ChatInfo newInfo) {
        dataSourceList.add(newInfo);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return CLASS_EVENT_CHAT;
    }

    @Override
    public int getCount() {
        return dataSourceList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSourceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ChatServer.ChatInfo data = dataSourceList.get(position);
        switch (getItemViewType(position)) {
            case CLASS_EVENT_CHAT:
                final DataSourceViewHolder dataSourceViewHolder;
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.chat_item, null);
                    dataSourceViewHolder = new DataSourceViewHolder();
                    dataSourceViewHolder.iv_chat_avatar = convertView.findViewById(R.id.iv_chat_avatar);
                    dataSourceViewHolder.iv_chat_avatar_self = convertView.findViewById(R.id.iv_chat_avatar_self);
                    dataSourceViewHolder.tv_chat_content = convertView.findViewById(R.id.tv_chat_content);
                    dataSourceViewHolder.tv_chat_content_self = convertView.findViewById(R.id.tv_chat_content_self);
                    dataSourceViewHolder.tv_chat_name = convertView.findViewById(R.id.tv_chat_name);
                    dataSourceViewHolder.tv_chat_name_self = convertView.findViewById(R.id.tv_chat_name_self);
                    dataSourceViewHolder.image_chat_role = convertView.findViewById(R.id.image_chat_role);
                    dataSourceViewHolder.mContainer = convertView.findViewById(R.id.layout_chat_left);
                    dataSourceViewHolder.mContainerSelf = convertView.findViewById(R.id.layout_chat_right);
                    dataSourceViewHolder.image_chat_role_self = convertView.findViewById(R.id.image_chat_role_self);
                    convertView.setTag(dataSourceViewHolder);
                } else {
                    dataSourceViewHolder = (DataSourceViewHolder) convertView.getTag();
                }
                switch (data.event) {
                    case IConnectService.eventMsgKey:
                        if (dataSourceList.get(position).role == Constant.USER_ROLE_TEACHER) { // 老师
                            dataSourceViewHolder.mContainer.setVisibility(View.VISIBLE);
                            dataSourceViewHolder.mContainerSelf.setVisibility(View.GONE);
                            dataSourceViewHolder.image_chat_role.setVisibility(View.VISIBLE);
                            dataSourceViewHolder.image_chat_role.setImageResource(R.drawable.vhall_class_techer);
                            dataSourceViewHolder.tv_chat_content.setText(EmojiUtils.getEmojiText(context, data.msgData.text), TextView.BufferType.SPANNABLE);
                            dataSourceViewHolder.tv_chat_name.setText(data.user_name);
                        } else if (dataSourceList.get(position).role == Constant.USER_ROLE_STUDENT) {
                            if (dataSourceList.get(position).userId.equals(VHClass.getInstance().getJoinId())) {// 学生 自己
                                dataSourceViewHolder.mContainer.setVisibility(View.GONE);
                                dataSourceViewHolder.mContainerSelf.setVisibility(View.VISIBLE);
                                dataSourceViewHolder.image_chat_role_self.setVisibility(View.GONE);
                                dataSourceViewHolder.tv_chat_content_self.setText(EmojiUtils.getEmojiText(context, data.msgData.text), TextView.BufferType.SPANNABLE);
                                dataSourceViewHolder.tv_chat_name_self.setText(data.user_name);
                            } else {// 学生 不是自己
                                dataSourceViewHolder.mContainer.setVisibility(View.VISIBLE);
                                dataSourceViewHolder.mContainerSelf.setVisibility(View.GONE);
                                dataSourceViewHolder.image_chat_role.setVisibility(View.GONE);
                                dataSourceViewHolder.tv_chat_content.setVisibility(View.VISIBLE);
                                dataSourceViewHolder.tv_chat_content.setText(EmojiUtils.getEmojiText(context, data.msgData.text), TextView.BufferType.SPANNABLE);
                                dataSourceViewHolder.tv_chat_name.setText(data.user_name);
                            }
                        }
                        break;
                    case IConnectService.eventOnlineKey:
                        dataSourceViewHolder.tv_chat_name.setText(data.user_name + "上线了！");
                        dataSourceViewHolder.tv_chat_content.setVisibility(View.INVISIBLE);
                        break;
                    case IConnectService.eventOfflineKey:
                        dataSourceViewHolder.tv_chat_name.setText(data.user_name + "下线了！");
                        dataSourceViewHolder.tv_chat_content.setVisibility(View.INVISIBLE);
                        break;
                }
                break;
        }
        return convertView;
    }

    public void clearData() {
        dataSourceList.clear();
        notifyDataSetChanged();
    }

    static class DataSourceViewHolder {
        ImageView iv_chat_avatar;
        ImageView iv_chat_avatar_self;
        ImageView image_chat_role;
        ImageView image_chat_role_self;
        TextView tv_chat_content;
        TextView tv_chat_content_self;
        TextView tv_chat_name;
        TextView tv_chat_name_self;
        RelativeLayout mContainer;
        RelativeLayout mContainerSelf;
    }

}