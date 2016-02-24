package net.zno_ua.app.view;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;

import java.util.Calendar;
import java.util.Locale;

import static net.zno_ua.app.provider.Query.TestingResult.Column;
import static net.zno_ua.app.provider.ZNOContract.Testing;

/**
 * @author vojkovladimir
 */
public class TestingResultItemVewHolder extends CursorViewHolder implements View.OnClickListener {
    private static final long MINUTES = 1000 * 60;
    private final TextView mTvTitle;
    private final TextView mTvDescription;
    private final TextView mTvPoint;
    private final String mYearFormat;
    private final String mPassedDateFormat;
    private final String[] mMonths;
    private final Calendar mDate;
    private final String mForMinutesFormat;
    private final String mExperimentalTest;
    private final String mOfficialTest;
    private final String mSession;
    private final String mVariant;
    private final String mLevelBasic;
    private final String mLevelSpecialized;
    private final OnTestingItemClickListener mOnTestingItemClickListener;

    public TestingResultItemVewHolder(LayoutInflater inflater, ViewGroup parent,
                                      @NonNull OnTestingItemClickListener listener) {
        super(inflater.inflate(R.layout.view_testing_result_item, parent, false));
        mTvTitle = (TextView) itemView.findViewById(R.id.title);
        mTvDescription = (TextView) itemView.findViewById(R.id.description);
        mTvPoint = (TextView) itemView.findViewById(R.id.point);
        mYearFormat = itemView.getContext().getString(R.string.year_format);
        mPassedDateFormat = itemView.getContext().getString(R.string.passed_date_format);
        mMonths = itemView.getResources().getStringArray(R.array.months);
        mDate = Calendar.getInstance();
        mForMinutesFormat = itemView.getContext().getString(R.string.for_minutes_format);
        mOfficialTest = itemView.getContext().getString(R.string.official_test);
        mExperimentalTest = itemView.getContext().getString(R.string.experimental_test);
        mSession = itemView.getContext().getString(R.string.session);
        mVariant = itemView.getContext().getString(R.string.variant);
        mLevelBasic = itemView.getContext().getString(R.string.level_basic);
        mLevelSpecialized = itemView.getContext().getString(R.string.level_specialized);
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
        String testTitle = "Official test";
        String testYear = String.format(mYearFormat, cursor.getInt(Column.YEAR));
        String dateAndTimeOfPassing = String.format(Locale.US, mPassedDateFormat, dayOfMonth, month);
        if (elapsedTime != Testing.NO_TIME) {
            final long minutes = elapsedTime / MINUTES;
            dateAndTimeOfPassing += " " + String.format(mForMinutesFormat, minutes);
        }
        mTvTitle.setText(testTitle);
        mTvDescription.setText(testYear + "\n" + dateAndTimeOfPassing);
        mTvPoint.setText(String.format(Locale.US, "%.1f", cursor.getFloat(Column.RATING_POINT)));
    }

    @Override
    public void onClick(View v) {
        mOnTestingItemClickListener.onTestingItemClicked(getAdapterPosition());
    }

    public interface OnTestingItemClickListener {

        void onTestingItemClicked(int adapterPosition);
    }
}
