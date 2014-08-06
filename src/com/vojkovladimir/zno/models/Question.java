package com.vojkovladimir.zno.models;

public class Question {

	public int id;
	public int idTestQuestion;
	public String question;
	public String answers;
	public String correctAnswer;
	public int balls;
	public int typeQuestion;
	public String test;

	public Question(int id, int idTestQuestion, String question,
			String answers, String correctAnswer, int balls, int typeQuestion,
			String test) {
		this.id = id;
		this.idTestQuestion = idTestQuestion;
		this.question = question;
		this.answers = answers;
		this.correctAnswer = correctAnswer;
		this.balls = balls;
		this.typeQuestion = typeQuestion;
		this.test = test;
	}

}
