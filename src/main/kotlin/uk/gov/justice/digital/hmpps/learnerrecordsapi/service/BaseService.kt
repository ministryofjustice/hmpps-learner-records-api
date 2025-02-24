package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException
import java.io.StringReader
import javax.xml.bind.JAXBContext

open class BaseService {

  protected fun parseError(xmlString: String): MIAPAPIException? {
    val regex = Regex("<ns10:MIAPAPIException[\\s\\S]*?</ns10:MIAPAPIException>")
    val match = regex.find(xmlString)
    val relevantXml = match?.value ?: throw IllegalArgumentException("Unparsable LRS Error")
    val jaxbContext = JAXBContext.newInstance(MIAPAPIException::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    return unmarshaller.unmarshal(StringReader(relevantXml)) as MIAPAPIException
  }
}
