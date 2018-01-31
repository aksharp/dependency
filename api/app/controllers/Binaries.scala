package controllers

import com.bryzek.dependency.v0.models.BinaryForm
import com.bryzek.dependency.v0.models.json._
import db.{Authorization, BinariesDao}
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.{Config, Validation}
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class Binaries @javax.inject.Inject() (
  binariesDao: BinariesDao,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents
  ) extends FlowController with Helpers {

  def get(
    id: Option[String],
    ids: Option[Seq[String]],
    projectId: Option[String],
    name: Option[String],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        binariesDao.findAll(
          Authorization.User(request.user.id),
          id = id,
          ids = optionals(ids),
          projectId = projectId,
          name = name,
          limit = limit,
          offset = offset
        )
      )
    )
  }

  def getById(id: String) = Identified { request =>
    withBinary(binariesDao, request.user, id) { binary =>
      Ok(Json.toJson(binary))
    }
  }

  def post() = Identified(parse.json) { request =>
    request.body.validate[BinaryForm] match {
      case e: JsError => {
        UnprocessableEntity(Json.toJson(Validation.invalidJson(e)))
      }
      case s: JsSuccess[BinaryForm] => {
        val form = s.get
        binariesDao.create(request.user, form) match {
          case Left(errors) => UnprocessableEntity(Json.toJson(Validation.errors(errors)))
          case Right(binary) => Created(Json.toJson(binary))
        }
      }
    }
  }

  def deleteById(id: String) = Identified { request =>
    withBinary(binariesDao, request.user, id) { binary =>
      binariesDao.delete(request.user, binary)
      NoContent
    }
  }

}
