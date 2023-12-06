package com.example.distrcache.persistence

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class JobStatusEntityId(val jobName: String?, val jobGroup: String?)  : Serializable{
    constructor() : this(null, null) {
    }
}