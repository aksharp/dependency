package db

import com.bryzek.dependency.v0.models.UserForm
import io.flow.common.v0.models.Name
import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import java.util.UUID

class GithubUsersDaoSpec extends  DependencySpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  "upsertById" in {
    val form = createGithubUserForm()
    val user1 = githubUsersDao.create(None, form)

    val user2 = githubUsersDao.upsertById(None, form)
    user1.id must be(user2.id)

    val user3 = githubUsersDao.upsertById(Some(systemUser), createGithubUserForm())

    user2.id must not be(user3.id)
    user2.id must not be(user3.id)
  }

  "findById" in {
    val user = createGithubUser()
    githubUsersDao.findById(user.id).map(_.id) must be(
      Some(user.id)
    )

    usersDao.findById(UUID.randomUUID.toString) must be(None)
  }

  "findAll by ids" in {
    val user1 = createGithubUser()
    val user2 = createGithubUser()

    githubUsersDao.findAll(id = Some(Seq(user1.id, user2.id))).map(_.id) must be(
      Seq(user1.id, user2.id)
    )

    githubUsersDao.findAll(id = Some(Nil)) must be(Nil)
    githubUsersDao.findAll(id = Some(Seq(UUID.randomUUID.toString))) must be(Nil)
    githubUsersDao.findAll(id = Some(Seq(user1.id, UUID.randomUUID.toString))).map(_.id) must be(Seq(user1.id))
  }

}
