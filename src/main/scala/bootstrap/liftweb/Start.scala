package bootstrap.liftweb

import java.io.File

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import net.liftweb.util.Props
import net.liftweb.common._
import net.liftweb.util.ControlHelpers._

object Start extends Logger {

  def main(args: Array[String]): Unit = {

    /* Basic way to start the jar is:
     * java -jar myjarname.jar
     * You can adjust run.mode when starting the jar like so:
     * java -Drun.mode=production -jar myjarname.jar
     * You can also give numeric parameter to decide what port to use:
     * java -Drun.mode=production -jar myjarname.jar 8090
     */

    /* Calculate run.mode dependent path to logback configuration file, and set
     * system property accordingly. Use same naming scheme as for props files. */
    System.setProperty("logback.configurationFile", {
      val propsDir = "props"
      val fileNameTail = "default.logback.xml"
      (Box !! System.getProperty("run.mode")).
        dmap(propsDir + "/" + fileNameTail)(propsDir + "/" + _ + "." + fileNameTail)
    })

    /* Choose different port for each of your webapps deployed on single machine.
     * You may then use it in nginx proxy-pass directive, to target virtual hosts.
     * If command line numeric parameter is given, it will be used for the port number.
     * Otherwise we will attempt to read jetty.emb.port property from props file or
     * use default 9090 as fall-back. */
    val port = {
      tryo { args(0).toInt }.filter(portNumber => portNumber > 0 && portNumber < 65536)
    }.getOrElse(Props.getInt("jetty.emb.port", 9090))

    val server = new Server(port)
    val webctx = new WebAppContext

    /* Use embedded webapp dir as source of content to be served. */
    val webappDirInsideJar = webctx.getClass.getClassLoader.getResource("webapp").toExternalForm
    webctx.setWar(webappDirInsideJar)

    /* We might use use external, already existing webapp dir instead of
     * referencing the webapp dir from the jar but it's not very useful. 
     * I put it here for reference, as it may make sense under some circumstances.
     * webctx.setResourceBase("/path/to/existing/webapp-dir") */

    webctx.setContextPath("/")
    /* Below we extract embedded webapp to specific temporary location and serve
     * from there. Often /tmp is used, but it is not always advisable, because it
     * gets cleaned-up from time to time, so you need to use other location such as
     * /var/www/sqrlrcrd.com for anything that should last. */
    Props.get("jetty.emb.tmpdir").foreach(dir => webctx.setTempDirectory(new File(dir)))

    server.setHandler(webctx)
    info("About to start embedded jetty server using port: " + port)
    server.start
    server.join
  }

}