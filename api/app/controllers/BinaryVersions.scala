package controllers

import com.bryzek.dependency.v0.models.BinaryVersion
import com.bryzek.dependency.v0.models.json._
import db.{Authorization, BinaryVersionsDao, DbImplicits}
import io.flow.common.v0.models.UserReference
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class BinaryVersions @javax.inject.Inject() (
  val db: Database,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends FlowController with DbImplicits {

  def get(
    id: Option[String],
    ids: Option[Seq[String]],
    binaryId: Option[String],
    projectId: Option[String],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        binaryVersionsDao.findAll(
          Authorization.User(request.user.id),
          id = id,
          ids = optionals(ids),
          binaryId = binaryId,
          projectId = projectId,
          limit = limit,
          offset = offset
        )
      )
    )
  }

  def getById(id: String) = Identified { request =>
    withBinaryVersion(request.user, id) { binary =>
      Ok(Json.toJson(binary))
    }
  }

  def withBinaryVersion(user: UserReference, id: String)(
    f: BinaryVersion => Result
  ): Result = {
    binaryVersionsDao.findById(Authorization.User(user.id), id) match {
      case None => {
        NotFound
      }
      case Some(binaryVersion) => {
        f(binaryVersion)
      }
    }
  }
}

