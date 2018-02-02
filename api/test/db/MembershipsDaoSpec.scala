package db

import java.util.UUID

import io.flow.dependency.v0.models.Role
import util.DependencySpec

class MembershipsDaoSpec extends DependencySpec {

  lazy val org = createOrganization()
  lazy val user = createUser()
  lazy val membership = createMembership(createMembershipForm(org = org, user = user))

  "isMember by id" in {
    membership // Create the membership record

    membershipsDao.isMemberByOrgId(org.id, user) must be(true)
    membershipsDao.isMemberByOrgId(org.id, createUser()) must be(false)
    membershipsDao.isMemberByOrgId(createOrganization().id, user) must be(false)
  }

  "isMember by key" in {
    membership // Create the membership record

    membershipsDao.isMemberByOrgKey(org.key, user) must be(true)
    membershipsDao.isMemberByOrgKey(org.key, createUser()) must be(false)
    membershipsDao.isMemberByOrgKey(createOrganization().key, user) must be(false)
  }

  "findByOrganizationIdAndUserId" in {
    membership // Create the membership record

    membershipsDao.findByOrganizationIdAndUserId(Authorization.All, org.id, user.id).map(_.id) must be(
      Some(membership.id)
    )

    membershipsDao.findByOrganizationIdAndUserId(Authorization.All, UUID.randomUUID.toString, user.id) must be(None)
    membershipsDao.findByOrganizationIdAndUserId(Authorization.All, org.id, UUID.randomUUID.toString) must be(None)
  }

  "findById" in {
    membershipsDao.findById(Authorization.All, membership.id).map(_.id) must be(
      Some(membership.id)
    )

    membershipsDao.findById(Authorization.All, UUID.randomUUID.toString) must be(None)
  }

  "soft delete" in {
    val membership = createMembership()
    membershipsDao.delete(systemUser, membership)
    membershipsDao.findById(Authorization.All, membership.id) must be(None)
  }

  "validates role" in {
    val form = createMembershipForm(role = Role.UNDEFINED("other"))
    membershipsDao.validate(systemUser, form) must be(Seq("Invalid role. Must be one of: member, admin"))
  }

  "validates duplicate" in {
    val org = createOrganization()
    val user = createUser()
    val form = createMembershipForm(org = org, user = user, role = Role.Member)
    val membership = createMembership(form)

    membershipsDao.validate(systemUser, form) must be(Seq("User is already a member"))
    membershipsDao.validate(systemUser, form.copy(role = Role.Admin)) must be(Seq("User is already a member"))
  }

  "validates access to org" in {
    membershipsDao.validate(createUser(), createMembershipForm()) must be(
      Seq("Organization does not exist or you are not authorized to access this organization")
    )
  }

  "findAll" must {

    "ids" in {
      val membership2 = createMembership()

      membershipsDao.findAll(Authorization.All, ids = Some(Seq(membership.id, membership2.id))).map(_.id) must be(
        Seq(membership.id, membership2.id)
      )

      membershipsDao.findAll(Authorization.All, ids = Some(Nil)) must be(Nil)
      membershipsDao.findAll(Authorization.All, ids = Some(Seq(UUID.randomUUID.toString))) must be(Nil)
      membershipsDao.findAll(Authorization.All, ids = Some(Seq(membership.id, UUID.randomUUID.toString))).map(_.id) must be(Seq(membership.id))
    }

    "userId" in {
      membershipsDao.findAll(Authorization.All, id = Some(membership.id), userId = Some(user.id)).map(_.id) must be(
        Seq(membership.id)
      )

      membershipsDao.findAll(Authorization.All, id = Some(membership.id), userId = Some(UUID.randomUUID.toString)) must be(Nil)
    }

    "organizationId" in {
      membershipsDao.findAll(Authorization.All, id = Some(membership.id), organizationId = Some(membership.organization.id)).map(_.id) must be(
        Seq(membership.id)
      )

      membershipsDao.findAll(Authorization.All, id = Some(membership.id), organizationId = Some(UUID.randomUUID.toString)) must be(Nil)
    }
  }
}

object MembershipsDaoSpec {

}
