package controllers

import javax.inject.Inject

import io.flow.healthcheck.v0.models.json._
import io.flow.healthcheck.v0.models.Healthcheck
import io.flow.play.controllers.{FlowController, FlowControllerComponents}
import io.flow.play.util.Config
import play.api.libs.json._
import play.api.mvc._

class Healthchecks @Inject()(
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
) extends FlowController {

  private val HealthyJson = Json.toJson(Healthcheck(status = "healthy"))

  def getHealthcheck() = Action { request =>
    Ok(HealthyJson)
  }

}
