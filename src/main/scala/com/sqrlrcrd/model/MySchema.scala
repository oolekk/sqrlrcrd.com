package com.sqrlrcrd.model

import org.squeryl.Schema
import com.sqrlrcrd.model.MySqrlRcrd

import net.liftweb.common.Loggable

object MySchema extends Schema with Loggable {

  val mySqrlrRcrds = table[MySqrlRcrd]("msr")

  /* lifecycle callbacks */
  override def callbacks = Seq(

    beforeUpdate(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.info("beforeUpdate")),
    beforeInsert(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.info("beforeInsert")),
    beforeDelete(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.info("beforeDelete")),

    afterInsert(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.info("afterInsert")),
    afterUpdate(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.info("afterUpdate")),
    afterDelete(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.info("afterDelete"))

  )

}
