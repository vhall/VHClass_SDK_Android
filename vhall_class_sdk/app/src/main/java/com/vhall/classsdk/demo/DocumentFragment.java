package com.vhall.classsdk.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.service.MessageServer;
import com.vhall.classsdk.widget.DocumentView;

public class DocumentFragment extends Fragment {
    private DocumentView mDocView;

    public static DocumentFragment newInstance() {
        return new DocumentFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_watch_doc, null);
        mDocView = rootView.findViewById(R.id.iv_doc);
        String docMode = VHClass.getInstance().getDocMode();
        switchDocumentMode(docMode);
//        if (docMode.equals("0")) {
//            MessageServer.MsgInfo mDefaultDocument = VHClass.getInstance().getDefaultDocument();
//            if (mDefaultDocument != null)
//                drawDocument(mDefaultDocument);
//        } else {
//            mDocView.updateDrawMode(DocumentView.DRAW_MODE_WHITEBOARD);
//        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void switchDocumentMode(String mode) {
        Log.e("document", mode);
        if (mDocView != null) {
            if (mode.equals("0")) {
                mDocView.updateDrawMode(DocumentView.DRAW_MODE_DOCUMENT);
                if (mDocView.mSaveDucumentBitmap == null) {
                    MessageServer.MsgInfo mDefaultDocument = VHClass.getInstance().getDefaultDocument();
                    if (mDefaultDocument != null)
                        drawDocument(mDefaultDocument);
                }
            } else
                mDocView.updateDrawMode(DocumentView.DRAW_MODE_WHITEBOARD);
        }
    }

    public void drawDocument(MessageServer.MsgInfo msgInfo) {
        mDocView.setStep(msgInfo);
    }
}
