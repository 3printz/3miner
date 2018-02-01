package com.score.cchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Trans(bankId: String,
                 id: UUID = UUIDs.random,
                 cheque: Cheque,
                 from: String,
                 to: String,
                 timestamp: Long = System.currentTimeMillis,
                 digsig: String,
                 state: String = "TRANSFER")
