package model

final case class JdbcConfig(url: String,
                            driver: String,
                            username: String,
                            password: String,
                            poolSize: Int
                           )
