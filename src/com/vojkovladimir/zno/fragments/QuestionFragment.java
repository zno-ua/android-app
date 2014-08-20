package com.vojkovladimir.zno.fragments;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.ViewImageActivity;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.models.Question;

public class QuestionFragment extends Fragment implements OnClickListener {

	Question question;
	int taskAll;

	FileManager fm;

	public static QuestionFragment newIntstance(Context context,
			Question question, int taskAll) {
		QuestionFragment f = new QuestionFragment();
		f.question = question;
		f.taskAll = taskAll;
		f.fm = new FileManager(context);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.question, container, false);

		switch (question.typeQuestion) {
		case Question.TYPE_1: {
			v.findViewById(R.id.test_question_header).setVisibility(
					View.VISIBLE);

			TextView tvId = (TextView) v.findViewById(R.id.test_question_num);
			TextView tvTaskAll = (TextView) v
					.findViewById(R.id.test_question_num_full);
			TextView tvQuestion = (TextView) v
					.findViewById(R.id.test_question_text);

			tvId.setText(getResources().getString(R.string.question) + " "
					+ question.idTestQuestion);
			tvTaskAll.setText(question.idTestQuestion + "/" + taskAll);
			tvQuestion.setText(Html
					.fromHtml(question.question, imgGetter, null));
			tvQuestion.setOnClickListener(this);

			LinearLayout answersList = (LinearLayout) v
					.findViewById(R.id.test_question_answers_list);

			String[] answers = question.answers.split("\n");

			TextView tvLetter;
			TextView tvAnswer;
			View answerItem;
			String answer;
			String letter;
			String[] tmp;

			for (int i = 0; i < answers.length; i++) {
				answerItem = inflater.inflate(R.layout.answers_list_item,
						answersList, false);

				tmp = answers[i].split(". ", 2);
				if (tmp.length == 2) {
					letter = tmp[0];
					answer = tmp[1];
				} else {
					letter = "";
					answer = "";
					break;
				}
				tvAnswer = (TextView) answerItem
						.findViewById(R.id.answer_item_text);
				tvAnswer.setText(Html.fromHtml(answer, imgGetter, null));

				tvLetter = (TextView) answerItem
						.findViewById(R.id.answer_item_letter);
				tvLetter.setText(Html.fromHtml(letter, imgGetter, null));

				answersList.addView(answerItem);
			}
		}

			break;
		case Question.TYPE_2: {
			v.findViewById(R.id.test_question_header).setVisibility(View.GONE);
			TextView tvQuestion = (TextView) v
					.findViewById(R.id.test_question_text);
			tvQuestion.setText(Html.fromHtml(question.question));
		}
			break;
		}

		return v;
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

	@Override
	public void onClick(View v) {
		if (question.question.contains("href")) {
			Matcher matcher = Pattern.compile("<img src=\"([^\"]+)").matcher(
					question.question);
			while (matcher.find()) {
				Intent viewImage = new Intent(getActivity(),
						ViewImageActivity.class);
				viewImage.putExtra(ZNOApplication.ExtrasKeys.IMG_SOURCE,
						matcher.group(1));
				startActivity(viewImage);
			}
		}
	}
}