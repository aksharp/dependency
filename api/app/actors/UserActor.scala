package com.bryzek.dependency.actors

import javax.inject.Inject

import com.bryzek.dependency.v0.models.{Publication, SubscriptionForm}
import io.flow.common.v0.models.User
import db._
import akka.actor.Actor
import play.api.db.Database

import scala.concurrent.ExecutionContext

object UserActor {

  trait Message

  object Messages {
    case class Data(id: String) extends Message
    case object Created extends Message
  }

}

class UserActor @Inject()(
  val db: Database,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends Actor with Util with DbImplicits {

  lazy val SystemUser = usersDao.systemUser
  var dataUser: Option[User] = None

  def receive = {

    case m @ UserActor.Messages.Data(id) => withErrorHandler(m.toString) {
      dataUser = usersDao.findById(id)
    }

    case m @ UserActor.Messages.Created => withErrorHandler(m.toString) {
      dataUser.foreach { user =>
        organizationsDao.upsertForUser(user)

        // This method will force create an identifier
        userIdentifiersDao.latestForUser(SystemUser, user)

        // Subscribe the user automatically to key personalized emails.
        Seq(Publication.DailySummary).foreach { publication =>
          subscriptionsDao.upsertByUserIdAndPublication(
            SystemUser,
            SubscriptionForm(
              userId = user.id,
              publication = publication
            )
          )
        }
      }
    }

    case m: Any => logUnhandledMessage(m)
  }

}
