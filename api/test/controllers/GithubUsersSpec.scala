package controllers

import com.bryzek.dependency.api.lib.MockGithubData
import com.bryzek.dependency.v0.models.GithubAuthenticationForm
import io.flow.github.v0.models.{OwnerType, User => GithubUser}
import play.api.test._
import util.{DependencySpec, MockDependencyClient}

class GithubUsersSpec extends DependencySpec with MockDependencyClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  def createLocalGithubUser(): GithubUser = {
    val login = createTestKey()
    GithubUser(
      id = random.positiveLong(),
      login = login,
      name = None,
      email = Some(createTestEmail()),
      avatarUrl = None,
      gravatarId = None,
      url = s"https://github.com/$login",
      htmlUrl = s"https://github.com/$login",
      `type` = OwnerType.User
    )
  }

  "POST /authentications/github with valid token" in new WithServer(port=port) {
    val githubUser = createLocalGithubUser()
    val code = "test"

    MockGithubData.addUser(githubUser, code)

    val user = await(anonClient.githubUsers.postGithub(GithubAuthenticationForm(code = code)))
    user.email must be(githubUser.email)

    githubUsersDao.findAll(userId = Some(user.id), limit = 1).headOption.map(_.user.id) must be(Some(user.id))

    // Test idempotence
    val user2 = await(anonClient.githubUsers.postGithub(GithubAuthenticationForm(code = code)))
    user2.email must be(githubUser.email)
  }

  "POST /authentications/github accepts account w/out email" in new WithServer(port=port) {
    val githubUser = createLocalGithubUser().copy(email = None)
    val code = "test"

    MockGithubData.addUser(githubUser, code)
    val user = await(
      anonClient.githubUsers.postGithub(GithubAuthenticationForm(code = code))
    )
    user.email must be(None)
    usersDao.findByGithubUserId(githubUser.id).map(_.id) must be(Some(user.id))
  }

}
