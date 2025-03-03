package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

object Roles {

  const val ROLE_LEARNERS_RO =
    "ROLE_LEARNER_RECORDS_SEARCH__RO"

  const val ROLE_LEARNERS_UI =
    "ROLE_LEARNER_RECORDS__LEARNER_RECORDS_MATCH_UI"

  const val ROLE_LEARNERS_SA =
    "ROLE_SAR_DATA_ACCESS"

  private const val READ = "read"
  private const val WRITE = "write"

  val ROLES = mapOf(
    ROLE_LEARNERS_RO to listOf(READ),
    ROLE_LEARNERS_UI to listOf(READ, WRITE),
  )
}
