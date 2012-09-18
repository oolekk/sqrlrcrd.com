import AssemblyKeys._ // put this at the top of the file

assemblySettings

// in that class there is some configuration, used by self serving
// embedded jetty jar -> for ex. port number 9090 or webtmpdir resolution
mainClass in assembly := Some("bootstrap.liftweb.Start")

organization <<= baseDirectory(_.getName) //organization name equals base dir name

name <<= organization(org => org + "-lift") //app name -> just add -lift suffix

version := "1.0"

scalaVersion := "2.9.1"

seq(webSettings: _*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

// uncomment & adjust to use alternate port number for serving webapp with jetty
// port in container.Configuration := 9090
// or read jetty settings from a file
// env in Compile := Some(file("jetty-env.xml") asFile)

scanDirectories in Compile := Nil


// you can also add multiple repositories at the same time
resolvers ++= Seq(
  "Sonatype OSS Snapshot repo" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Release repo" at"http://oss.sonatype.org/content/repositories/releases/",
  "Scales Repo" at "http://scala-scales.googlecode.com/svn/repo",
  "OpenNMS - for fast-md5" at "http://repo.opennms.org/content/groups/opennms.org-release/"
)

// if you have issues pulling dependencies from the scala-tools
// repositories (checksums don't match), you can disable checksums
// checksums := Nil

libraryDependencies ++= {
  val liftVersion = "2.5-SNAPSHOT" // Put the current/latest lift version here
  Seq(
	"net.liftweb" %% "lift-webkit" % liftVersion,
	//"net.liftweb" %% "lift-json" % liftVersion
    //"net.liftweb" %% "lift-json-ext" % liftVersion,
	//"net.liftweb" %% "lift-wizard" % liftVersion,
	//"net.liftweb" %% "lift-widgets" % liftVersion,
	//"net.liftweb" %% "lift-testkit" % liftVersion,
	"net.liftweb" %% "lift-record" % liftVersion,
    //"net.liftweb" %% "lift-squeryl-record" % liftVersion, //standard
    //latest - if got problems with snapshot, comment out next 2 lines and use line above instead
    "net.liftweb" %% "lift-squeryl-record" % liftVersion exclude("org.squeryl","squeryl"), 
    "org.squeryl" %% "squeryl" % "0.9.5-SNAPSHOT",
    "com.h2database" % "h2" % "1.3.168",
    "mysql" % "mysql-connector-java" % "5.1.19",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "ch.qos.logback" % "logback-classic" % "1.0.6", // Logging
    "com.jolbox" % "bonecp" % "0.7.1.RELEASE" // connection pooling 
    )
}

// Customize any further dependencies
libraryDependencies ++= Seq(
		// -- jetty related --
	"org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")
    // dependencies below may come handy, but not required in this basic setup
		// -- testing frameworks --
	// "org.specs2" %% "specs2" % "1.11" % "test",
	// "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test",
		// date-time alternative
	// "org.joda" % "joda-convert" % "1.2" % "provided", //required compile-time dependency
	// "joda-time" % "joda-time" % "2.1" % "compile", //time handling with joda-time
		// -- img processing --
	// "org.im4java" % "im4java" % "1.2.0"
)

/* streamlined generation of self serving embedded jetty jar - thx Diego */
resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map
{ (managedBase, base) =>
  val webappBase = base / "src" / "main" / "webapp"
  for {
    (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase /
      "main" / "webapp")
  } yield {
    Sync.copy(from, to)
    to
  }
}

// latest jetty has same named about.html files in its artifacts
// top location - we need to resolve name conflict while merging
// single jar with assembly plugin. assembly + jetty lets us
// create self serving jar which can be run without installing
// a standalone server wih: java -jar my-app.assembly-1.0.jar
// it is a very flexible and light-weight way to serve your app
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case "about.html" => MergeStrategy.rename
    case x => old(x)
  }
}

// MAY NEED SOME DAY
//"com.codecommit" %% "anti-xml" % "0.4-SNAPSHOT"
//"org.mindrot" % "jbcrypt" % "0.3m"
//"org.im4java" % "im4java" % "1.2.0"
