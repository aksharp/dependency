package controllers

import java.util.UUID

import com.bryzek.dependency.v0.models.UserForm
import io.flow.common.v0.models.Name
import play.api.test._
import util.{DependencySpec, MockDependencyClient}

class UsersSpec extends DependencySpec with MockDependencyClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val user1 = createUser()
  lazy val user2 = createUser()

  "GET /users requires auth" in new WithServer(port=port) {
    expectNotAuthorized {
      anonClient.users.get()
    }
  }

  "GET /users/:id" in new WithServer(port=port) {
    expectNotAuthorized {
      anonClient.users.getById(UUID.randomUUID.toString)
    }
  }

  "GET /users by id" in new WithServer(port=port) {
    await(
      identifiedClient().users.get(id = Some(user1.id))
    ).map(_.id) must contain theSameElementsAs Seq(user1.id)

    await(
      identifiedClient().users.get(id = Some(UUID.randomUUID.toString))
    ).map(_.id) must be(
      Nil
    )
  }

  "GET /users by email" in new WithServer(port=port) {
    await(
      identifiedClient().users.get(email = user1.email)
    ).map(_.email) must contain theSameElementsAs Seq(user1.email)

    await(
      identifiedClient().users.get(email = Some(UUID.randomUUID.toString))
    ) must be(
      Nil
    )
  }

  "GET /users/:id" in new WithServer(port=port) {
    await(identifiedClient().users.getById(user1.id)).id must be(user1.id)
    await(identifiedClient().users.getById(user2.id)).id must be(user2.id)

    expectNotFound {
      identifiedClient().users.getById(UUID.randomUUID.toString)
    }
  }

  "POST /users w/out name" in new WithServer(port=port) {
    val email = createTestEmail()
    val user = await(anonClient.users.post(UserForm(email = Some(email))))
    user.email must be(Some(email))
    user.name.first must be(None)
    user.name.last must be(None)
  }

  "POST /users w/ name" in new WithServer(port=port) {
    val email = createTestEmail()
    val user = await(
      anonClient.users.post(
        UserForm(
          email = Some(email),
          name = Some(
            Name(first = Some("Michael"), last = Some("Bryzek"))
          )
        )
      )
    )
    user.email must be(Some(email))
    user.name.first must be(Some("Michael"))
    user.name.last must be(Some("Bryzek"))
  }

  "POST /users validates duplicate email" in new WithServer(port=port) {
    expectErrors(
      anonClient.users.post(UserForm(email = Some(user1.email.get)))
    ).errors.flatMap(_.messages) must contain theSameElementsAs Seq("Email is already registered")
  }

  "POST /users validates empty email" in new WithServer(port=port) {
    expectErrors(
      anonClient.users.post(UserForm(email = Some("   ")))
    ).errors.flatMap(_.messages) must contain theSameElementsAs Seq("Email address cannot be empty")
  }

  "POST /users validates email address format" in new WithServer(port=port) {
    expectErrors(
      anonClient.users.post(UserForm(email = Some("mbfoo.com")))
    ).errors.flatMap(_.messages) must contain theSameElementsAs Seq("Please enter a valid email address")
  }

}
