package com.vojkovladimir.zno.fragments;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.ViewImageActivity;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.models.Question;

public class QuestionFragment extends Fragment {

	private static final char ENG_LETTER = 65;
	private static final char UKR_LETTER = 1040;

	Question question;
	int taskAll;
	char firstLetter;

	FileManager fm;

	QuestionActions questionActions;

	public static QuestionFragment newInstance(Context context, Question question, int taskAll,
			int lessonId,QuestionActions questionActions) {
		QuestionFragment f = new QuestionFragment();
		f.question = question;
		f.taskAll = taskAll;
		f.fm = new FileManager(context);
		f.firstLetter = (lessonId == 7) ? ENG_LETTER : UKR_LETTER;
		f.questionActions = questionActions;
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		switch (question.typeQuestion) {
		case Question.TYPE_1:
			return createTypeOneQuestionView(inflater, container);
		case Question.TYPE_2:
			return createTypeTwoQuestionView(inflater, container);
		case Question.TYPE_3:
			return createTypeThreeQuestionView(inflater, container);
		case Question.TYPE_4:
			return createTypeFourQuestionView(inflater, container);
		case Question.TYPE_5:
			return createTypeFiveQuestionView(inflater, container);
		default:
			View v = inflater.inflate(R.layout.test_question, container, false);
			return v;
		}
	}

	private View createTypeOneQuestionView(LayoutInflater inflater, ViewGroup container) {
		View v = inflater.inflate(R.layout.test_question, container, false);
		LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);
		
		questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
		questionContainer.addView(createQuestionText(inflater, questionContainer));
		
		LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
		questionContainer.addView(answersContainer);
				
		String[] answers = question.answers.split("\n");
		final View[] answerItems = new View[answers.length];
		Pattern answerPattern = Pattern.compile("(^.*?)(\\.\\s|\\s)(.*?$)");
		Matcher matcher;

		TextView answerItemLetter;
		TextView answerItemText;

