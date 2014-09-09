package com.vojkovladimir.zno.models;

import java.util.ArrayList;

public class Test extends TestInfo{

	public ArrayList<Question> questions;
	
	public Test(TestInfo testInfo, ArrayList<Question> questions) {
		super(testInfo);
		this.questions = questions;
	}

}
