package services.repos

import javax.inject.{Inject, Singleton}

import services.ids._
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

case class BlogPost(userId: UserId, title: String, summary: String, createdAt: DateTime,
                    id: Option[BlogPostId] = None)

@Singleton
class BlogPostRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                             val usersRepo: UsersRepo) extends Mappings {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.driver.api._

  private[services] val blogPosts = TableQuery[BlogPostTable]

  private[services] class BlogPostTable(tag: Tag) extends Table[BlogPost](tag, BlogPostsTable.name) {
    def id = column[BlogPostId]("ID", O.AutoInc, O.PrimaryKey)
    def userId = column[UserId]("USER_ID")
    def title = column[String]("TITLE")
    def summary = column[String]("SUMMARY")
    def createdAt = column[DateTime]("CREATED_AT")
    def * = (userId, title, summary, createdAt, id.?) <> (BlogPost.tupled, BlogPost.unapply)
    def userIdFk = foreignKey("BLOGPOST_USERID_FK", userId, usersRepo.users)(_.id)
  }
}
