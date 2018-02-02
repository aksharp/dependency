package controllers

import io.flow.dependency.v0.models.json._
import db.{Authorization, DbImplicits, ProjectLibrariesDao}
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class ProjectLibraries @javax.inject.Inject() (
  val db: Database,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends FlowController with DbImplicits {

  def get(
    id: Option[String],
    ids: Option[Seq[String]],
    projectId: Option[String],
    libraryId: Option[String],
    isSynced: Option[Boolean],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        projectLibrariesDao.findAll(
          Authorization.User(request.user.id),
          id = id,
          ids = optionals(ids),
          projectId = projectId,
          libraryId = libraryId,
          isSynced = isSynced,
          limit = Some(limit),
          offset = offset
        )
      )
    )
  }

}
