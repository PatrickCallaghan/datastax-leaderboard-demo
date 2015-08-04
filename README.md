Leaderboard Demo
====================

This demo shows how to use DataStax Enterprise to get the highest scores on a leaderboard of millions of users. This demo assumes the same user cannot simultaneously play the same game.  

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

To create the a single node cluster with replication factor of 1 for standard localhost setup, run the following

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup"

To start inserting run

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.leaderboard.Main"
    
The default example uses 1Million users but these can be changed by adding the argument 'noOfUsers'. To change the demo to use 100000 users add the following '-DnoOfUsers=100000' 

In another window, create the Solr core for our table. Run 

	bin/dsetool create_core datastax_leaderboard_demo.age_of_darkness_leaderboard generateResources=true reindex=true


Using the command line tool, cqlsh. To use the limit properly, turn off the pagination using
	
	paging off
	
Then we can run the following to get the top ten scores.  
	
	select user,total from age_of_darkness_leaderboard where solr_query='{"q": "*:*", "sort":"total desc"}' limit 10 ;

	
	