package com.bryzek.dependency.actors

import javax.inject.Inject

import akka.actor.Actor
import com.bryzek.dependency.api.lib.{Email, Recipient}
import com.bryzek.dependency.lib.Urls
import com.bryzek.dependency.v0.models.{Publication, Subscription}
import db._
import io.flow.play.util.Config
import io.flow.postgresql.Pager
import org.joda.time.{DateTime, DateTimeZone}
import play.api.db.Database

object EmailActor {

  object Messages {

    case object ProcessDailySummary

  }

  val PreferredHourToSendEst: Int = {
    val config = play.api.Play.current.injector.instanceOf[Config]
    val value = config.requiredString("com.bryzek.dependency.api.email.daily.summary.hour.est").toInt
    assert(value >= 0 && value < 23)
    value
  }

}

class EmailActor @Inject()(
  val db: Database,
  batchEmailProcessor: BatchEmailProcessor,
  dailySummaryEmailMessage: DailySummaryEmailMessage,
  @javax.inject.Named("main-actor") val mainActorRef: akka.actor.ActorRef
) extends Actor with Util with DbImplicits {

  private[this] def currentHourEst(): Int = {
    (new DateTime()).toDateTime(DateTimeZone.forID("America/New_York")).getHourOfDay
  }

  def receive = {

    /**
      * Selects people to whom we delivery email by:
      *
      * If it is our preferred time to send (7am), filter by anybody
      * who has been a member for at least 2 hours and who has not
      * received an email in last 2 hours. We use 2 hours to catch up
      * from emails sent the prior day late (at say 10am) to get them
      * back on schedule, while making sure we don't send back to
      * back emails
      *
      * Otherwise, filter by 26 hours to allow us to catch up on any
      * missed emails
      */
    case m@EmailActor.Messages.ProcessDailySummary => withErrorHandler(m) {
      val hoursForPreferredTime = 2
      val hours = currentHourEst match {
        case EmailActor.PreferredHourToSendEst => hoursForPreferredTime
        case _ => 24 + hoursForPreferredTime
      }

      batchEmailProcessor.process(
        Publication.DailySummary,
        Pager.create { offset =>
          subscriptionsDao.findAll(
            publication = Some(Publication.DailySummary),
            minHoursSinceLastEmail = Some(hours),
            minHoursSinceRegistration = Some(hours),
            offset = offset
          )
        }
      ) { recipient =>
        dailySummaryEmailMessage.generate(recipient)
      }
    }

  }

}

class BatchEmailProcessor @Inject()(
  usersDao: UsersDao,
  lastEmailsDao: LastEmailsDao,
  dailySummaryEmailMessage: DailySummaryEmailMessage,
  userIdentifiersDao: UserIdentifiersDao
) {

  lazy val SystemUser = usersDao.systemUser

  def process(
    publication: Publication,
    subscriptions: Iterator[Subscription],
  )(
    generator: Recipient => GeneratedEmailMessage
  ) {
    subscriptions.foreach { subscription =>
      usersDao.findById(subscription.user.id).foreach { user =>
        Recipient.fromUser(userIdentifiersDao, usersDao, user).map {
          dailySummaryEmailMessage.generate
        }.map { generator =>
          // Record before send in case of crash - prevent loop of
          // emails.
          lastEmailsDao.record(
            SystemUser,
            LastEmailForm(
              userId = user.id,
              publication = publication
            )
          )

          Email.sendHtml(
            recipient = generator.recipient,
            subject = generator.subject(),
            body = generator.body()
          )
        }
      }
    }
  }
}

trait GeneratedEmailMessage {
  def recipient(): Recipient

  def subject(): String

  def body(): String
}

/**
  * Class which generates email message
  */

class DailySummaryEmailMessage @Inject()(
  LastEmailsDao: LastEmailsDao,
  RecommendationsDao: RecommendationsDao,
  config: Config
) {

  def generate(r: Recipient): GeneratedEmailMessage =
    new GeneratedEmailMessage {
      override def recipient(): Recipient = r
      override def subject(): String = subject()
      override def body(): String = body()
    }

  private[this] val MaxRecommendations = 250

  def subject() = "Daily Summary"

  def body(recipient: Recipient) = {
    val lastEmail = LastEmailsDao.findByUserIdAndPublication(recipient.userId, Publication.DailySummary)
    val recommendations = RecommendationsDao.findAll(
      Authorization.User(recipient.userId),
      limit = MaxRecommendations
    )

    val (newRecommendations, oldRecommendations) = lastEmail match {
      case None => (recommendations, Nil)
      case Some(email) => {
        (
          recommendations.filter {
            !_.createdAt.isBefore(email.createdAt)
          },
          recommendations.filter {
            _.createdAt.isBefore(email.createdAt)
          }
        )
      }
    }

    views.html.emails.dailySummary(
      recipient = recipient,
      newRecommendations = newRecommendations,
      oldRecommendations = oldRecommendations,
      lastEmail = lastEmail,
      urls = Urls(config)
    ).toString
  }

}
