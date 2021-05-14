package com.vhall.classsdk.demo.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.demo.R;
import com.vhall.classsdk.demo.utils.emoji.InputUser;
import com.vhall.classsdk.demo.utils.emoji.InputView;
import com.vhall.classsdk.demo.utils.emoji.KeyBoardManager;
import com.vhall.classsdk.interfaces.RequestCallback;
import com.vhall.classsdk.service.ChatServer;

import java.util.ArrayList;
import java.util.List;

public class ChatView extends RelativeLayout implements View.OnClickListener {
    private Context context;
    private ListView mChatList;
    private ChatAdapter chatAdapter;
    private InputView inputView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout mView;
    private Activity activity;

    public ChatView(Context context) {
        this(context, null);
    }

    public ChatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void init(Activity activity) {
        this.activity = activity;
        View root = LayoutInflater.from(context).inflate(R.layout.chat_view, this, true);
        mView = root.findViewById(R.id.widget_layout_softboard);
        mView.setOnClickListener(this);
        root.findViewById(R.id.widget_layout_emo).setOnClickListener(this);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getWebinarChat(20);
                        swipeRefresh.setRefreshing(false);
                    }
                }, 500);
            }
        });
        mChatList = root.findViewById(R.id.lv_chat);
        chatAdapter = new ChatAdapter(context, new ArrayList<ChatServer.ChatInfo>());
        mChatList.setAdapter(chatAdapter);

        inputView = new InputView(context, KeyBoardManager.getKeyboardHeight(context), KeyBoardManager.getKeyboardHeightLandspace(context));
        inputView.add2Window(activity);
        inputView.setClickCallback(new InputView.ClickCallback() {
            @Override
            public void onEmojiClick() {

            }
        });
        inputView.setOnSendClickListener(new InputView.SendMsgClickListener() {
            @Override
            public void onSendClick(String msg, InputUser user) {
                if (TextUtils.isEmpty(msg.trim())) {
                    Toast.makeText(context, "请输入您要发送的内容", Toast.LENGTH_SHORT).show();
                } else {
                    send(msg);
                }
            }
        });
        inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
            @Override
            public void onHeightReceived(int screenOri, int height) {
                if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    KeyBoardManager.setKeyboardHeight(context, height);
                } else {
                    KeyBoardManager.setKeyboardHeightLandspace(context, height);
                }
            }
        });
        getWebinarChat(20);
    }

    public void send(String msg) {
        VHClass.getInstance().sendChat(msg, new RequestCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(context, "发送失败 " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getWebinarChat(int num) {
        VHClass.getInstance().getChat(num, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(int totalPage,int curPage,List<ChatServer.ChatInfo> newList) {
                if (newList.size() > 0) {
                    chatAdapter.updateDataSourceList(newList, false);
                }
            }

            @Override
            public void onError(int errorCode, String messaage) {

            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && inputView.getContentView().getVisibility() == View.VISIBLE) {
            boolean isDismiss = isShouldHideInput(inputView.getContentView(), ev);
            if (isDismiss) {
                inputView.dismiss();
                return false;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View view, MotionEvent event) {
        if (view.getVisibility() == View.GONE)
            return false;
        int[] leftTop = {0, 0};
        //获取输入框当前的location位置
        inputView.getContentView().getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + inputView.getContentView().getHeight();
        int right = left + inputView.getContentView().getWidth();
        return !(event.getX() > left && event.getX() < right
                && event.getY() > top && event.getY() < bottom);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.widget_layout_softboard: // 点击显示聊天框
                inputView.show(false, null);
                break;
            case R.id.widget_layout_emo:
                inputView.show(true, null);
                break;
        }
    }

    public void updateSource(ChatServer.ChatInfo chatInfo) {
        chatAdapter.updateDataSource(chatInfo);
    }
}

