package net.zno_ua.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.CursorRecyclerViewAdapter;
import net.zno_ua.app.adapter.SectionCursorRecyclerViewAdapter;
import net.zno_ua.app.provider.ZNOContract;

import java.util.HashMap;

import static android.view.LayoutInflater.from;
import static java.lang.String.valueOf;

public class SubjectTestsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_SUBJECT_ID = "subject_id";

    private static final String SELECTION = ZNOContract.Test.SUBJECT_ID + " = ?";

    private static final String[] PROJECTION = new String[]{
            ZNOContract.Test._ID,
            ZNOContract.Test.TYPE,
            ZNOContract.Test.YEAR,
            ZNOContract.Test.SESSION,
            ZNOContract.Test.STATUS
    };

    private static final int TYPE_COLUMN_ID = 1;
    private static final int YEAR_COLUMN_ID = 2;
    private static final int SESSION_COLUMN_ID = 3;
    private static final int STATUS_COLUMN_ID = 4;

    private long mSubjectId;

    private SectionCursorRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnTestSelectedListener mListener;

    public static SubjectTestsFragment newInstance(long id) {
        SubjectTestsFragment fragment = new SubjectTestsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SUBJECT_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public SubjectTestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSubjectId = getArguments().getLong(ARG_SUBJECT_ID);
        }
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_tests, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.tests_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new TestAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
//            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                ZNOContract.Test.CONTENT_URI,
                PROJECTION,
                SELECTION,
                new String[]{valueOf(mSubjectId)},
                ZNOContract.Test.SORT_ORDER);
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
        ImageView actionIcon;
        View bottomShadow;
        View divider;

        public TestVH(View itemView) {
            super(itemView);
            primaryText = (TextView) itemView.findViewById(R.id.primary_text);
            secondaryText = (TextView) itemView.findViewById(R.id.secondary_text);
            actionIcon = (ImageView) itemView.findViewById(R.id.action_icon);
            bottomShadow = itemView.findViewById(R.id.bottom_shadow);
            divider = itemView.findViewById(R.id.divider);
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

    private class TestAdapter extends SectionCursorRecyclerViewAdapter<String>
            implements View.OnClickListener {

        public TestAdapter() {
            super(null, null);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            switch (viewType) {
                case TYPE_ITEM:
                    itemView = from(getActivity())
                            .inflate(R.layout.test_list_item, parent, false);
                    itemView.setOnClickListener(this);

                    return new TestVH(itemView);
                case TYPE_SECTION:
                    itemView = from(getActivity())
                            .inflate(R.layout.test_year_section_item, parent, false);
                    return new YearSectionVH(itemView);
                default:
                    throw new IllegalArgumentException("Invalid viewType " + viewType);
            }
        }

        @Override
        public void onClick(View v) {

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
            int type = cursor.getInt(TYPE_COLUMN_ID);
            int session = cursor.getInt(SESSION_COLUMN_ID);
            int status = cursor.getInt(STATUS_COLUMN_ID);
            String infoText = "";

            switch (type) {
                case ZNOContract.Test.OFFICIAL:
                    viewHolder.primaryText.setText(R.string.official_test);
                    if (session == 1) {
                        infoText += "I " + getString(R.string.session);
                    } else if (session == 2) {
                        infoText += "II " + getString(R.string.session);
                    }
                    break;
                case ZNOContract.Test.EXPERIMENTAL:
                    viewHolder.primaryText.setText(R.string.experimental_test);
                    if (session != 0) {
                        infoText += session + " " + getString(R.string.variant);
                    }
                    break;
            }

            if (infoText.length() != 0) {
                infoText += ", ";
            }
            switch (status) {
                default:
                    /* TODO: FLAG_NOT_LOADED */
                    infoText += getString(R.string.needed_to_download);
            }

            viewHolder.secondaryText.setText(infoText);

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
    }

    /**
     * Interface to be invoked when subject test is selected.
     */
    public interface OnTestSelectedListener {
        /**
         * @param id unique id od the row of the test in the database.
         */
        void onTestSelected(long id);
    }

}
