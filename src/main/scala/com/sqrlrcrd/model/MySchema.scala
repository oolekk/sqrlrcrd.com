package com.sqrlrcrd.model

import net.liftweb._
import common._

import org.squeryl.Schema
import squerylrecord.RecordTypeMode._

object MySchema extends Schema with Loggable{
	
	val msrs = table[MySqrlRcrd]("msr")
	

	/* lifecycle callbacks */
	override def callbacks = Seq(
		
		beforeUpdate(msrs) call (MySqrlRcrd => logger.debug("beforeUpdate")),
		beforeInsert(msrs) call (MySqrlRcrd => logger.debug("beforeInsert")),
		
		beforeDelete(msrs) call (MySqrlRcrd => logger.debug("beforeDelete")),
		afterInsert(msrs) call (MySqrlRcrd => logger.debug("afterInsert")),
		afterUpdate(msrs) call (MySqrlRcrd => logger.debug("afterUpdate")),
		afterDelete(msrs) call (MySqrlRcrd => logger.debug("afterDelete"))

	)

}
