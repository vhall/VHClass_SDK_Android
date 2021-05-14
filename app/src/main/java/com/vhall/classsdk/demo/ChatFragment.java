package com.vhall.classsdk.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vhall.classsdk.demo.widget.ChatView;
import com.vhall.classsdk.service.ChatServer;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private Activity mActivity;
    private ChatView chatView;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_chat, null);
        chatView = view.findViewById(R.id.chat_view);
        chatView.init(mActivity);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {

    }


    public void updateData(ChatServer.ChatInfo chatInfo) {
        chatView.updateSource(chatInfo);
    }
}
