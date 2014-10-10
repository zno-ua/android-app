package com.vojkovladimir.zno.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Test extends TestInfo {

    public ArrayList<Question> questions;

    public Test(TestInfo testInfo, ArrayList<Question> questions) {
    super(testInfo);
        this.questions = questions;
    }

    public String getAnswers() {
        String answers = "[";

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            if (question .type==Question.TYPE_2&&question .balls!=0){
                if(question.userAnswer.isEmpty()){
                    question.userAnswer = String.valueOf(question.balls / 2);
                }
            }
            answers += String.format("\"%s\"", questions.get(i).userAnswer);
            if (i != questions.size() - 1) {
                answers += ",";
            }
        }

        answers += "]";
        return answers;
    }

    public void putAnswers(String savedAnswers) {
        if (savedAnswers != null) {
            try {
                JSONArray answers = new JSONArray(savedAnswers);
                for (int i = 0; i < answers.length(); i++) {
                    questions.get(i).userAnswer = answers.getString(i);
                }
            } catch (JSONException e) {
                Log.e("MyLogs", e.toString());
                e.printStackTrace();
            }
        }
    }

    public int getTestBall() {
        int ball = 0;
        for (Question question : questions) {
            ball += question.getBall();
        }
        return ball;
    }

    public boolean hasUnAnsweredQuestions() {
        for (Question question : questions) {
            if (!question.isAnswered()) {
                return true;
            }
        }
        return false;
    }
}
