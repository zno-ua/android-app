package net.zno_ua.app.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import net.zno_ua.app.R;
import net.zno_ua.app.activity.TestingActivity;
import net.zno_ua.app.adapter.TestingResultsAdapter;
import net.zno_ua.app.util.Utils;
import net.zno_ua.app.viewholder.TestingResultItemVewHolder;
import net.zno_ua.app.widget.DividerItemDecoration;
import net.zno_ua.app.widget.SelectableItemDecoration;

import static net.zno_ua.app.provider.Query.TestingResult;
import static net.zno_ua.app.provider.Query.TestingResult.Column;
import static net.zno_ua.app.provider.ZNOContract.Test.TEST_LOADED;

/**
 * @author vojkovladimir
 */
public class TestingResultFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        TestingResultItemVewHolder.OnTestingItemClickListener, View.OnClickListener {

    public static Fragment newInstance() {
        return new TestingResultFragment();
    }

    private TestingResultsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnPassTestingSelectListener mListener;

    private View mPassTestingPromptLayout;
    private View mPassTestingPromptIcon;
    private View mPassTestingPromptActions;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnPassTestingSelectListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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
        mRecyclerView.setVisibility(View.GONE);
        Utils.disableSupportsChangeAnimations(mRecyclerView);
        view.findViewById(R.id.pass_testing).setOnClickListener(this);
        mPassTestingPromptLayout = view.findViewById(R.id.pass_testing_prompt_layout);
        mPassTestingPromptIcon = view.findViewById(R.id.pass_testing_prompt_icon);
        mPassTestingPromptActions = view.findViewById(R.id.pass_testing_prompt_actions);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    protected String getTitle() {
        return getString(R.string.my_results);
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
        validateData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
        validateData(null);
    }

    private void validateData(Cursor data) {
        if (data == null || data.getCount() == 0) {
            showPassTestingPrompt();
        } else {
            if (mPassTestingPromptLayout.getVisibility() == View.VISIBLE) {
                mPassTestingPromptLayout.setVisibility(View.INVISIBLE);
            }
            if (mRecyclerView.getVisibility() == View.GONE) {
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onTestingItemClicked(int adapterPosition) {
        final Cursor cursor = mAdapter.getCursor();
        if (cursor.moveToPosition(mAdapter.getItemPosition(adapterPosition))) {
            final int result = cursor.getInt(Column.TEST_RESULT);
            final long testingId = cursor.getLong(Column._ID);
            final long testId = cursor.getLong(Column.TEST_ID);
            if (result == TEST_LOADED) {
                final Intent intent = new Intent(getActivity(), TestingActivity.class);
                intent.setAction(TestingActivity.Action.VIEW_TEST);
                intent.putExtra(TestingActivity.Key.TEST_ID, testId);
                intent.putExtra(TestingActivity.Key.TESTING_ID, testingId);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.activity_open_translate_right,
                        R.anim.activity_close_alpha);
            }
        }
    }

    private void showPassTestingPrompt() {
        mPassTestingPromptLayout.setVisibility(View.VISIBLE);
        float stopY = mPassTestingPromptIcon.getY() + mPassTestingPromptIcon.getHeight();
        float startY = stopY + mPassTestingPromptActions.getHeight() / 5;
        final ObjectAnimator fadeInIcon = ObjectAnimator
                .ofFloat(mPassTestingPromptIcon, View.ALPHA, 0.0f, 1.0f);
        fadeInIcon.setDuration(500);
        fadeInIcon.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPassTestingPromptActions.setVisibility(View.INVISIBLE);
            }
        });
        final ObjectAnimator slideUpActions = ObjectAnimator
                .ofFloat(mPassTestingPromptActions, View.Y, startY, stopY);
        slideUpActions.setDuration(400);
        slideUpActions.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPassTestingPromptActions.setVisibility(View.VISIBLE);
            }
        });
        final ObjectAnimator fadeInActions = ObjectAnimator
                .ofFloat(mPassTestingPromptActions, View.ALPHA, 0.0f, 1.0f);
        fadeInActions.setDuration(400);
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.play(fadeInIcon).before(slideUpActions).with(fadeInActions);
        animatorSet.start();
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onPassTestingSelected();
        }
    }

    public interface OnPassTestingSelectListener {
        void onPassTestingSelected();
    }
}
