
organization := "sqrlrcrd.com"

name := "sqrlrcrd webapp"

version := "1.0"

scalaVersion := "2.9.1"

seq(webSettings: _*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

scanDirectories in Compile := Nil

/* read jetty settings from this file */
env in Compile := Some(file("jetty-env.xml") asFile)

// you can also add multiple repositories at the same time
resolvers ++= Seq(
  "Default maven repository" at "http://repo1.maven.org/maven2/",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Scala Tools Releases" at "http://scala-tools.org/repo-releases/",
  "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/",
  "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Scales Repo" at "http://scala-scales.googlecode.com/svn/repo",
  "OpenNMS - for fast-md5" at "http://repo.opennms.org/maven2/"
)

// if you have issues pulling dependencies from the scala-tools repositories (checksums don't match), you can disable checksums
//checksums := Nil

libraryDependencies ++= {
  val liftVersion = "2.5-SNAPSHOT" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-record" % liftVersion,
    "net.liftweb" %% "lift-webkit" % liftVersion,
    "net.liftweb" %% "lift-squeryl-record" %  liftVersion exclude("org.squeryl", "squeryl"),
    "org.squeryl" %% "squeryl" % "0.9.5",
    "com.h2database" % "h2" % "1.3.161", // In-process db, useful for development
    "mysql" % "mysql-connector-java" % "5.1.19",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
    "ch.qos.logback" % "logback-classic" % "1.0.2", // Logging
    "net.liftweb" %% "lift-wizard" % liftVersion,
    "net.liftweb" %% "lift-testkit" % liftVersion,
    "net.liftweb" %% "lift-widgets" % liftVersion
    )
}

// Customize any further dependencies as desired
libraryDependencies ++= Seq(
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.0.v20120127" % "test, container", // For Jetty 8
    "org.eclipse.jetty" % "jetty-plus" % "8.1.0.v20120127" % "container", // For Jetty Config
    "javax.servlet" % "servlet-api" % "2.5" % "provided",
    "junit" % "junit" % "4.8" % "test", // For JUnit 4 testing
    //"org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
    "org.specs2" %% "specs2" % "1.9" % "test",
    "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test"
)

