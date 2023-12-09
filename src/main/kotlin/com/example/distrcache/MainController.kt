package com.example.distrcache

import com.example.distrcache.model.JobStatusEnum
import com.example.distrcache.model.JobStatusValue
import com.example.distrcache.persistence.JobStatusEntity
import com.example.distrcache.persistence.JobStatusRepository
import org.infinispan.Cache
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.util.*


@RestController
class MainController(
    val replicatedCache: Cache<String, JobStatusValue>,
    @Value("\${k8s.pod.name}") val hostname: String,
    val jobStatusRepository: JobStatusRepository,
    val cacheEventService: CacheEventService
) {

    @GetMapping("/entries")
    fun getEntries(): List<Pair<String, JobStatusValue>> {
        return replicatedCache.entries.map { Pair(it.key, it.value) }
    }

    @GetMapping("/entries-from-db")
    fun getEntriesFromDb(): List<JobStatusEntity> {
        return jobStatusRepository.findAll().toList()
    }

    private fun cacheContents() =
        replicatedCache.entries.map { "${it.key} -> ${it.value}" }.joinToString(postfix = "\n")

    @PostMapping("/entries")
    fun updateEntry(@RequestBody updateRequest: UpdateRequest): JobStatusValue {
        val newValue = JobStatusValue(
            status = updateRequest.status, updateTime = Date(), nodeName = hostname
        )
        replicatedCache[updateRequest.jobId] = newValue
        return newValue
    }

    data class UpdateRequest(val jobId: String, val status: JobStatusEnum)

    /**
     * Subscribes to updates and emits ServerSentEvents.
     * @param jobId optional job ID to filter events
     * @param completeAfterFinalJobStatus optional, whether to complete the event stream once a final
     *  job status was reached
     */
    @GetMapping(path = ["/entries-stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamEvents(
        @RequestParam(name = "jobId") jobId: String?,
        @RequestParam(name = "completeAfterFinalJobStatus") completeAfterFinalJobStatus: Boolean?
    ): Flux<ServerSentEvent<String>> {
        return cacheEventService.streamEvents(jobId, completeAfterFinalJobStatus == true)
    }

}