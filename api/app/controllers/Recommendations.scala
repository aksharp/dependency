package controllers

import db.{Authorization, RecommendationsDao}
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import com.bryzek.dependency.v0.models.RecommendationType
import com.bryzek.dependency.v0.models.json._
import io.flow.error.v0.models.json._
import io.flow.play.util.Config
import play.api.mvc._
import play.api.libs.json._

@javax.inject.Singleton
class Recommendations @javax.inject.Inject() (
  recommendationsDao: RecommendationsDao,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents
) extends FlowController with Helpers {

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
