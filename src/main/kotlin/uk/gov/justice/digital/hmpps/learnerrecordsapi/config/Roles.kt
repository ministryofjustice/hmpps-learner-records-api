package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

object Keys {

  const val KEY_LEARNERS = "role-learners"
  const val KEY_MATCHING = "role-matching"
}

object Roles {

  const val ROLE_LEARNERS = "ROLE_LEARNERS__RO"
  const val ROLE_MATCHING = "ROLE_MATCHING__RW"

  private const val READ = "read"
  private const val WRITE = "write"

  val PERMISSIONS = mapOf(
    ROLE_LEARNERS to listOf(READ),
    ROLE_MATCHING to listOf(READ, WRITE),
  )
}
