package com.sqrlrcrd.model

import com.sqrlrcrd.model.MySqrlRcrd

import net.liftweb.common.Loggable
import net.liftweb.squerylrecord.RecordTypeMode._

object DemoData extends Loggable {

  def prepareMySqrlRcrds(): List[MySqrlRcrd] = {
    List(
      MySqrlRcrd.createRecord.name("foo"),
      MySqrlRcrd.createRecord.name("bar")
    )
  }

  def createDemoData = {
    /* *
     * we can use a List as insert argument
     * to batch insert all the items
     * */

    val msrTable = MySqrlRcrd.table

    logger.debug("about to do batch insert of foo & bar")
    transaction { msrTable.insert(prepareMySqrlRcrds()) }
    logger.debug("foo & bar inserted")

    logger.debug("about to do single insert of baz")
    val baz = MySqrlRcrd.createRecord name ("baz")
    transaction { msrTable.insert(baz) }
    logger.debug("baz inserted");

    logger.debug("about to do partial update of baz - no callbacks")
    transaction {
      update(msrTable)(msr ⇒ where(msr.name === "baz") set (msr.name := "Baz"))
    }
    logger.debug("baz updated")

    logger.debug("about to do full update of foo - trigger callbacks")
    transaction {
      msrTable.lookup(1L) map { rec ⇒
        rec.name("Foo")
        msrTable.update(rec)
      }
    }
    logger.debug("foo updated")

    logger.debug("about to do delete by-key of foo - trigger callbacks")
    transaction {
      msrTable.delete(1L)
    }
    logger.debug("foo deleted")

    logger.debug("about to delete bar using deleteWhere - no callbacks")
    transaction {
      msrTable.deleteWhere(msr ⇒ msr.id === 2)
    }
    logger.debug("bar deleted")

  }

}
