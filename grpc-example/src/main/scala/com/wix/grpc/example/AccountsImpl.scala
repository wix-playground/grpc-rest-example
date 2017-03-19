package com.wix.grpc.example

import com.wix.grpc.example.api.v1.account.{Account, Address}
import com.wix.grpc.example.api.v1.accounts.AccountsGrpc.Accounts
import com.wix.grpc.example.api.v1.accounts.{CreateAccountRequest, GetAccountRequest, InternalUpdateRequest}
import com.wix.grpc.FutureHelper._

import scala.concurrent.Future

class AccountsImpl extends Accounts {
  override def get(request: GetAccountRequest): Future[Account] =
    Account(id = "10", name = "10")

  override def create(request: CreateAccountRequest): Future[Account] =
    Account(id = "hey", name = request.name, country = request.country, email = request.email,
      address = Some(Address(zipCode = "10010", city = "NewYork", state = "New York", address = "52st"))
    )

  override def update(request: Account): Future[Account] =
    request

  override def internalUpdate(request: InternalUpdateRequest): Future[Account] =
    throw new RuntimeException("not supported")
}
