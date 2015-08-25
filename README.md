Leaderboard Demo
====================

## Running the demo 

You will need a java runtime along with maven 3 to run this demo. Start DSE version 4.7.1 or greater in SearchAnalytics mode. This can be done using 

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

In another window, create the Solr cores for our tables. Follow the steps in the Commands.txt file in the Solr directory in src/main/resources. This will give you the most optimal version. 

For a quick version you can use the dsetool 

	dsetool create_core datastax_leaderboard_demo.age_of_darkness_leaderboard generateResources=true reindex=true
	
	dsetool create_core datastax_leaderboard_demo.stage_leaderboard generateResources=true reindex=true
	
We will use the command line tool, cqlsh. Then we can run the following to get the top ten overall scores.  
	
	select user,total from age_of_darkness_leaderboard where solr_query='{"q": "*:*", "sort":"total desc"}' limit 10 ;
	
To look at a specific stage, we can use

	select user,high_score from stage_leaderboard where solr_query='{"q": "stage:1321", "sort":"high_score desc"}' limit 10 ;

Or to look at the high_scores over all stages, we can use 

	select stage,user,high_score from stage_leaderboard where solr_query='{"q": "*:*", "sort":"high_score desc"}' limit 10 ;
	
To look up scores below a certain score 

	select user,high_score from stage_leaderboard where solr_query='{"q": "stage:1321", "fq":"high_score:[* TO 92.3]", "sort":"high_score desc"}' limit 5;

and above a certain score

	select user,high_score from stage_leaderboard where solr_query='{"q": "stage:1321", "fq":"high_score:[92.3 TO *]", "sort":"high_score asc"}' limit 5;



