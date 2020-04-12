package util

import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

trait LoggerAppConfig {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.DEBUG
}
