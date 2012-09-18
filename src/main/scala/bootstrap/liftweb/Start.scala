package bootstrap.liftweb

import java.io.File
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import net.liftweb.util.Props
import net.liftweb.common._

object Start {

  def main(args: Array[String]): Unit = {
    /* choose different port for each of your webapps deployed on single server
     * you may use it in nginx proxy-pass directive, to target virtual hosts
     * line below will attempt to read embedded.jetty.prot property or use
     * supplied default 9090*/
    val port = Props.getInt("embedded.jetty.port", 9090)
    val server = new Server(port)
    val webctx = new WebAppContext
    /* use embeded webapp dir as source of the web content -> webapp
     * this is the dir within jar where we have put stuff with zip.
     * it was in a directory created by package-war, in target (also
     * named webapp), which was outside the jar. now, thanks to zip
     * it's inside so we need to use method bellow to get to it.
     * web.xml is in default location, of that embedded webapp dir,
     * so we don't have do webctx.setDescriptor */
    val webappDirInsideJar = webctx.getClass.getClassLoader.getResource("webapp").toExternalForm
    webctx.setWar(webappDirInsideJar)
    /* might use use external pre-existing webapp dir instead of referencing
     * the embeddded webapp dir but it's not very useful. why would we put webapp inside if we end up
     * using some other external directory. I put it for reference, may make sense under some circumstances.
     * webctx.setResourceBase("webapp")
     * */

    webctx.setContextPath("/")
    /* optionally extract embedded webapp to specific temporary location and serve
     * from there. In fact /tmp is not a good place, because it gets cleaned up from
     * time to time so you need to specify some location such as /var/www/sqrlrcrd.com
     * for anything that should last */
    val webtmpdir = Props.get("web.tmpdir", "/tmp")
    webctx.setTempDirectory(new File(webtmpdir))

    val logger = new org.eclipse.jetty.util.log.Slf4jLog
    logger.setDebugEnabled(false)
    webctx.setLogger(logger)

    server.setHandler(webctx)
    server.start
    server.join

  }

}