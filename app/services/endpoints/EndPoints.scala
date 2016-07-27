package services.endpoints

object GOAuthEndpoints {
  val goauthServiceURL = s"""https://accounts.google.com/o/oauth2/v2/auth"""
  val clientId = s"""995561758104-2civgrjum3kij0fhj1h836rppi4jqg04.apps.googleusercontent.com"""
  val clientSecret = s"""rA7Gu4LIuYC2dPYvTUQk0B9x"""

  object UserInfo {
    def userInfoURL(accessToken: String) = s"""https://www.googleapis.com/oauth2/v1/userinfo?access_token=$accessToken"""
  }

}
