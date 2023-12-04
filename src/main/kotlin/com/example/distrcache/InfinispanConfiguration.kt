package com.example.distrcache

import org.infinispan.Cache
import org.infinispan.commons.api.CacheContainerAdmin
import org.infinispan.configuration.cache.CacheMode
import org.infinispan.configuration.cache.CacheType
import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.configuration.global.GlobalConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.manager.EmbeddedCacheManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration as Configuration1


@Configuration1
class InfinispanConfiguration(@Value("\${k8s.pod.name}") val hostname: String) {

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
    fun replicatedCache(cacheManager: EmbeddedCacheManager): Cache<String, String> {
        // Create a distributed cache with synchronous replication.
        val builder = ConfigurationBuilder()
        builder.clustering()
            .cacheType(CacheType.REPLICATION)
            .cacheMode(CacheMode.REPL_SYNC)

        // Obtain a volatile cache.
        val cache: Cache<String, String> =
            cacheManager
                .administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache("myCache", builder.build())
        cacheManager.addListener(TopologyChangedListener(cache, hostname))
        return cache
    }

//    @Bean
//    fun globalCustomizer(): InfinispanGlobalConfigurationCustomizer? {
//        // See parameters in default-jgroups-kubernetes.xml
//        return InfinispanGlobalConfigurationCustomizer { builder: GlobalConfigurationBuilder ->
//        }
//    }
//
//    @Bean
//    fun configurationCustomizer(): InfinispanConfigurationCustomizer? {
//        return InfinispanConfigurationCustomizer { builder: ConfigurationBuilder ->
//            builder.memory().evictionType(EvictionType.COUNT)
//        }
//    }
//
//    @Bean
//    fun cacheConfigurer(): InfinispanCacheConfigurer? {
//        return InfinispanCacheConfigurer { manager: EmbeddedCacheManager ->
//            val ispnConfig: org.infinispan.configuration.cache.Configuration =
//                ConfigurationBuilder()
//                    .clustering()
//                    .cacheMode(CacheMode.LOCAL)
//                    .build()
//            manager.defineConfiguration("local-sync-config", ispnConfig)
//        }
//    }

}