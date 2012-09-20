package bootstrap.liftweb

import java.io.File
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import net.liftweb.util.Props
import net.liftweb.common._

object Start {

  def main(args: Array[String]): Unit = {
    
    /*
     * When running executable jar logging settings will always be read from 
     * resources/logback.xml, it doesn't make difference which run
     * mode you set up either in this file or in Boot.scala
     * Line below will decide which props file will be read by your app when
     * starting the executable jar. It goes like this:
     * "run.mode" "development" -> resources/props/default.props.xml
     * "run.mode" "production" -> resources/props/production.default.props.xml
     * ... perhaps some other modes can be used such as staging etc.
     * IMPORTANT, apart from deciding which props file to read, this does
     * not really change the mode in which lift will work internally, so
     * does not influence for example template caching strategy. To set that
     * change same thing but in Boot.scala.
     */
    System.setProperty("run.mode","production")
    
    /* choose different port for each of your webapps deployed on single server
     * you may use it in nginx proxy-pass directive, to target virtual hosts
     * line below will attempt to read embedded.jetty.prot property or use
     * supplied default 9090*/
    val port = Props.getInt("jetty.emb.port", 9090)
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
    val shouldExtract = Props.getBool("jetty.emb.extract", false)
    if(shouldExtract){
      val webtmpdir = Props.get("jetty.emb.tmpdir", "/tmp")
      webctx.setTempDirectory(new File(webtmpdir))
    }

    server.setHandler(webctx)
    server.start
    server.join

  }

}