package com.example.distrcache

import com.example.distrcache.model.JobStatusValue
import com.fasterxml.jackson.databind.ObjectMapper
import org.infinispan.Cache
import org.infinispan.metadata.Metadata
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent
import org.infinispan.notifications.cachelistener.filter.CacheEventFilter
import org.infinispan.notifications.cachelistener.filter.EventType
import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

/**
 * Shows how cache updates can be converted to
 * <a href="https://en.wikipedia.org/wiki/Server-sent_events">server-sent events</a>.
 */
@Service
class CacheEventService(val replicatedCache: Cache<String, JobStatusValue>) {

    val log = LoggerFactory.getLogger(CacheEventService::class.java)

    val objectMapper = ObjectMapper()
    fun streamEvents(jobId: String? = null, completeAfterFinalJobStatus: Boolean = false): Flux<ServerSentEvent<String>> {
        val listener = CacheUpdatesFlux(jobId, completeAfterFinalJobStatus)
        // Subscribe our listener which also acts as an event filter.
        // We have to pass a "converter" to make the Kotlin compiler happy ;)
        replicatedCache.addListener(listener, listener) { _, _, _, newValue, _, _ -> newValue }
        return listener.asFlux().map { cacheUpdate: CacheEntryModifiedEvent<String, JobStatusValue> ->
            ServerSentEvent.builder<String>().id(cacheUpdate.key).event("entry-updated")
                .data(objectMapper.writeValueAsString(cacheUpdate.newValue)).build()
        }.doOnCancel {
            log.info("Client disconnected from event stream")
            replicatedCache.removeListener(listener)
        }
    }

    /**
     * Acts as a cache listener. Events for new and updated entries are published as a Flux.
     */
    @Listener
    class CacheUpdatesFlux(private val jobIdToFilter: String?, private val completeAfterFinalJobStatus: Boolean) :
        CacheEventFilter<String, JobStatusValue> {

        private val sink: Sinks.Many<CacheEntryModifiedEvent<String, JobStatusValue>> =
            Sinks.many().unicast().onBackpressureError()

        @CacheEntryModified
        fun entryModified(event: CacheEntryModifiedEvent<String, JobStatusValue>) {
            // We could handle errors from emitting here if needed by using "emitNext"
            // with a failure handler.
            sink.tryEmitNext(event)
            if (completeAfterFinalJobStatus && event.newValue.status.finalStatus) {
                sink.tryEmitComplete()
            }
        }

        fun asFlux(): Flux<CacheEntryModifiedEvent<String, JobStatusValue>> {
            return sink.asFlux()
        }

        /**
         * Determines whether an event should be reported to the listener.
         */
        override fun accept(
            key: String?,
            oldValue: JobStatusValue?,
            oldMetadata: Metadata?,
            newValue: JobStatusValue?,
            newMetadata: Metadata?,
            eventType: EventType?
        ): Boolean {
            return if (this.jobIdToFilter == null) {
                true
            } else {
                this.jobIdToFilter == key
            }
        }

    }
}