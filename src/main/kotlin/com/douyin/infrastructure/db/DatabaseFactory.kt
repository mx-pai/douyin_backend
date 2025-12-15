package com.douyin.infrastructure.db

import com.douyin.application.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(config: DatabaseConfig) {
        val dataSource = hikari(config)
        runFlyway(dataSource)
        Database.connect(dataSource)
    }

    private fun hikari(config: DatabaseConfig): HikariDataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = config.driverClassName
        hikariConfig.jdbcUrl = config.jdbcUrl
        hikariConfig.username = config.username
        hikariConfig.password = config.password
        hikariConfig.maximumPoolSize = 3
        hikariConfig.isAutoCommit = false
        hikariConfig.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        hikariConfig.validate()
        return HikariDataSource(hikariConfig)
    }

    private fun runFlyway(dataSource: HikariDataSource) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
        flyway.migrate()
    }
}
