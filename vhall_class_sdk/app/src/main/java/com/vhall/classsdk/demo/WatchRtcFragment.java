package com.vhall.classsdk.demo;

import android.content.Context;
import android.graphics.PixelFormat;
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

import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.WatchRTC;
import com.vhall.classsdk.demo.utils.CommonUtils;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhrtc.RoomCallback;
import com.vhall.vhrtc.VHRTC;


public class WatchRtcFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = WatchRtcFragment.class.getName();

    private Context mContext;
    private WatchRTC vhrtc;
    private VHRenderView vhRenderView;
    private RelativeLayout mContainer;
    private LinearLayout mSubContainer;
    private ImageView mDowm, mCamera, mVideo, mAudio;
    private int width, height;

    boolean hasVideo = false;
    boolean hasAudio = false;

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
        vhRenderView.setScalingMode(VHRenderView.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
        vhRenderView.init(vhrtc.getEglBase().getEglBaseContext(), null);
        //TODO 常量定义
        vhrtc.setLocalView(vhRenderView, VHRTC.VHRTC_STREAM_AUDIO_AND_VIDEO);
        addLocalView(vhRenderView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inter_btn_down:
                leave();
                break;
            case R.id.image_switch_camera://切换摄像头
                vhrtc.switchCamera();
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
        vhrtc.switchDevice(WatchRTC.CAMERA_AUDIO, hasAudio ? WatchRTC.CAMERA_DEVICE_OPEN : WatchRTC.CAMERA_DEVICE_CLOSE, new VHClass.RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
            }
        });
        hasAudio = !hasAudio;
    }

    private void switchVideo() {
        vhrtc.switchDevice(WatchRTC.CAMERA_VIDEO, hasVideo ? WatchRTC.CAMERA_DEVICE_OPEN : WatchRTC.CAMERA_DEVICE_CLOSE, new VHClass.RequestCallback() {
            @Override
            public void onSuccess() {
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

    public void addLocalView(VHRenderView vhRenderView) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(vhRenderView, 0, params);
    }

    public void addStream(VHRenderView vhRenderView) {
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        this.mSubContainer.addView(vhRenderView, 0, params);
    }

    public void removeStream(Stream stream) {
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView ");
        if (vhrtc == null) return;
        vhrtc.destory();
        vhrtc = null;
    }

    public class VHRoomCallback implements RoomCallback {

        @Override
        public void onDidConnect() {
            Log.e(TAG, "onDidConnect");
            vhrtc.publish();
        }

        @Override
        public void onDidError() {
            Log.e(TAG, "onDidError");
        }

        @Override
        public void onDidPublishStream() {// 上麦
            Log.e(TAG, "onDidPublishStream");
            /** 当本地推流成功后调用上麦成功接口*/
        }

        @Override
        public void onDidUnPublishStream() { //下麦
            Log.e(TAG, "onDidUnPublishStream");
        }

        @Override
        public void onDidSubscribeStream(Stream stream, final VHRenderView newRenderView) {
            Log.e(TAG, "onDidSubscribeStream");
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    newRenderView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                    newRenderView.setZOrderMediaOverlay(true);
                    newRenderView.setScalingMode(VHRenderView.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                    addStream(newRenderView);
                }
            });
        }

        @Override
        public void onDidRoomStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
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
        public void onDidRemoveStream(Room room, Stream stream) {
            removeStream(stream);
        }
    }

}
