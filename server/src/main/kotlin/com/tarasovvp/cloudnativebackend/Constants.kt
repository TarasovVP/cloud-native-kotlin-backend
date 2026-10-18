package com.tarasovvp.cloudnativebackend

const val HTTP_HOST = "0.0.0.0"
const val HTTP_PORT = 8080
const val PORT_ENV = "PORT"

const val JDBC_URL_ENV = "JDBC_URL"
const val DRIVER_CLASS_NAME_ENV = "DRIVER_CLASS_NAME"
const val DB_USER_NAME_ENV = "DB_USER_NAME"
const val DB_USER_PASSWORD_ENV = "DB_USER_PASSWORD"

const val DB_NAME = "DemoObjectWithOwner"
const val DB_INSTANCE_CONNECTION_NAME =
    "android-architecture-templates:us-central1:android-architecture-templates-db"
const val DEFAULT_JDBC_URL =
    "jdbc:postgresql:///$DB_NAME?cloudSqlInstance=$DB_INSTANCE_CONNECTION_NAME" +
        "&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
const val DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver"
const val DEFAULT_DB_USER_NAME = "dbusername"
const val DEFAULT_DB_USER_PASSWORD = "yourpassword"
