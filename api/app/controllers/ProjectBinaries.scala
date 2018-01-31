package controllers

import com.bryzek.dependency.v0.models.json._
import db.{Authorization, ProjectBinariesDao}
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class ProjectBinaries @javax.inject.Inject() (
  projectBinariesDao: ProjectBinariesDao,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents
) extends FlowController {

  def get(
    id: Option[String],
    ids: Option[Seq[String]],
    projectId: Option[String],
    binaryId: Option[String],
    isSynced: Option[Boolean],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        projectBinariesDao.findAll(
          Authorization.User(request.user.id),
          id = id,
          ids = optionals(ids),
          projectId = projectId,
          binaryId = binaryId,
          isSynced = isSynced,
          limit = limit,
          offset = offset
        )
      )
    )
  }

}
