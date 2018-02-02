package controllers

import com.bryzek.dependency.v0.models.RecommendationType
import com.bryzek.dependency.v0.models.json._
import db.{Authorization, DbImplicits}
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class Recommendations @javax.inject.Inject() (
  val db: Database,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends FlowController with Helpers with DbImplicits {

  def get(
    organization: Option[String],
    projectId: Option[String],
    `type`: Option[RecommendationType],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        recommendationsDao.findAll(
          Authorization.User(request.user.id),
          organization = organization,
          projectId = projectId,
          `type` = `type`,
          limit = limit,
          offset = offset
        )
      )
    )
  }

}
