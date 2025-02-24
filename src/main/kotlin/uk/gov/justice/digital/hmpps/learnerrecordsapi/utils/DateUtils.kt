package uk.gov.justice.digital.hmpps.learnerrecordsapi.utils

import java.time.LocalDate

fun LocalDate.toISOFormat() = String.format("yyyy-MM-dd", this)
