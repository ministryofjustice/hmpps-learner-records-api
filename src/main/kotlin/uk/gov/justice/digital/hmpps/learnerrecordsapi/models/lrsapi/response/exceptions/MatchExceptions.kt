package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus

abstract class MatchException(
  val nomisId: String,
  val status: CheckMatchStatus,
  message: String,
) : RuntimeException("$message: $nomisId!")

class MatchNotFoundException(nomisId: String) :
  MatchException(
    nomisId,
    CheckMatchStatus.NotFound,
    "Cannot find a match for",
  )

class MatchNotPossibleException(nomisId: String) :
  MatchException(
    nomisId,
    CheckMatchStatus.NoMatch,
    "Not possible to match",
  )
