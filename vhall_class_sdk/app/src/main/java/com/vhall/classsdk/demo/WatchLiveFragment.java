package com.vhall.classsdk.demo;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.base.IVHPlayer;
import com.vhall.base.IVHWatchCallBack;
import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.WatchLive;
import com.vhall.classsdk.demo.utils.CommonUtils;
import com.vhall.classsdk.demo.widget.CircleView;
import com.vhall.watchlive.play.BasicVideoPlayer;
import com.vhall.watchlive.play.IVideoPlayer;

import org.json.JSONArray;
import org.json.JSONException;


public class WatchLiveFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = WatchLiveFragment.class.getName();
    private CountDownTimer onHandDownTimer;
    private Context mContext;
    private BasicVideoPlayer player;
    private ImageView mImagePlayer;
    private ImageView mDrawMode, mBack;
    private CircleView mHand;
    private TextView mDownLoad;
    private LinearLayout mLinearButtonContainer;

    private WatchLive vhClassLive;
    private int[] drawModes = new int[]{IVideoPlayer.DRAW_MODE_NONE, IVideoPlayer.DRAW_MODE_ASPECTFIT, IVideoPlayer.DRAW_MODE_ASPECTFILL};
    private int drawMode = IVideoPlayer.DRAW_MODE_ASPECTFIT;
    private int durationSec = 30; // 举手上麦倒计时

    public static WatchLiveFragment newInstance() {
        return new WatchLiveFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_live, null);
        player = view.findViewById(R.id.player);
        mImagePlayer = view.findViewById(R.id.image_play);
        mImagePlayer.setOnClickListener(this);
        mDownLoad = view.findViewById(R.id.text_download);
        mDrawMode = view.findViewById(R.id.switch_draw_mode);
        mDrawMode.setOnClickListener(this);
        mHand = view.findViewById(R.id.watch_live_openhand);
        mHand.setOnClickListener(this);
        mLinearButtonContainer = view.findViewById(R.id.linear_button_container_live);
        mBack = view.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (VHClass.getInstance().getIsHand()) {
            mHand.setVisibility(View.VISIBLE);
        } else
            mHand.setVisibility(View.GONE);
        WatchLive.Builder builder = new WatchLive.Builder();
        builder.videoPlayer(player)
                .defaultDPI(IVHPlayer.DPI_SAME)
                .connectTimeout(10000)
                .callback(new LiveCallback());
        vhClassLive = builder.build(mContext);
        vhClassLive.setDrawMode(IVideoPlayer.DRAW_MODE_ASPECTFIT);
        vhClassLive.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_play:
                if (vhClassLive.getPlaying()) {
                    vhClassLive.stop();
                } else
                    vhClassLive.start();
                break;
            case R.id.switch_draw_mode://设置观看模式
                drawMode = drawModes[(++drawMode) % drawModes.length];
                vhClassLive.setDrawMode(drawMode);
                break;
            case R.id.watch_live_openhand: // 举手
                sendHandMsg();
                break;
            case R.id.back:
                this.getActivity().finish();
                break;
        }
    }

    private void sendHandMsg() {
        if (durationSec == 30) {
            vhClassLive.hand(new VHClass.RequestCallback() {
                @Override
                public void onSuccess() {
                    startDownTimer(durationSec);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {

                }
            });
        } else {
            Toast.makeText(mContext, "请勿重复点击", Toast.LENGTH_SHORT).show();
        }
    }

    public void startDownTimer(int secondTimer) {
        onHandDownTimer = new CountDownTimer(secondTimer * 1000 + 1080, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mHand.setTextAndInvalidate((int) millisUntilFinished / 1000 - 1);
                durationSec = (int) millisUntilFinished / 1000 - 1;
            }

            @Override
            public void onFinish() {
                onHandDownTimer.cancel();
                durationSec = 30;
            }
        }.start();
    }

    public void setWatchStatus(int status) {
        switch (status) {
            case IVHPlayer.EVENT_STATUS_STARTED:
                mImagePlayer.setImageResource(R.mipmap.vhall_icon_live_pause);
                break;
            case IVHPlayer.EVENT_STATUS_STOPED:
                mImagePlayer.setImageResource(R.mipmap.vhall_icon_live_play);
                break;
        }
    }

    public void setDownBuffer(String buffer) {
        mDownLoad.setText(buffer);
    }


    /**
     * 发送拒绝消息
     * 在接收到老师的邀请后允许拒绝
     */
    public void sendRefuseCmd() {
        vhClassLive.sendRefuseCmd(new VHClass.RequestCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(mContext, "" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void switchHand(String classStatus) {
        if (classStatus.equals("1")) {
            mHand.setVisibility(View.VISIBLE);
        } else
            mHand.setVisibility(View.GONE);
    }

    public void openShareScreen() {

    }

    public class LiveCallback implements IVHWatchCallBack {

        @Override
        public void onEvent(int event, String msg) {
            Log.e(TAG, "event = " + event + " msg = " + msg);
            switch (event) {
                case IVHPlayer.EVENT_STATUS_STARTING:
                    break;
                case IVHPlayer.EVENT_STATUS_STARTED:
                    setWatchStatus(IVHPlayer.EVENT_STATUS_STARTED);
                    break;
                case IVHPlayer.EVENT_STATUS_STOPED:
                    setWatchStatus(IVHPlayer.EVENT_STATUS_STOPED);
                    break;
                case IVHPlayer.EVENT_SUPPORT_DPI://当前可用的分辨率
                    Log.e(TAG, "DPI:" + msg);
                    try {
                        JSONArray array = new JSONArray(msg);
                        mLinearButtonContainer.removeAllViews();
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                String dpi = (String) array.opt(i);
                                ImageView imageView = new ImageView(getContext());
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dp2px(mContext, 35), CommonUtils.dp2px(mContext, 35));
                                imageView.setLayoutParams(layoutParams);
                                if (dpi.equals(IVHPlayer.DPI_SAME)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution);
                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            vhClassLive.setDPI(IVHPlayer.DPI_SAME);
                                        }
                                    });
                                } else if (dpi.equals(IVHPlayer.DPI_LDR)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution_sd);
                                } else if (dpi.equals(IVHPlayer.DPI_SDR)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution_hd);
                                } else if (dpi.equals(IVHPlayer.DPI_HDR)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution_uhd);
                                } else if (dpi.equals(IVHPlayer.DPI_AUDIO)) {
                                    imageView.setImageResource(R.mipmap.icon_audio_open);
                                }
                                mLinearButtonContainer.addView(imageView);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case IVHPlayer.EVENT_DPI_CHANGED:
                    showToast(" DPI : " + msg);
                    break;
                case IVHPlayer.EVENT_VIDEO_SIZE_CHANGED: //获取到视频的尺寸
                    break;
                case IVHPlayer.EVENT_DOWNLOAD_SPEED:
                    setDownBuffer(msg + "kb/s");
                    break;
            }
        }

        @Override
        public void onError(int errorCode, String errorMsg) {
            switch (errorCode) {
                case IVHPlayer.ERROR_CONNECT:
                    showToast("ERROR : " + errorMsg);
                    break;
            }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public void release() {
        if (vhClassLive != null) {
            vhClassLive.release();
            vhClassLive = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG , "watchLive destory  " );
        super.onDestroy();
        release();
    }
}
