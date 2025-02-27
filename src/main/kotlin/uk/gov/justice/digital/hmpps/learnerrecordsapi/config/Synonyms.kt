package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

object Synonyms {

  private val synonyms = mapOf(
    "maths" to "mathematics",
  )

  fun getSynonyms(keywords: List<String>): List<String> {
    val list = mutableListOf<String>()
    synonyms.forEach { (key, value) ->
      if (keywords.contains(key)) list.add(value)
      if (keywords.contains(value)) list.add(key)
    }
    return list + keywords
  }
}
