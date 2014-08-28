package com.vojkovladimir.zno.fragments;

import java.io.FileNotFoundException;
import java.util.ArrayList;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.ViewImageActivity;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.models.Question;

public class QuestionFragment extends Fragment {
	
	Pattern lettersPattern = Pattern.compile("(^.*?)(\\.\\s|\\s)(.*?$)");

	Question question;
	int taskAll;

	FileManager fm;

	public static QuestionFragment newIntstance(Context context, Question question, int taskAll) {
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

		View questionHeader = v.findViewById(R.id.test_question_header);
		TextView questionNum = (TextView) v.findViewById(R.id.test_question_num);
		TextView questionNumFull = (TextView) v.findViewById(R.id.test_question_num_full);
		questionNum.setText(getResources().getString(R.string.question) + " " + question.idTestQuestion);
		questionNumFull.setText(question.idTestQuestion + "/" + taskAll);
		
		TextView questionText = (TextView) v.findViewById(R.id.test_question_text);
		LinearLayout answersList = (LinearLayout) v.findViewById(R.id.test_question_answers_list);
		View answerItem;
		TextView answerItemLetter;
		TextView answerItemText;
		TextView answerCoupleNum;
		Spinner answersCoupleVars;
		EditText answerItemInput;
		
		switch (question.typeQuestion) {
		case Question.TYPE_1: {
			questionHeader.setVisibility(View.VISIBLE);
			questionText.setText(Html.fromHtml(question.question, imgGetter, null));
			questionText.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(question.question.contains("href")){
						openImage(parseSRC(question.question));
					}
				}
			});

			String[] answers = question.answers.split("\n");
			Matcher matcher;

			for (int i = 0; i < answers.length; i++) {		
				matcher = lettersPattern.matcher(answers[i]);

				if(matcher.find()){
					answerItem = inflater.inflate(R.layout.answers_list_item,answersList, false);
					
					answerItemLetter = (TextView) answerItem.findViewById(R.id.answer_item_letter);
					answerItemText = (TextView) answerItem.findViewById(R.id.answer_item_text);
					
					answerItemLetter.setText(Html.fromHtml(matcher.group(1), imgGetter, null));
					answerItemText.setText(Html.fromHtml(matcher.group(3), imgGetter, null));
					
					answersList.addView(answerItem);
				} 
			}
		}

			break;
		case Question.TYPE_2: {
			questionHeader.setVisibility(View.GONE);
			questionText.setText(Html.fromHtml(question.question));
		}
			break;
		case Question.TYPE_3: {
			questionHeader.setVisibility(View.VISIBLE);
			questionText.setText(Html.fromHtml(question.question,imgGetter,null));
			
			int numCounts = Integer.parseInt(question.answers.split("-")[0]);
			int varCounts = Integer.parseInt(question.answers.split("-")[1]);
			
			for (int i = 0; i < numCounts; i++) {
				answerItem = inflater.inflate(R.layout.answers_list_item_couple,answersList, false);
				
				answerCoupleNum = (TextView)answerItem.findViewById(R.id.answer_couple_num);
				answersCoupleVars = (Spinner) answerItem.findViewById(R.id.answer_couple_vars);
				ArrayList<String> vars = new ArrayList<String>();
				for(int j = 0; j< varCounts;j++){
					vars.add(String.valueOf((char)('А'+j)) );
				}
				ArrayAdapter<String> varsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,vars);
				answersCoupleVars.setAdapter(varsAdapter);
				answerCoupleNum.setText(String.valueOf((i+1)+" - "));
				
				answersList.addView(answerItem);
			}

		}
			break;
		case Question.TYPE_4:{
			questionHeader.setVisibility(View.VISIBLE);
			questionText.setText(Html.fromHtml(question.question,imgGetter,null));
			answerItem = inflater.inflate(R.layout.answer_three_correct, answersList, false);
			answerItemInput = (EditText) answerItem.findViewById(R.id.answer_item_three_correct);
			
			answersList.addView(answerItem);			
		}
			break;
		case Question.TYPE_5:{
			questionHeader.setVisibility(View.VISIBLE);
			questionText.setText(Html.fromHtml(question.question,imgGetter,null));
			answerItem = inflater.inflate(R.layout.answer_item_short, answersList, false);
			answerItemInput = (EditText) answerItem.findViewById(R.id.answer_item_input);
			
			answersList.addView(answerItem);			
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

	private void openImage(String source){
		if(source!=null){
			Intent viewImage = new Intent(getActivity(),
					ViewImageActivity.class);
			viewImage.putExtra(ZNOApplication.ExtrasKeys.IMG_SOURCE,
					source);
			startActivity(viewImage);
		}
	}
	
	private String parseSRC(String text){
		Matcher matcher = Pattern.compile("<img src=\"([^\"]+)").matcher(text);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
}