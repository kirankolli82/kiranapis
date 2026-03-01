# API Token Fetching — GCP Secret Manager Flow

This document explains how API tokens for the external quote providers (StockData.org and AlphaVantage)
are retrieved securely from Google Cloud Secret Manager at request time.

For setup instructions (creating secrets, IAM permissions, authentication), see
[SECRETS_MANAGER_SETUP.md](SECRETS_MANAGER_SETUP.md).

---

## Overview

API tokens are **never stored in application configuration or source code**. Instead, they are
fetched on demand from GCP Secret Manager and held in an in-memory cache to avoid redundant
network calls to GCP. The token is injected into each outbound HTTP request automatically via a
JAX-RS `ClientRequestFilter`.

There are two independent flows — one per external provider — but both share the same underlying
`SecretManagerService` and cache.

---

## Request Flow

```
Incoming HTTP request  (GET /quotes)
        │
        ▼
  REST Resource  (StockDataResource / AlphaVantageResource)
        │
        ▼
  JAX-RS Client  (QuoteClient / AlphaVantageClient)
        │
        │  [ClientRequestFilter intercepts every outbound call]
        ▼
  ┌────────────────────────────────────────────────────┐
  │  QuoteClientRequestFilter                          │
  │  AlphaVantageClientRequestFilter                   │
  │                                                    │
  │  1. Read gcpProjectId + secretId from properties   │
  │  2. Call SecretManagerService.getSecret(...)       │
  │  3. Append token as query parameter to URI         │
  └────────────────────────────────────────────────────┘
        │
        ▼
  ┌────────────────────────────────────────────────────┐
  │  SecretManagerService  (Spring @Service)           │
  │                                                    │
  │  @Cacheable(value = "secrets",                     │
  │             key = "projectId:secretId:version")    │
  │                                                    │
  │  Cache HIT  ──► return cached token               │
  │  Cache MISS ──► call GCP Secret Manager API       │
  │                 ──► store result in cache          │
  │                 ──► return token                   │
  └────────────────────────────────────────────────────┘
        │
        ▼
  Outbound HTTP call with ?api_token=<token>
  (or ?apikey=<token> for AlphaVantage)
        │
        ▼
  External API  (StockData.org / AlphaVantage)
```

---

## Components

### 1. `QuoteClientProperties` / `AlphaVantageClientProperties`

Records bound from `application.properties` that carry the GCP project ID and the secret ID for
each provider's API token.

| Property key | Provider | Env variable override |
|---|---|---|
| `quote-client.gcp-project-id` | StockData.org | `GCP_PROJECT_ID` |
| `quote-client.api-token-secret-id` | StockData.org | `API_TOKEN_SECRET_ID` |
| `alphavantage-client.gcp-project-id` | AlphaVantage | `GCP_PROJECT_ID` |
| `alphavantage-client.api-key-secret-id` | AlphaVantage | `ALPHAVANTAGE_API_KEY_SECRET_ID` |

Default secret IDs (used when environment variables are absent):

| Secret ID | Default value |
|---|---|
| StockData.org token | `stockdata_org_token` |
| AlphaVantage key | `alpha_vantage_access_key` |

---

### 2. `ClientRequestFilter` implementations

`QuoteClientRequestFilter` and `AlphaVantageClientRequestFilter` both implement the JAX-RS
`ClientRequestFilter` interface. They intercept every outbound HTTP call made by the respective
REST client and append the API credential as a query parameter before the request is dispatched.

```
QuoteClientRequestFilter.filter(requestContext)
  │
  ├─ secretManagerService.getSecret(gcpProjectId, apiTokenSecretId)
  │    └─ returns token (from cache or GCP)
  │
  └─ rebuilds URI: original URI + ?api_token=<token>
```

```
AlphaVantageClientRequestFilter.filter(requestContext)
  │
  ├─ secretManagerService.getSecret(gcpProjectId, apiKeySecretId)
  │    └─ returns key (from cache or GCP)
  │
  └─ rebuilds URI: original URI + ?apikey=<key>
```

---

### 3. `SecretManagerService`

A Spring `@Service` in `com.kiran.stockapi.common.gcp` that wraps the GCP Secret Manager SDK.
It provides two overloads:

| Method | Description |
|---|---|
| `getSecret(projectId, secretId)` | Fetches the `latest` version of a secret |
| `getSecret(projectId, secretId, versionId)` | Fetches a specific version |

