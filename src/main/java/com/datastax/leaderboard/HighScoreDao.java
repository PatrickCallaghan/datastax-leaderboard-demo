package com.datastax.leaderboard;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.model.Score;

public class HighScoreDao {

	private static Logger logger = LoggerFactory.getLogger(HighScoreDao.class);
	private AtomicLong TOTAL_POINTS = new AtomicLong(0);
	private Session session;
	private static String keyspaceName = "datastax_leaderboard_demo";
	private static String tableNameAgeOfDarkness = keyspaceName + ".age_of_darkness_leaderboard";
	private static String tableNameLeadboard = keyspaceName + ".stage_leaderboard";

	private static final String INSERT_INTO_TOTAL = "Insert into " + tableNameAgeOfDarkness + " (user,date,total) values (?, ?,?);";
	private static final String SELECT_FROM_TOTAL= "Select total from " + tableNameAgeOfDarkness + " where user = ? ";

	private static final String INSERT_INTO_STAGE_LEADERBOARD = "Insert into " + tableNameLeadboard + " (stage, user,date,high_score) values (?, ?,?,?);";
	private static final String SELECT_FROM_STAGE_LEADERBOARD= "Select high_score from " + tableNameLeadboard + " where user = ? and stage = ?";

	private static final String TOTAL_READ = "select user,total from " + tableNameAgeOfDarkness + "  where solr_query='{\"q\": \"*:*\", \"sort\":\"total desc\"}' limit 10;";
	
	private PreparedStatement insertTotalStmt;
	private PreparedStatement selectTotalStmt;
	private PreparedStatement insertStageLeaderBoardStmt;
	private PreparedStatement selectStageLeaderBoardStmt;
		
	private PreparedStatement totalReadlStmt;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.zzz");
	
	public HighScoreDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();
		
		this.session = cluster.connect();

		this.insertTotalStmt = session.prepare(INSERT_INTO_TOTAL);		
		this.selectTotalStmt = session.prepare(SELECT_FROM_TOTAL);		
		this.insertStageLeaderBoardStmt = session.prepare(INSERT_INTO_STAGE_LEADERBOARD);		
		this.selectStageLeaderBoardStmt = session.prepare(SELECT_FROM_STAGE_LEADERBOARD);		

		this.totalReadlStmt = session.prepare(TOTAL_READ);		
	}

	public void insertScore(Score score) {

		//Now check high score 
		double previousTotal= getTotalScore(score.getUserId());
		
		BoundStatement bound = new BoundStatement(this.insertTotalStmt);
		session.executeAsync(bound.bind(score.getUserId(), score.getTime(), previousTotal + score.getScore()));	
		
		double previousStageScore = getPreviousStageScore(score.getUserId(), score.getStage());
		
		if (score.getScore() > previousStageScore){
			bound = new BoundStatement(this.insertStageLeaderBoardStmt);
			session.executeAsync(bound.bind(score.getStage(), score.getUserId(), score.getTime(), score.getScore()));
		}

	}

	private double getPreviousStageScore(String userId, String stage) {
		BoundStatement bound = new BoundStatement(this.selectStageLeaderBoardStmt);
		ResultSet rs = session.execute(bound.bind(userId, stage));
		
		if (rs.isExhausted()){
			return 0;
		}else{
			rs.one().getDouble("high_score");
		}
		return 0;
	}

	private double getTotalScore(String userId) {
		
		BoundStatement bound = new BoundStatement(this.selectTotalStmt);
		ResultSet rs = session.execute(bound.bind(userId));
		
		if (rs.isExhausted()){
			return 0;
		}else{
			rs.one().getDouble("total");
		}
		return 0;
	}
	
	public void readTotals(){
		logger.info("In Totals");
		SimpleStatement stmt = new SimpleStatement(TOTAL_READ);
		logger.info(stmt.getQueryString());
		stmt.setFetchSize(10);
		ResultSet rs = session.execute(stmt);
		logger.info("Getting results");
		Iterator<Row> iter = rs.iterator();
		
		logger.info("Totals");
		
		while (iter.hasNext()){
			Row row = iter.next();
			logger.info(row.getString("user") + "-" + row.getDouble("total"));			
		}
		logger.info("Finished");
	}
	
	public void readStages(){

		String randomStage = new Double(Math.random()*1000000).intValue() + "";
		
		String cql = "select user,high_score from " + tableNameLeadboard + " where solr_query='{\"q\": \"stage: " + randomStage   
		+ "\", \"sort\":\"high_score desc\"}' limit 10";		
		
		ResultSet rs = session.execute(cql);		
		Iterator<Row> iter = rs.iterator();
		
		logger.info("Top 10 for Stage : " + randomStage);
		while (iter.hasNext()){
			Row row = iter.next();
			logger.info(row.getString("user") + "-" + row.getDouble("high_score"));
		}		
		logger.info("Finished");
	}
	
	
}
