package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Keys.KEY_LEARNERS
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Keys.KEY_MATCHING
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.PERMISSIONS
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_MATCHING

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://hmpps-learner-records-api-dev.hmpps.service.justice.gov.uk").description("Development"),
        Server().url("https://hmpps-learner-records-api-uat.hmpps.service.justice.gov.uk").description("User Acceptance Testing"),
        Server().url("https://hmpps-learner-records-api-preprod.hmpps.service.justice.gov.uk")
          .description("Pre-Production"),
        Server().url("https://hmpps-learner-records-api.hmpps.service.justice.gov.uk").description("Production"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .components(
      Components().addSecuritySchemes(
        KEY_LEARNERS,
        SecurityScheme().addBearerJwtRequirement(ROLE_LEARNERS),
      ).addSecuritySchemes(
        KEY_MATCHING,
        SecurityScheme().addBearerJwtRequirement(ROLE_MATCHING),
      ),
    )
    .addSecurityItem(SecurityRequirement().addList(KEY_LEARNERS, PERMISSIONS[ROLE_LEARNERS]))
    .addSecurityItem(SecurityRequirement().addList(KEY_MATCHING, PERMISSIONS[ROLE_MATCHING]))
}

private fun SecurityScheme.addBearerJwtRequirement(role: String): SecurityScheme = type(SecurityScheme.Type.HTTP)
  .scheme("bearer")
  .bearerFormat("JWT")
  .`in`(SecurityScheme.In.HEADER)
  .name("Authorization")
  .description("A HMPPS Auth access token with the `$role` role.")
