package com.score.cchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Block(miner: String,
                 id: UUID = UUIDs.timeBased(),
                 transactions: List[Trans],
                 timestamp: Long,
                 merkleRoot: String,
                 preHash: String,
                 hash: String,
                 signatures: List[Signature] = List())