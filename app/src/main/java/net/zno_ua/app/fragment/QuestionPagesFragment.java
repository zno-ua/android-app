package net.zno_ua.app.fragment;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewSwitcher;

import net.zno_ua.app.R;
import net.zno_ua.app.activity.TestingActivity;
import net.zno_ua.app.adapter.QuestionPagesAdapter;
import net.zno_ua.app.adapter.QuestionsGridAdapter;
import net.zno_ua.app.model.TestingInfo;
import net.zno_ua.app.util.TestingAnswersUtils;
import net.zno_ua.app.util.Utils;
import net.zno_ua.app.viewholder.question.QuestionNumberVH;
import net.zno_ua.app.widget.SpaceItemDecoration;

import static net.zno_ua.app.provider.Query.Question;
import static net.zno_ua.app.provider.Query.QuestionAndAnswer;
import static net.zno_ua.app.provider.Query.selectionArgs;

public class QuestionPagesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, QuestionFragment.OnQuestionAnswerListener,
        QuestionNumberVH.OnQuestionNumberClickListener {
    public static final String TAG = QuestionPagesFragment.class.getName();

    private static final int VIEW_CAROUSEL = 0;
    private static final int VIEW_COMFY = 1;

    private static final int LOADER_QUESTIONS = 0;
    private static final int LOADER_QUESTIONS_AND_ANSWERS = 1;
    private static final String KEY_CURRENT_ITEM = "KEY_CURRENT_ITEM";
    private static final String KEY_VIEW = "KEY_VIEW";

    private ViewSwitcher mViewSwitcher;
    private RecyclerView mRecyclerView;
    private ViewPager mViewPager;
    private QuestionPagesAdapter mQuestionsAdapter;
    private QuestionsGridAdapter mQuestionsGridAdapter;
    private OnViewPagerChangeListener mPagerChangeListener;
    private TestingInfo mTestingInfo;
    private int mCurrentItem = TestingAnswersUtils.NO_POSITION;
    private int mView = VIEW_CAROUSEL;
    private int mSpanCount;
    private Animation mSlideInLeft;
    private Animation mSlideInRight;
    private Animation mSlideOutLeft;
    private Animation mSlideOutRight;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPagerChangeListener = (OnViewPagerChangeListener) activity;
        mTestingInfo = ((TestingActivity) activity).getTestingInfo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuestionsAdapter = new QuestionPagesAdapter(this, mTestingInfo);
        mQuestionsGridAdapter = new QuestionsGridAdapter(getActivity(), mTestingInfo, this);
        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt(KEY_CURRENT_ITEM, TestingAnswersUtils.NO_POSITION);
            mView = savedInstanceState.getInt(KEY_VIEW);
        }
        if (!mTestingInfo.isPassed() && mCurrentItem == TestingAnswersUtils.NO_POSITION) {
            mCurrentItem = TestingAnswersUtils.findFirstUnansweredQuestion(getActivity(), mTestingInfo);
        }
        setHasOptionsMenu(true);
        mSlideInLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        mSlideInRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        mSlideOutRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        mSlideOutLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);
        mSlideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewSwitcher.setInAnimation(mSlideInRight);
                mViewSwitcher.setOutAnimation(mSlideOutLeft);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mSlideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewSwitcher.setInAnimation(mSlideInLeft);
                mViewSwitcher.setOutAnimation(mSlideOutRight);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mSpanCount = getResources().getInteger(R.integer.questions_grid_span_count);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_testing_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem item = menu.findItem(R.id.action_view);
        if (item != null) {
            if (mView == VIEW_CAROUSEL) {
                item.setIcon(R.drawable.vec_view_comfy_white_24dp);
            } else {
                item.setIcon(R.drawable.vec_view_carousel_white_24dp);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_view) {
            toggleDisplayedView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleDisplayedView() {
        setDisplayedView(Math.abs(mView - 1));
        getActivity().invalidateOptionsMenu();
    }

    private void setDisplayedView(int view) {
        mView = view;
        mPagerChangeListener.onViewPagerVisibilityChanged(mView == VIEW_CAROUSEL);
        if (mView == VIEW_COMFY) {
            mRecyclerView.scrollToPosition(mViewPager.getCurrentItem());
        }
        mViewSwitcher.setDisplayedChild(mView);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_ITEM, mViewPager.getCurrentItem());
        outState.putInt(KEY_VIEW, mView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_question_pages, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.view_switcher);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mViewPager.setAdapter(mQuestionsAdapter);
        mViewPager.setCurrentItem(mCurrentItem, false);
        final SpaceItemDecoration decoration = new SpaceItemDecoration(getActivity(), true, true);
        decoration.setLastItemExtraSpace(getActivity(), R.dimen.fab_space_size);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mSpanCount));
        mRecyclerView.setAdapter(mQuestionsGridAdapter);
        mRecyclerView.addItemDecoration(decoration);
        Utils.disableSupportsChangeAnimations(mRecyclerView);
        setDisplayedView(mView);
        mViewSwitcher.setInAnimation(mSlideInRight);
        mViewSwitcher.setOutAnimation(mSlideOutLeft);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_QUESTIONS_AND_ANSWERS, null, this);
        getLoaderManager().initLoader(LOADER_QUESTIONS, null, this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPagerChangeListener != null) {
            mPagerChangeListener.onCurrentItemChanged(mViewPager.getCurrentItem());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPagerChangeListener = null;
        mTestingInfo = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final CursorLoader loader = new CursorLoader(getActivity());
        switch (id) {
            case LOADER_QUESTIONS:
                loader.setUri(Question.URI);
                loader.setProjection(Question.PROJECTION);
                loader.setSelection(Question.SELECTION);
                loader.setSelectionArgs(selectionArgs(mTestingInfo.getTestId()));
                loader.setSortOrder(Question.SORT_ORDER);
                return loader;
            case LOADER_QUESTIONS_AND_ANSWERS:
                loader.setUri(QuestionAndAnswer.URI);
                loader.setProjection(QuestionAndAnswer.PROJECTION);
                loader.setSelection(QuestionAndAnswer.SELECTION);
                loader.setSelectionArgs(selectionArgs(mTestingInfo.getTestingId(),
                        mTestingInfo.getTestId()));
                loader.setSortOrder(QuestionAndAnswer.SORT_ORDER);
                return loader;
            default:
                throw new IllegalArgumentException("Illegal loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_QUESTIONS:
                if (mQuestionsAdapter.getCursor() == data) {
                    return;
                }
                mQuestionsAdapter.changeCursor(data);
                if (mCurrentItem != TestingAnswersUtils.NO_POSITION) {
                    mViewPager.setCurrentItem(mCurrentItem, true);
                    mCurrentItem = TestingAnswersUtils.NO_POSITION;
                }
                onViewPagerChanged();
                mPagerChangeListener.onViewPagerVisibilityChanged(mView == VIEW_CAROUSEL);
                break;
            case LOADER_QUESTIONS_AND_ANSWERS:
                mQuestionsGridAdapter.changeCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_QUESTIONS:
                mQuestionsAdapter.changeCursor(null);
                onViewPagerDetached();
                break;
            case LOADER_QUESTIONS_AND_ANSWERS:
                mQuestionsGridAdapter.changeCursor(null);
                break;
        }
    }

    private void onViewPagerChanged() {
        if (mPagerChangeListener != null) {
            mPagerChangeListener.onViewPagerDataChanged(mViewPager);
        }
    }

    private void onViewPagerDetached() {
        if (mPagerChangeListener != null) {
            mPagerChangeListener.onViewPagerDetached();
        }
    }

    @Override
    public void onQuestionAnswered() {
        final int nextItem = mViewPager.getCurrentItem() + 1;
        if (mPagerChangeListener != null && nextItem < mQuestionsAdapter.getCount()) {
            mPagerChangeListener.onCurrentItemChanged(nextItem);
        }
        mViewPager.setCurrentItem(nextItem);
    }

    @Override
    public void onQuestionNumberClicked(int position) {
        mViewPager.setCurrentItem(position, false);
        toggleDisplayedView();
    }

    public interface OnViewPagerChangeListener {

        void onViewPagerDataChanged(ViewPager viewPager);

        void onViewPagerVisibilityChanged(boolean isVisible);

        void onViewPagerDetached();

        void onCurrentItemChanged(int item);

    }

}
