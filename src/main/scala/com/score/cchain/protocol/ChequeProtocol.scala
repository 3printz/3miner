package com.score.cchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Cheque(bankId: String,
                  id: UUID = UUIDs.random,
                  amount: String,
                  blob: String,
                  origin_bank: String,
                  origin_account: String,
                  origin_zaddress: String)

