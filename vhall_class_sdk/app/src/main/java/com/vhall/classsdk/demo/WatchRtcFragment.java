package com.vhall.classsdk.demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.vhall.classsdk.ClassInfo;
import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.WatchRTC;
import com.vhall.classsdk.demo.utils.CommonUtils;
import com.vhall.classsdk.interfaces.ClassCallback;
import com.vhall.classsdk.interfaces.RequestCallback;
import com.vhall.classsdk.service.IConnectService;
import com.vhall.classsdk.service.MessageServer;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONObject;
import org.webrtc.RendererCommon;


public class WatchRtcFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = WatchRtcFragment.class.getName();

    private Context mContext;
    private WatchRTC vhrtc;
    private VHRenderView vhRenderView;
    private RelativeLayout mContainer;
    private LinearLayout mSubContainer;
    private ImageView mDowm, mCamera, mVideo, mAudio;
    private int width, height;
    private Stream localStream;

    boolean hasVideo = false;
    boolean hasAudio = false;
    private Handler mHandler = new Handler();
    private ClassInfo info;

    public static WatchRtcFragment newInstance() {
        return new WatchRtcFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        height = CommonUtils.dp2px(mContext, 90);
        width = CommonUtils.dp2px(mContext, 120);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_rtc, null);
        this.mContainer = view.findViewById(R.id.inter_root_container);
        this.mSubContainer = view.findViewById(R.id.inter_root_sub_container);
        this.mDowm = view.findViewById(R.id.inter_btn_down);
        this.mDowm.setOnClickListener(this);
        this.mCamera = view.findViewById(R.id.image_switch_camera);
        this.mCamera.setOnClickListener(this);
        this.mVideo = view.findViewById(R.id.image_switch_video);
        this.mVideo.setOnClickListener(this);
        this.mAudio = view.findViewById(R.id.image_switch_audio);
        this.mAudio.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        vhrtc = new WatchRTC(mContext, new VHRoomCallback());
        vhRenderView = new VHRenderView(mContext);
        vhRenderView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        vhRenderView.init(null, null);
        //TODO 常量定义
        localStream = vhrtc.createLocalStream(Stream.VhallFrameResolutionValue.VhallFrameResolution480x360.getValue(), "class SDK", 1);
        localStream.removeAllRenderView();
        localStream.addRenderView(vhRenderView);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(vhRenderView, 0, params);
        info = VHClass.getInstance().getInfo();
        VHClass.getInstance().addClassCallback(callback);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inter_btn_down:
                leave();
                break;
            case R.id.image_switch_camera://切换摄像头
                localStream.switchCamera();
                break;
            case R.id.image_switch_video: //关闭画面
                switchVideo();
                break;
            case R.id.image_switch_audio: //关闭声音
                switchAudio();
                break;
        }
    }

    private void leave() {
        vhrtc.leaveRoom();
    }

    private void switchAudio() {
        vhrtc.muteAudio(hasAudio ? WatchRTC.CAMERA_DEVICE_OPEN : WatchRTC.CAMERA_DEVICE_CLOSE, VHClass.getInstance().getJoinId(), new RequestCallback() {
            @Override
            public void onSuccess() {
                if (hasAudio) {
                    mAudio.setImageResource(R.mipmap.icon_audio_close);
                } else {
                    mAudio.setImageResource(R.mipmap.icon_audio_open);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
            }
        });
        hasAudio = !hasAudio;
    }

    private void switchVideo() {
        vhrtc.muteVideo(hasVideo ? WatchRTC.CAMERA_DEVICE_OPEN : WatchRTC.CAMERA_DEVICE_CLOSE, VHClass.getInstance().getJoinId(), new RequestCallback() {
            @Override
            public void onSuccess() {
                if (hasVideo) {
                    mVideo.setImageResource(R.mipmap.icon_video_close);
                } else {
                    mVideo.setImageResource(R.mipmap.icon_video_open);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
            }
        });
        hasVideo = !hasVideo;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void addStream(Stream stream) {
        if (stream == null) return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int height = CommonUtils.dp2px(mContext, 90);
                int with = CommonUtils.dp2px(mContext, 120);
                VHRenderView renderView = new VHRenderView(getContext());
                renderView.init(null, null);
                stream.removeAllRenderView();
                stream.addRenderView(renderView);
                renderView.setZOrderOnTop(true);
                final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(with, height);
                mSubContainer.addView(renderView, 0, params);
            }
        });

    }

    public void removeStream(Stream stream) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (stream == null) return;
                int childCount = mSubContainer.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    VHRenderView view = (VHRenderView) mSubContainer.getChildAt(i);
                    if (view.getStream().userId == stream.userId) {
                        view.release();
                        mSubContainer.removeView(view);
                        break;
                    }
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView ");
        VHClass.getInstance().removeClassCallback(callback);
        if (vhrtc == null) return;
        vhrtc.release();
        vhrtc = null;
    }

    public class VHRoomCallback implements Room.RoomDelegate {


        @Override
        public void onDidConnect(Room room, JSONObject jsonObject) {
            Log.e(TAG, "onDidConnect");
            for (Stream stream : room.getRemoteStreams()) {
                room.subscribe(stream);
            }
            vhrtc.publish();
        }

        @Override
        public void onDidError(Room room, Room.VHRoomErrorStatus vhRoomErrorStatus, String s) {
            Log.i(TAG, "onDidError");
            for (Stream stream : room.getRemoteStreams()) {
                removeStream(stream);
            }
        }

        @Override
        public void onDidPublishStream(Room room, Stream stream) {//上麦
            Log.e(TAG, "onDidPublishStream");
            /** 当本地推流成功后调用上麦成功接口*/
        }

        @Override
        public void onDidUnPublishStream(Room room, Stream stream) {//下麦
            Log.e(TAG, "onDidUnPublishStream");

        }

        @Override
        public void onDidSubscribeStream(Room room, Stream stream) {//订阅其他流
            Log.e(TAG, "onDidSubscribeStream");
            addStream(stream);
        }

        @Override
        public void onDidUnSubscribeStream(Room room, Stream stream) {
            Log.i(TAG, "onDidUnSubscribeStream");
            removeStream(stream);
        }

        @Override
        public void onDidChangeStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:// 异常退出
                    //TODO 销毁页面
                    break;
                case VHRoomStatusError:
                    Log.e(TAG, "VHRoomStatusError");
                    break;
                case VHRoomStatusReady:
                    Log.e(TAG, "VHRoomStatusReady");
                    break;
                case VHRoomStatusConnected: // 重连进房间
                    Log.e(TAG, "VHRoomStatusConnected");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDidAddStream(Room room, Stream stream) {
            Log.i(TAG, "onDidAddStream");
            room.subscribe(stream);
        }

        @Override
        public void onDidRemoveStream(Room room, Stream stream) {
            removeStream(stream);
        }

        @Override
        public void onDidUpdateOfStream(Stream stream, JSONObject jsonObject) {
            Log.i(TAG, "onDidUpdateOfStream");

        }

        @Override
        public void onReconnect(int i, int i1) {
            Log.e(TAG, "onReconnect" + i + " i1 " + i1);

        }

        @Override
        public void onStreamMixed(JSONObject jsonObject) {

        }

    }

    protected ClassCallback callback = new ClassCallback() {
        @Override
        public void onEvent(int eventCode, String msg) {

        }

        @Override
        public void onMessageReceived(MessageServer.MsgInfo msgInfo) {
            switch (msgInfo.event) {
                case IConnectService.EVENT_CLASS_VIDEO: // 关闭视频
                    if (msgInfo.target_id.equals(info.join.id)) {//关闭的是自己的画面
                        if (msgInfo.classStatus.equals("0")) {
                            localStream.unmuteVideo(null);
                            mVideo.setImageResource(R.mipmap.icon_video_open);

                        } else {
                            localStream.muteVideo(null);
                            mVideo.setImageResource(R.mipmap.icon_video_close);
                        }
                    }
                    break;
                case IConnectService.EVENT_CLASS_AUDIO: // 关闭音频
                    if (msgInfo.target_id.equals(info.join.id)) {//关闭的是自己的音频
                        if (msgInfo.classStatus.equals("0")) {
                            localStream.unmuteAudio(null);
                            mAudio.setImageResource(R.mipmap.icon_audio_open);
                        } else {
                            localStream.muteAudio(null);
                            mAudio.setImageResource(R.mipmap.icon_audio_close);
                        }
                    }
                    break;
                case IConnectService.EVENT_CLASS_AUDIO_ALL: // 全体静音
                    if (info.join.role_type.equals("2")) {
                        if (msgInfo.classStatus.equals("0")) {
                            localStream.unmuteAudio(null);
                            mAudio.setImageResource(R.mipmap.icon_audio_open);
                        } else {
                            localStream.muteAudio(null);
                            mAudio.setImageResource(R.mipmap.icon_audio_close);

                        }
                    }
                    break;
                case IConnectService.EVENT_CLASS_PREPARE_MICS: // 预上下麦
                    if (msgInfo.classStatus.equals("1")) {//预上麦
                    } else {
                        vhrtc.publish();
                    }
                    break;
                case IConnectService.EVENT_CLASS_SWITCH_MIC:
                    Log.e(TAG, "onMessageReceived: " + msgInfo.toString());
                    break;
            }
        }

        @Override
        public void onError(int eventCode, String msg) {

        }
    };
}
