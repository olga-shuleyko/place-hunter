package model

final case class JdbcConfig(url: String,
                            driver: String,
                            user: String,
                            password: String
                           )
