package com.github.kapitanfloww.jump.persistence.sql;

import com.github.kapitanfloww.jump.config.FlowwJumpConfiguration;
import com.github.kapitanfloww.jump.model.Jump;
import com.github.kapitanfloww.jump.persistence.JumpRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.java.Log;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

@Log
public class JdbcJumpRepository implements JumpRepository {

    private final HikariDataSource hikariDataSource;

    public JdbcJumpRepository(FlowwJumpConfiguration configuration) {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(configuration.getJdbcUrl());
        hikariConfig.setUsername(configuration.getJdbcUsername());
        hikariConfig.setPassword(configuration.getJdbcPassword());
        hikariConfig.setDataSourceClassName(configuration.getJdbcDataSourceClassName());
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public Jump save(Jump jump) {
        return null;
    }

    @Override
    public Jump merge(Jump jump) {
        return null;
    }

    @Override
    public Optional<Jump> find(String name) {
        return Optional.empty();
    }

    @Override
    public Set<Jump> findAll() {
        return null;
    }

    @Override
    public void delete(Jump jump) {

    }

    @Override
    public Set<String> findAllJumpNames() {
        return null;
    }

    private Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }
}
