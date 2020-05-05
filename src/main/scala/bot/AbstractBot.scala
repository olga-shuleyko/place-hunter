package bot

import cats.effect.{Async, ContextShift}
import com.bot4s.telegram.cats.TelegramBot
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import model.Credentials._

import scala.language.higherKinds

abstract class AbstractBot[F[_]: Async: ContextShift](val token: BotToken)
  extends TelegramBot(token.token, AsyncHttpClientCatsBackend())
