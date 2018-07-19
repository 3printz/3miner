package com.score.cchain.contract

import com.score.cchain.util.SenzLogger

case class Account(address: String, balance: Int, trans: List[Trans])

case class Trans(amount: Int, keyHash: String)

object FundTrans extends SenzLogger {

  var accounts = scala.collection.mutable.Map[String, Account]()

  def init(address: String) = {
    accounts.put(address, Account(address, 0, List()))
  }

  def reg(address: String, amount: Int, keyHash: String) = {
    // save contract(as transaction) in db
    val tran = Trans(amount, keyHash)
    //val trans = tran :: accounts.get(address).map(a => a.trans).getOrElse(List(tran))
    val acc = accounts.get(address)
      .map(a => Account(a.address, a.balance, tran :: a.trans))
      .getOrElse(Account(address, 0, List(tran)))
    accounts.put(address, acc)
  }

  def pay(address: String, key: String) = {
    // get trans and account
    val acc = accounts.values.find(a => a.trans.exists(t => t.keyHash == key))
    val tran = accounts.values.flatMap(a => a.trans).find(t => t.keyHash == key)

    tran match {
      case Some(t) =>
        // transfer money
        accounts.put(acc.get.address, Account(acc.get.address, acc.get.balance - t.amount, acc.get.trans.drop(acc.get.trans.indexOf(t))))
        accounts.put(address, Account(address, accounts(address).balance + t.amount, accounts(address).trans))
      case None =>
        // error trans
        logger.error(s"No match trans to pay $address, $key")
    }
  }

}
