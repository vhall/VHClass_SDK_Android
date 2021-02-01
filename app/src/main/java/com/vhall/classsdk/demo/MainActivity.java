package com.vhall.classsdk.demo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;
import com.vhall.classsdk.ClassInfo;
import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.interfaces.ClassInfoCallback;
import com.vhall.classsdk.interfaces.RequestCallback;
import com.vhall.classsdk.utils.Constant;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Demo";
    private static final int PAGE_ROOM = 0;
    private static final int PAGE_LOGIN = 1;
    private static final int PAGE_FUC = 2;

    public static final int FUC_LIVE = 1;
    public static final int FUC_INTERACTIVE = 6;
    public static final int FUC_PLAYBACK = 3;
    public static final int FUC_CHAT = 4;
    public static final int FUC_DOC = 5;

    public static final int CLASS_STATUS_START = 1; // 上课
    public static final int CLASS_STATUS_PREPARE = 2; // 预告
    public static final int CLASS_STATUS_BACK = 3; // 回放
    public static final int CLASS_STATUS_CONVERT = 4; // 转播
    public static final int CLASS_STATUS_STOP = 5; // 下课

    private static final int REQUEST_PERMISSIONS = 1;

    //data
    private int currentPage = PAGE_ROOM;
    private int currentFunc = FUC_LIVE;
    private boolean loading = false;
    private String mClassId = "";
    //view
    private EditText mRoomidView;
    private LinearLayout mLoginView;
    private TextView mRoomNameView;
    private TextView mRoomStatusView;
    private TextView mRoomLayoutView;
    private TextView mRoomTypeView;
    private EditText mPwdView;
    private EditText mNicknameView;
    private LinearLayout mFunctionsView;
    private RadioGroup mFunctionsGroupView;
    private ClassInfo.Webinar webinarInfo;
    private Button mCommitView;
    private MMKV mv;
    public String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mv = MMKV.defaultMMKV();

        mRoomidView.setText(mv.decodeString("CLASS_ID", "")); //edu_52d5538e  edu_85145fdc    最新：edu_c55b26c0
        mPwdView.setText(mv.decodeString("PASSWORD", "")); // 780103   782918    最新学员：943549
        mNicknameView.setText(mv.decodeString("USER_NAME", ""));
        showPage(PAGE_ROOM);
        mCommitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentPage) {
                    case PAGE_ROOM:
                        enter();
                        break;
                    case PAGE_LOGIN:
                        join();
                        break;
                    case PAGE_FUC:
                        if (currentFunc == FUC_LIVE && VHClass.getInstance().getClassStatus() != Constant.CLASS_STATUS_LIVE) {
                            Toast.makeText(MainActivity.this, "当前不是直播状态", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (currentFunc == FUC_PLAYBACK && VHClass.getInstance().getClassStatus() != Constant.CLASS_STATUS_BACK) {
                            Toast.makeText(MainActivity.this, "当前不是回放状态", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, FunctionActivity.class);
                        intent.putExtra("fuc", currentFunc);
                        startActivity(intent);
                        break;
                }
            }
        });
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT < 23) return;
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showPage(PAGE_ROOM);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (currentPage == PAGE_FUC) {
            VHClass.getInstance().leaveClass();
        }
    }

    private void initView() {
        mRoomidView = findViewById(R.id.et_roomid);
        mLoginView = findViewById(R.id.ll_login);
        mRoomNameView = findViewById(R.id.tv_name);
        mRoomStatusView = findViewById(R.id.tv_status);
        mRoomLayoutView = findViewById(R.id.tv_layout);
        mRoomTypeView = findViewById(R.id.tv_type);
        mPwdView = findViewById(R.id.et_pwd);
        mNicknameView = findViewById(R.id.et_nickname);
        mFunctionsView = findViewById(R.id.ll_functions);
        mCommitView = findViewById(R.id.commit);
        mFunctionsGroupView = findViewById(R.id.rg_fuc);
        mFunctionsGroupView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_live:
                        currentFunc = FUC_LIVE;
                        break;
//                    case R.id.rb_interactive:
//                        currentFunc = FUC_INTERACTIVE;
//                        break;
                    case R.id.rb_playback:
                        currentFunc = FUC_PLAYBACK;
                        break;
                    case R.id.rb_doc:
                        currentFunc = FUC_DOC;
                        break;
                    case R.id.rb_chat:
                        currentFunc = FUC_CHAT;
                        break;
                }
            }
        });
    }

    private void enter() {
        if (loading)
            return;
        mClassId = mRoomidView.getText().toString();
        if (TextUtils.isEmpty(mClassId)) {
            mRoomidView.setError("请输入roomid");
            mRoomidView.requestFocus();
            return;
        }
        loading = true;
        mv.encode("CLASS_ID", mClassId);
        VHClass.getInstance().getClassInfo(mClassId, ClassApplication.device, new ClassInfoCallback() {
            @Override
            public void onSuccess(ClassInfo.Webinar webinar) {
                webinarInfo = webinar;
                loading = false;
                showPage(PAGE_LOGIN);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                loading = false;
                Toast.makeText(MainActivity.this, "" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void join() {
        if (loading)
            return;
        String pwd = mPwdView.getText().toString();
        String nickname = mNicknameView.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            mPwdView.setError("请输入口令");
            mPwdView.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(nickname)) {
            mNicknameView.setError("请输入昵称");
            mNicknameView.requestFocus();
            return;
        }
        loading = true;
        mv.encode("PASSWORD", pwd);
        mv.encode("USER_NAME", nickname);
        VHClass.getInstance().joinClass(mClassId, ClassApplication.device, nickname, pwd,new RequestCallback() {
            @Override
            public void onSuccess() {
                loading = false;
                //进入课堂成功
                showPage(PAGE_FUC);

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                loading = false;
                Toast.makeText(MainActivity.this, "" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPage(int page) {
        currentPage = page;
        switch (currentPage) {
            case PAGE_ROOM:
                mRoomidView.setVisibility(View.VISIBLE);
                mLoginView.setVisibility(View.GONE);
                mFunctionsView.setVisibility(View.GONE);
                mCommitView.setText("进入");
                break;
            case PAGE_LOGIN:
                mRoomidView.setVisibility(View.GONE);
                mLoginView.setVisibility(View.VISIBLE);
                mFunctionsView.setVisibility(View.GONE);
                mCommitView.setText("验证身份");
                refushBaseData();
                break;
            case PAGE_FUC:
                mRoomidView.setVisibility(View.GONE);
                mLoginView.setVisibility(View.GONE);
                mFunctionsView.setVisibility(View.VISIBLE);
                mCommitView.setText("进入");
                break;
        }
    }

    private void refushBaseData() {
        mRoomNameView.setText(webinarInfo.subject);
        switch (webinarInfo.type) {
            case MainActivity.CLASS_STATUS_START:// 上课中
                mRoomStatusView.setText("上课中");
                break;
            case MainActivity.CLASS_STATUS_PREPARE:
                mRoomStatusView.setText("预告");
                break;
            case MainActivity.CLASS_STATUS_BACK:
                mRoomStatusView.setText("回放");
                break;
            case MainActivity.CLASS_STATUS_CONVERT:
                mRoomStatusView.setText("转播");
                break;
            case MainActivity.CLASS_STATUS_STOP:
                mRoomStatusView.setText("已下课");
                break;
        }
        if (webinarInfo.layout == 3) {
            mRoomLayoutView.setText("视频+文档");
        } else {
            mRoomLayoutView.setText("单视频");
        }
        switch (webinarInfo.course_type){
            case 0:
                mRoomTypeView.setText("公开课");
                break;
            case 1:
                mRoomTypeView.setText("小课堂");
                break;
            case 2:
                mRoomTypeView.setText("录播课");
                break;
            case 3:
                mRoomTypeView.setText("系列课");
                break;
        }

    }
}
