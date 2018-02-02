package com.bryzek.dependency.actors

import javax.inject.Inject

import akka.actor.Actor
import db._
import play.api.db.Database

object SearchActor {

  sealed trait Message

  object Messages {
    case class SyncBinary(id: String) extends Message
    case class SyncLibrary(id: String) extends Message
    case class SyncProject(id: String) extends Message
  }

}

class SearchActor @Inject()(
  val db: Database
) extends Actor with Util with DbImplicits {

  lazy val SystemUser = usersDao.systemUser

  def receive = {

    case m @ SearchActor.Messages.SyncBinary(id) => withErrorHandler(m) {
      println(s"SearchActor.Messages.SyncBinary($id)")
      binariesDao.findById(Authorization.All, id) match {
        case None => itemsDao.deleteByObjectId(Authorization.All, SystemUser, id)
        case Some(binary) => itemsDao.replaceBinary(SystemUser, binary)
      }
    }

    case m @ SearchActor.Messages.SyncLibrary(id) => withErrorHandler(m) {
      librariesDao.findById(Authorization.All, id) match {
        case None => itemsDao.deleteByObjectId(Authorization.All, SystemUser, id)
        case Some(library) => itemsDao.replaceLibrary(SystemUser, library)
      }
    }

    case m @ SearchActor.Messages.SyncProject(id) => withErrorHandler(m) {
      projectsDao.findById(Authorization.All, id) match {
        case None => itemsDao.deleteByObjectId(Authorization.All, SystemUser, id)
        case Some(project) => itemsDao.replaceProject(SystemUser, project)
      }
    }

    case m: Any => logUnhandledMessage(m)
  }

}
