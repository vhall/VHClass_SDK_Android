package com.vhall.classsdk.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vhall.classsdk.widget.DocumentView;

public class DocumentFragment extends Fragment {
    private RelativeLayout main;
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
        main = rootView.findViewById(R.id.main);
        mDocView = new DocumentView(getContext());
        mDocView.setEventListener(new DocumentView.EventListener() {
            @Override
            public void onShow() {
                if (mDocView.getActiveView() != null && mDocView.getActiveView().getParent() == null) {
                    main.removeAllViews();
                    main.addView(mDocView.getActiveView(), new ViewGroup.LayoutParams(-1, -1));
                }
            }

            @Override
            public void onDestroy() {

            }

        });
        return rootView;
    }

}
