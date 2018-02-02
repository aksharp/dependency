package db

import javax.inject.{Inject, Singleton}

import com.bryzek.dependency.api.lib.Recommendations
import com.bryzek.dependency.v0.models.{Library, LibraryVersion, Project, ProjectLibrary, VersionForm}
import io.flow.postgresql.Pager
import anorm._
import play.api.db._
import play.api.libs.json._

case class LibraryRecommendation(
  library: Library,
  from: String,
  to: LibraryVersion,
  latest: LibraryVersion
)

@Singleton
class LibraryRecommendationsDao @Inject() (
  val db: Database,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends DbImplicits {

  def forProject(project: Project): Seq[LibraryRecommendation] = {
    var recommendations = scala.collection.mutable.ListBuffer[LibraryRecommendation]()
    val auth = Authorization.Organization(project.organization.id)

    projectLibrariesDao.findAll(
      Authorization.Organization(project.organization.id),
      projectId = Some(project.id),
      hasLibrary = Some(true),
      limit = None
    ).foreach { projectLibrary =>
      projectLibrary.library.flatMap { lib => librariesDao.findById(auth, lib.id) }.map { library =>
        val recentVersions = versionsGreaterThan(auth, library, projectLibrary.version)
        recommend(projectLibrary, recentVersions).map { v =>
          recommendations ++= Seq(
            LibraryRecommendation(
              library = library,
              from = projectLibrary.version,
              to = v,
              latest = recentVersions.headOption.getOrElse(v)
            )
          )
        }
      }
    }

    recommendations
  }

  def recommend(current: ProjectLibrary, others: Seq[LibraryVersion]): Option[LibraryVersion] = {
    Recommendations.version(
      VersionForm(current.version, current.crossBuildVersion),
      others.map(v => VersionForm(v.version, v.crossBuildVersion))
    ).map { version =>
      others.find { _.version == version }.getOrElse {
        sys.error(s"Failed to find recommended library with version[$version]")
      }
    }
  }

  /**
   * Returns all versions of a library greater than the one specified
   */
  private[this] def versionsGreaterThan(
    auth: Authorization,
    library: Library,
    version: String
  )(implicit libraryVersionsDao: LibraryVersionsDao): Seq[LibraryVersion] = {
    libraryVersionsDao.findAll(
      auth,
      libraryId = Some(library.id),
      greaterThanVersion = Some(version),
      limit = None
    )
  }

}
