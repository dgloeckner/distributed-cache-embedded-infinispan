package com.example.distrcache.model

import com.example.distrcache.model.JobStatusSchema.Companion.PACKAGE_NAME
import org.infinispan.protostream.GeneratedSchema
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder

/**
 * Enables automatic generation of the Protobuf schema
 * via an annotation processor. The annotation processor
 * is executed via the <code>protostream-processor</code> Maven plugin.
 * In addition, the schema below is registered as a <a href="https://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html">Service Provider</a>
 * which is automatically discovered when using embedded Infinispan caches.
 */
@AutoProtoSchemaBuilder(
    schemaPackageName = PACKAGE_NAME,
    includeClasses = [JobStatusValue::class, JobStatusEnum::class],
    schemaFilePath = "proto"
)
interface JobStatusSchema : GeneratedSchema {
    companion object {
        const val PACKAGE_NAME = "jobstatus"
    }
}