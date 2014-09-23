package com.vojkovladimir.zno.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vojkovladimir.zno.R;

public class TestTimerFragment extends Fragment {

    public static final String MILLIS_LEFT = "millis_left";
    public static final int SHOW_TIME = 5000;

    long millisLeft;
    CountDownTimer timer;
    OnTimerStates callBack;

    public static TestTimerFragment newInstance(long millisLeft) {
        TestTimerFragment f = new TestTimerFragment();
        Bundle args = new Bundle();
        args.putLong(MILLIS_LEFT, millisLeft);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callBack = (OnTimerStates) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            millisLeft = savedInstanceState.getLong(MILLIS_LEFT);
        } else {
            millisLeft = getArguments().getLong(MILLIS_LEFT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final TextView timeText = (TextView) inflater.inflate(R.layout.timer, container, false);
        timeText.setText(getTimerText(millisLeft));
        if (millisLeft / 60000 <= 10) {
            timeText.setBackgroundColor(getResources().getColor(R.color.red));
        }
        timer = new CountDownTimer(millisLeft, 1000) {
            @Override
            public void onTick(long millisInFuture) {
                millisLeft = millisInFuture;
                if ((millisInFuture % 60000) / 1000 == 0) {
                    timeText.setText(getTimerText(millisInFuture));
                    if (millisInFuture / 60000 <= 10) {
                        timeText.setBackgroundColor(getResources().getColor(R.color.red));
                    }
                    callBack.onTick(millisInFuture);
                }
            }

            @Override
            public void onFinish() {
                callBack.onFinish();
            }
        };
        return timeText;
    }

    private String getTimerText(long millisLeft) {
        int minutes = (int) (millisLeft / 60000);
        if (minutes >= 20 || (minutes >= 0 && minutes <10)) {
            switch (minutes % 10) {
                case 1:
                    return String.format(getResources().getString(R.string.timer_one_minute), minutes);
                case 2:
                case 3:
                case 4:
                    return String.format(getResources().getString(R.string.timer_two_four_minutes), minutes);
            }
        }
        return String.format(getResources().getString(R.string.timer_minutes), minutes);
    }

    @Override
    public void onStart() {
        timer.start();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    public long getMillisLeft() {
        return millisLeft;
    }

    public void cancel() {
        timer.cancel();
    }

    public interface OnTimerStates {
        void onTick(long millisInFuture);
        void onFinish();
    }

}
