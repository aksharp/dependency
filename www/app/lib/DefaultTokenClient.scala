package com.bryzek.dependency.www.lib

import io.flow.common.v0.models.UserReference
import io.flow.token.v0.{OrganizationTokens, PartnerTokens, TokenValidations}
import io.flow.token.v0.interfaces.Client
import io.flow.token.v0.errors.UnitResponse
import io.flow.token.v0.models.{Cleartext, TokenAuthenticationForm, TokenReference, Token => FlowToken}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class DefaultTokenClient() extends Client {

  def baseUrl = throw new UnsupportedOperationException()

  def tokens: io.flow.token.v0.Tokens = new Tokens()

  def validations = throw new UnsupportedOperationException()

  override def organizationTokens: OrganizationTokens = throw new UnsupportedOperationException()

  override def partnerTokens: PartnerTokens = throw new UnsupportedOperationException()

  override def tokenValidations: TokenValidations = throw new UnsupportedOperationException()
}

class Tokens() extends io.flow.token.v0.Tokens {

  override def get(id: Option[Seq[String]], organization: Option[String], partner: Option[String], mine: Option[Boolean], limit: Long, offset: Long, sort: String, requestHeaders: Seq[(String, String)])
    (implicit ec: ExecutionContext): Future[Seq[FlowToken]] = {
    Future {
      val s = id.getOrElse(Seq.empty)
      val flowTokens: Seq[FlowToken] =
        s.map(t => {
          io.flow.token.v0.mock.Factories.makeOrganizationToken().copy(id = t, user = UserReference(t), createdAt = new DateTime, partial = t)
        })
      flowTokens
    }
  }

  override def getById(id: String, requestHeaders: Seq[(String, String)])(implicit ec: ExecutionContext): Future[FlowToken] =
    get(id = Option(Seq(id)), requestHeaders = requestHeaders).map(_.headOption.getOrElse {
      throw new UnitResponse(404)
    })

  override def getCleartextById(id: String, requestHeaders: Seq[(String, String)])(implicit ec: ExecutionContext): Future[Cleartext] = throw new UnsupportedOperationException()

  override def postAuthentications(tokenAuthenticationForm: TokenAuthenticationForm, requestHeaders: Seq[(String, String)])(implicit ec: ExecutionContext): Future[TokenReference] = throw new UnsupportedOperationException()

  override def deleteById(id: String, requestHeaders: Seq[(String, String)])(implicit ec: ExecutionContext): Future[Unit] = throw new UnsupportedOperationException()
}
