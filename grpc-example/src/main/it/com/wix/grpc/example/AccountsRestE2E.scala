package com.wix.grpc.example

import com.wix.e2e.http._
import com.wix.e2e.{BaseUri, ResponseMatchers}
import org.specs2.mutable.SpecificationWithJUnit
import AccountsRestE2E._

class AccountsRestE2E extends SpecificationWithJUnit with ResponseMatchers {
  sequential
  ITEnv
  implicit val sut = BaseUri(host = "localhost", port = 9901)

  "Account GRPC Rest" should {

    "GetAccount is available as GET:/api/accounts/{accountId}" in {
      val expected = Account(id = "10", name = "10")
      get("/api/accounts/123") must beSuccessfulWith(expected)
    }

    "CreateAccount is available as POST: /api/accounts" in {
      val request = CreateAccountRequest(name = "Iurii", email = "iuriim@wix.com", country = "US")
      val expected = Account(id = "hey", name = "Iurii", email = "iuriim@wix.com", country = "US",
        address = Some(AccountAddress(zipCode = "10010", city = "NewYork", state = "New York", address = "52st")))

      post("/api/accounts", but = withPayload(request)) must beSuccessfulWith(expected)
    }

    "UpdateAccount is available as PUT: /api/accounts/hey" in {
      val account = Account(id = "hey", name = "Iurii", email = "iuriim@wix.com", country = "US",
        address = Some(AccountAddress(zipCode = "10010", city = "NewYork", state = "New York", address = "52st")))

      put("/api/accounts/hey", but = withPayload(account.copy(id = null))) must beSuccessfulWith(account)
    }
  }
}

object AccountsRestE2E {

  private case class CreateAccountRequest(name: String, country: String, email: String)

  private case class Account(id: String, name: String, country: String = null, email: String = null, address: Option[AccountAddress] = None)

  private case class AccountAddress(zipCode: String = null, city: String = null, state: String = null, address: String = null)

}