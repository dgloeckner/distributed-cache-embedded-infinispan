package com.example.distrcache

import jakarta.annotation.PostConstruct
import org.infinispan.Cache
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@Listener
class MainController(val replicatedCache: Cache<String, String>, @Value("\${k8s.pod.name}") val hostname: String)  {

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

    private fun cacheContents() = replicatedCache.entries
        .map { "${it.key} -> ${it.value}" }
        .joinToString(postfix = "\n")

    @PostMapping("/entries")
    fun addEntry(): String {
        replicatedCache[System.currentTimeMillis().toString()] = hostname
        return cacheContents()
    }
}