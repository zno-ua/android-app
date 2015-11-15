package net.zno_ua.app.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
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

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.QuestionsAdapter;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.QuestionAndAnswer;

public class TestingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        QuestionsAdapter.OnAnswerChangeListener {

    private static final int LOADER_QUESTIONS = 0;
    private static final int LOADER_ANSWERS = 1;

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TEST_ID = "test_id";
    public static final String EXTRA_SUBJECT_ID = "subject_id";
    public static final String EXTRA_IS_PASSED = "is_passed";

    public TestingFragment() {
    }

    public static TestingFragment newInstance(long id, long testId, long subjectId,
                                              boolean isPassed) {
        TestingFragment fragment = new TestingFragment();

        Bundle args = new Bundle();
        args.putLong(EXTRA_ID, id);
        args.putLong(EXTRA_TEST_ID, testId);
        args.putLong(EXTRA_SUBJECT_ID, subjectId);
        args.putBoolean(EXTRA_IS_PASSED, isPassed);

        fragment.setArguments(args);

        return fragment;
    }

    private long testingId;
    private RecyclerView mRecyclerView;
    private QuestionsAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testingId = getArguments().getLong(EXTRA_ID);
        getLoaderManager().initLoader(LOADER_QUESTIONS, getArguments(), this);
        getLoaderManager().initLoader(LOADER_ANSWERS, getArguments(), this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_testing, container, false);

        boolean viewMode = getArguments().getBoolean(EXTRA_IS_PASSED);
        long subjectId = getArguments().getLong(EXTRA_SUBJECT_ID);
        mAdapter = new QuestionsAdapter(getActivity(), subjectId, viewMode);
        if (!viewMode)
            mAdapter.setOnAnswerChangeListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_QUESTIONS:
                return new CursorLoader(getActivity(),
                        Question.CONTENT_URI,
                        QuestionsAdapter.QUESTIONS_PROJECTION,
                        Question.TEST_ID + " = ?",
                        new String[]{valueOf(args.getLong(EXTRA_TEST_ID))},
                        Question.SORT_ORDER);
            case LOADER_ANSWERS:
                long testId = args.getLong(EXTRA_TEST_ID);
                Log.d("MyLogs", "loader testing: " + testingId + ", test " + testId);
                return new CursorLoader(getActivity(),
                        QuestionAndAnswer.CONTENT_URI,
                        QuestionsAdapter.ANSWERS_PROJECTION,
                        QuestionAndAnswer.TEST_ID + " = ?",
                        new String[]{valueOf(testingId), String.valueOf(testId)},
                        Question.SORT_ORDER);
            default:
                throw new IllegalArgumentException("Illegal Loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_QUESTIONS:
                mAdapter.changeCursor(data);
                break;
            case LOADER_ANSWERS:
                mAdapter.changeAnswersCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_QUESTIONS:
                mAdapter.changeCursor(null);
                break;
            case LOADER_ANSWERS:
                mAdapter.changeAnswersCursor(null);
                break;
        }
    }

    @Override
    public void onAnswerChanged(long questionId, String answer) {
        Log.d("MyLogs", "answered #" + questionId + ": " + answer + " [" + testingId + "]");
        ContentValues values = new ContentValues();
        values.put(Answer.QUESTION_ID, questionId);
        values.put(Answer.TESTING_ID, testingId);
        values.put(Answer.ANSWER, answer);
        int rowsUpdated = getActivity().getContentResolver().update(Answer.CONTENT_URI,
                values,
                Answer.QUESTION_ID + " = ?" + " AND " + Answer.TESTING_ID + " = ?",
                new String[]{valueOf(questionId), valueOf(testingId)}
        );
        if (rowsUpdated == 0)
            getActivity().getContentResolver().insert(Answer.CONTENT_URI, values);
    }
}
