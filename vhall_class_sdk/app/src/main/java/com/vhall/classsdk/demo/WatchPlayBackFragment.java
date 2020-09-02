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

import com.vhall.classsdk.WatchVod;
import com.vhall.classsdk.demo.utils.CommonUtils;
import com.vhall.jni.VhallLiveApi;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

import static com.vhall.player.Constants.Event;
import static com.vhall.player.Constants.Rate;
import static com.vhall.player.Constants.State;

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

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // 每秒更新SeekBar
                    if (watchPlayBack.getState() != State.IDLE) {
                        mCurrentPosition = watchPlayBack.getPosition();
                        mSeekbar.setProgress((int) mCurrentPosition);
                        mBufferPosition = watchPlayBack.getBufferPosition();
                        mSeekbar.setSecondaryProgress((int) mBufferPosition);
                    }
                    break;
            }
            return false;
        }
    });


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
        VhallLiveApi.EnableDebug(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        watchPlayBack = new WatchVod(mContext);
        watchPlayBack.setDisplay(mSurfaceview);
        watchPlayBack.setListener(new BackCallBack());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frame_class_back_player:
                Log.e(TAG, "watchBack.getStatus() == " + watchPlayBack.getState());
                if (watchPlayBack.getState() == State.IDLE) {
                    watchPlayBack.start();
                } else if (watchPlayBack.getState() == State.START) {
                    watchPlayBack.pause();
                } else if (watchPlayBack.getState() == State.STOP) {
                    watchPlayBack.resume();
                } else if (watchPlayBack.getState() == State.END) {
                    watchPlayBack.seekto(0);
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
            watchPlayBack.seekto(seekBar.getProgress());
        }
    }

    private class BackCallBack implements VHPlayerListener {

        @Override
        public void onStateChanged(State state) {
            switch (state) {
                //统一维护五个播放器状态
                case IDLE:
                    break;
                case BUFFER:
                    break;

                case START:
                    handlePosition();
                    int max = (int) watchPlayBack.getDuration();
                    mSeekbar.setMax(max);
                    mSeekbar.setEnabled(true);
                    mMaxTime.setText(CommonUtils.converLongTimeToStr(max));
                    mStart.setImageResource(R.mipmap.vhall_icon_live_pause);
                    break;
                case STOP:
                case END:
                    mStart.setImageResource(R.mipmap.vhall_icon_live_play);
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {

                //维护Event状态
                case Event.EVENT_DPI_CHANGED: // 每当回调次Event 播放器重新播放数据源地址并切换分辨率
                    Log.e(TAG, "EVENT_DPI_CHANGED = " + msg);
                    break;
                case Event.EVENT_VIDEO_SIZE_CHANGED: // 视频大小发生改变回调
                    Log.e(TAG, "EVENT_VIDEO_SIZE_CHANGED = " + msg);
                    break;
                case Event.EVENT_DPI_LIST://返回支持的分辨率
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
                                if (dpi.equals(Constants.Rate.DPI_SAME)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution);
                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            watchPlayBack.setDPI(Rate.DPI_SAME);
                                        }
                                    });
                                } else if (dpi.equals(Rate.DPI_SD)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution_sd);
                                } else if (dpi.equals(Rate.DPI_HD)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution_hd);
                                } else if (dpi.equals(Rate.DPI_XHD)) {
                                    imageView.setImageResource(R.mipmap.vhall_icon_resolution_uhd);
                                } else if (dpi.equals(Constants.Rate.DPI_AUDIO)) {
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
        public void onError(int i, int i1, String s) {
            Toast.makeText(mContext, "errorCode = " + i + " errorMsg = " + s, Toast.LENGTH_SHORT).show();

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
