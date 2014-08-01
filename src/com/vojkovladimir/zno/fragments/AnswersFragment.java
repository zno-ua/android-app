package com.vojkovladimir.zno.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vojkovladimir.zno.R;

public class AnswersFragment extends Fragment {

	Spanned[] letters;
	Spanned[] answers;
	LinearLayout answersList;

	public static AnswersFragment newIntstance(String[] answers) {
		AnswersFragment f = new AnswersFragment();
		f.answers = new Spanned[answers.length];
		f.letters = new Spanned[answers.length];
		String[] tmp;
		for (int i = 0; i < answers.length; i++) {
			tmp = answers[i].split(". ", 2);
			if (tmp.length == 2) {
				f.letters[i] = Html.fromHtml(tmp[0]);
				f.answers[i] = Html.fromHtml(tmp[1]);
			} else {
				f.letters[i] = Html.fromHtml("");
				f.answers[i] = Html.fromHtml("");
			}
		}
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		answersList = (LinearLayout) inflater.inflate(R.layout.answers_list,
				null);
		TextView letter;
		TextView answer;
		View answerItem;
		for (int i = 0; i < answers.length; i++) {
			answerItem = inflater.inflate(R.layout.answers_list_item, null);

			answer = (TextView) answerItem.findViewById(R.id.answer_item_text);
			answer.setText(answers[i]);

			letter = (TextView) answerItem
					.findViewById(R.id.answer_item_letter);
			letter.setText(letters[i]);

			answersList.addView(answerItem);
		}
		return answersList;
	}

}
