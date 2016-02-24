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

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.TestsAdapter;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.util.Utils;
import net.zno_ua.app.view.TestItemVewHolder;
import net.zno_ua.app.widget.DividerItemDecoration;
import net.zno_ua.app.widget.SelectableItemDecoration;

import static net.zno_ua.app.provider.Query.Test;
import static net.zno_ua.app.provider.Query.selectionArgs;

public class SubjectTestsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        TestItemVewHolder.OnTestItemClickListener {
    private static final String KEY_SUBJECT_ID = "KEY_SUBJECT_ID";

    private long mSubjectId;

    private TestsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnTestSelectedListener mListener;

    public static Fragment newInstance(long id) {
        final Bundle args = new Bundle();
        args.putLong(KEY_SUBJECT_ID, id);
        final Fragment fragment = new SubjectTestsFragment();
        fragment.setArguments(args);
        return fragment;
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
        mAdapter = new TestsAdapter(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_subject_tests, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SelectableItemDecoration itemDecoration = new DividerItemDecoration(getActivity());
        itemDecoration.setDecoratorSelector(mAdapter);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(itemDecoration);
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
        final CursorLoader loader = new CursorLoader(getActivity());
        loader.setUri(Test.URI);
        loader.setProjection(Test.PROJECTION);
        loader.setSelection(Test.SELECTION);
        loader.setSelectionArgs(selectionArgs(mSubjectId));
        loader.setSortOrder(Test.SORT_ORDER);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onTestItemClicked(int position, long id, boolean isAction) {
        final Cursor cursor = mAdapter.getCursor();
        if (cursor.moveToPosition(mAdapter.getItemPosition(position))) {
            final int status = cursor.getInt(Test.Column.STATUS);
            final int result = cursor.getInt(Test.Column.RESULT);
            if (status == ZNOContract.Test.STATUS_IDLE) {
                if (result == ZNOContract.Test.TEST_LOADED) {
                    if (isAction) {
                        mListener.onStartDeletingTest(id);
                    } else {
                        mListener.onStartPassingTest(id);
                    }
                } else if (result == ZNOContract.Test.NO_LOADED_DATA) {
                    mListener.onStartDownloadingTest(id);
                } else {
                    mListener.onReStartDownloadingTest(id);
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
