package net.zno_ua.app.viewholder;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.provider.ZNOContract;

import static android.text.TextUtils.isEmpty;
import static net.zno_ua.app.provider.Query.Test.Column;
import static net.zno_ua.app.provider.ZNOContract.Test.getTestLevel;
import static net.zno_ua.app.provider.ZNOContract.Test.getTestSession;
import static net.zno_ua.app.provider.ZNOContract.Test.getTestType;

public class TestItemVewHolder extends CursorViewHolder implements View.OnClickListener {
    private final TextView mTvTitle;
    private final TextView mTvDescription;
    private final View mActionView;
    private final AppCompatImageView mIvActionIcon;
    private final OnTestItemClickListener mOnTestItemClickListener;

    public TestItemVewHolder(LayoutInflater inflater, ViewGroup parent,
                             @NonNull OnTestItemClickListener listener) {
        super(inflater.inflate(R.layout.view_test_item, parent, false));
        mTvTitle = (TextView) itemView.findViewById(R.id.title);
        mTvDescription = (TextView) itemView.findViewById(R.id.description);
        mActionView = itemView.findViewById(R.id.action);
        mIvActionIcon = (AppCompatImageView) itemView.findViewById(R.id.action_icon);
        mOnTestItemClickListener = listener;
        itemView.setOnClickListener(this);
        mActionView.setOnClickListener(this);
    }

    @Override
    public void bind(Cursor cursor) {
        final int session = cursor.getInt(Column.SESSION);
        final int result = cursor.getInt(Column.RESULT);
        final int status = cursor.getInt(Column.STATUS);
        final int type = cursor.getInt(Column.TYPE);
        final int level = cursor.getInt(Column.LEVEL);
        String primary = getTestType(itemView.getContext(), type);
        String description = session != 0 ? getTestSession(itemView.getContext(), type, session) : "";
        if (level != 0) {
            primary += " " + getTestLevel(itemView.getContext(), level);
        }
        mTvTitle.setText(primary);
        if (status == ZNOContract.Test.STATUS_IDLE) {
            final int actionResId;
            if (result == ZNOContract.Test.NO_LOADED_DATA) {
                actionResId = R.drawable.ic_file_download_white_24dp;
                description += TextUtils.isEmpty(description) ? "" : ", ";
                description += itemView.getContext().getString(R.string.needed_to_download);
            } else if (result == ZNOContract.Test.TEST_LOADED) {
                actionResId = R.drawable.ic_delete_white_24dp;
                description += (isEmpty(description) ? "" : ", ");
                description += buildQuestionsCount(cursor.getInt(Column.QUESTIONS_COUNT));
            } else {
                actionResId = R.drawable.ic_refresh_white_24dp;
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
        mOnTestItemClickListener.onTestItemClicked(getAdapterPosition(), isAction);
    }

    public interface OnTestItemClickListener {

        void onTestItemClicked(int adapterPosition, boolean isAction);
    }
}
