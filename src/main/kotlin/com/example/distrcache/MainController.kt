package com.example.distrcache

import com.example.distrcache.model.JobStatusEnum
import com.example.distrcache.model.JobStatusValue
import com.example.distrcache.persistence.JobStatusRepository
import jakarta.annotation.PostConstruct
import org.infinispan.Cache
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.Date


@RestController
@Listener
class MainController(val replicatedCache: Cache<String, JobStatusValue>, @Value("\${k8s.pod.name}") val hostname: String,
    val jobStatusRepository: JobStatusRepository)  {

    val log = LoggerFactory.getLogger(MainController::class.java)
    @CacheEntryCreated
    fun print(event: org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent<String, String>) {
        if (!event.isPre) {
            log.info("New entry was added: ${event.key} --> ${event.value}")
        }
    }


    @PostConstruct
    fun registerListener() {
        replicatedCache.addListener(this)
    }

    @GetMapping("/entries")
    fun getEntries() : String{
        return cacheContents()
    }

    @GetMapping("/entries-from-db")
    fun getEntriesFromDb(): String {
        return jobStatusRepository.findAll().map { it.toString() }
            .joinToString("\n")
    }

    private fun cacheContents() = replicatedCache.entries
        .map { "${it.key} -> ${it.value}" }
        .joinToString(postfix = "\n")

    @PostMapping("/entries")
    fun updateEntry(@RequestBody updateRequest: UpdateRequest): String {
        replicatedCache[updateRequest.jobId] = JobStatusValue(
            status = JobStatusEnum.RUNNING,
            updateTime = Date(),
            nodeName = hostname
        )
        return cacheContents()
    }

    data class UpdateRequest(val jobId: String)
}