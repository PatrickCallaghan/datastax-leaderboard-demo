package com.datastax.leaderboard;

import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicLong;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.model.Score;

public class HighScoreDao {

	private AtomicLong TOTAL_POINTS = new AtomicLong(0);
	private Session session;
	private static String keyspaceName = "datastax_leaderboard_demo";
	private static String tableNameAgeOfDarkness = keyspaceName + ".age_of_darkness_leaderboard";
	private static String tableNameLeadboard = keyspaceName + ".stage_leaderboard";

	private static final String INSERT_INTO_TOTAL = "Insert into " + tableNameAgeOfDarkness + " (user,date,total) values (?, ?,?);";
	private static final String SELECT_FROM_TOTAL= "Select total from " + tableNameAgeOfDarkness + " where user = ? ";

	private static final String INSERT_INTO_STAGE_LEADERBOARD = "Insert into " + tableNameLeadboard + " (stage, user,date,high_score) values (?, ?,?,?);";
	private static final String SELECT_FROM_STAGE_LEADERBOARD= "Select high_score from " + tableNameLeadboard + " where user = ? and stage = ?";

	private PreparedStatement insertTotalStmt;
	private PreparedStatement selectTotalStmt;
	private PreparedStatement insertStageLeaderBoardStmt;
	private PreparedStatement selectStageLeaderBoardStmt;
		
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.zzz"); 

	public HighScoreDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();
		
		this.session = cluster.connect();

		this.insertTotalStmt = session.prepare(INSERT_INTO_TOTAL);		
		this.selectTotalStmt = session.prepare(SELECT_FROM_TOTAL);		
		this.insertStageLeaderBoardStmt = session.prepare(INSERT_INTO_STAGE_LEADERBOARD);		
		this.selectStageLeaderBoardStmt = session.prepare(SELECT_FROM_STAGE_LEADERBOARD);		

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
	
}
