package com.vojkovladimir.zno.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;
import com.vojkovladimir.zno.models.TestInfo;

public class QuestionFragmentAdapter {

	Context context;

	FragmentManager manager;
	FragmentTransaction transaction;
	QuestionFragment questionFragment;
	QuestionFragment prevQuestionFragment;

	ZNODataBaseHelper db;

	ArrayList<Question> questionsAll;
	ArrayList<Question> questions;

	TestInfo test;
	int current = 0;

	public QuestionFragmentAdapter(Activity parentActivity, Test test, ZNODataBaseHelper db) {
		this.context = parentActivity;
		this.questionsAll = test.questionsAll;
		this.questions = test.questions;
		this.test = test;
		manager = parentActivity.getFragmentManager();
		this.db = db;
		load();
	}

	public void next() {
		if (current < test.taskAll - 1) {
			current++;
			load();
		}
	}

	public void previous() {
		if (current > 0) {
			current--;
			load();
		}
	}

	public void load(int i) {
		current = i;
		load();
	}

	public int getCurrent() {
		return current;
	}

	private void load() {
		Question question = questions.get(current);
		transaction = manager.beginTransaction();
		
		if(questionFragment != null){
			transaction.remove(questionFragment);
			questionFragment = null;
		}
		
		if(prevQuestionFragment != null){
			transaction.remove(prevQuestionFragment);
			prevQuestionFragment = null;
		}
		
		questionFragment = QuestionFragment.newIntstance(context, question, test.taskAll,test.lessonId);
		
		if(question.id > question.idTestQuestion){
			if(current == 0 || (question.id - questions.get(current-1).id)>1){
				prevQuestionFragment = QuestionFragment.newIntstance(context, questionsAll.get(question.id-2), test.taskAll,test.lessonId);
				transaction.add(R.id.test_question_container, prevQuestionFragment);
				transaction.add(R.id.test_question_container, questionFragment);
			}else{
				transaction.replace(R.id.test_question_container, questionFragment);
			}
			
		}else{
			transaction.replace(R.id.test_question_container, questionFragment);
		}

		transaction.commit();
	}

}
