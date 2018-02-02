package controllers

import io.flow.dependency.actors.MainActor
import db.{Authorization, DbImplicits, LibrariesDao, ProjectsDao}
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import io.flow.postgresql.Pager
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc._

@javax.inject.Singleton
class GithubWebhooks @javax.inject.Inject() (
  val db: Database,
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends FlowController with DbImplicits {

  def postByProjectId(projectId: String) = Action { request =>
    projectsDao.findById(Authorization.All, projectId) match {
      case None => {
        NotFound
      }
      case Some(project) => {
        play.api.Logger.info(s"Received github webook for project[${project.id}] name[${project.name}]")
        mainActorRef ! MainActor.Messages.ProjectSync(project.id)

        // Find any libaries with the exact name of this project and
        // opportunistically trigger a sync of that library a few
        // times into the future. This supports the normal workflow of
        // tagging a repository and then publishing a new version of
        // that artifact. We want to pick up that new version
        // reasonably quickly.
        Pager.create { offset =>
          librariesDao.findAll(
            Authorization.All,
            artifactId = Some(project.name),
            offset = offset
          )
        }.foreach { library =>
          Seq(30, 60, 120, 180).foreach { seconds =>
            mainActorRef ! MainActor.Messages.LibrarySyncFuture(library.id, seconds)
          }
        }

        Ok(Json.toJson(Map("result" -> "success")))
      }
    }
  }

}
