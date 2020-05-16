package repositories

trait SchemaManager[F[_]] {
  def createSchema(): F[Int]
}
