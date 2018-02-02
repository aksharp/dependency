package controllers

import com.bryzek.dependency.v0.models.LibraryVersion
import com.bryzek.dependency.v0.models.json._
import db.{Authorization, DbImplicits, LibraryVersionsDao}
import io.flow.common.v0.models.UserReference
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class LibraryVersions @javax.inject.Inject() (
  val db: Database,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents
) extends FlowController with DbImplicits {

  def get(
    id: Option[String],
    ids: Option[Seq[String]],
    libraryId: Option[String],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        libraryVersionsDao.findAll(
          Authorization.User(request.user.id),
          id = id,
          ids = optionals(ids),
          libraryId = libraryId,
          limit = Some(limit),
          offset = offset
        )
      )
    )
  }

  def getById(id: String) = Identified { request =>
    withLibraryVersion(request.user, id) { library =>
      Ok(Json.toJson(library))
    }
  }

  def withLibraryVersion(user: UserReference, id: String) (
    f: LibraryVersion => Result
  ): Result = {
    libraryVersionsDao.findById(
      Authorization.User(user.id),
      id
    ) match {
      case None => {
        NotFound
      }
      case Some(libraryVersion) => {
        f(libraryVersion)
      }
    }
  }
}

