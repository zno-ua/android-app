package net.zno_ua.app.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.TestingResultsAdapter;
import net.zno_ua.app.util.Utils;
import net.zno_ua.app.view.TestingResultItemVewHolder;
import net.zno_ua.app.widget.DividerItemDecoration;
import net.zno_ua.app.widget.SelectableItemDecoration;

import static net.zno_ua.app.provider.Query.TestingResult;

/**
 * @author vojkovladimir
 */
public class TestingResultFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, TestingResultItemVewHolder.OnTestingItemClickListener {

    public static Fragment newInstance() {
        return new TestingResultFragment();
    }

    private TestingResultsAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TestingResultsAdapter(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_testing_result, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    protected String getTitle() {
        return getString(R.string.testing_result);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final CursorLoader loader = new CursorLoader(getActivity());
        loader.setUri(TestingResult.URI);
        loader.setProjection(TestingResult.PROJECTION);
        loader.setSelection(TestingResult.SELECTION);
        loader.setSortOrder(TestingResult.SORT_ORDER);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onTestingItemClicked(int adapterPosition) {
        Log.d("Logs", "clicked " + adapterPosition);
    }
}
