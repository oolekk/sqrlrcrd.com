package bootstrap.liftweb

import java.io.File

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import net.liftweb.util.Props

object Start {

  def main(args: Array[String]): Unit = {

      /* 
     * adjust run.mode when starting the jar like so:
     * java -Drun.mode=production -jar myjarname.jar
     */

    /* calculate run.mode dependent path to logback configuration file
     * use same naming scheme as for props files  */
    val logbackConfFile = {
      val propsDir = "props"
      val fileNameTail = "default.logback.xml"
      val mode = System.getProperty("run.mode")
      if (mode != null) propsDir + "/" + mode + "." + fileNameTail
      else propsDir + "/" + fileNameTail
    }
    /* set logback config file appropriately */
    System.setProperty("logback.configurationFile", logbackConfFile)

    /* choose different port for each of your webapps deployed on single machine
     * you may then use it in nginx proxy-pass directive, to target virtual hosts
     * line below will attempt to read jetty.emb.port property from props file or
     * use supplied default 9090. Alternatively, if commandline numeric
     * parameter is given, it will be used for the port number.  */
    val portNumFromArgs: Option[Int] = {
      try {
        val arg0 = args(0)
        if (arg0.toInt > 0 && arg0.toInt < 65536)
          Some(arg0.toInt) else None
      }
      catch { case _ ⇒ None }
    }

    val port = portNumFromArgs.getOrElse(Props.getInt("jetty.emb.port", 9090))
    println("USING PORT: " + port)
    val server = new Server(port)
    val webctx = new WebAppContext
    /* use embedded webapp dir as source of the web content
     * web.xml is in default location, of embedded webapp dir,
     * so don't need to adjust that */
    val webappDirInsideJar = webctx.getClass.getClassLoader.getResource("webapp").toExternalForm
    webctx.setWar(webappDirInsideJar)

    /* might use use external pre-existing webapp dir instead of referencing
     * the embedded webapp dir but it's not very useful. why would we put
     * webapp inside jar if we end up using some other external webapp dir.
     * I put it for reference, as it may make sense under some circumstances.
     * webctx.setResourceBase("/path/to/existing/webapp-dir") */

    webctx.setContextPath("/")
    /* optionally extract embedded webapp to specific temporary location and serve
     * from there. Often /tmp is used, but it is not always advisable, because it
     * gets cleaned-up from time to time, so you need to use other location such as
     * /var/www/sqrlrcrd.com for anything that should last */
    Props.get("jetty.emb.tmpdir").foreach(dir => webctx.setTempDirectory(new File(dir)))

    server.setHandler(webctx)
    server.start
    server.join
  }

}