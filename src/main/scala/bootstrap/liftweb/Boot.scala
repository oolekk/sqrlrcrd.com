package bootstrap.liftweb

import com.sqrlrcrd.model.MySchemaHelper

import javax.servlet.FilterConfig
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.http.Html5Properties
import net.liftweb.http.LiftFilter
import net.liftweb.http.LiftRules
import net.liftweb.http.NotFoundAsTemplate
import net.liftweb.http.NoticeType
import net.liftweb.http.OnDiskFileParamHolder
import net.liftweb.http.ParsePath
import net.liftweb.http.Req
import net.liftweb.http.S
import net.liftweb.sitemap.Loc.LinkText.strToLinkText
import net.liftweb.sitemap.Loc.Link
import net.liftweb.sitemap.Loc
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.squerylrecord.RecordTypeMode.inTransaction
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import net.liftweb.util.Vendor.valToVender
import net.liftweb.util.LoanWrapper
import net.liftweb.util.NamedPF
import net.liftweb.util.Props

class RunModeLiftFilter extends LiftFilter {
  override def init(config: FilterConfig) {
    /*
     * When running your app from sbt or normal war (but not from executable jar)
     * only mode setting from Boot.scala is taken into consideration, to decide
     * run.mode and which props files to read.
     * 1) Run mode setting from Boot.scala will decide which props file to read:
     * "run.mode" "development" -> resources/props/default.props.xml
     * "run.mode" "production" -> resources/props/production.default.props.xml
     * ... some other possible run modes (staging, test)
     * 2) Run mode setting from Boot.scala will decide which config
     * file for logging to read:
     * "run.mode" "development" -> resources/props/default.logback.xml
     * "run.mode" "production" -> resources/props/production.default.logback.xml
     * .... some other possible run modes (staging, test)
     * 3) Run mode setting from Boot.scala will decide which mode of operation
     * will lift use, for example:
     * "run.mode" "development" -> watch for templates changes and reload them
     * "run.mode" "production" -> cache templates, ignore changes
     * 
     * It's a quite different story when your app is run from executable jar.
     * Then the situation looks like this:
     * 1) Run mode setting from Start.scala will decide which props file to read:
     * "run.mode" "development" -> resources/props/default.props.xml
     * "run.mode" "production" -> resources/props/production.default.props.xml
     * 2) Completely ignored are the logging settings from 
     * "run.mode" "development" -> resources/props/default.logback.xml
     * "run.mode" "production" -> resources/props/production.default.logback.xml
     * .... some other possible run modes (staging, test)
     * instead resources/logback.xml file is read to determine logging behaviour,
     * no matter what run mode is chosen either in Boot or Start
     * 3) IMPORTANT AND A BIT SURPRISING - this stays unaffected by run mode
     * setting specified in Start 
     * Run mode setting from Boot.scala will decide which mode of operation
     * will lift use, for example:
     * "run.mode" "development" -> watch for templates changes and reload them
     * "run.mode" "production" -> cache templates, ignore changes
     * 
     * So settings in Boot.scala and Start.scala do not have to match, and do
     * different things. When jetty is run from sbt, or when your app is run
     * from standard war only settings in this file have any meaning. But when
     * running an executable jar, settings from both Boot.scala and Start.scala
     * are important. You could possibly set run mode to development in Start.scala,
     * but to production in Boot.scala and have situation, where props are read
     * from resources/props/deafault.props.xml, logging settings are read from
     * resources/logback.xml, and your app still runs in production mode internally
     * and caches templates. In production you will most probably want to set
     * run mode to production both in Boot.scala and in Start.scala
     */
    System.setProperty("run.mode", "development")
    super.init(config)
  }
}

class Boot extends Loggable {

  /* Force the request to be UTF-8 */
  private def makeUtf8(req: HTTPRequest) { req.setCharacterEncoding("UTF-8") }

