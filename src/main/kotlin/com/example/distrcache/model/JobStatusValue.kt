package com.example.distrcache.model

import org.infinispan.protostream.annotations.ProtoFactory
import org.infinispan.protostream.annotations.ProtoField
import java.time.ZonedDateTime
import java.util.Date

/**
 * The value of a cache entry.
 * <i>Note:</i> the field names from the <code>ProtoField</code> annotations
 * are also used by the <code>TableJdbcStore</code> to resolve database columns.
 * @see org.infinispan.persistence.sql.TableJdbcStore
 */
data class JobStatusValue @ProtoFactory
constructor(
    @get:ProtoField(number = 1, required = true, name = "job_status") val status: JobStatusEnum,
    @get:ProtoField(number = 2, required = true, name = "update_time") val updateTime: Date,
    @get:ProtoField(number = 3, required = true, name = "node_name") var nodeName: String
) {
}