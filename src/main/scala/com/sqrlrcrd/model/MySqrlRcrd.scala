package com.sqrlrcrd.model

import net.liftweb.record.field.LongField
import net.liftweb.record.field.StringField
import net.liftweb.record.Record
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.KeyedRecord
import org.squeryl.annotations.Column

class MySqrlRcrd extends Record[MySqrlRcrd] with KeyedRecord[Long] {

	def meta = MySqrlRcrd

	@Column(name = "id")
	val idField = new LongField(this)

	val name = new StringField(this, "")

}

object MySqrlRcrd extends MySqrlRcrd with MetaRecord[MySqrlRcrd] {

	def table = MySchema.msrs

	def idFromString(in: String) = in.toLong

}
