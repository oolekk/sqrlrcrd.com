package bootstrap.liftweb

import com.sqrlrcrd.model.MySchemaHelper

import javax.servlet.FilterConfig
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.http.LiftFilter
import net.liftweb.http.LiftRules
import net.liftweb.http.NotFoundAsTemplate
import net.liftweb.http.NoticeType
import net.liftweb.http.OnDiskFileParamHolder
import net.liftweb.http.ParsePath
import net.liftweb.http.Req
import net.liftweb.http.S
import net.liftweb.http.XHtmlInHtml5OutProperties
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
		/* generate HTML5 documents as output from internal XHTML templates */
		LiftRules.htmlProperties.default.set((r: Req) ⇒ new XHtmlInHtml5OutProperties(r.userAgent))
		/* store uploads as files on disk */
		LiftRules.handleMimeFile = OnDiskFileParamHolder.apply
		/* set max total upload size */
		LiftRules.maxMimeSize = 1024 * 1024 * 32
		/* set max per-file upload size */
		LiftRules.maxMimeFileSize = 1024 * 1024 * 32;
		/* use jQuery framework */
		LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts
		/* Show the spinny image when an Ajax call starts */
		LiftRules.ajaxStart = Full(() ⇒ LiftRules.jsArtifacts.show("ajax-loader").cmd)
		/* Make the spinny image go away when it ends */
		LiftRules.ajaxEnd = Full(() ⇒ LiftRules.jsArtifacts.hide("ajax-loader").cmd)
		/* notice fade out (start after x, fade out duration y) */
		LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) ⇒ {
			notices match {
				case NoticeType.Notice ⇒ Full((4 seconds, 2 seconds))
				case NoticeType.Warning ⇒ Full((6 seconds, 2 seconds))
				case NoticeType.Error ⇒ Full((8 seconds, 2 seconds))
				case _ ⇒ Empty
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
			case _ ⇒ logger.info("\nRunMode is TEST, PILOT or STAGING @ sqrlrcrd.com\n")
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
		SiteMap.enforceUniqueLinks = false

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

