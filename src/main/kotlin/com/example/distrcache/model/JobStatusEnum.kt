package com.example.distrcache.model

import org.infinispan.protostream.annotations.ProtoEnumValue

enum class JobStatusEnum(val finalStatus: Boolean) {
    @ProtoEnumValue(number = 0)
    SCHEDULED(false),
    @ProtoEnumValue(number = 1)
    RUNNING(false),
    @ProtoEnumValue(number = 2)
    FAILED(true),
    @ProtoEnumValue(number = 3)
    FINISHED(true)
}