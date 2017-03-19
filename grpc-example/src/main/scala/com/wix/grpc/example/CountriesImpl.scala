package com.wix.grpc.example

import com.wix.grpc.example.api.v1.countries.CountriesGrpc.Countries
import com.wix.grpc.example.api.v1.countries.{GetCountryRequest, ListCountriesRequest, ListCountriesResponse}
import com.wix.grpc.example.api.v1.country.Country
import com.wix.grpc.FutureHelper._

import scala.concurrent.Future

class CountriesImpl extends Countries {

  override def listCountries(request: ListCountriesRequest): Future[ListCountriesResponse] =
    ListCountriesResponse(Seq(
      Country(code = "US", name = "USA"),
      Country(code = "UA", name = "Ukraine")
    ))

  override def getCountry(request: GetCountryRequest): Future[Country] = {
    if (request.countryId == "1") Country(code = "US", name = "USA")
    else if (request.countryId == "2") Country(code = "UA", name = "Ukraine")
    else throw new RuntimeException("Hip-hop")
  }
}
