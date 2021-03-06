package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, Controller}
import services.{OAuthServices, Sha1Services, UserServices}
import services.endpoints.GOAuthEndpoints
import services.ids.UserId
import services.status.{AuthFailure, LoginSuccess, UnknownException}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AuthController @Inject()(oAuthServices: OAuthServices,
                               gOAuthEndpoints: GOAuthEndpoints,
                               sha1Services: Sha1Services,
                               userServices: UserServices) extends Controller {

  def login = Action.async { req =>
    Logger.info(s"""login action, session contents: ${req.session.data.mkString(" ")}""")
    val optId = req.session.get("id")
    optId.map { id =>
      Future.successful(Redirect(routes.AuthController.initiateLogin))
    }.getOrElse {
      Future.successful(Ok(views.html.login()))
    }
  }

  def initiateLogin = Action.async { req =>
    Logger.info(s"""initiate login, session contents: ${req.session.data.mkString(" ")}""")
    val optId = req.session.get("id")
    optId.map { id =>
      Logger.info("id present going to index")
      userServices.checkUserExists(UserId(id)).map { exists =>
        if (exists) {
          Logger.info(s"user exists with $id. going to index")
          Redirect(routes.ApplicationController.index).withSession("id" -> id)
        }
        else {
          Logger.info("no user present going to oauth2")
          val key = sha1Services.sha1(System.nanoTime().toString)
          Redirect(routes.AuthController.oauth2(key)).withNewSession
        }
      }
    }.getOrElse {
      Logger.info(s"""no id present going to oauth2, session contents: ${req.session.data.mkString(" ")}""")
      val key = sha1Services.sha1(System.nanoTime().toString)
      Future.successful(Redirect(routes.AuthController.oauth2(key)).withNewSession)
    }
  }

  implicit class MapConverter(rMap: Map[String, String]) {
    def convert: List[String] = rMap.map(pair => s"${pair._1}=${pair._2}").toList
  }

  def oauth2(state: String) = Action {
    Logger.info("oauth2 action")
    val params = Map[String, String](
      "response_type" -> "token",
      "client_id" -> s"${gOAuthEndpoints.clientId}",
      "redirect_uri" -> "http://rxcode.herokuapp.com/oauth2callback",
      "scope" -> "email",
      "state" -> s"${state}"
    ).convert.mkString("?", "&", "").toString
    val requestURI = s"${gOAuthEndpoints.goauthServiceURL}${params}"

    Redirect(requestURI).withSession("state" -> state)
  }

  def oauth2callback() = Action {
    Logger.info("oauth2callback action")
    Ok(views.html.oauth2callback())
  }

  def oauth2callbackCleaned() = Action.async { req =>
    Logger.info("oauth2callbackcleaned action")
    val qMap = req.queryString
    val optReqState: Option[String] = qMap.get("state").flatMap (_.headOption)
    val opSessionSate: Option[String] = req.session.data.get("state")
    optReqState -> opSessionSate match {
      case (Some(rState), Some(sState)) =>
        if (rState == sState) {
          val accessToken = qMap.get("access_token").flatMap(_.headOption)
          oAuthServices.getUserInfo(accessToken.get).flatMap {
            case LoginSuccess(loginInfo) =>
              userServices.onBoardUser(loginInfo).map { userId: UserId =>
                Logger.info(s"user creation successful userId: $userId")
                Redirect(routes.ApplicationController.index).withSession("id" -> userId.id)
              }.recover {
                case th =>
                  th.printStackTrace()
                  Logger.error(s"""routing to index page failed ${th.getMessage}""")
                  Redirect(routes.AuthController.oops())
              }
            case AuthFailure =>
              Logger.error("auth failure")
              Future.successful(Redirect(routes.AuthController.login()).withNewSession)
            case UnknownException(ex) =>
              ex.printStackTrace()
              Future.successful(Redirect(routes.AuthController.oops()))
          }
        } else {
          Future.successful(Redirect(routes.AuthController.oops()))
        }
      case _ => Future.successful(Redirect(routes.AuthController.oops()))
    }
  }

  def oops = Action { req =>
    Logger.info("oops action")
    Ok(views.html.oops())
  }

  def logout = Action {
    Redirect(routes.AuthController.login).withNewSession
  }
}