  def boot {

    /* default package to search for snippets, views, comet */
    LiftRules.addToPackages("com.sqrlrcrd")
    /* Force the request to be UTF-8 */
    LiftRules.early.append(makeUtf8)
    /* use HTML5 templates */
    LiftRules.htmlProperties.default.set((r: Req) ⇒ new Html5Properties(r.userAgent))
    /* store uploads as files on disk */
    LiftRules.handleMimeFile = OnDiskFileParamHolder.apply
    /* set max total upload size */
    LiftRules.maxMimeSize = 1024 * 1024 * 32
    /* set max per-file upload size */
    LiftRules.maxMimeFileSize = 1024 * 1024 * 32;
    /* use jQuery framework */
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQueryArtifacts
    /* Show the spinny image when an Ajax call starts */
    LiftRules.ajaxStart = Full(() ⇒ LiftRules.jsArtifacts.show("ajax-loader").cmd)
    /* Make the spinny image go away when it ends */
    LiftRules.ajaxEnd = Full(() ⇒ LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    /* notice fade out (start after x, fade out duration y) */
    LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) ⇒ {
      notices match {
        case NoticeType.Notice  ⇒ Full((4 seconds, 2 seconds))
        case NoticeType.Warning ⇒ Full((6 seconds, 2 seconds))
        case NoticeType.Error   ⇒ Full((8 seconds, 2 seconds))
        case _                  ⇒ Empty
      }
    })
    /* set custom 404 handler */
    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) ⇒
        NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })

    /* vvvvvv DB CONNECTION AND TRANSACTION WRAPPING vvvvvv */

    /* choose database type */
    val dbtype = Props.get("use.db", "h2");
    if (dbtype == "h2") MySchemaHelper.initSquerylRecordWithH2DB
    else if (dbtype == "mysql") MySchemaHelper.initSquerylRecordWithMySqlDB
    else if (dbtype == "postgres") MySchemaHelper.initSquerylRecordWithPostgresDB

    // drop & create schema (to be used on the first run, or to purge db)
    if (Props.getBool("db.schemify", false)) { MySchemaHelper.dropAndCreateSchema }
    // only touch db to initialize connection pool
    else { MySchemaHelper.touchDB }

    Props.mode match {

      case Props.RunModes.Development ⇒ {
        logger.info("\nRunMode is DEVELOPMENT @ sqrlrcrd.com\n")
        // pass paths that start with 'console' to be processed by the H2Console servlet
        if (MySchemaHelper.isUsingH2Driver) {
          /* make db console browser-accessible in dev mode at /console 
           * see http://www.h2database.com/html/tutorial.html#tutorial_starting_h2_console 
           * Embedded Mode JDBC URL: jdbc:h2:mem:test User Name:test Password:test */
          logger.info("Set up H2 db console at /console ")
          LiftRules.liftRequest.append({
            case r if (r.path.partPath match { case "console" :: _ ⇒ true case _ ⇒ false }) ⇒ false
          })
        }
      }
      case Props.RunModes.Production ⇒ logger.info("\nRunMode is PRODUCTION @ sqrlrcrd.com\n")
      case _                         ⇒ logger.info("\nRunMode is TEST, PILOT or STAGING @ sqrlrcrd.com\n")
    }

    /* Make transaction wrap around the whole HTTP request */
    S.addAround(new LoanWrapper {
      override def apply[T](f: ⇒ T): T =
        {
          inTransaction { f }
        }
    })
    /* ^^^^^^ DB CONNECTION AND TRANSACTION WRAPPING ^^^^^^ */

    /* vvvvvv SITEMAP STUFF vvvvvv */

    /* uncomment to disable uniqueness check for SiteMap */
    // SiteMap.enforceUniqueLinks = false

    /* build SiteMap entries */
    val staticMenu = Menu(Loc("Static", Link(List("static"), true, "/static/index"), "Static"))

    val indexMenu = Menu(Loc("home", Link(List("index"), false, "/index"), "HOME"))

    val entries = List[Menu](
      indexMenu,
      staticMenu
    )

    /* Set SiteMap. If you don't want access control for each page, comment this out */
    LiftRules.setSiteMap(SiteMap(entries: _*))

    /* ^^^^^^ SITEMAP STUFF ^^^^^^ */

  }
}

