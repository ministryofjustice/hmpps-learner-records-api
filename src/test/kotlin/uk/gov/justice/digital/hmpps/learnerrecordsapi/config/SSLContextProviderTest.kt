package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.junit.jupiter.api.Assertions.*
 class SSLContextProviderTest {


   fun `Creates SSL Context when a path to the certificate file is provided`() {
     val sslContextProvider = SSLContextProvider("somethingPathToFile")


   }
 }