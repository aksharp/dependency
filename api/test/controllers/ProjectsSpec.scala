package controllers

import java.util.UUID

import play.api.test._
import util.{DependencySpec, MockDependencyClient}

class ProjectsSpec extends DependencySpec with MockDependencyClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val org = createOrganization()
  lazy val project1 = createProject(org)()
  lazy val project2 = createProject(org)()

  "GET /projects by id" in  {
    await(
      identifiedClient().projects.get(id = Some(project1.id))
    ).map(_.id) must contain theSameElementsAs Seq(project1.id)

    await(
      identifiedClient().projects.get(id = Some(UUID.randomUUID.toString))
    ).map(_.id) must be(
      Nil
    )
  }

  /*
  "GET /projects by name" in  {
    await(
      client.projects.get(name = Some(project1.name))
    ).map(_.name) must beEqualTo(
      Seq(project1.name)
    )

    await(
      client.projects.get(name = Some(project1.name.toUpperCase))
    ).map(_.name) must beEqualTo(
      Seq(project1.name)
    )

    await(
      client.projects.get(name = Some(UUID.randomUUID.toString))
    ) must be(
      Nil
    )
  }

  "GET /projects/:id" in  {
    await(client.projects.getById(project1.id)).id must beEqualTo(project1.id)
    await(client.projects.getById(project2.id)).id must beEqualTo(project2.id)

    expectNotFound {
      client.projects.getById(UUID.randomUUID.toString)
    }
  }

  "POST /projects" in  {
    val form = createProjectForm(org)
    val project = await(client.projects.post(form))
    project.name must beEqualTo(form.name)
    project.scms must beEqualTo(form.scms)
    project.uri must beEqualTo(form.uri)
  }

  "POST /projects validates duplicate name" in  {
    expectErrors(
      client.projects.post(createProjectForm(org).copy(name = project1.name))
    ).errors.map(_.message) must beEqualTo(
      Seq("Project with this name already exists")
    )
  }

  "PUT /projects/:id" in  {
    val form = createProjectForm(org)
    val project = createProject(org)(form)
    val newUri = "http://github.com/mbryzek/test"
    await(client.projects.putById(project.id, form.copy(uri = newUri)))
    await(client.projects.getById(project.id)).uri must beEqualTo(newUri)
  }

  "PATCH /projects/:id w/ no data leaves project unchanged" in  {
    val project = createProject(org)()
    await(client.projects.patchById(project.id, ProjectPatchForm()))
    val updated = await(client.projects.getById(project.id))
    updated.name must beEqualTo(project.name)
    updated.scms must beEqualTo(project.scms)
    updated.uri must beEqualTo(project.uri)
  }

  "PATCH /projects/:id w/ name" in  {
    val project = createProject(org)()
    val newName = project.name + "2"
    await(client.projects.patchById(project.id, ProjectPatchForm(name = Some(newName))))
    await(client.projects.getById(project.id)).name must beEqualTo(newName)
  }

  "DELETE /projects" in  {
    val project = createProject(org)()
    await(
      client.projects.deleteById(project.id)
    ) must beEqualTo(())

    expectNotFound(
      client.projects.getById(project.id)
    )

    expectNotFound(
      client.projects.deleteById(project.id)
    )
  }
 */
}
