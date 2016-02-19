package net.zno_ua.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.SectionCursorRecyclerViewAdapter;
import net.zno_ua.app.util.Utils;

import java.util.HashMap;

import static android.text.TextUtils.isEmpty;
import static android.view.LayoutInflater.from;
import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Test;

public class SubjectTestsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String KEY_SUBJECT_ID = "KEY_SUBJECT_ID";

    private static final String SELECTION = Test.SUBJECT_ID + " = ?";

    private static final String[] PROJECTION = new String[]{
            Test._ID,
            Test.YEAR,
            Test.TYPE,
            Test.SESSION,
            Test.LEVEL,
            Test.QUESTIONS_COUNT,
            Test.STATUS,
            Test.RESULT,
    };

    private static final int ID_COLUMN_ID = 0;
    private static final int YEAR_COLUMN_ID = 1;
    private static final int TYPE_COLUMN_ID = 2;
    private static final int SESSION_COLUMN_ID = 3;
    private static final int LEVEL_COLUMN_ID = 4;
    private static final int QUESTIONS_COUNT_COLUMN_ID = 5;
    private static final int STATUS_COLUMN_ID = 6;
    private static final int RESULT_COLUMN_ID = 7;

    private long mSubjectId;

    private SectionCursorRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnTestSelectedListener mListener;

    public static Fragment newInstance(long id) {
        final Bundle args = new Bundle();
        args.putLong(KEY_SUBJECT_ID, id);
        final Fragment fragment = new SubjectTestsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SubjectTestsFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTestSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSubjectId = getArguments().getLong(KEY_SUBJECT_ID);
        }
        mAdapter = new TestsAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_subject_tests, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.tests_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        Utils.disableSupportsChangeAnimations(mRecyclerView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Test.CONTENT_URI,
                PROJECTION,
                SELECTION,
                new String[]{valueOf(mSubjectId)},
                Test.SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private static class TestVH extends RecyclerView.ViewHolder {
        TextView primaryText;
        TextView secondaryText;
        View action;
        ImageView actionIcon;
        View bottomShadow;
        View divider;

        public TestVH(View itemView) {
            super(itemView);
            primaryText = (TextView) itemView.findViewById(R.id.primary_text);
            secondaryText = (TextView) itemView.findViewById(R.id.secondary_text);
            action = itemView.findViewById(R.id.action);
            actionIcon = (ImageView) itemView.findViewById(R.id.action_icon);
            bottomShadow = itemView.findViewById(R.id.bottom_shadow);
            divider = itemView.findViewById(R.id.divider);
        }

        public void setActionIcon(int resId) {
            actionIcon.setImageResource(resId);
        }

        public void setPrimaryText(String text) {
            setText(primaryText, text);
        }

        public void setSecondaryText(String text) {
            setText(secondaryText, text);
        }

        private static void setText(TextView textView, String text) {
            if (!(text.equals(textView.getText()))) {
                textView.setText(text);
            }
        }
    }

    private static class YearSectionVH extends RecyclerView.ViewHolder {
        View topShadow;
        TextView year;

        public YearSectionVH(View itemView) {
            super(itemView);
            topShadow = itemView.findViewById(R.id.top_shadow);
            year = (TextView) itemView.findViewById(R.id.text);
        }
    }

    private class TestsAdapter extends SectionCursorRecyclerViewAdapter<String>
            implements View.OnClickListener {

        public TestsAdapter() {
            super(null, null);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            switch (viewType) {
                case TYPE_ITEM:
                    itemView = from(getActivity())
                            .inflate(R.layout.test_list_item, parent, false);
                    TestVH holder = new TestVH(itemView);

                    holder.itemView.setOnClickListener(this);
                    holder.action.setOnClickListener(this);

                    return holder;
                case TYPE_SECTION:
                    itemView = from(getActivity())
                            .inflate(R.layout.test_year_section_item, parent, false);
                    return new YearSectionVH(itemView);
                default:
                    throw new IllegalArgumentException("Invalid viewType " + viewType);
            }
        }

        @Override
        protected HashMap<Integer, String> createSections(Cursor cursor) {
            HashMap<Integer, String> sections = new HashMap<>();
            String year;
            int position = 0;

            if (cursor.moveToFirst()) {
                do {
                    year = valueOf(cursor.getInt(YEAR_COLUMN_ID));
                    if (!sections.containsValue(year)) {
                        sections.put(position, year);
                        position++;
                    }
                    position++;
                } while (cursor.moveToNext());
            }

            return sections;
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, Cursor cursor,
                                         int position) {
            TestVH viewHolder = (TestVH) holder;
            int session = cursor.getInt(SESSION_COLUMN_ID);
            int result = cursor.getInt(RESULT_COLUMN_ID);
            String primary = "";
            String description = "";

            switch (cursor.getInt(TYPE_COLUMN_ID)) {
                case Test.TYPE_OFFICIAL:
                    primary = getString(R.string.official_test);
                    description = (session == 1 ? "I " : "II ")
                            + getString(R.string.session);
                    break;
                case Test.TYPE_EXPERIMENTAL:
                    primary = getString(R.string.experimental_test);
                    if (session != 0) {
                        description = session + " " + getString(R.string.variant);
                    }
                    break;
            }

            switch (cursor.getInt(LEVEL_COLUMN_ID)) {
                case Test.LEVEL_BASIC:
                    primary += " " + getString(R.string.level_basic);
                    break;
                case Test.LEVEL_SPECIALIZED:
                    primary += " " + getString(R.string.level_specialized);
                    break;
            }

            viewHolder.setPrimaryText(primary);

            int status = cursor.getInt(STATUS_COLUMN_ID);

            if (status == Test.STATUS_IDLE) {
                if (result == Test.NO_LOADED_DATA) {
                    viewHolder.setActionIcon(R.drawable.ic_file_download_black_24dp);
                    description += (isEmpty(description) ? "" : ", ");
                    description += getString(R.string.needed_to_download);
                } else if (result == Test.TEST_LOADED) {
                    viewHolder.setActionIcon(R.drawable.ic_delete_black_24dp);
                    description += (isEmpty(description) ? "" : ", ");
                    description += buildQuestionsCount(cursor.getInt(QUESTIONS_COUNT_COLUMN_ID));
                } else {
                    viewHolder.setActionIcon(R.drawable.ic_refresh_black_24dp);
                    description = getString(R.string.downloading_error);
                }

                viewHolder.actionIcon.setVisibility(View.VISIBLE);
            } else if (status == Test.STATUS_DELETING) {
                viewHolder.actionIcon.setVisibility(View.GONE);
                description = getString(R.string.deleting);
            } else {
                viewHolder.actionIcon.setVisibility(View.GONE);
                description = getString(R.string.downloading);
            }

            viewHolder.setSecondaryText(description);

            if (position == getItemCount() - 1) {
                viewHolder.bottomShadow.setVisibility(View.VISIBLE);
                viewHolder.divider.setVisibility(View.GONE);
            } else {
                if (getItemViewType(position + 1) == TYPE_ITEM)
                    viewHolder.divider.setVisibility(View.VISIBLE);
                else
                    viewHolder.divider.setVisibility(View.GONE);

                viewHolder.bottomShadow.setVisibility(View.GONE);
            }
            viewHolder.action.setTag(position);
        }

        @Override
        public void onBindSectionViewHolder(RecyclerView.ViewHolder holder, String section,
                                            int position) {
            YearSectionVH viewHolder = (YearSectionVH) holder;
            viewHolder.year.setText(section);
            if (position == 0)
                viewHolder.topShadow.setVisibility(View.GONE);
            else
                viewHolder.topShadow.setVisibility(View.VISIBLE);
        }

        private String buildQuestionsCount(int questionsCount) {
            switch (questionsCount % 10) {
                case 1:
                case 2:
                case 3:
                case 4:
                    return questionsCount + " " + getString(R.string.tasks);
                default:
                    return questionsCount + " " + getString(R.string.tasks_genitive);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getItemPosition((v.getId() == R.id.action) ?
                    (int) v.getTag() : mRecyclerView.getChildLayoutPosition(v));
            Cursor cursor = getCursor();
            if (cursor.moveToPosition(position)) {
                long id = cursor.getLong(ID_COLUMN_ID);
                int status = cursor.getInt(STATUS_COLUMN_ID);
                int result = cursor.getInt(RESULT_COLUMN_ID);

                if (status == Test.STATUS_IDLE) {
                    if (result == Test.TEST_LOADED) {
                        if (v.getId() == R.id.action)
                            mListener.onStartDeletingTest(id);
                        else
                            mListener.onStartPassingTest(id);
                    } else if (result == Test.NO_LOADED_DATA) {
                        mListener.onStartDownloadingTest(id);
                    } else {
                        mListener.onReStartDownloadingTest(id);
                    }
                }
            }
        }
    }

    /**
     * Interface to be invoked when subject test is selected.
     */
    public interface OnTestSelectedListener {
        /**
         * @param id unique id od the row of the test in the database.
         */
        void onStartPassingTest(long id);

        /**
         * @param id unique id od the row of the test in the database.
         */
        void onStartDownloadingTest(long id);

        /**
         * @param id unique id od the row of the test in the database.
         */
        void onReStartDownloadingTest(long id);

        /**
         * @param id unique id od the row of the test in the database.
         */
        void onStartDeletingTest(long id);

    }

}
