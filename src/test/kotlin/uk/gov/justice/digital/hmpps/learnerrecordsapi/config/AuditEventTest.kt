package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AuditEvent.createAuditEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import java.time.Instant

class AuditEventTest {

  @Test
  fun `should create Audit Event when correct set of parameters are passed`() {
    val request = LearnersRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      dateOfBirth = "1990-01-01",
      gender = Gender.valueOf("MALE"),
      lastKnownPostCode = "NE2 2AS",
      emailAddress = "test@example.com",
    )
    val hmppsAuditEvent = createAuditEvent("searchLearnersByDemographics", "User", request.toString())
    assertEquals("hmpps-learner-records-api", hmppsAuditEvent.service)
    assertEquals("searchLearnersByDemographics", hmppsAuditEvent.what)
    assertEquals("User", hmppsAuditEvent.who)
    assertThat(hmppsAuditEvent.`when`).isBeforeOrEqualTo(Instant.now())
    assertTrue(hmppsAuditEvent.details.toString().contains("NE2 2AS"))
  }
}
