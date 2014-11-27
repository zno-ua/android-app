package net.zno_ua.app.fragments;


import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;

public class TestTimerFragment extends Fragment {

    public static final String TAG = "test_timer";
    public static final String MILLIS_LEFT = "millis_left";
    public static final int SHOW_TIME = 5000;

    private static final String ONE_LEFT_FORMAT;
    private static final String TWO_FOUR_LEFT_FORMAT;
    private static final String LEFT_FORMAT;
    private static final String ONE_MINUTE;
    private static final String TWO_FOUR_MINUTES;
    private static final String MINUTES;
    private static final String ONE_SECOND;
    private static final String TWO_FOUR_SECONDS;
    private static final String SECONDS;
    private static final String TIME_IS_UP;

    static {
        Resources resources = ZNOApplication.getInstance().getResources();
        ONE_LEFT_FORMAT = resources.getString(R.string.time_one_left);
        TWO_FOUR_LEFT_FORMAT = resources.getString(R.string.time_two_four_left);
        LEFT_FORMAT = resources.getString(R.string.time_left);
        ONE_MINUTE = resources.getString(R.string.one_minute);
        TWO_FOUR_MINUTES = resources.getString(R.string.two_four_minutes);
        MINUTES = resources.getString(R.string.minutes);
        ONE_SECOND = resources.getString(R.string.one_second);
        TWO_FOUR_SECONDS = resources.getString(R.string.two_four_seconds);
        SECONDS = resources.getString(R.string.seconds);
        TIME_IS_UP = resources.getString(R.string.time_is_up);
    }

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
        timerText.setText(getTimerText((int) (millisLeft / 60000), 0));
        return timerText;
    }

    private String getTimerText(int minutes, int seconds) {
        String text;

        if (minutes > 0) {
            if ((minutes < 10) || (minutes > 20 && minutes < 110) || minutes > 120) {
                switch (minutes % 10) {
                    case 1:
                        text = String.format(ONE_LEFT_FORMAT, minutes, ONE_MINUTE);
                        break;
                    case 2:
                    case 3:
                    case 4:
                        text = String.format(TWO_FOUR_LEFT_FORMAT, minutes, TWO_FOUR_MINUTES);
                        break;
                    default:
                        text = String.format(LEFT_FORMAT, minutes, MINUTES);
                }
            } else {
                text = String.format(LEFT_FORMAT, minutes, MINUTES);
            }
        } else {
            if (seconds == 0) {
                text = TIME_IS_UP;
            } else if (seconds >= 20 || (seconds > 0 && seconds < 10)) {
                switch (seconds % 10) {
                    case 1:
                        text = String.format(ONE_LEFT_FORMAT, seconds, ONE_SECOND);
                        break;
                    case 2:
                    case 3:
                    case 4:
                        text = String.format(TWO_FOUR_LEFT_FORMAT, seconds, TWO_FOUR_SECONDS);
                        break;
                    default:
                        text = String.format(LEFT_FORMAT, seconds, SECONDS);
                }
            } else {
                text = String.format(LEFT_FORMAT, seconds, SECONDS);
            }
        }

        return text;
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
            int minutes = (int) (millisInFuture / 60000);
            int seconds = (int) (millisInFuture % 60000 / 1000);

            if (minutes == 0) {
                timerText.setText(getTimerText(minutes, seconds));
            }

            if (minutes != 0 && seconds == 0) {
                timerText.setText(getTimerText(minutes, seconds));
                callBack.onMinutePassed(millisInFuture);
            }

            if (minutes <= 10) {
                timerText.setBackgroundColor(getResources().getColor(R.color.red));
            }
        }

        @Override
        public void onFinish() {
            millisLeft = 0;
            callBack.onTimeIsUp();
        }

    }

}
