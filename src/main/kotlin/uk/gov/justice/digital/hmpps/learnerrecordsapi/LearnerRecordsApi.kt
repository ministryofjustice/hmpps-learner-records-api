package uk.gov.justice.digital.hmpps.learnerrecordsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LearnerRecordsApi

fun main(args: Array<String>) {
  runApplication<LearnerRecordsApi>(*args)
}
