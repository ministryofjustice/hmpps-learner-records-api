//package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.resource
//
//import org.springframework.web.bind.annotation.*
//import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicDetails
//import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicWithULN
//import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.service.DatabaseService
//
//// This resource just shows how we can use the service from some other place in code.
//// In this case we can actually request to our api to trigger the service.
//
//@RestController
//@RequestMapping("/ourEntity")
//class DatabaseResource(private val databaseService: DatabaseService) {
//
//  @PostMapping
//  fun saveDemographic(@RequestBody request: OurRequest): DemographicDetails {
//    return databaseService.saveOurEntity(request.something, request.somethingElse)
//  }
//
//  @GetMapping("/{id}")
//  fun getUser(@PathVariable id: Long): DemographicWithULN? {
//    return databaseService.getOurEntityById(id)
//  }
//}
//
//data class OurRequest(
//  val something: String,
//  val somethingElse: String
//)
