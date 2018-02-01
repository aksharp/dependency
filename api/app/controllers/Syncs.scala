package controllers

import com.bryzek.dependency.actors.MainActor
import com.bryzek.dependency.v0.models.SyncEvent
import com.bryzek.dependency.v0.models.json._
import db.{BinariesDao, LibrariesDao, ProjectsDao, SyncsDao}
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class Syncs @javax.inject.Inject() (
  syncsDao: SyncsDao,
  binariesDao: BinariesDao,
  librariesDao: LibrariesDao,
  projectsDao: ProjectsDao,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents
) extends FlowController  with Helpers {

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
      MainActor.ref ! MainActor.Messages.BinarySync(binary.id)
      NoContent
    }
  }

  def postLibrariesById(id: String) = Identified { request =>
    withLibrary(librariesDao, request.user, id) { library =>
      MainActor.ref ! MainActor.Messages.LibrarySync(library.id)
      NoContent
    }
  }

  def postProjectsById(id: String) = Identified { request =>
    withProject(projectsDao, request.user, id) { project =>
      MainActor.ref ! MainActor.Messages.ProjectSync(id)
      NoContent
    }
  }

}
