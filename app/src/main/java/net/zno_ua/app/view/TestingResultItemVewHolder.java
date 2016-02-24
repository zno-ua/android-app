package net.zno_ua.app.view;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.zno_ua.app.R;

import java.util.Calendar;
import java.util.Locale;

import static net.zno_ua.app.provider.Query.TestingResult.Column;
import static net.zno_ua.app.provider.ZNOContract.Test.TEST_LOADED;
import static net.zno_ua.app.provider.ZNOContract.Test.getTestLevel;
import static net.zno_ua.app.provider.ZNOContract.Test.getTestSession;
import static net.zno_ua.app.provider.ZNOContract.Test.getTestType;
import static net.zno_ua.app.provider.ZNOContract.TestingResult.getSentimentColor;
import static net.zno_ua.app.provider.ZNOContract.TestingResult.getSentimentOfRatingPoint;
import static net.zno_ua.app.provider.ZNOContract.TestingResult.getSentimentResourceId;

/**
 * @author vojkovladimir
 */
public class TestingResultItemVewHolder extends CursorViewHolder implements View.OnClickListener {
    private static final long MINUTE = 1000 * 60;
    private static final long MIN_TIME_TO_SHOW = 5 * MINUTE;
    private final TextView mTvTitle;
    private final TextView mTvDescription;
    private final TextView mTvPoint;
    private final ImageView mIvSentiment;
    private final String mYearFormat;
    private final String mPassedDateFormat;
    private final String[] mMonths;
    private final Calendar mDate;
    private final String mForMinutesFormat;
    private final OnTestingItemClickListener mOnTestingItemClickListener;

    public TestingResultItemVewHolder(LayoutInflater inflater, ViewGroup parent,
                                      @NonNull OnTestingItemClickListener listener) {
        super(inflater.inflate(R.layout.view_testing_result_item, parent, false));
        mTvTitle = (TextView) itemView.findViewById(R.id.title);
        mTvDescription = (TextView) itemView.findViewById(R.id.description);
        mTvPoint = (TextView) itemView.findViewById(R.id.point);
        mIvSentiment = (ImageView) itemView.findViewById(R.id.sentiment);
        mYearFormat = itemView.getContext().getString(R.string.year_format);
        mPassedDateFormat = itemView.getContext().getString(R.string.passed_date_format);
        mMonths = itemView.getResources().getStringArray(R.array.months);
        mDate = Calendar.getInstance();
        mForMinutesFormat = itemView.getContext().getString(R.string.for_minutes_format);
        mOnTestingItemClickListener = listener;
        itemView.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bind(Cursor cursor) {
        mDate.setTimeInMillis(cursor.getLong(Column.DATE));
        final String month = mMonths[mDate.get(Calendar.MONTH)];
        final long elapsedTime = cursor.getLong(Column.ELAPSED_TIME);
        final int dayOfMonth = mDate.get(Calendar.DAY_OF_MONTH);
        final float ratingPoint = cursor.getFloat(Column.RATING_POINT);
        final int type = cursor.getInt(Column.TEST_TYPE);
        final int session = cursor.getInt(Column.TEST_SESSION);
        final int level = cursor.getInt(Column.TEST_LEVEL);
        String testTitle = getTestType(itemView.getContext(), type);
        String description = String.format(mYearFormat, cursor.getInt(Column.TEST_YEAR));
        if (session != 0) {
            description += ", " + getTestSession(itemView.getContext(), type, session);
        }
        if (level != 0) {
            testTitle += " " + getTestLevel(itemView.getContext(), level);
        }
        description += "\n" + String.format(Locale.US, mPassedDateFormat, dayOfMonth, month);
        if (elapsedTime >= MIN_TIME_TO_SHOW) {
            final long minutes = elapsedTime / MINUTE;
            description += ", " + String.format(mForMinutesFormat, minutes);
        }
        final int sentimentResult = getSentimentOfRatingPoint(ratingPoint);
        final int sentimentColor = getSentimentColor(itemView.getContext(), sentimentResult);
        mTvTitle.setText(testTitle);
        mTvDescription.setText(description);
        mTvPoint.setText(String.format(Locale.US, "%.1f", ratingPoint));
        mTvPoint.setTextColor(sentimentColor);
        mIvSentiment.setImageResource(getSentimentResourceId(sentimentResult));
        mIvSentiment.setColorFilter(sentimentColor);
        itemView.setClickable(cursor.getInt(Column.TEST_RESULT) == TEST_LOADED);
    }

    @Override
    public void onClick(View v) {
        mOnTestingItemClickListener.onTestingItemClicked(getAdapterPosition());
    }

    public interface OnTestingItemClickListener {

        void onTestingItemClicked(int adapterPosition);
    }
}
