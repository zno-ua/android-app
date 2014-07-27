package com.vojkovladimir.zno.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vojkovladimir.zno.R;

public class QuestionFragment extends Fragment {

	int num;
	int numMax;
	String text;

	TextView questNum;
	TextView questNumFull;
	TextView questText;
	
	public static QuestionFragment newIntstance(int num, int numMax, String text){
		QuestionFragment f = new QuestionFragment();
		f.num = num;
		f.numMax = numMax;
		f.text = text;
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.question, null);

		questNum = (TextView) v.findViewById(R.id.test_question_num);
		questNumFull = (TextView) v.findViewById(R.id.test_question_num_full);
		questText = (TextView) v.findViewById(R.id.test_question_text);

		questNum.setText(getResources().getString(R.string.question) + " "
				+ num);
		questNumFull.setText(num + "/" + numMax);
		questText.setText(text);

		return v;
	}

}