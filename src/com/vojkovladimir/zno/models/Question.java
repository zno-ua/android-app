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
    public String userAnswer;

    public Question(int id, String question, String parentQuestion, String answers, String correctAnswer, int balls, int type, String userAnswer) {
        this.id = id;
        this.question = question.replace("<a href=\"", "<a href=\"open.image://?src=");
        this.parentQuestion = parentQuestion;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.balls = balls;
        this.type = type;
        if (userAnswer != null) {
            this.userAnswer = userAnswer;
        } else if (type == TYPE_3) {
            this.userAnswer = "";
            for (int i = 0; i < Integer.parseInt(this.answers.split("-")[0]); i++) {
                this.userAnswer += "0";
            }
        } else {
            this.userAnswer = new String();
        }
    }

    public int getBall() {
        switch (type) {
            case TYPE_2: {
                if (balls != 0) {
                    return Integer.parseInt(userAnswer);
                }
            }
            case TYPE_4: {
                int ball = 0;
                StringBuilder correctAnswer = new StringBuilder(this.correctAnswer);
                for (int i = 0; i < userAnswer.length(); i++) {
                    int index = correctAnswer.indexOf(String.valueOf(userAnswer.charAt(i)));
                    if (index != -1) {
                        ball++;
                        correctAnswer.deleteCharAt(index);
                    }
                }
                return ball;
            }
            case TYPE_3: {
                if (answers.charAt(0) == answers.charAt(2)) {
                    boolean is_full = true;
                    for (int i = 0; i < correctAnswer.length(); i++) {
                        if (correctAnswer.charAt(i) != userAnswer.charAt(i)) {
                            is_full = false;
                            break;
                        }
                    }
                    if (is_full) {
                        return 3;
                    } else {
                        int lastId = correctAnswer.length() - 1;
                        if (correctAnswer.charAt(lastId) == userAnswer.charAt(lastId)) {
                            if (correctAnswer.charAt(0) == userAnswer.charAt(0)) {
                                return 2;
                            } else {
                                return 1;
                            }
                        }
                    }
                } else {
                    int ball = 0;
                    for (int i = 0; i < correctAnswer.length(); i++) {
                        if (correctAnswer.charAt(i) == userAnswer.charAt(i)) {
                            ball++;
                        }
                    }
                    return ball;
                }
            }
            case TYPE_1:
            case TYPE_5:
                if (userAnswer.equals(correctAnswer)) {
                    return balls;
                }
            default:
                return 0;
        }
    }

    public boolean isAnswered() {
        switch (type) {
            case TYPE_3:
                if (userAnswer.contains("0")) {
                    return false;
                }
                return true;
            default:
                if (userAnswer.isEmpty()) {
                    return false;
                }
                return true;
        }
    }
}
