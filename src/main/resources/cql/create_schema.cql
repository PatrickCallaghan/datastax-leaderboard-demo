create keyspace if not exists datastax_leaderboard_demo WITH replication = {'class': 'NetworkTopologyStrategy', 'SearchAnalytics' : 3};

use datastax_leaderboard_demo; 

CREATE TABLE if not exists age_of_darkness_leaderboard ( 
	user text,
	date timestamp,	
	total double,
	PRIMARY KEY (user)
) WITH compaction= { 'class': 'LeveledCompactionStrategy', 'tombstone_threshold':'0.1' };


CREATE TABLE if not exists stage_leaderboard ( 
	stage text,
	user text,
	date timestamp,	
	high_score double,
	PRIMARY KEY ((stage, user))
) WITH compaction= { 'class': 'LeveledCompactionStrategy', 'tombstone_threshold':'0.1'  };

CREATE TABLE if not exists stage_bucket_leaderboard ( 
	stage text,
	bucket int,
	user text,
	date timestamp,	
	high_score double,
	PRIMARY KEY ((stage, bucket), user)
) WITH compaction= { 'class': 'LeveledCompactionStrategy', 'tombstone_threshold':'0.1'  };

truncate age_of_darkness_leaderboard;
truncate stage_leaderboard;
