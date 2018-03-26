package com.score.cchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Trans(bank: String,
                 id: UUID = UUIDs.random,
                 cheque: Cheque,

                 fromAccount: String,
                 fromBank: String,
                 fromZaddress: String,

                 toAccount: String,
                 toBank: String,
                 toZaddress: String,

                 timestamp: Long = System.currentTimeMillis,
                 digsig: String,
                 _type: String = "TRANSFER")