Both overloads are annotated with `@Cacheable` (cache name: `secrets`). The cache key is
`<projectId>:<secretId>:<versionId>`, so different secrets and versions are cached independently.

On a **cache miss**, the service:
1. Creates a `SecretManagerServiceClient` via Application Default Credentials (ADC)
2. Builds a `SecretVersionName` resource path (`projects/<id>/secrets/<id>/versions/<ver>`)
3. Calls `accessSecretVersion(...)` on the GCP API
4. Extracts the raw UTF-8 payload and returns it

On a **cache hit**, the GCP API is not called at all.

---

### 4. `CacheConfig`

Configures the in-memory `ConcurrentMapCacheManager` for the `secrets` cache name.

| Behaviour | Detail |
|---|---|
| Cache type | In-memory (`ConcurrentMapCache`) |
| Eviction schedule | Every 12 hours — at 02:00 and 14:00 daily |
| Eviction scope | All entries cleared (respects secret rotation) |

The scheduled eviction (`@CacheEvict(allEntries = true)`) ensures that if a secret is rotated in
GCP Secret Manager, the application will pick up the new value within at most 12 hours without
requiring a restart.

---

## Configuration Reference

```properties
# application.properties

# StockData.org
quote-client.base-url=https://api.stockdata.org/v1
quote-client.gcp-project-id=${GCP_PROJECT_ID:kiran-stock-api-project}
quote-client.api-token-secret-id=${API_TOKEN_SECRET_ID:stockdata_org_token}

# AlphaVantage
alphavantage-client.base-url=https://www.alphavantage.co
alphavantage-client.gcp-project-id=${GCP_PROJECT_ID:kiran-stock-api-project}
alphavantage-client.api-key-secret-id=${ALPHAVANTAGE_API_KEY_SECRET_ID:alpha_vantage_access_key}
```

---

## Authentication

The GCP SDK uses **Application Default Credentials (ADC)** automatically. ADC checks for
credentials in the following order at runtime:

```
1. GOOGLE_APPLICATION_CREDENTIALS env var  (service account JSON key file)
        ↓ not set
2. gcloud CLI credentials  ←── used for local development
        ↓ not set
3. GCE / GKE metadata server  (only reachable on GCP-hosted infrastructure)
```

| Environment | How credentials are resolved |
|---|---|
| Local development | `gcloud auth application-default login` (gcloud CLI) |
| GCP-hosted (GKE, Cloud Run, etc.) | Attached service account on the compute resource |
| Non-GCP with a key file | `GOOGLE_APPLICATION_CREDENTIALS` env variable → path to JSON key |

### Local development — gcloud CLI credentials

When running the application locally the `SecretManagerServiceClient` (created inside
`SecretManagerService`) resolves credentials via the gcloud CLI. No additional configuration in
`application.properties` is required.

**One-time setup:**

```bat
gcloud auth application-default login
```

This opens a browser, asks you to sign in with your Google account, and writes a credentials file
to:

```
%APPDATA%\gcloud\application_default_credentials.json   (Windows)
~/.config/gcloud/application_default_credentials.json   (macOS / Linux)
```

The GCP SDK picks up this file automatically through the ADC lookup chain.

**Verify your local setup:**

| Check | Command |
|---|---|
| gcloud is installed | `gcloud version` |
| Logged in | `gcloud auth list` |
| ADC token is obtainable | `gcloud auth application-default print-access-token` |
| Active project is correct | `gcloud config get-value project` |

> **IAM requirement**: the Google account used for `gcloud auth application-default login` must
> have the `roles/secretmanager.secretAccessor` role granted on the relevant secrets in GCP.

See [SECRETS_MANAGER_SETUP.md](SECRETS_MANAGER_SETUP.md) for step-by-step instructions on
creating the secrets, granting IAM permissions, and troubleshooting authentication issues.

---

## Class Locations

| Class | Package |
|---|---|
| `SecretManagerService` | `com.kiran.stockapi.common.gcp` |
| `QuoteClientRequestFilter` | `com.kiran.stockapi.stockdata.api.client` |
| `QuoteClientProperties` | `com.kiran.stockapi.stockdata.api.config` |
| `AlphaVantageClientRequestFilter` | `com.kiran.stockapi.alphavantage.api.client` |
| `AlphaVantageClientProperties` | `com.kiran.stockapi.alphavantage.api.config` |
| `CacheConfig` | `com.kiran.stockapi.config` |
