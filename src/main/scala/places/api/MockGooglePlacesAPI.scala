package places.api

import java.io.{BufferedReader, File, FileReader}

import cats.effect.{Resource, Sync}
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import io.circe.parser.decode
import model.ClientError.ParseError
import model.GooglePlacesResponseModel.SearchResponse
import model.PlacesRequestModel.SearchPlacesRequest

import collection.JavaConverters._

class MockGooglePlacesAPI[F[_]: Sync] extends PlacesAPI[F] {

  override def explorePlaces(searchRequest: SearchPlacesRequest): F[SearchResponse] = {
    readLinesFromFile(new File("google_output.json")).flatMap { lines =>
      decode[SearchResponse](lines.mkString).fold(
        error => ParseError(error.getMessage).raiseError[F, SearchResponse],
        _.pure
      )
    }
  }

  private def readLinesFromFile(file: File): F[List[String]] =
    reader(file).use(br => readAllLines(br))

  private def readAllLines(bufferedReader: BufferedReader): F[List[String]] =
    Sync[F].delay(bufferedReader.lines().iterator().asScala.toList)

  private def reader(file: File): Resource[F, BufferedReader] =
    Resource.fromAutoCloseable(new BufferedReader(new FileReader(file)).pure[F])
}
