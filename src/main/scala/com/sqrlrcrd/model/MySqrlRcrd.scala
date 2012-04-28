package com.sqrlrcrd.model

import net.liftweb.record.{ Record, MetaRecord, Field }
import net.liftweb.record.field._
import org.squeryl.annotations.Column
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._


class MySqrlRcrd extends Record[MySqrlRcrd] with KeyedRecord[Long]{

	def meta = MySqrlRcrd
	
	@Column(name="id")
	val idField = new LongField(this)
	
	val name = new StringField(this,"")
	
}

object MySqrlRcrd extends MySqrlRcrd with MetaRecord[MySqrlRcrd]{
	
	def table = MySchema.msrs

	def idFromString(in: String) = in.toLong
	
}
