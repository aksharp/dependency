package controllers

import io.flow.dependency.api.lib.Github
import io.flow.dependency.v0.models.GithubAuthenticationForm
import io.flow.dependency.v0.models.json._
import db.DbImplicits
import io.flow.common.v0.models.json._
import io.flow.error.v0.models.json._
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.{Config, Validation}
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

class GithubUsers @javax.inject.Inject()(
  val db: Database,
  val tokenClient: io.flow.token.v0.interfaces.Client,
  val github: Github,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends FlowController with DbImplicits {

  import scala.concurrent.ExecutionContext.Implicits.global

  def postGithub() = Action.async(parse.json) {
    request =>
      request.body.validate[GithubAuthenticationForm] match {
        case e: JsError => Future {
          UnprocessableEntity(Json.toJson(Validation.invalidJson(e)))
        }
        case s: JsSuccess[GithubAuthenticationForm] => {
          val form = s.get
          github.getUserFromCode(usersDao, githubUsersDao, tokensDao, form.code).map {
            case Left(errors) => UnprocessableEntity(Json.toJson(Validation.errors(errors)))
            case Right(user) => Ok(Json.toJson(user))
          }
        }
      }
  }

}

