package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLES
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_RO
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://learner-records-api-dev.hmpps.service.justice.gov.uk").description("Development"),
        Server().url("https://learner-records-api-preprod.hmpps.service.justice.gov.uk")
          .description("Pre-Production"),
        Server().url("https://learner-records-api.hmpps.service.justice.gov.uk").description("Production"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .components(
      Components().addSecuritySchemes(
        ROLE_LEARNERS_RO,
        SecurityScheme().addBearerJwtRequirement(ROLE_LEARNERS_RO),
      ).addSecuritySchemes(
        ROLE_LEARNERS_UI,
        SecurityScheme().addBearerJwtRequirement(ROLE_LEARNERS_UI),
      ),
    )
    .addSecurityItem(SecurityRequirement().addList(ROLE_LEARNERS_RO, ROLES[ROLE_LEARNERS_RO]))
    .addSecurityItem(SecurityRequirement().addList(ROLE_LEARNERS_UI, ROLES[ROLE_LEARNERS_UI]))
}

private fun SecurityScheme.addBearerJwtRequirement(role: String): SecurityScheme = type(SecurityScheme.Type.HTTP)
  .scheme("bearer")
  .bearerFormat("JWT")
  .`in`(SecurityScheme.In.HEADER)
  .name("Authorization")
  .description("A HMPPS Auth access token with the `$role` role.")
