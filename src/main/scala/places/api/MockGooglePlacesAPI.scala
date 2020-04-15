package places.api

import java.io.{BufferedReader, File, FileReader}

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.flatMap._
import io.circe.parser.decode
import cats.effect.{Resource, Sync}
import io.circe.Decoder
import model.ClientError.ParseError
import model.{ChatId, SearchRequest}
import model.GooglePlacesResponseModel.SearchResponse

import collection.JavaConverters._

class MockGooglePlacesAPI[F[_]: Sync](decoder: Decoder[SearchResponse])
  extends PlacesAPI[F] {

  override def explorePlaces(chatId: ChatId, searchRequest: SearchRequest): F[SearchResponse] = {
    val ME = MonadError[F, Throwable]
    readLinesFromFile(new File("google_output.json")).flatMap { lines =>
      decode[SearchResponse](lines.mkString)(decoder).fold(
        error => ME.raiseError[SearchResponse](ParseError(chatId, error.getMessage)),
        (res: SearchResponse) => ME.pure(res)
      )
    }
  }

  private def readLinesFromFile(file: File): F[List[String]] =
    reader(file).use(br => readAllLines(br))

  private def readAllLines(bufferedReader: BufferedReader): F[List[String]] =
    bufferedReader.lines().iterator().asScala.toList.pure[F]

  private def reader(file: File): Resource[F, BufferedReader] =
    Resource.fromAutoCloseable(new BufferedReader(new FileReader(file)).pure[F])
}
