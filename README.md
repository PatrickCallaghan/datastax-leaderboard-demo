Leaderboard Demo
====================

This demo shows how to use DataStax Enterprise to get the highest scores on a leaderboard of millions of users. This demo assumes the same user cannot simultaneously play the same game.  This is based on a imaginery game called 'age of darkness'. There is one overall leaderboard and then separate leaderboards for 100,000 thousand stages. This may represent different levels, settings or difficulty settings. The requirement is to provide an overall total leaderboard which just appends points won to an overall score and a stage leaderboard to show the best at each level.

## Running the demo 

You will need a java runtime along with maven 3 to run this demo. Start DSE 4.7.x in SearchAnalytics mode. This can be done using 

	'dse cassandra -s -k'
	
You can refer to the documentation for other options.

This demo just runs as a standalone process on the localhost.


## Schema Setup
Note : This will drop the keyspace "datastax_leaderboard_demo" and create a new one. All existing data will be lost. 

The schema can be found in src/main/resources/cql/

To specify contact points use the contactPoints command line parameter e.g. '-DcontactPoints=192.168.25.100,192.168.25.101'
The contact points can take mulitple points in the IP,IP,IP (no spaces).

To create the a keyspace with replication factor of 3 for a SearchAnalytics DC, run the following

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup"

To start inserting run

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.leaderboard.Main"
    
The default example uses 1Million users but these can be changed by adding the argument 'noOfUsers'. To change the demo to use 100000 users add the following '-DnoOfUsers=100000' 

In another window, create the Solr cores for our tables. Run 

	dsetool create_core datastax_leaderboard_demo.age_of_darkness_leaderboard generateResources=true reindex=true
	
	dsetool create_core datastax_leaderboard_demo.stage_leaderboard generateResources=true reindex=true
	
We will use the command line tool, cqlsh. To use the limit properly with Solr Search queries, turn off the pagination using
	
	paging off
	
Then we can run the following to get the top ten overall scores.  
	
	select user,total from age_of_darkness_leaderboard where solr_query='{"q": "*:*", "sort":"total desc"}' limit 10 ;
	
To look at a specific stage, we can use

	select user,high_score from stage_leaderboard where solr_query='{"q": "stage:1321", "sort":"high_score desc"}' limit 10 ;

Or to look at the high_scores over all stages, we can use 

	select stage,user,high_score from stage_leaderboard where solr_query='{"q": "*:*", "sort":"high_score desc"}' limit 10 ;
	

	