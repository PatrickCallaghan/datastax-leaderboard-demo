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
	private static String tableNameTick = keyspaceName + ".age_of_darkness_leaderboard";

	private static final String INSERT_INTO_HIGHSCORE = "Insert into " + tableNameTick + " (user,date,high_score) values (?, ?,?);";
	private static final String SELECT_FROM_HIGHSCORE= "Select high_score from " + tableNameTick + " where user = ? ";

	private PreparedStatement insertStmt;
	private PreparedStatement selectStmt;
	
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.zzz"); 

	public HighScoreDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();
		
		this.session = cluster.connect();

		this.insertStmt = session.prepare(INSERT_INTO_HIGHSCORE);		
		//this.insertStmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
		
		this.selectStmt = session.prepare(SELECT_FROM_HIGHSCORE);		
	//	this.selectStmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

	}

	public void insertScore(Score score) {

		//Insert score for user
		
		//Now check high score 
		double previousHighScore = getPreviousHighScore(score.getUserId());
		
		if (score.getScore() > previousHighScore){
			BoundStatement bound = new BoundStatement(this.insertStmt);
			session.execute(bound.bind(score.getUserId(), score.getTime(), score.getScore()));
		}
	}

	private double getPreviousHighScore(String userId) {
		
		BoundStatement bound = new BoundStatement(this.selectStmt);
		ResultSet rs = session.execute(bound.bind(userId));
		
		if (rs.isExhausted()){
			return 0;
		}else{
			rs.one().getDouble("high_score");
		}
		return 0;
	}
	
}
