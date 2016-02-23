package net.zno_ua.app.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.zno_ua.app.R;
import net.zno_ua.app.activity.SubjectActivity;
import net.zno_ua.app.adapter.CursorRecyclerViewAdapter;
import net.zno_ua.app.adapter.SubjectsAdapter;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.util.Utils;
import net.zno_ua.app.view.SubjectViewHolder;
import net.zno_ua.app.widget.SpaceItemDecoration;

public class SubjectsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] PROJECTION = new String[]{
            ZNOContract.Subject._ID,
            ZNOContract.Subject.NAME
    };

    private CursorRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mSpanCount;

    public static Fragment newInstance() {
        return new SubjectsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SubjectsAdapter(getActivity(), new SubjectViewHolder.OnSubjectClickListener() {
            @Override
            public void onSubjectClicked(long id) {
                final Intent intent = new Intent(getActivity(), SubjectActivity.class);
                intent.putExtra(SubjectActivity.EXTRA_SUBJECT_ID, id);
                getActivity().startActivity(intent);
            }
        });
        mSpanCount = getResources().getInteger(R.integer.subjects_grid_span_count);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_subjects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.subjects_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mSpanCount));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getActivity(), true, true));
        Utils.disableSupportsChangeAnimations(mRecyclerView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    protected String getTitle() {
        return getString(R.string.testing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                ZNOContract.Subject.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ZNOContract.Subject.POSITION + ZNOContract.ASC);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
