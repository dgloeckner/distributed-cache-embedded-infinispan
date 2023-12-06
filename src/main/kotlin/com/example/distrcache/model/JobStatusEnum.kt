package com.example.distrcache.model

import org.infinispan.protostream.annotations.ProtoEnumValue

enum class JobStatusEnum {
    @ProtoEnumValue(number = 0)
    SCHEDULED,
    @ProtoEnumValue(number = 1)
    RUNNING,
    @ProtoEnumValue(number = 2)
    FAILED,
    @ProtoEnumValue(number = 3)
    FINISHED
}