package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

object Keys {

  const val KEY_LEARNERS_RD = "role-learners-rd"
  const val KEY_LEARNERS_UI = "role-learners-ui"
}

object Roles {

  const val ROLE_LEARNER_RECORDS_SEARCH__RO =
    "ROLE_LEARNER_RECORDS_SEARCH__RO"

  const val ROLE_LEARNER_RECORDS__LEARNER_RECORDS_MATCH_UI =
    "ROLE_LEARNER_RECORDS__LEARNER_RECORDS_MATCH_UI"

  private const val READ = "read"
  private const val WRITE = "write"

  val ROLES = mapOf(
    ROLE_LEARNER_RECORDS_SEARCH__RO to listOf(READ),
    ROLE_LEARNER_RECORDS__LEARNER_RECORDS_MATCH_UI to listOf(READ, WRITE),
  )
}
