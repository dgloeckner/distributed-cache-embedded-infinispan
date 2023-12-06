package com.example.distrcache.model

import org.infinispan.protostream.annotations.ProtoFactory
import org.infinispan.protostream.annotations.ProtoField

data class JobStatusKey @ProtoFactory
constructor(
    @get:ProtoField(value = 1, required = true, name = "job_name")
    val jobName: String,
    @get:ProtoField(value = 2, required = true, name = "job_group")
    val jobGroup: String,
) {
}