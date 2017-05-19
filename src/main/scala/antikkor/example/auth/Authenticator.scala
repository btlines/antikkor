package antikkor.example.auth

import Model._

case class Authenticator(
  nextToken: Token = Token(1),
  sessions: Map[Token, User] = Map.empty
) {
  def isValidUser(user: User): Boolean = user.name.nonEmpty
  def validateToken(token: Token): Option[User] = sessions.get(token)
  def signIn(user: User, token: Token): Authenticator = Authenticator(
    Token(token.id + 1),
    sessions + (token -> user)
  )
  def signOut(token: Token) = Authenticator(nextToken, sessions - token)
}
