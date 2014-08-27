package com.vojkovladimir.zno.models;

public class Question {

	public static final int TYPE_1 = 1;
	public static final int TYPE_2 = 2;
	public static final int TYPE_3 = 3;
	public static final int TYPE_4 = 4;
	public static final int TYPE_5 = 5;

	public int id;
	public int idTestQuestion;
	public String question;
	public String answers;
	public String correctAnswer;
	public int balls;
	public int typeQuestion;

	public Question(int id, int idTestQuestion, String question,
			String answers, String correctAnswer, int balls, int typeQuestion) {
		this.id = id;
		this.idTestQuestion = idTestQuestion;
		this.question = question;
		this.answers = answers;
		this.correctAnswer = correctAnswer;
		this.balls = balls;
		this.typeQuestion = typeQuestion;
	}

}
