package com.kiran.musicapi.config;

import static com.kiran.musicapi.config.CacheConfig.SECRET_CACHE;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SecretManagerService {

	/**
	 * Fetches a secret from Google Cloud Secret Manager. Results are cached based
	 * on projectId, secretId, and versionId to avoid repeated API calls.
	 *
	 * @param projectId
	 *            The GCP project ID
	 * @param secretId
	 *            The secret ID
	 * @param versionId
	 *            The version of the secret (use "latest" for the latest version)
	 * @return The secret value as a String
	 */
	@Cacheable(value = SECRET_CACHE, key = "#projectId + ':' + #secretId + ':' + #versionId")
	public String getSecret(String projectId, String secretId, String versionId) {
		log.info("Cache miss - Fetching secret from GCP Secret Manager - Project: {}, Secret: {}, Version: {}",
				projectId, secretId, versionId);

		try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
			SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
			String resourceName = secretVersionName.toString();
			log.info("Accessing secret with resource name: {}", resourceName);

			AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
			String secret = response.getPayload().getData().toStringUtf8();
			log.info("Successfully retrieved secret: {}", secretId);
			return secret;
		} catch (IOException e) {
			log.error("IOException while creating SecretManagerServiceClient or retrieving secret: {}", secretId, e);
			log.error("Error details - Project: {}, Secret: {}, Version: {}", projectId, secretId, versionId);
			throw new RuntimeException("Failed to retrieve secret from Google Cloud Secret Manager: " + e.getMessage(),
					e);
		} catch (Exception e) {
			log.error("Unexpected error while retrieving secret: {}", secretId, e);
			log.error("Error type: {}, Message: {}", e.getClass().getName(), e.getMessage());
			log.error("Error details - Project: {}, Secret: {}, Version: {}", projectId, secretId, versionId);
			throw new RuntimeException("Failed to retrieve secret from Google Cloud Secret Manager: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Fetches the latest version of a secret from Google Cloud Secret Manager.
	 * Results are cached based on projectId and secretId. This is a convenience
	 * method that delegates to getSecret(projectId, secretId, "latest").
	 *
	 * @param projectId
	 *            The GCP project ID
	 * @param secretId
	 *            The secret ID
	 * @return The secret value as a String
	 */
	@Cacheable(value = SECRET_CACHE, key = "#projectId + ':' + #secretId + ':latest'")
	public String getSecret(String projectId, String secretId) {
		log.info("Cache miss - Fetching secret from GCP Secret Manager - Project: {}, Secret: {}, Version: latest",
				projectId, secretId);

		try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
			SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, "latest");
			String resourceName = secretVersionName.toString();
			log.info("Accessing secret with resource name: {}", resourceName);

			AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
			String secret = response.getPayload().getData().toStringUtf8();
			log.info("Successfully retrieved secret: {}", secretId);
			return secret;
		} catch (IOException e) {
			log.error("IOException while creating SecretManagerServiceClient or retrieving secret: {}", secretId, e);
			log.error("Error details - Project: {}, Secret: {}, Version: latest", projectId, secretId);
			throw new RuntimeException("Failed to retrieve secret from Google Cloud Secret Manager: " + e.getMessage(),
					e);
		} catch (Exception e) {
			log.error("Unexpected error while retrieving secret: {}", secretId, e);
			log.error("Error type: {}, Message: {}", e.getClass().getName(), e.getMessage());
			log.error("Error details - Project: {}, Secret: {}, Version: latest", projectId, secretId);
			throw new RuntimeException("Failed to retrieve secret from Google Cloud Secret Manager: " + e.getMessage(),
					e);
		}
	}
}
