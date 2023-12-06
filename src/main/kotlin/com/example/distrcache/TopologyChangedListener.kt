package com.example.distrcache

import com.example.distrcache.model.JobStatusEnum
import com.example.distrcache.model.JobStatusValue
import org.infinispan.Cache
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged
import org.infinispan.notifications.cachemanagerlistener.event.Event
import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent
import org.slf4j.LoggerFactory
import java.util.*

@Listener
class TopologyChangedListener(
    private val replicatedCache: Cache<String, JobStatusValue>,
    private val hostname: String
) {

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
        replicatedCache["initial"] = JobStatusValue(
            status = JobStatusEnum.FINISHED,
            updateTime = Date(),
            nodeName = hostname
        )
    }
}