		for (int i = 0; i < answers.length; i++) {
			answerItems[i] = inflater.inflate(R.layout.answer, answersContainer, false);

			answerItemLetter = (TextView) answerItems[i].findViewById(R.id.answer_letter);
			answerItemText = (TextView) answerItems[i].findViewById(R.id.answer_text);

			matcher = answerPattern.matcher(answers[i]);

			if (matcher.find()) {
				answerItemLetter.setText(Html.fromHtml(matcher.group(1), imgGetter, null));
				answerItemText.setText(Html.fromHtml(matcher.group(3), imgGetter, null));

				final int num = i;
				answerItems[i].setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!question.answer.isEmpty()) {
							int oldNum = Integer.valueOf(question.answer) - 1;
							answerItems[oldNum].setSelected(false);
						}
						answerItems[num].setSelected(true);
						question.answer = String.valueOf((num + 1));
						questionActions.onAnswerSelected();
					}
				});

				answersContainer.addView(answerItems[i]);
			}
		}

		if (!question.answer.isEmpty()) {
			int num = Integer.valueOf(question.answer) - 1;
			answerItems[num].setSelected(true);
		}

		return v;
	}

	private View createTypeTwoQuestionView(LayoutInflater inflater, ViewGroup container) {
		View v = inflater.inflate(R.layout.test_question, container, false);
		LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);
		
		questionContainer.addView(createQuestionText(inflater, questionContainer));
		
		return v;
	}

	private View createTypeThreeQuestionView(LayoutInflater inflater, ViewGroup container) {
		View v = inflater.inflate(R.layout.test_question, container, false);
		LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

		questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
		questionContainer.addView(createQuestionText(inflater, questionContainer));
		
		LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
		questionContainer.addView(answersContainer);

		int numCounts = Integer.parseInt(question.answers.split("-")[0]);
		int varCounts = Integer.parseInt(question.answers.split("-")[1]);

		final View[] answerItems = new View[numCounts];
		final View[][] answerItemLetters = new View[numCounts][varCounts];

		LinearLayout answerLettersContainer;
		TextView answerCoupleNum;
		TextView answerItemLetter;

		for (int i = 0; i < answerItems.length; i++) {
			answerItems[i] = inflater.inflate(R.layout.answers_connections, answersContainer, false);

			answerCoupleNum = (TextView) answerItems[i].findViewById(R.id.answer_couple_num);
			answerCoupleNum.setText(String.valueOf((i + 1)));
			answerLettersContainer = (LinearLayout) answerItems[i].findViewById(R.id.answer_couple_letters_container);

			final int num = i;
			for (int j = 0; j < answerItemLetters[0].length; j++) {
				answerItemLetters[i][j] = inflater.inflate(R.layout.answers_letter,
						answerLettersContainer, false);
				answerItemLetter = (TextView) answerItemLetters[i][j].findViewById(R.id.answer_letter);
				answerItemLetter.setText(String.valueOf((char) (firstLetter + j)));
				final int letterNum = j;
				answerItemLetters[i][j]
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								StringBuilder sb = new StringBuilder(question.answer);
								char oldNumLetter = sb.charAt(num);
								char newNumLetter = (char) ('0' + letterNum + 1);
								if (oldNumLetter != '0') {
									answerItemLetters[num][oldNumLetter - '0' - 1].setSelected(false);
								}
								int usedNumLetter = sb.indexOf(String.valueOf(newNumLetter));
								if (usedNumLetter != -1) {
									answerItemLetters[usedNumLetter][letterNum].setSelected(false);
									sb.setCharAt(usedNumLetter, '0');
								}

								sb.setCharAt(num, newNumLetter);
								answerItemLetters[num][letterNum].setSelected(true);
								question.answer = sb.toString();
								if (!question.answer.contains("0")) {
									questionActions.onAnswerSelected();
								}
							}
						});

				answerLettersContainer.addView(answerItemLetters[i][j]);
			}
			char oldNumLetter = question.answer.charAt(i);
			if (oldNumLetter != '0') {
				answerItemLetters[i][oldNumLetter - '0' - 1].setSelected(true);
			}
			answersContainer.addView(answerItems[i]);
		}

		return v;
	}

	private View createTypeFourQuestionView(LayoutInflater inflater, ViewGroup container) {
		View v = inflater.inflate(R.layout.test_question, container, false);
		LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

		questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
		questionContainer.addView(createQuestionText(inflater, questionContainer));
		
		LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
		questionContainer.addView(answersContainer);
		
		View answerItem = inflater.inflate(R.layout.answer_only_correct,answersContainer, false);
		EditText answerItemInput = (EditText) answerItem.findViewById(R.id.answer_only_correct_input);

		if (!question.answer.isEmpty()) {
			answerItemInput.setText(question.answer);
		}

		answerItemInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				question.answer = s.toString();
			}
		});
		
		answerItemInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					hideKeyboard();
					if (question.answer.length() == 3) {
						questionActions.onAnswerSelected();
					}
					return true;
				}
				return false;
			}
		});


		answersContainer.addView(answerItem);
		return v;
	}

	private View createTypeFiveQuestionView(LayoutInflater inflater,
			ViewGroup container) {
		View v = inflater.inflate(R.layout.test_question, container, false);
		LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

		questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
		questionContainer.addView(createQuestionText(inflater, questionContainer));
		
		LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
		questionContainer.addView(answersContainer);

		View answerItem = inflater.inflate(R.layout.answer_short, answersContainer, false);
		EditText answerItemInput = (EditText) answerItem.findViewById(R.id.answer_input);

		if (!question.answer.isEmpty()) {
			answerItemInput.setText(question.answer);
		}

		answerItemInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				question.answer = s.toString();
			}
		});

		answerItemInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					hideKeyboard();
					if (!question.answer.isEmpty()) {
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								questionActions.onAnswerSelected();
							}
							
						}, 200);
					}
					return true;
				}
				return false;
			}
		});

		answersContainer.addView(answerItem);
		return v;
	}
	
	private View createQuestionHeaderView(LayoutInflater inflater, ViewGroup container) {
		View v = inflater.inflate(R.layout.test_question_header, container, false);
		
		TextView questionId = (TextView) v.findViewById(R.id.test_question_id);
		TextView questionTaskAll = (TextView) v.findViewById(R.id.test_question_task_all);
		
		questionId.setText(getResources().getString(R.string.question) + " " + question.idTestQuestion);
		questionTaskAll.setText(question.idTestQuestion + "/" + taskAll);
		
		return v;
	}
	
	private View createQuestionText(LayoutInflater inflater, ViewGroup container){		
		TextView questionText = (TextView) inflater.inflate(R.layout.test_question_text, container, false);
		questionText.setText(Html.fromHtml(question.question, imgGetter, null));
		questionText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (question.question.contains("href")) {
					openImage(parseSRC(question.question));
				}
			}
		});		
		
		return questionText;
	}
	
	private LinearLayout createAnswersContainer(LayoutInflater inflater, ViewGroup container){		
		return (LinearLayout) inflater.inflate(R.layout.test_question_answers, container, false);
	}

	private ImageGetter imgGetter = new ImageGetter() {

		public Drawable getDrawable(String source) {
			Drawable drawable = null;

			try {
				drawable = fm.openDrawable(source);
			} catch (FileNotFoundException e) {
					e.printStackTrace();
			}
			
			if (drawable == null) {
				drawable = getResources().getDrawable(R.drawable.emo_im_crying);
			}

			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

			int width = (int) (drawable.getIntrinsicWidth() * displayMetrics.scaledDensity);
			int heigth = (int) (drawable.getIntrinsicHeight() * displayMetrics.scaledDensity);
			int maxWidth = (int) (displayMetrics.widthPixels * 0.75f);

			if (width > maxWidth) {
				float scale = (float) maxWidth / (float) width;
				width = maxWidth;
				heigth = (int) (heigth * scale);
			}

			drawable.setBounds(0, 0, width, heigth);

			return drawable;
		}
	};

	private void openImage(String source) {
		if (source != null) {
			Intent viewImage = new Intent(getActivity(), ViewImageActivity.class);
			viewImage.putExtra(ZNOApplication.ExtrasKeys.IMG_SOURCE, source);
			startActivity(viewImage);
		}
	}

	private String parseSRC(String text) {
		Matcher matcher = Pattern.compile("<img src=\"([^\"]+)").matcher(text);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public interface QuestionActions {
		void onAnswerSelected();
	}
	
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	}

}