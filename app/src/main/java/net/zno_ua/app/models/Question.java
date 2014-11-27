package net.zno_ua.app.models;


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
    private String userAnswer;

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void makeUserAnswer() {
        if (type == TYPE_3) {
            this.userAnswer = String.format("%0" + answers.split("-")[0] + "d", 0);
        } else {
            this.userAnswer = "";
        }
    }

    public int getBall() {
        switch (type) {
            case TYPE_2: {
                if (balls != 0) {
                    if (userAnswer.isEmpty()) {
                        return balls / 2;
                    }
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
            case TYPE_2:
                return !(userAnswer.isEmpty() || userAnswer.equals(String.valueOf(balls / 2)));
            case TYPE_3:
                return !userAnswer.contains("0");
            default:
                return !userAnswer.isEmpty();
        }
    }

    public boolean isCorrect() {
        return type == TYPE_2 && balls != 0 || userAnswer.equals(correctAnswer);
    }

}
