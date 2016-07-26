package models.repos

import javax.inject.{Inject, Singleton}

import models.ids.UserId
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.Future

case class User(name: String, email: String, key: String, createdAt: DateTime, id: Option[UserId] = None)

@Singleton
class UsersRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Mappings {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.driver.api._
  val db = dbConfig.db

  val users = TableQuery[Users]

  def findById(id: UserId): Future[User] = {
    db.run(users.filter(_.id === id).result.head)
  }

  class Users(tag: Tag) extends Table[User](tag, UsersTable.name) {
    def id = column[UserId]("ID", O.AutoInc, O.PrimaryKey)
    def name = column[String]("NAME")
    def email = column[String]("EMAIL")
    def key = column[String]("KEY")
    def createdAt = column[DateTime]("CREATED_AT")
    def * = (name, email, key, createdAt, id.?) <> (User.tupled, User.unapply)
  }

}
