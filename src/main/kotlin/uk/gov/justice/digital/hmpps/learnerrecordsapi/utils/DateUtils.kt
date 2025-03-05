package uk.gov.justice.digital.hmpps.learnerrecordsapi.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.toISOFormat() = String.format("yyyy-MM-dd", this)

fun String.toLocalDate(): LocalDate? {
  if (this.isBlank()) {
    return null
  }
  return LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
}
