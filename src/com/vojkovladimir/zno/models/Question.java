package com.vojkovladimir.zno.models;


public class Question {

    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;
    public static final int TYPE_3 = 3;
    public static final int TYPE_4 = 4;
    public static final int TYPE_5 = 5;

    public static final String ID = "id";
    public static final String QUESTION = "question";
    public static final String PARENT_QUESTION = "parent_question";
    public static final String ANSWERS = "answers";
    public static final String ANSWER = "answer";
    public static final String TYPE = "type";
    public static final String BALLS = "balls";
    public static final String CORRECT_ANSWER = "correct_answer";

    public int id;
    public String question;
    public String parentQuestion;
    public String answers;
    public String correctAnswer;
    public int balls;
    public int type;
    public String answer;

    public Question(int id, String question, String parentQuestion, String answers, String correctAnswer, int balls, int type, String answer) {
        this.id = id;
        this.question = question.replace("<a href=\"", "<a href=\"open.image://?src=");
        this.parentQuestion = parentQuestion;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.balls = balls;
        this.type = type;
        if (answer != null) {
            this.answer = answer;
        } else if (type == TYPE_3) {
            this.answer = "";
            for (int i = 0; i < Integer.parseInt(this.answers.split("-")[0]); i++) {
                this.answer += "0";
            }
        } else {
            this.answer = new String();
        }
    }

}
