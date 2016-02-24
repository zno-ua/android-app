package net.zno_ua.app.view;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.provider.ZNOContract;

import static android.text.TextUtils.isEmpty;
import static net.zno_ua.app.provider.Query.Test.Column;

public class TestItemVewHolder extends CursorViewHolder implements View.OnClickListener {
    private final TextView mTvTitle;
    private final TextView mTvDescription;
    private final View mActionView;
    private final ImageView mIvActionIcon;
    private final String mExperimentalTest;
    private final String mOfficialTest;
    private final String mSession;
    private final String mVariant;
    private final String mLevelBasic;
    private final String mLevelSpecialized;
    private final OnTestItemClickListener mOnTestItemClickListener;

    public TestItemVewHolder(LayoutInflater inflater, ViewGroup parent,
                             @NonNull OnTestItemClickListener listener) {
        super(inflater.inflate(R.layout.view_test_item, parent, false));
        mTvTitle = (TextView) itemView.findViewById(R.id.title);
        mTvDescription = (TextView) itemView.findViewById(R.id.description);
        mActionView = itemView.findViewById(R.id.action);
        mIvActionIcon = (ImageView) itemView.findViewById(R.id.action_icon);
        mOfficialTest = itemView.getContext().getString(R.string.official_test);
        mExperimentalTest = itemView.getContext().getString(R.string.experimental_test);
        mSession = itemView.getContext().getString(R.string.session);
        mVariant = itemView.getContext().getString(R.string.variant);
        mLevelBasic = itemView.getContext().getString(R.string.level_basic);
        mLevelSpecialized = itemView.getContext().getString(R.string.level_specialized);
        mOnTestItemClickListener = listener;
        itemView.setOnClickListener(this);
        mActionView.setOnClickListener(this);
    }

    @Override
    public void bind(Cursor cursor) {
        final int session = cursor.getInt(Column.SESSION);
        final int result = cursor.getInt(Column.RESULT);
        String primary = "";
        String description = "";
        switch (cursor.getInt(Column.TYPE)) {
            case ZNOContract.Test.TYPE_OFFICIAL:
                primary = mOfficialTest;
                description = (session == 1 ? "I " : "II ") + mSession;
                break;
            case ZNOContract.Test.TYPE_EXPERIMENTAL:
                primary = mExperimentalTest;
                if (session != 0) {
                    description = session + " " + mVariant;
                }
                break;
        }

        switch (cursor.getInt(Column.LEVEL)) {
            case ZNOContract.Test.LEVEL_BASIC:
                primary += " " + mLevelBasic;
                break;
            case ZNOContract.Test.LEVEL_SPECIALIZED:
                primary += " " + mLevelSpecialized;
                break;
        }

        mTvTitle.setText(primary);

        final int status = cursor.getInt(Column.STATUS);

        if (status == ZNOContract.Test.STATUS_IDLE) {
            final int actionResId;
            if (result == ZNOContract.Test.NO_LOADED_DATA) {
                actionResId = R.drawable.ic_file_download_black_24dp;
                description += TextUtils.isEmpty(description) ? "" : ", ";
                description += itemView.getContext().getString(R.string.needed_to_download);
            } else if (result == ZNOContract.Test.TEST_LOADED) {
                actionResId = R.drawable.ic_delete_black_24dp;
                description += (isEmpty(description) ? "" : ", ");
                description += buildQuestionsCount(cursor.getInt(Column.QUESTIONS_COUNT));
            } else {
                actionResId = R.drawable.ic_refresh_black_24dp;
                description = itemView.getContext().getString(R.string.downloading_error);
            }
            mIvActionIcon.setImageResource(actionResId);
            mIvActionIcon.setVisibility(View.VISIBLE);
        } else if (status == ZNOContract.Test.STATUS_DELETING) {
            mIvActionIcon.setVisibility(View.GONE);
            description = itemView.getContext().getString(R.string.deleting);
        } else {
            mIvActionIcon.setVisibility(View.GONE);
            description = itemView.getContext().getString(R.string.downloading);
        }

        mTvDescription.setText(description);
    }

    private String buildQuestionsCount(int questionsCount) {
        switch (questionsCount % 10) {
            case 1:
            case 2:
            case 3:
            case 4:
                return questionsCount + " " + itemView.getContext().getString(R.string.tasks);
            default:
                return questionsCount + " " + itemView.getContext().getString(R.string.tasks_genitive);
        }
    }

    @Override
    public void onClick(View v) {
        final boolean isAction = v.getId() == R.id.action;
        mOnTestItemClickListener.onTestItemClicked(getAdapterPosition(), getItemId(), isAction);
    }

    public interface OnTestItemClickListener {

        void onTestItemClicked(int adapterPosition, long itemId, boolean isAction);
    }
}
