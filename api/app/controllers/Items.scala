package controllers

import db.{DbImplicits, ItemsDao}
import com.bryzek.dependency.v0.models.json._
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.db.Database
import play.api.mvc._
import play.api.libs.json._

@javax.inject.Singleton
class Items @javax.inject.Inject() (
  val db: Database,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends FlowController with Helpers with DbImplicits {

  def get(
    q: Option[String],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        itemsDao.findAll(
          authorization(request),
          q = q,
          limit = limit,
          offset = offset
        )
      )
    )
  }

}
