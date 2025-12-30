# Google Cloud Secret Manager Integration

## Overview
The application fetches the StockData API token from Google Cloud Secret Manager instead of storing it directly in application properties. This improves security by keeping sensitive credentials out of configuration files. Secrets are cached using Spring's declarative caching to minimize API calls and improve performance.

## Configuration

### Application Properties
Configure the following properties in `application.properties`:

```properties
quote-client.gcp-project-id=${GCP_PROJECT_ID:your-gcp-project-id}
quote-client.api-token-secret-id=${API_TOKEN_SECRET_ID:stockdata-api-token}
```

### Environment Variables
Set these environment variables:
- `GCP_PROJECT_ID`: Your Google Cloud Platform project ID
- `API_TOKEN_SECRET_ID`: The secret ID in Google Cloud Secret Manager (default: `stockdata-api-token`)

## Setup Steps

### 1. Create Secret in Google Cloud Secret Manager

```bash
# Create the secret
gcloud secrets create stockdata-api-token \
    --replication-policy="automatic" \
    --project=YOUR_PROJECT_ID

# Add the secret value
echo -n "your-actual-api-token" | gcloud secrets versions add stockdata-api-token \
    --data-file=- \
    --project=YOUR_PROJECT_ID
```

### 2. Grant Service Account Permissions

The application service account needs the `Secret Manager Secret Accessor` role:

```bash
gcloud secrets add-iam-policy-binding stockdata-api-token \
    --member="serviceAccount:YOUR_SERVICE_ACCOUNT@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor" \
    --project=YOUR_PROJECT_ID
```

### 3. Authentication

The application uses Application Default Credentials (ADC). Ensure authentication is configured:

**For local development:**
```bash
gcloud auth application-default login
```

**For production (GCP):**
- The service account attached to your compute resource (GKE, Cloud Run, etc.) should have the necessary permissions

**For production (non-GCP):**
- Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to your service account key file

## How It Works

### Components

1. **SecretManagerService**: A Spring service that fetches secrets from Google Cloud Secret Manager
   - Annotated with `@Cacheable` for automatic caching
   - Cache key: `projectId:secretId:versionId`
   
2. **QuoteClientRequestFilter**: Fetches the API token from Secret Manager (via cache) on each request
   
3. **QuoteClientProperties**: Stores GCP configuration (project ID, secret ID)

4. **CacheConfig**: Configures in-memory caching for secrets
   - Automatic cache eviction every 12 hours (2 AM and 2 PM)
   - Ensures secret rotation is respected

### Caching Implementation (Quarkus-Style)

The application uses Spring's `@Cacheable` annotation (similar to Quarkus's `@CacheResult`):

```java
@Cacheable(value = SECRET_CACHE, key = "#projectId + ':' + #secretId + ':' + #versionId")
public String getSecret(String projectId, String secretId, String versionId) {
    // Only executes on cache miss
    // Subsequent calls return cached value
}
```

**Cache behavior:**
- **First call**: Secret fetched from GCP Secret Manager (cache miss)
- **Subsequent calls**: Secret returned from cache (cache hit, no GCP API call)
- **Cache eviction**: Automatic every 12 hours to support secret rotation

**Cache key example:** `kiran-stock-api-project:stockdata_org_token:latest`

## Benefits

- **Security**: API tokens are not stored in code or configuration files
- **Performance**: Cached secrets eliminate repeated GCP API calls
- **Cost**: Reduces Secret Manager API usage
- **Rotation**: Tokens can be rotated in Secret Manager; cache auto-refreshes every 12 hours
- **Auditing**: Google Cloud provides audit logs for secret access
- **Centralized Management**: All secrets managed in one secure location
- **Reliability**: Continues working if GCP has temporary issues (uses cached value)

## Monitoring & Debugging

### Enable Cache Logging
To see cache activity, add to `application.properties`:
```properties
logging.level.org.springframework.cache=DEBUG
```

You'll see:
- `"Cache miss - Fetching secret from GCP Secret Manager..."` on first call
- No log messages on subsequent calls (cache hit)

### Enable GCP Diagnostics
For troubleshooting authentication or Secret Manager access issues:
```properties
gcp.diagnostics.enabled=true
```

See `TROUBLESHOOTING_SECRET_MANAGER.md` for detailed debugging steps.

## Manual Cache Management

### Evict All Secrets
If you need to force refresh (e.g., after rotating a secret):

```java
@Autowired
private CacheManager cacheManager;

public void evictAllSecrets() {
    cacheManager.getCache(CacheConfig.SECRET_CACHE).clear();
}
```

### Evict Specific Secret
```java
public void evictSecret(String projectId, String secretId, String versionId) {
    Cache cache = cacheManager.getCache(CacheConfig.SECRET_CACHE);
    String key = projectId + ":" + secretId + ":" + versionId;
    cache.evict(key);
}
```

## Advanced Configuration

### Disable Caching
If needed, disable caching in `application.properties`:
```properties
spring.cache.type=none
```

### Change Cache Eviction Schedule
Edit `CacheConfig.java`:
```java
@Scheduled(cron = "0 0 2,14 * * ?")  // Default: 2 AM and 2 PM
@CacheEvict(value = SECRET_CACHE, allEntries = true)
public void evictSecretsCache()
```

### Use Different Cache Provider

**Option 1: Caffeine (TTL-based expiration)**
```groovy
implementation 'com.github.ben-manes.caffeine:caffeine'
```

**Option 2: Redis (distributed caching)**
```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

## Testing with Cache

### Disable Cache in Tests
```java
@SpringBootTest
@EnableAutoConfiguration(exclude = CacheAutoConfiguration.class)
class MyTest {
    // Cache disabled for this test
}
```

### Clear Cache Between Tests
```java
@Autowired
private CacheManager cacheManager;

@BeforeEach
void setUp() {
    cacheManager.getCache(CacheConfig.SECRET_CACHE).clear();
}
```

## Security Considerations

- Secrets are stored in memory (heap) while cached
- JVM heap dumps will contain cached secrets
- For highly sensitive environments:
  - Consider shorter cache TTL
  - Use encrypted cache storage
  - Use external cache with encryption at rest (e.g., Redis)
  
## Comparison to Quarkus

| Feature | Quarkus | Spring Boot (This Implementation) |
|---------|---------|-----------------------------------|
| Annotation | `@CacheResult` | `@Cacheable` |
| Cache Key | `@CacheKey` on parameters | SpEL: `key = "#projectId + ':' + #secretId"` |
| Cache Name | `cacheName` attribute | `value` attribute |
| Cache Eviction | `@CacheInvalidateAll` | `@CacheEvict(allEntries = true)` |
| Enable Caching | Auto-enabled | `@EnableCaching` required |

## Troubleshooting

- **PERMISSION_DENIED errors**: See `TROUBLESHOOTING_SECRET_MANAGER.md`
- **Cache not working**: Enable debug logging (`logging.level.org.springframework.cache=DEBUG`)
- **Secret not updating**: Wait for scheduled cache eviction or manually clear cache


