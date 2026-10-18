package com.tarasovvp.cloudnativebackend.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.tarasovvp.cloudnativebackend.DB_USER_NAME_ENV
import com.tarasovvp.cloudnativebackend.DB_USER_PASSWORD_ENV
import com.tarasovvp.cloudnativebackend.DEFAULT_DB_USER_NAME
import com.tarasovvp.cloudnativebackend.DEFAULT_DB_USER_PASSWORD
import com.tarasovvp.cloudnativebackend.DEFAULT_DRIVER_CLASS_NAME
import com.tarasovvp.cloudnativebackend.DEFAULT_JDBC_URL
import com.tarasovvp.cloudnativebackend.DRIVER_CLASS_NAME_ENV
import com.tarasovvp.cloudnativebackend.JDBC_URL_ENV
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DatabaseDriverFactory {
    fun createDriver(): SqlDriver {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv(JDBC_URL_ENV) ?: DEFAULT_JDBC_URL
            driverClassName = System.getenv(DRIVER_CLASS_NAME_ENV) ?: DEFAULT_DRIVER_CLASS_NAME
            username = System.getenv(DB_USER_NAME_ENV) ?: DEFAULT_DB_USER_NAME
            password = System.getenv(DB_USER_PASSWORD_ENV) ?: DEFAULT_DB_USER_PASSWORD
            maximumPoolSize = 5
            minimumIdle = 2
            idleTimeout = 10_000
            connectionTimeout = 30_000
            maxLifetime = 1_800_000
        }
        return HikariDataSource(config).asJdbcDriver()
    }
}
