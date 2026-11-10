package com.barofit.analytics.service;

import com.barofit.testsupport.BarofitTestPostgres;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 실제 PostgreSQL 동작을 검증하되 개발자의 로컬 DB에는 쓰지 않는다. barofit-common의 공유 컨테이너를 analytics 스키마로 마이그레이션해서 쓴다.
 *
 * <p>이전에는 {@code localhost:5432/barofit_analytics_test}를 하드코딩해서, 로컬에 그 DB가 미리 준비돼 있어야만 통과했다 (없으면
 * Hibernate가 dialect를 못 찾는 것처럼 보이는 연결 실패로 나타난다).
 */
abstract class AnalyticsPostgresSupport {
  private static final PostgreSQLContainer<?> POSTGRES = migrate();

  private static PostgreSQLContainer<?> migrate() {
    PostgreSQLContainer<?> container = BarofitTestPostgres.container();
    org.flywaydb.core.Flyway.configure()
        .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
        .schemas("analytics")
        .locations("classpath:db/migration/analytics")
        .load()
        .migrate();
    return container;
  }

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.datasource.url",
        () ->
            POSTGRES.getJdbcUrl()
                + (POSTGRES.getJdbcUrl().contains("?") ? "&" : "?")
                + "currentSchema=analytics");
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }
}
