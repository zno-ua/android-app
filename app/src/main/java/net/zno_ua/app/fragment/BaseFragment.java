package net.zno_ua.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {

    private OnTitleChangeListener mOnTitleChangeListener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnTitleChangeListener) {
            mOnTitleChangeListener = (OnTitleChangeListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnTitleChangeListener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String title = getTitle();
        //noinspection ConstantConditions
        if (mOnTitleChangeListener != null && title != null) {
            mOnTitleChangeListener.onTitleChanged(getTitle());
        }
    }

    @Nullable
    protected String getTitle() {
        return null;
    }

    public interface OnTitleChangeListener {
        void onTitleChanged(String title);
    }
}
