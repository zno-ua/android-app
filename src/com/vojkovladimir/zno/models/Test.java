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
            answers += String.format("\"%s\"", questions.get(i).userAnswer);
            if (i != questions.size() - 1) {
                answers += ",";
            }
        }

        answers += "]";
        Log.i("MyLogs", "Test.getAnswers");
        return answers;
    }

    public void putAnswers(String savedAswers) {
        Log.i("MyLogs", "Test.putAnswers");
        try {
            JSONArray answers = new JSONArray(savedAswers);
            for (int i = 0; i < answers.length(); i++) {
                questions.get(i).userAnswer = answers.getString(i);
            }
        } catch (JSONException e) {
            Log.e("MyLogs", e.toString());
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e("MyLogs", "savedAswers = null");
        }
    }

    public int getTestBall() {
        int ball = 0;
        for (Question question : questions) {
            ball += question.getBall();
        }
        return ball;
    }
}
