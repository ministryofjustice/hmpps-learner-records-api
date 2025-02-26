package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

object Keys {

  const val KEY_LEARNERS = "role-learners"
  const val KEY_MATCHING = "role-matching"
}

object Roles {

  const val ROLE_LEARNER_RECORDS_SEARCH__RO =
    "ROLE_LEARNER_RECORDS_SEARCH__RO"

  const val ROLE_LEARNER_RECORDS_MATCH__RW =
    "ROLE_LEARNER_RECORDS_MATCH__RW"

  private const val READ = "read"
  private const val WRITE = "write"

  val ROLES = mapOf(
    ROLE_LEARNER_RECORDS_SEARCH__RO to listOf(READ),
    ROLE_LEARNER_RECORDS_MATCH__RW to listOf(READ, WRITE),
  )
}
