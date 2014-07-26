package com.vojkovladimir.zno.models;

public class Question {

	public int id;
	public int idQuest;
	public int type;
	public String text;
	public String answers;
	public String correct;
	public int ball;

	public Question(int id, int idQuest, int type, String text, String answers,
			String correct, int ball) {
		this.id = id;
		this.idQuest = idQuest;
		this.type = type;
		this.text = text;
		this.answers = answers;
		this.correct = correct;
		this.ball = ball;

	}
	
	@Override
	public String toString() {
		return "# "+id+" "+idQuest+" "+type+" "+text+" "+answers+" "+correct+" "+ball+" @";
	}

}
