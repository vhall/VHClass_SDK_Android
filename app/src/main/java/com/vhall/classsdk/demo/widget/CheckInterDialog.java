package com.vhall.classsdk.demo.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.vhall.classsdk.demo.R;


/**
 * 邀请上麦的Dialog
 */
public class CheckInterDialog extends AlertDialog implements View.OnClickListener {
    private static final String TAG = "CheckInterDialog";
    private Context mContext;
    private TextView mSure, mAudio, mrefuse;

    public CheckInterDialog(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    private void initView() {
        if (mContext != null) {
            View root = View.inflate(mContext, R.layout.dialog_invite_mics, null);
            mSure = root.findViewById(R.id.text_class_mics_sure);
            mAudio = root.findViewById(R.id.text_class_mics_audio);
            mrefuse = root.findViewById(R.id.text_class_mics_no);
            mSure.setOnClickListener(this);
            mAudio.setOnClickListener(this);
            mrefuse.setOnClickListener(this);

            this.setView(root);
            this.setCanceledOnTouchOutside(false); //点击外部不消失
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.text_class_mics_sure:
                listener.onAllow();
                break;
            case R.id.text_class_mics_no:
                listener.onRefuse();
                break;
        }
    }

    public interface ClickCheckListener {
        void onAllow();

        void onRefuse();
    }

    public ClickCheckListener listener;

    public void setClickCheckListener(ClickCheckListener listener) {
        this.listener = listener;
    }
}
