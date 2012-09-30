package bootstrap.liftweb

import java.io.File
import net.liftweb.common._
import net.liftweb.util.Props
import net.liftweb.util.Helpers._
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext

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
     * system property accordingly. Use same naming scheme as for props files.
     * TO WORK IT NEEDS TO BE SET EARLY (BEFORE PROP IS READ -> FIRST THING TO RUN) */
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
      tryo { args(0).toInt }.filter(portNumber ⇒ portNumber > 0 && portNumber < 65536)
    }.getOrElse(Props.getInt("jetty.emb.port", 9090))

    val connector = new SelectChannelConnector()
    connector.setPort(port)
    val server = new Server()
    server.addConnector(connector)

    val webctx = new WebAppContext
    /* Use embedded webapp dir as source of content to be served. */
    val webappDirFromJar = webctx.getClass.getClassLoader.getResource("webapp").toExternalForm
    webctx.setWar(webappDirFromJar)
    /* We might use use external, already existing webapp dir instead of
     * referencing the webapp dir from the jar but it's not very useful. 
     * I put it here for reference, as it may make sense under some circumstances.
     * webctx.setResourceBase("/path/to/existing/webapp-dir") */
    webctx.setContextPath("/")
    /* Below we extract embedded webapp to specific temporary location and serve
     * from there. Often /tmp is used, but it is not always advisable, because it
     * gets cleaned-up from time to time, so you need to use other location such as
     * /var/www/sqrlrcrd.com for anything that should last. */
    Props.get("jetty.emb.tmpdir").foreach(dir ⇒ {
      webctx.setTempDirectory(new File(dir))
      info("USING TEMP DIRECTORY: " + webctx.getTempDirectory)
    })

    server.setHandler(webctx)
    server.start
    server.join

    /* And here is a little gem, which took me much time and experimentation.
     * To serve arbitrary resource directory (may be located outside webapp dir) 
     * use code below instead of previous block. This can be indispensible if 
     * your app uses some resources, which should not be put inside the executable
     * jar, because they are large, or change frequently, but should still be
     * accessible when jar is started. For example, this is very useful to access
     * a directory containing photo gallery pictures, kept separately, where stuff
     * can easily change when new pics are uploaded. */

    //    val myResHandler = makeResourceHandler("/images", "/var/img-resources", true, false)
    //    val handlerList = new HandlerList()
    //    /* IMPORTANT: order in the array matters, webctx should come last */
    //    handlerList.setHandlers(Array(myResHandler, webctx))
    //    
    //    server.setHandler(handlerList)
    //    server.start
    //    server.join

  }

  /* Use this function to create handler, which will let you serve resources
   * from arbitrary directory. You may allow dir listings and symlink traversal.
   * */
  def makeResourceHandler(
    contextPath: String, resourceBase: String,
    listDirs: Boolean = false, allowAliases: Boolean = false) = {
    info("MAKE RESOURCE HANDLER contextPath:%s resourceBase:%s listDirs:%s allowAliases:%s".
      format(contextPath, resourceBase, listDirs, allowAliases))

    val resHandler = new ResourceHandler()
    resHandler.setResourceBase(resourceBase)
    /* Normally only files can be accessed directly, but we may optionally allow
     * accessing dirs through the browser and serve pages with a list of dir contents. */
    resHandler.setDirectoriesListed(listDirs)

    val resCtxHandler = new ContextHandler()
    resCtxHandler.setContextPath(contextPath)
    /* Enabling this will make aliases and symbolic links work,
     * which is turned off by default to avoid security risks. */
    resCtxHandler.setAliases(allowAliases)
    resCtxHandler.setHandler(resHandler)

    resCtxHandler
  }

}