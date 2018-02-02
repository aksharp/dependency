package controllers

import io.flow.dependency.actors.MainActor
import io.flow.dependency.v0.models.SyncEvent
import io.flow.dependency.v0.models.json._
import db._
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class Syncs @javax.inject.Inject() (
  val db: Database,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends FlowController  with Helpers with DbImplicits {

  def get(
    objectId: Option[String],
    event: Option[SyncEvent],
    limit: Long = 25,
    offset: Long = 0
  ) = Identified { request =>
    Ok(
      Json.toJson(
        syncsDao.findAll(
          objectId = objectId,
          event = event,
          limit = limit,
          offset = offset
        )
      )
    )
  }

  def postBinariesById(id: String) = Identified { request =>
    withBinary(binariesDao, request.user, id) { binary =>
      mainActorRef ! MainActor.Messages.BinarySync(binary.id)
      NoContent
    }
  }

  def postLibrariesById(id: String) = Identified { request =>
    withLibrary(librariesDao, request.user, id) { library =>
      mainActorRef ! MainActor.Messages.LibrarySync(library.id)
      NoContent
    }
  }

  def postProjectsById(id: String) = Identified { request =>
    withProject(projectsDao, request.user, id) { project =>
      mainActorRef ! MainActor.Messages.ProjectSync(id)
      NoContent
    }
  }

}
