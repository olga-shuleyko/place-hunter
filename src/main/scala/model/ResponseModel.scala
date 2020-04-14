package model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object ResponseModel {

  final case class ResponseLocation(position: Seq[Double])
  final case class Context(location: ResponseLocation)
  final case class Search(context: Context)
  final case class Result(items: List[Item])
  final case class Item(id: String, title: String, position: Seq[Double])

  final case class SearchResponse(search: Search, results: Result)

  implicit val ResponseLocationEncoder: Encoder[ResponseLocation] = deriveEncoder[ResponseLocation]
  implicit val ResponseLocationDecoder: Decoder[ResponseLocation] = deriveDecoder[ResponseLocation]
  implicit val ContextDecoder: Decoder[Context] = deriveDecoder[Context]
  implicit val ContextEncoder: Encoder[Context] = deriveEncoder[Context]
  implicit val SearchDecoder: Decoder[Search] = deriveDecoder[Search]
  implicit val SearchEncoder: Encoder[Search] = deriveEncoder[Search]
  implicit val ResultDecoder: Decoder[Result] = deriveDecoder[Result]
  implicit val ResultEncoder: Encoder[Result] = deriveEncoder[Result]
  implicit val ItemDecoder: Decoder[Item] = deriveDecoder[Item]
  implicit val ItemEncoder: Encoder[Item] = deriveEncoder[Item]
  implicit val SearchResponseDecoder: Decoder[SearchResponse] = deriveDecoder[SearchResponse]
  implicit val SearchResponseEncoder: Encoder[SearchResponse] = deriveEncoder[SearchResponse]
  
}