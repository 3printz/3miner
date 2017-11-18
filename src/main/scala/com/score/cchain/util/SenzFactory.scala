package com.score.cchain.util

import com.score.cchain.config.AppConf

object SenzFactory extends AppConf {
  def isValid(msg: String) = {
    msg == null || msg.isEmpty
  }

  def regSenz = {
    // unsigned senz
    val publicKey = RSAFactory.loadRSAPublicKey()
    val timestamp = (System.currentTimeMillis / 1000).toString
    val receiver = switchName
    val sender = senzieName

    s"SHARE #pubkey $publicKey #time $timestamp @$receiver ^$sender"
  }

  def pingSenz = {
    // unsigned senz
    val timestamp = (System.currentTimeMillis / 1000).toString
    val receiver = switchName
    val sender = senzieName

    s"PING #time $timestamp @$receiver ^$sender"
  }

  def blockSignSenz(blockId: String) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val receiver = "*"
    val sender = senzieName

    s"PUT #block $blockId #sign #time $timestamp @$receiver ^$sender"
  }

  def shareSuccessSenz(uid: String, to: String, cId: String, cBnk: String) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val sender = senzieName

    s"DATA #status SUCCESS #cbnk $cBnk #cid $cId #uid $uid #time $timestamp @$to ^$sender"
  }

  def shareFailSenz(uid: String, to: String, cId: String, cBnk: String) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val sender = senzieName

    s"DATA #status FAIL #cbnk $cBnk #cid $cId #uid $uid #time $timestamp @$to ^$sender"
  }

}
