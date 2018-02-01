package controllers

import io.flow.common.v0.models.UserReference
import io.flow.token.v0.interfaces.{Client => TokenClient}
import io.flow.token.v0.models.{OrganizationToken, PartnerToken}
import scala.concurrent.Future

object Helpers {

  def userFromSession(
    tokenClient: TokenClient,
    session: play.api.mvc.Session
  )(
    implicit ec: scala.concurrent.ExecutionContext
  ): scala.concurrent.Future[Option[UserReference]] = {
    session.get("user_id") match {
      case None => {
        Future {
          None
        }
      }

      case Some(userId) => {
        tokenClient.tokens.get(id = Some(Seq(userId))).map { result =>
          result.headOption.flatMap {
            case t: OrganizationToken => Option(t.user)
            case t: PartnerToken => Option(t.user)
            case _ => None
          }
        }
      }
    }
  }

}
