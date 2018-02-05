package controllers

import db.Authorization
import io.flow.play.controllers.IdentifiedRequest

trait BaseIdentifiedController {

  def authorization[T](request: IdentifiedRequest[T]): Authorization = {
    Authorization.User(request.user.id)
  }

}
