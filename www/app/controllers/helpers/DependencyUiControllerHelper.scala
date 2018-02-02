package io.flow.dependency.controllers.helpers

import io.flow.dependency.v0.Client
import io.flow.dependency.v0.models.Organization
import io.flow.dependency.www.lib.{DependencyClientProvider, Section, UiData}
import controllers.routes
import io.flow.common.v0.models.UserReference
import io.flow.play.controllers.{AnonymousRequest, IdentifiedRequest}
import play.api.i18n.I18nSupport
import play.api.mvc.{Result, Results}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

trait DependencyUiControllerHelper extends I18nSupport {

  def section: Option[Section] = None

  val dependencyClientProvider: DependencyClientProvider

  private[this] lazy val client = dependencyClientProvider.newClient(user = None)

  def uiData[T](
    request: AnonymousRequest[T],
    userReferenceOption: Option[UserReference]
  )(
    implicit ec: ExecutionContext
  ): UiData = {
    val user = userReferenceOption.flatMap { ref =>
      Await.result(
        client.users.get(id = Some(ref.id)),
        Duration(1, "seconds")
      ).headOption
    }

    UiData(
      requestPath = request.path,
      user = user,
      section = section
    )
  }

  def uiData[T](
    request: IdentifiedRequest[T]
  )(
    implicit ec: ExecutionContext
  ): UiData = {
    val user = Await.result(
      client.users.get(id = Some(request.user.id)),
      Duration(1, "seconds")
    ).headOption

    UiData(
      requestPath = request.path,
      user = user,
      section = section
    )
  }

  def dependencyClient[T](request: IdentifiedRequest[T]): Client = {
    dependencyClientProvider.newClient(user = Some(request.user))
  }

  def withOrganization[T](
    request: IdentifiedRequest[T],
    key: String
  )(
    f: Organization => Future[Result]
  )(
    implicit ec: scala.concurrent.ExecutionContext
  ) = {
    dependencyClient(request).organizations.get(key = Some(key), limit = 1).flatMap { organizations =>
      organizations.headOption match {
        case None => Future {
          Results.Redirect(routes.ApplicationController.index()).flashing("warning" -> s"Organization not found")
        }
        case Some(org) => {
          f(org)
        }
      }
    }
  }

  def organizations[T](
    request: IdentifiedRequest[T]
  )(
    implicit ec: scala.concurrent.ExecutionContext
  ): Future[Seq[Organization]] = {
    dependencyClient(request).organizations.get(
      userId = Some(request.user.id),
      limit = 100
    )
  }
}
