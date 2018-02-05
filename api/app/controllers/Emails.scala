package controllers

import com.google.inject.Provider
import db.{LastEmailsDao, RecommendationsDao, UserIdentifiersDao, UsersDao}
import io.flow.dependency.v0.models.Publication
import io.flow.dependency.api.lib.{Email, Recipient}
import io.flow.dependency.actors._
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.mvc._
import play.api.libs.json._

@javax.inject.Singleton
class Emails @javax.inject.Inject() (
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  usersDao: UsersDao,
  lastEmailsDao: LastEmailsDao,
  recommendationsDao: RecommendationsDao,
  userIdentifiersDao: UserIdentifiersDao
) extends FlowController   {

  private[this] val TestEmailAddressName = "io.flow.dependency.api.test.email"
  private[this] lazy val TestEmailAddress = config.optionalString(TestEmailAddressName)

  def user(
    session: play.api.mvc.Session,
    headers: play.api.mvc.Headers,
    path: String,
    queryString: Map[String, Seq[String]]
  ) (
    implicit ec: scala.concurrent.ExecutionContext
  ) = scala.concurrent.Future { None }

  def get() = Action { request =>
    TestEmailAddress match {
      case None => Ok(s"Set the $TestEmailAddressName property to enable testing")
      case Some(email) => {
        usersDao.findByEmail("mbryzek@alum.mit.edu") match {
          case None => Ok(s"No user with email address[$email] found")
          case Some(user) => {
            val recipient = Recipient.fromUser(userIdentifiersDao, usersDao, user).getOrElse {
              Recipient(email = "noemail@test.flow.io", name = user.name, userId = user.id, identifier = "TESTID")
            }
            val generator = new DailySummaryEmailMessage(recipient)

            Ok(
              Seq(
                "Subject: " + Email.subjectWithPrefix(config, generator.subject()),
                "<br/><br/><hr size=1/>",
                generator.body(lastEmailsDao, recommendationsDao, config)
              ).mkString("\n")
            ).as(HTML)
          }
        }
      }
    }
  }

}
