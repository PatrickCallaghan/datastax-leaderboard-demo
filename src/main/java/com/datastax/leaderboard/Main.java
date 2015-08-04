package com.datastax.leaderboard;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;
import com.datastax.model.Score;

public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	private String ONE_MILLION = "1000000";
	private String TEN_MILLION = "10000000";
	private String FIFTY_MILLION = "50000000";
	private String ONE_HUNDRED_MILLION = "100000000";
	private String ONE_BILLION = "1000000000";
	
	private Random rand = new Random();
	private int counter;

	public Main() {

		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
		String noOfThreadsStr = PropertyHelper.getProperty("noOfThreads", "10");
		String noOfUsersStr = PropertyHelper.getProperty("noOfUsers", ONE_MILLION);
		
		HighScoreDao dao = new HighScoreDao(contactPointsStr.split(","));
		
		int noOfThreads = Integer.parseInt(noOfThreadsStr);
		long noOfUsers = Long.parseLong(noOfUsersStr);		
			
		logger.info("Processing " + NumberFormat.getInstance().format(noOfUsers) + " users");
		Timer timer = new Timer();
		
		//Start processing random scores
		while (true){
			dao.insertScore(createRandomScore(noOfUsers));
			counter ++;
			
			if (counter%1000 == 0){
				sleep(100);
				logger.info("Processed : " + counter);
			}
		}
	}
	
	private Score createRandomScore(long noOfUsers) {
		
		double score = rand.nextGaussian() * 100;
		
		score = score < 0 ? score*-1 : score; 
		
		String userId = new Double(Math.random()*noOfUsers).intValue() + "";
		String stage = new Double(Math.random()*100000).intValue() + "";
		
		return new Score (stage, userId, new Date(), score);
	}

	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();
	}
}
