package com.vhall.classsdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.WatchRTC;
import com.vhall.classsdk.demo.widget.CheckInterDialog;
import com.vhall.classsdk.interfaces.ClassCallback;
import com.vhall.classsdk.service.ChatServer;
import com.vhall.classsdk.service.IConnectService;
import com.vhall.classsdk.service.MessageServer;
import com.vhall.classsdk.utils.Constant;

public class FunctionActivity extends AppCompatActivity {
    private static final String TAG = "FunctionActivity";
    int function;
    private FragmentManager mFragmanager;
    private WatchLiveFragment mLiveFrag;
    private WatchPlayBackFragment mBackFrag;
    private WatchRtcFragment mRTCFrag;
    private ChatFragment chatFragment;
    private DocumentFragment documentFragment;
    private MyClassCallback classCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        function = getIntent().getIntExtra("fuc", MainActivity.FUC_LIVE);
        mFragmanager = getSupportFragmentManager();
        setContentView(R.layout.activity_function);
        showFunction(function);
        classCallback = new MyClassCallback();
        VHClass.getInstance().addClassCallback(classCallback);
    }

    //根据功能展示对应Fragmeng
    public void showFunction(int function) {
        int classStatus = -1;
        switch (function) {
            case MainActivity.FUC_LIVE:
                mLiveFrag = WatchLiveFragment.newInstance();
                mFragmanager.beginTransaction().replace(R.id.container, mLiveFrag).commit();
                break;
            case MainActivity.FUC_PLAYBACK:
                mBackFrag = WatchPlayBackFragment.newInstance();
                mFragmanager.beginTransaction().replace(R.id.container, mBackFrag).commit();
                break;
            case MainActivity.FUC_INTERACTIVE:
                mRTCFrag = WatchRtcFragment.newInstance();
                mFragmanager.beginTransaction().replace(R.id.container, mRTCFrag).commit();
                break;
            case MainActivity.FUC_CHAT:
                chatFragment = ChatFragment.newInstance();
                mFragmanager.beginTransaction().replace(R.id.container, chatFragment).commit();
                VHClass.getInstance().joinChat(new ChatCallback());
                break;
            case MainActivity.FUC_DOC:
                documentFragment = DocumentFragment.newInstance();
                mFragmanager.beginTransaction().replace(R.id.container, documentFragment).commit();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VHClass.getInstance().removeClassCallback(classCallback);
        VHClass.getInstance().leaveClass();
    }

    private class ChatCallback implements ChatServer.Callback {

        @Override
        public void onChatServerConnected() {

        }

        @Override
        public void onConnectFailed(String msg) {

        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            switch (chatInfo.event) {
                case IConnectService.eventMsgKey:
                    if (chatFragment != null)
                        chatFragment.updateData(chatInfo);
                    break;
            }
        }

        @Override
        public void onChatServerClosed() {

        }
    }

    private class MyClassCallback implements ClassCallback {

        @Override
        public void onEvent(int eventCode, String msg) {

        }

        @Override
        public void onMessageReceived(MessageServer.MsgInfo msgInfo) {
            Log.e(TAG, "msgInfo.event = " + msgInfo.event);
            switch (msgInfo.event) {
                case IConnectService.EVENT_CLASS_PREPARE_MICS://预上麦
                    if (msgInfo.classStatus.equals(WatchRTC.VHCLASS_RTC_MIC_UP)) {//
                        showMicDialog();
                    } else {
                        if (mRTCFrag != null && msgInfo.target_id.equals(VHClass.getInstance().getJoinId())) {
                            mFragmanager.beginTransaction().remove(mRTCFrag).commit();
                            mLiveFrag = WatchLiveFragment.newInstance();
                            mFragmanager.beginTransaction().replace(R.id.container, mLiveFrag).commit();
                        }
                    }
                    break;
                case IConnectService.EVENT_KICKOUT:
                    Toast.makeText(FunctionActivity.this, "您已被提出", Toast.LENGTH_SHORT).show();
                    VHClass.getInstance().leaveClass();
                    finish();
                    Intent intent = new Intent(FunctionActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                case IConnectService.EVENT_OVER:
                    Toast.makeText(FunctionActivity.this, "已下课", Toast.LENGTH_SHORT).show();
                    if (VHClass.getInstance().getClassStatus() == Constant.CLASS_STATUS_STOP) {
                        if (mLiveFrag != null) {
                            mFragmanager.beginTransaction().remove(mLiveFrag).commit();
                        }
                        if (mRTCFrag != null)
                            mFragmanager.beginTransaction().remove(mRTCFrag).commit();
                    }
                    break;
                case IConnectService.EVENT_CLASS_SWITCH_MIC:
                    if (msgInfo.target_id.equals(VHClass.getInstance().getJoinId())) {
                        if (msgInfo.classStatus.equals(WatchRTC.VHCLASS_RTC_MIC_UP)) {//
                        } else { // 下麦
                            if (mRTCFrag != null && msgInfo.target_id.equals(VHClass.getInstance().getJoinId())) {
                                mFragmanager.beginTransaction().remove(mRTCFrag).commit();
                                mLiveFrag = WatchLiveFragment.newInstance();
                                mFragmanager.beginTransaction().replace(R.id.container, mLiveFrag).commit();
                            }
                        }
                    }
                    break;
                case MessageServer.EVENT_CLASS_OPEN_HAND://公开课专属 , 是否举手
                    if (mLiveFrag != null)
                        mLiveFrag.switchHand(msgInfo.classStatus);
                    break;
                case MessageServer.EVENT_CLASS_OPEN_SCREENSHARE:
                    if (VHClass.getInstance().getClassStatus() == 1) {
                        if (mLiveFrag != null) {
                            mLiveFrag.openShareScreen();
                        }
                    }
                    break;
                case MessageServer.CLASS_EVENT_START: // 上课
                    Toast.makeText(FunctionActivity.this, "上课了", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onError(int eventCode, String msg) {
            Toast.makeText(FunctionActivity.this, "" + msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 互动显示邀请上麦弹窗
     */
    public void showMicDialog() {
        final CheckInterDialog dialog = new CheckInterDialog(this);
        dialog.setClickCheckListener(new CheckInterDialog.ClickCheckListener() {
            @Override
            public void onAllow() {
                mRTCFrag = WatchRtcFragment.newInstance();
                mFragmanager.beginTransaction().replace(R.id.container, mRTCFrag).commit();
                dialog.dismiss();
            }

            @Override
            public void onRefuse() {
                //TODO 需要发送CMD消息通知PC端
                if (mLiveFrag != null) {
                    mLiveFrag.sendRefuseCmd();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
