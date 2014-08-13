package com.vojkovladimir.zno.fragments;

import java.io.FileNotFoundException;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.R;

public class AnswersFragment extends Fragment {

	FileManager fm;

	Spanned[] letters;
	String[] answers;
	LinearLayout answersList;

	public static AnswersFragment newIntstance(Context context, String[] answers) {
		AnswersFragment f = new AnswersFragment();
		f.answers = new String[answers.length];
		f.letters = new Spanned[answers.length];
		String[] tmp;
		for (int i = 0; i < answers.length; i++) {
			tmp = answers[i].split(". ", 2);
			if (tmp.length == 2) {
				f.letters[i] = Html.fromHtml(tmp[0]);
				f.answers[i] = tmp[1];
			} else {
				f.letters[i] = Html.fromHtml("");
				f.answers[i] = "";
			}
		}
		f.fm = new FileManager(context);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		answersList = (LinearLayout) inflater.inflate(R.layout.answers_list,
				container, false);
		TextView letter;
		TextView answer;
		View answerItem;
		for (int i = 0; i < answers.length; i++) {
			answerItem = inflater.inflate(R.layout.answers_list_item, answersList,false);

			answer = (TextView) answerItem.findViewById(R.id.answer_item_text);
			answer.setText(Html.fromHtml(answers[i], imgGetter, null));

			letter = (TextView) answerItem
					.findViewById(R.id.answer_item_letter);
			letter.setText(letters[i]);

			answersList.addView(answerItem);
		}
		return answersList;
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

}
