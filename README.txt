				Cassandra Client

{Date: October 12, 2018)

Cassclient is a database application that interacts with the Cassandra
NoSQL DBMS via the Java version of the Datastax driver, implemented as a Java API. 
Through this driver, we interact w/Cassandra via Native Transport API and other
protocols for communicating between Cassandra nodes and database app clients. 

In the near future, I want to create a feature where queries can be stored in 
files and read by the cassclient to query the Cassandra DBMS. This could be
used for automating and customizing your own testing purposes.

As of now, I am in the process of adding support for CQL commands, that will
behave very similarly to the CQL shell that comes with the Cassandra project.
Other commands unique to cassclient will be added as well, such as: mode, for
changing the mode in which cassclient operates in.

I am still in the early stages of development so no documentation other than 
this readme file is available at this time.

As of now, I only have support for LINUX environment.

What is needed for this project?

Java 8 OpenJDK (For Linux only)
	To download, on the command line, type:
	
	sudo apt-get install openjdk-8-jdk
	
Maven 3.5.4				- http://maven.apache.org/index.html

DataStax Java Driver - http://docs.datastax.com/en/drivers/java/3.0/index.html



