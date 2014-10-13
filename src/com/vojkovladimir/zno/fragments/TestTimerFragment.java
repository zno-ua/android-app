package com.vojkovladimir.zno.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vojkovladimir.zno.R;

public class TestTimerFragment extends Fragment {

    public static final String TAG = "test_timer";
    public static final String MILLIS_LEFT = "millis_left";
    public static final int SHOW_TIME = 5000;

    public interface OnTimeChangedListener {
        void onTimeIsUp();
        void onMinutePassed(long millisLeft);
    }

    TextView timerText;
    Timer timer;
    long millisLeft;
    OnTimeChangedListener callBack;

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
        callBack = (OnTimeChangedListener) activity;
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
        timerText = (TextView) inflater.inflate(R.layout.timer, container, false);
        timerText.setText(getTimerText(millisLeft));
        return timerText;
    }

    private String getTimerText(long millisLeft) {
        int minutes = (int) (millisLeft / 60000);
        if (minutes >= 20 || (minutes >= 0 && minutes < 10)) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MILLIS_LEFT, millisLeft);
    }

    @Override
    public void onStart() {
        super.onStart();
        timer = new Timer(millisLeft);
        timer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    public long getMillisLeft() {
        return millisLeft;
    }

    class Timer extends CountDownTimer {

        private static final long COUNT_DOWN_INTERVAL = 1000;

        public Timer(long millisInFuture) {
            super(millisInFuture, COUNT_DOWN_INTERVAL);
            millisLeft = millisInFuture;
        }

        @Override
        public void onTick(long millisInFuture) {
            millisLeft = millisInFuture;
            if ((millisInFuture % 60000) / 1000 == 0) {
                timerText.setText(getTimerText(millisInFuture));
                callBack.onMinutePassed(millisInFuture);
            }
            if (millisInFuture / 60000 <= 10) {
                timerText.setBackgroundColor(getResources().getColor(R.color.red));
            }
        }

        @Override
        public void onFinish() {
            callBack.onTimeIsUp();
        }

    }

}
