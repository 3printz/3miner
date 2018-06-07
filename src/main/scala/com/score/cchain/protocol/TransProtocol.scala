package com.score.cchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Trans(oriZaddr: String,
                 id: UUID = UUIDs.random,
                 fromZaddr: String,
                 toZaddr: String,
                 action: String,
                 blob: String,
                 timestamp: Long = System.currentTimeMillis,
                 digsig: String)
