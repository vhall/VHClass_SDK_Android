package com.vhall.classsdk.demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.base.IVHPlayer;
import com.vhall.base.IVHWatchCallBack;
import com.vhall.classsdk.WatchVod;
import com.vhall.classsdk.demo.utils.CommonUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

import static com.vhall.base.IVHPlayer.DPI_SAME;

public class WatchPlayBackFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "WatchBackActivity";
    private Context mContext;

    private SurfaceView mSurfaceview;
    private SeekBar mSeekbar;
    private ImageView mStart, mBack;
    private TextView mCurrentTime, mMaxTime;
    private WatchVod watchPlayBack;
    private Timer timer;
    private LinearLayout mLinearButtonContainer;

    private long mCurrentPosition = 0L;
    private long mBufferPosition = 0L;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: // 每秒更新SeekBar
                    if (watchPlayBack.getStatus() != IVHPlayer.STATE_IDLE) {
                        mCurrentPosition = watchPlayBack.getCurrentPosition();
                        mSeekbar.setProgress((int) mCurrentPosition);
                        mBufferPosition = watchPlayBack.getbufferMS();
                        mSeekbar.setSecondaryProgress((int) mBufferPosition);
                    }
                    break;
            }
        }
    };


    public static WatchPlayBackFragment newInstance() {
        return new WatchPlayBackFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_watch_playback, null);
        mSurfaceview = rootView.findViewById(R.id.surfaceview);
        mSeekbar = rootView.findViewById(R.id.seekbar);
        mSeekbar.setOnSeekBarChangeListener(new SeekbarListener());
        mStart = rootView.findViewById(R.id.frame_class_back_player);
        mStart.setOnClickListener(this);
        mCurrentTime = rootView.findViewById(R.id.tv_current_time);
        mMaxTime = rootView.findViewById(R.id.tv_end_time);
        mLinearButtonContainer = rootView.findViewById(R.id.linear_button_container_back);
        mBack = rootView.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WatchVod.Builder builder = new WatchVod.Builder();
        builder.context(mContext)
                .surfaceView(mSurfaceview)
                .isReplay(false)
                .callback(new BackCallBack());
        watchPlayBack = builder.build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frame_class_back_player:
                Log.e(TAG, "watchBack.getStatus() == " + watchPlayBack.getStatus());
                if (watchPlayBack.getStatus() == IVHPlayer.STATE_IDLE) {
                    watchPlayBack.start();
                } else if (watchPlayBack.getStatus() != IVHPlayer.STATE_IDLE && watchPlayBack.getPlaying()) {
                    watchPlayBack.pause();
                } else if (watchPlayBack.getStatus() != IVHPlayer.STATE_IDLE && !watchPlayBack.getPlaying()) {
                    watchPlayBack.resume();
                }
                break;
            case R.id.back:
                this.getActivity().finish();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        watchPlayBack.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        watchPlayBack.resume();
    }

    private void handlePosition() {
        if (timer != null)
            return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    class SeekbarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mCurrentTime.setText(CommonUtils.converLongTimeToStr(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            watchPlayBack.seekTo(seekBar.getProgress());
        }
    }

    private class BackCallBack implements IVHWatchCallBack {

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                //统一维护五个播放器状态
                case IVHPlayer.STATE_IDLE:
                    break;
                case IVHPlayer.STATE_PREPARING:
                    break;
                case IVHPlayer.STATE_BUFFERING:
                    break;
                case IVHPlayer.STATE_READY:
                    handlePosition(); //TODO
                    if (watchPlayBack.getPlaying()) {
                        int max = (int) watchPlayBack.getDuration();
                        mSeekbar.setMax(max);
                        mSeekbar.setEnabled(true);
                        mMaxTime.setText(CommonUtils.converLongTimeToStr(max));
                        mStart.setImageResource(R.mipmap.vhall_icon_live_pause);
                    } else
                        mStart.setImageResource(R.mipmap.vhall_icon_live_play);
                    break;
                case IVHPlayer.STATE_ENDED:
                    mStart.setImageResource(R.mipmap.vhall_icon_live_play);
                    break;
                //维护Event状态
                case IVHPlayer.EVENT_DPI_CHANGED: // 每当回调次Event 播放器重新播放数据源地址并切换分辨率
                    Log.e(TAG, "EVENT_DPI_CHANGED = " + msg);
                    break;
                case IVHPlayer.EVENT_VIDEO_SIZE_CHANGED: // 视频大小发生改变回调
                    Log.e(TAG, "EVENT_VIDEO_SIZE_CHANGED = " + msg);
                    break;
                case IVHPlayer.EVENT_SUPPORT_DPI://返回支持的分辨率
                    Log.e(TAG, "EVENT_SUPPORT_DPI = " + msg);
                    try {
                        JSONArray array = new JSONArray(msg);
                        mLinearButtonContainer.removeAllViews();
                        if (array != null && array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                String dpi = (String) array.opt(i);
                                ImageView imageView = new ImageView(getContext());
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dp2px(mContext, 30), CommonUtils.dp2px(mContext, 30));
                                imageView.setLayoutParams(layoutParams);
                                if (dpi.equals(IVHPlayer.DPI_SAME)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution);
                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            watchPlayBack.setDPI(DPI_SAME);
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
            }
        }

        @Override
        public void onError(int errorCode, String errorMsg) {
            Toast.makeText(mContext, "errorCode = " + errorCode + " errorMsg = " + errorMsg, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroy");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        watchPlayBack.release();
        watchPlayBack = null;
    }

}
