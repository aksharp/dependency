package com.bryzek.dependency.actors

import javax.inject.Inject

import akka.actor.Actor
import com.bryzek.dependency.api.lib.DefaultLibraryArtifactProvider
import com.bryzek.dependency.v0.models.{Library, VersionForm}
import db._
import io.flow.postgresql.Pager
import play.api.db.Database

object LibraryActor {

  object Messages {
    case class Data(id: String)
    case object Sync
    case object Deleted
  }

}

class LibraryActor @Inject()(
  val db: Database,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends Actor with Util with DbImplicits {

  lazy val SystemUser = usersDao.systemUser

  var dataLibrary: Option[Library] = None

  def receive = {

    case m @ LibraryActor.Messages.Data(id: String) => withErrorHandler(m) {
      dataLibrary = librariesDao.findById(Authorization.All, id)
    }

    case m @ LibraryActor.Messages.Sync => withErrorHandler(m) {
      dataLibrary.foreach { lib =>
        syncsDao.withStartedAndCompleted(SystemUser, "library", lib.id) {
          resolversDao.findById(Authorization.All, lib.resolver.id).foreach { resolver =>
            DefaultLibraryArtifactProvider().resolve(
              resolversDao = resolversDao,
              resolver = resolver,
              groupId = lib.groupId,
              artifactId = lib.artifactId
            ).foreach { resolution =>
              resolution.versions.foreach { version =>
                libraryVersionsDao.upsert(
                  createdBy = SystemUser,
                  libraryId = lib.id,
                  form = VersionForm(version.tag.value, version.crossBuildVersion.map(_.value))
                )
              }
            }
          }
        }

        // TODO: Should we only send if something changed?
        sender ! MainActor.Messages.LibrarySyncCompleted(lib.id)
      }
    }

    case m @ LibraryActor.Messages.Deleted => withErrorHandler(m) {
      dataLibrary.foreach { lib =>
        itemsDao.deleteByObjectId(Authorization.All, SystemUser, lib.id)

        Pager.create { offset =>
          projectLibrariesDao.findAll(Authorization.All, libraryId = Some(lib.id), limit = Some(100), offset = offset)
        }.foreach { projectLibrary =>
          projectLibrariesDao.removeLibrary(SystemUser, projectLibrary)
          sender ! MainActor.Messages.ProjectLibrarySync(projectLibrary.project.id, projectLibrary.id)
        }
      }
      context.stop(self)
    }

    case m: Any => logUnhandledMessage(m)
  }

}
