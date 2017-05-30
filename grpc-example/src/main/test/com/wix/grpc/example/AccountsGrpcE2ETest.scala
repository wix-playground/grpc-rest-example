package com.wix.grpc.example

import com.wix.grpc.example.api.v1.account.{Account, Address}
import com.wix.grpc.example.api.v1.accounts.{AccountsGrpc, CreateAccountRequest, GetAccountRequest, InternalUpdateRequest}
import io.grpc.{ManagedChannelBuilder, StatusRuntimeException}
import org.specs2.mutable.SpecificationWithJUnit

class AccountsGrpcE2ETest extends SpecificationWithJUnit {
  ITEnv
  private val channel = ManagedChannelBuilder.forAddress("localhost", 9902).usePlaintext(true).directExecutor().build()
  private val accountsClient = AccountsGrpc.blockingStub(channel)

  "Account GRPC" should {
    "support GetAccount" in {
      accountsClient.get(GetAccountRequest("123")) mustEqual Account(id = "10", name = "10")
    }

    "support CreateAccount" in {
      val request = CreateAccountRequest(name = "Iurii", email = "iuriim@wix.com", country = "US")
      val expected = Account(id = "hey", name = "Iurii", email = "iuriim@wix.com", country = "US",
        address = Some(Address(zipCode = "10010", city = "NewYork", state = "New York", address = "52st")))

      accountsClient.create(request) mustEqual expected
    }

    "support UpdateAccount" in {
      val account = Account(id = "hey", name = "Iurii", email = "iuriim@wix.com", country = "US",
        address = Some(Address(zipCode = "10010", city = "NewYork", state = "New York", address = "52st")))

      accountsClient.update(account) mustEqual account
    }

    "suppoer InternalUpdate" in {
      accountsClient.internalUpdate(InternalUpdateRequest()) must throwA[StatusRuntimeException]
    }
  }
}
