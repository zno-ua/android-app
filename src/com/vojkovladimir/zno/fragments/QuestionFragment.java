package com.vojkovladimir.zno.fragments;

import java.io.FileNotFoundException;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.R;

public class QuestionFragment extends Fragment{

	int id;
	int taskAll;
	String question;

	TextView tvId;
	TextView tvTaskAll;
	TextView tvQuestion;

	FileManager fm;

	public static QuestionFragment newIntstance(Context context, int id,
			int taskAll, String question) {
		QuestionFragment f = new QuestionFragment();
		f.id = id;
		f.taskAll = taskAll;
		f.question = question;
		f.fm = new FileManager(context);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.question, container, false);

		tvId = (TextView) v.findViewById(R.id.test_question_num);
		tvTaskAll = (TextView) v.findViewById(R.id.test_question_num_full);
		tvQuestion = (TextView) v.findViewById(R.id.test_question_text);

		tvId.setText(getResources().getString(R.string.question) + " " + id);
		tvTaskAll.setText(id + "/" + taskAll);
		tvQuestion.setText(Html.fromHtml(question, imgGetter, null));

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
	
}