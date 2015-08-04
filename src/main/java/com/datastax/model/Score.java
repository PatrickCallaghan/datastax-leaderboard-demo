package com.datastax.model;

import java.util.Date;

public class Score {
	
	private String stage;
	private String userId;
	private Date time;
	private double score;
	public Score(String stage, String userId, Date time, double score) {
		super();
		this.stage = stage;
		this.userId = userId;
		this.time = time;
		this.score = score;
	}
	public String getStage() {
		return stage;
	}
	public String getUserId() {
		return userId;
	}
	public Date getTime() {
		return time;
	}
	public double getScore() {
		return score;
	}
	@Override
	public String toString() {
		return "Score [stage=" + stage + ", userId=" + userId + ", time=" + time + ", score=" + score + "]";
	}
}
