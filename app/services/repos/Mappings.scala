package models.repos

import java.sql.Timestamp

import services.ids._
import org.joda.time.DateTime
import services.models.{Email, NickName, Source}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait Mappings {
  val dbConfig: DatabaseConfig[JdbcProfile]

  import dbConfig.driver.api._

  implicit def dateTimeMapping = MappedColumnType.base[DateTime, Timestamp](
    { t: DateTime => new Timestamp(t.getMillis) }, { d: Timestamp => new DateTime(d.getTime) })

  implicit def userIdMapping = MappedColumnType.base[UserId, String](_.id, UserId(_))

  implicit def tagIdMapping = MappedColumnType.base[TypeTagId, Long](_.id, TypeTagId(_))

  implicit def blogPostIdMapping = MappedColumnType.base[BlogPostId, Long](_.id, BlogPostId(_))

  implicit def gistIdMapping = MappedColumnType.base[GistId, Long](_.id, GistId(_))

  implicit def githubIdMapping = MappedColumnType.base[GithubId, String](_.id, GithubId(_))

  implicit def nickNameMapping = MappedColumnType.base[NickName, String](_.nameStr, NickName(_))

  implicit def emailMapping = MappedColumnType.base[Email, String](_.emailStr, Email(_))

  implicit def sourceValueMapping = MappedColumnType.base[Source.Value, String](
    { (a: Source.Value) => a.toString }, {
      case str if str == Source.GOOGLE.toString => Source.GOOGLE
      case str if str == Source.FACEBOOK.toString => Source.FACEBOOK
      case str: String => Source.Other
    })
}
