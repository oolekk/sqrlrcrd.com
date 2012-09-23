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
    
Then cd to target dir and run:

    java -jar sqrlrcrd.com-lift-assembly-1.0.jar
    
You can make it run in production mode like so:
    
    java -Drun.mode=production -jar sqrlrcrd.com-lift-assembly-1.0.jar
    
You can give custom port number to be used by jetty:
    
    java -jar sqrlrcrd.com-lift-assembly-1.0.jar 8090
     
Your server will start from the jar. Note, you can bring this jar on your
usb stick and make any computer with java serve it. You don't need to
install anything else, except from maybe a database :) but you can use h2
in-file db, to make it self contained. By default embedded jetty will 
use port 9090. You can close sbt, and this service will still work,
with some adjustments this can be used in production.

In development mode, h2 console will be browser accessible at:   
[http://localhost:9090/console](http://localhost:9090/console)  
You may need to change port number to port given on the command line when starting
executable jar or to default 8080 used by sbt if run from sbt. At the h2 console
login screen, JDBC URL must be adjusted to something like this:

    jdbc:h2:mem:sqrlrcrd_com
    
Use login name 'test' and a blank password. This can be changed by
editing props files. Also there is a kind of up-to-date dependency
collection in build.sbt

### Credits

Database connection setup is based on code from Peter Petersson's
[example](https://github.com/karma4u101/Basic-SquerylRecord-User-Setup)
with some minor adjustments.

Embedded jetty configuration assembled by me with big help from Diego Medina
and Lift mailing list participants.
