package util

import com.bryzek.dependency.v0.Client
import io.flow.common.v0.models.UserReference
import io.flow.play.util.{AuthHeaders, FlowSession}
import io.flow.test.utils.{FlowMockClient, FlowPlaySpec}

trait MockDependencyClient extends FlowMockClient[
  com.bryzek.dependency.v0.Client,
  com.bryzek.dependency.v0.errors.ErrorsResponse,
  com.bryzek.dependency.v0.errors.UnitResponse
  ]{
  self: FlowPlaySpec =>

  override def createAnonymousClient(baseUrl: String): Client =
    new com.bryzek.dependency.v0.Client(
      ws = wsClient,
      baseUrl = baseUrl
    )

  override def createIdentifiedClient(baseUrl: String, user: UserReference, org: Option[String], session: Option[FlowSession]): Client = {
    val auth = org match {
      case None =>  AuthHeaders.user(user, session = session)
      case Some(o) => AuthHeaders.organization(user, o, session = session)
    }

    new com.bryzek.dependency.v0.Client(
      ws = wsClient,
      baseUrl = baseUrl,
      defaultHeaders = authHeaders.headers(auth)
    )
  }
}
