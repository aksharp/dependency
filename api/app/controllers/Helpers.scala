package controllers

import db.{Authorization, BinariesDao, LibrariesDao, OrganizationsDao, ProjectsDao, ResolversDao, UsersDao}
import com.bryzek.dependency.v0.models.{Binary, Library, Organization, Project, Resolver}
import io.flow.common.v0.models.{User, UserReference}
import io.flow.play.controllers.IdentifiedRequest
import play.api.mvc.{Result, Results}

trait Helpers {

  def authorization[T](request: IdentifiedRequest[T]): Authorization = {
    Authorization.User(request.user.id)
  }

  def withBinary(binariesDao: BinariesDao, user: UserReference, id: String)(
    f: Binary => Result
  ): Result = {
    binariesDao.findById(Authorization.User(user.id), id) match {
      case None => {
        Results.NotFound
      }
      case Some(binary) => {
        f(binary)
      }
    }
  }
  
  def withLibrary(librariesDao: LibrariesDao, user: UserReference, id: String)(
    f: Library => Result
  ): Result = {
    librariesDao.findById(Authorization.User(user.id), id) match {
      case None => {
        Results.NotFound
      }
      case Some(library) => {
        f(library)
      }
    }
  }

  def withOrganization(organizationsDao: OrganizationsDao, user: UserReference, id: String)(
    f: Organization => Result
  ) = {
    organizationsDao.findById(Authorization.User(user.id), id) match {
      case None => {
        Results.NotFound
      }
      case Some(organization) => {
        f(organization)
      }
    }
  }

  def withProject(projectsDao: ProjectsDao, user: UserReference, id: String)(
    f: Project => Result
  ): Result = {
    projectsDao.findById(Authorization.User(user.id), id) match {
      case None => {
        Results.NotFound
      }
      case Some(project) => {
        f(project)
      }
    }
  }

  def withResolver(resolversDao: ResolversDao, user: UserReference, id: String)(
    f: Resolver => Result
  ): Result = {
    resolversDao.findById(Authorization.User(user.id), id) match {
      case None => {
        Results.NotFound
      }
      case Some(resolver) => {
        f(resolver)
      }
    }
  }

  def withUser(usersDao: UsersDao, id: String)(
    f: User => Result
  ) = {
    usersDao.findById(id) match {
      case None => {
        Results.NotFound
      }
      case Some(user) => {
        f(user)
      }
    }
  }

}
