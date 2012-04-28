package com.sqrlrcrd.model

import net.liftweb.common.Loggable
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Props

object DemoData extends Loggable{

	def createDemoData = {
		/* *
     * we can use a List as insert argument
     * to batch insert all  the items
     * */
     
     
    logger.debug("about to insert foo & bar");
		transaction{MySchema.msrs.insert(prepareMySqrlRcrds())}
		logger.debug("foo & bar inserted");
		
		logger.debug("about to insert baz");
		val baz = MySqrlRcrd.createRecord name("baz")
		transaction{MySqrlRcrd.table.insert(baz)}
		logger.debug("baz inserted");
		
		logger.debug("about to update baz");
		transaction{
			update(MySqrlRcrd.table)(msr => where(msr.name === "baz") set(msr.name := "Baz"))}
		logger.debug("baz updated");
		
		logger.debug("about to delete foo");
		transaction{
			MySqrlRcrd.table.deleteWhere(msr => msr.name === "foo")}
		logger.debug("foo deleted");

	}

	def prepareMySqrlRcrds(): List[MySqrlRcrd] = {
		List(
			MySqrlRcrd.createRecord.name("foo"),
			MySqrlRcrd.createRecord.name("bar")
		)
	}

}
