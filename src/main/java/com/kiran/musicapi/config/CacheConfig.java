package com.kiran.musicapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Cache configuration for the application. Enables caching for Google Cloud
 * Secret Manager secrets to avoid repeated API calls.
 */
@Slf4j
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

	public static final String SECRET_CACHE = "secrets";

	/**
	 * Configure the cache manager for the application. Uses a simple in-memory
	 * concurrent map cache.
	 *
	 * @return CacheManager instance
	 */
	@Bean
	public CacheManager cacheManager() {
		log.info("Initializing cache manager with cache: {}", SECRET_CACHE);
		return new ConcurrentMapCacheManager(SECRET_CACHE);
	}

	/**
	 * Evict all cached secrets every 12 hours. This ensures secrets are refreshed
	 * periodically in case they are rotated in GCP. Runs at 2 AM and 2 PM daily.
	 */
	@Scheduled(cron = "0 0 2,14 * * ?")
	@CacheEvict(value = SECRET_CACHE, allEntries = true)
	public void evictSecretsCache() {
		log.info("Evicting all entries from secrets cache (scheduled refresh)");
	}
}
