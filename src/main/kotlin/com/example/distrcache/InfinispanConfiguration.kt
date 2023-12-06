package com.example.distrcache

import com.example.distrcache.model.JobStatusSchema
import com.example.distrcache.model.JobStatusValue
import com.example.distrcache.persistence.JobStatusEntity.Companion.TABLE_NAME
import org.infinispan.Cache
import org.infinispan.commons.api.CacheContainerAdmin
import org.infinispan.configuration.cache.*
import org.infinispan.configuration.global.GlobalConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.manager.EmbeddedCacheManager
import org.infinispan.persistence.jdbc.common.DatabaseType
import org.infinispan.persistence.sql.configuration.TableJdbcStoreConfigurationBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration as Configuration1


@Configuration1
class InfinispanConfiguration(
    @Value("\${k8s.pod.name}") val hostname: String,
    @Value("\${spring.datasource.url}") val dbUrl: String,
    @Value("\${spring.datasource.username}") val dbUser: String,
    @Value("\${spring.datasource.password}") val dbPassword: String
) {

    @Bean
    fun cacheManager(): DefaultCacheManager {
        // jgroups.dns.query needs to be set as a system property
        // For Kubernetes this would typically resolve to a headless service.
        val global = GlobalConfigurationBuilder.defaultClusteredBuilder()
            .transport().defaultTransport()
            .clusterName("my_cluster")
            // Add Kubernetes JGroups stack
            .addProperty("configurationFile", "default-configs/default-jgroups-kubernetes.xml")
            .stack("kubernetes")

        return DefaultCacheManager(global.build())
    }

    // https://infinispan.org/docs/stable/titles/spring_boot/starter.html
    @Bean
    fun replicatedCache(cacheManager: EmbeddedCacheManager): Cache<String, JobStatusValue> {
        // Create a distributed cache with synchronous replication.
        val builder = ConfigurationBuilder()
        builder.clustering()
            .cacheType(CacheType.REPLICATION)
            .cacheMode(CacheMode.REPL_SYNC)
            .encoding().mediaType("application/x-protostream")
            // Configure a cache store to enable "write-through" caching.
            .persistence().addStore(TableJdbcStoreConfigurationBuilder::class.java)
            .dialect(DatabaseType.POSTGRES)
            // Shared to avoid needless writes to the DB in a replicated cache.
            // Only the "owner" node of an update will persist it.
            .shared(true)
            .tableName(TABLE_NAME)
            .schema()
            .packageName(JobStatusSchema.PACKAGE_NAME)
            .messageName(JobStatusValue::class.simpleName)
            .connectionPool()
            .connectionUrl(dbUrl)
            .username(dbUser)
            .password(dbPassword)
            .driverClass("org.postgresql.Driver")
            .preload(true)
            .purgeOnStartup(false)
            .maxBatchSize(1000)

        // Obtain a volatile cache. Volatile refers to the cache configuration.
        // When our application gets restarted (0 Infinispan nodes) a new cache
        // will be created with the configuration that we provide here via API calls.
        val cache: Cache<String, JobStatusValue> =
            cacheManager
                .administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache("myCache", builder.build())
        cacheManager.addListener(TopologyChangedListener(cache, hostname))
        return cache
    }

}