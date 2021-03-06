package places.api

import java.io.{BufferedReader, File, FileReader}

import cats.effect.{Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.either._
import io.circe.parser.decode
import model.ClientError.ParseError
import model.GooglePlacesResponseModel.SearchResponse
import model.PlacesRequestModel.SearchPlacesRequest

import collection.JavaConverters._

class MockGooglePlacesAPI[F[_]: Sync] extends PlacesAPI[F] {

  override def explorePlaces(searchRequest: SearchPlacesRequest): F[SearchResponse] = {
    readLinesFromFile(new File("google_output.json")).flatMap { lines =>
      decode[SearchResponse](lines.mkString)
        .leftMap(error => ParseError(error.getMessage))
        .liftTo[F]
    }
  }

  private def readLinesFromFile(file: File): F[List[String]] =
    reader(file).use(br => readAllLines(br))

  private def readAllLines(bufferedReader: BufferedReader): F[List[String]] =
    Sync[F].delay(bufferedReader.lines().iterator().asScala.toList)

  private def reader(file: File): Resource[F, BufferedReader] =
    Resource.fromAutoCloseable(Sync[F].delay(new BufferedReader(new FileReader(file))))
}
