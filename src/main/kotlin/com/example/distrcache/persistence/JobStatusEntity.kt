package com.example.distrcache.persistence

import com.example.distrcache.model.JobStatusEnum
import com.example.distrcache.persistence.JobStatusEntity.Companion.TABLE_NAME
import jakarta.persistence.*
import java.time.ZonedDateTime


@Entity
@Table(name = TABLE_NAME)
data class JobStatusEntity(
    @Id
    val id: String? = null,

    @Enumerated(EnumType.STRING)
    var jobStatus: JobStatusEnum? = null,

    var updateTime: ZonedDateTime? = null,

    var nodeName: String? = null
) {
    companion object {
        const val TABLE_NAME = "job_status"
    }
}
