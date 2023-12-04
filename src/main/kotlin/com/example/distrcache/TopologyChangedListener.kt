package com.example.distrcache

import org.infinispan.Cache
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged
import org.infinispan.notifications.cachemanagerlistener.event.Event
import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent
import org.slf4j.LoggerFactory

@Listener
class TopologyChangedListener(private val replicatedCache: Cache<String, String>, private val hostname: String) {

    val log = LoggerFactory.getLogger(TopologyChangedListener::class.java)
    @ViewChanged
    fun viewChangedEvent(event: ViewChangedEvent) {
        log.info("ViewChangedEvent $event")
        primeCache(event)
    }

    @Merged
    fun merged(event: MergeEvent) {
        log.info("MergeEvent $event")
        primeCache(event)
    }

    fun primeCache(event: Event) {
        replicatedCache["INITIAL-$hostname"] = "Something"
    }
}