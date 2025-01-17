package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException

class LRSException(
  val miapapiException: MIAPAPIException? = null,
) : RuntimeException(
  if (miapapiException != null) {
    buildString {
      append("LRS returned an error: MIAPAPIException(")
      append("errorCode=${miapapiException.errorCode}, ")
      append("errorActor=${miapapiException.errorActor}, ")
      append("description=${miapapiException.description}, ")
      append("furtherDetails=${miapapiException.furtherDetails}, ")
      append("errorTimestamp=${miapapiException.errorTimestamp})")
    }
  } else {
    "LRS returned an error without detail"
  },
)
