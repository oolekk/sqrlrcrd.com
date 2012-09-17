package com.sqrlrcrd.model

import org.squeryl.Schema
import com.sqrlrcrd.model.MySqrlRcrd

import net.liftweb.common.Loggable

object MySchema extends Schema with Loggable {

  val mySqrlrRcrds = table[MySqrlRcrd]("msr")

  /* lifecycle callbacks */
  override def callbacks = Seq(

    beforeUpdate(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.debug("beforeUpdate")),
    beforeInsert(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.debug("beforeInsert")),
    beforeDelete(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.debug("beforeDelete")),

    afterInsert(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.debug("afterInsert")),
    afterUpdate(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.debug("afterUpdate")),
    afterDelete(mySqrlrRcrds) call (MySqrlRcrd ⇒ logger.debug("afterDelete"))

  )

}
