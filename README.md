squeryl-record + sample DB config & H2 console + jetty
======================================================

There is embedded jetty and assembly sbt plugin for automated creation
of self-serving jar package.

This app inserts/updates/deletes some simple records in the db.  
Read the code (there are only few files) and play with it to get the idea.  
Watch logging messages, to see what happens.

Apart from showing sample setup of squeryl-record with different database
engines, this simple app includes a working configuration for building
self-serving jar with embedded jetty. It is a very lightweight and flexible
way to easily do deployment on any computer that has java. To create the
jar, in sbt run:

    assembly
    
Then cd to target dir, and run:

    java -jar sqrlrcrd.com-lift-assembly-1.0.jar
    
Your server is running. Note, you can bring this jar on your usb stick and make
any computer with java serve it. You don't need to install anything else,
except from maybe a database :) but you can use h2 in-file db, to make it
self contained. Access this app on port defined in

    lift.bootstrap.Start.scala
    
Which is set to 9090 in this example. You can close sbt, and this service
will still work -> it can be used in production.

When running from sbt, h2 database console will be here  
http://localhost:8080/console  
JDBC URL must be adjusted to access the database  
jdbc:h2:mem:sqrlrcrd_com  

Also there is a kind of up-to-date dependency collection in build.sbt
With latest jetty, logging with ch.qos.logback etc ...

* Database connection setup is based on code from Peter Petersson's
[example](https://github.com/karma4u101/Basic-SquerylRecord-User-Setup)
with some minor adjustments.
* Embedded jetty configuration assembled by me with big help from Diego Medina
and Lift mailing list participants.
