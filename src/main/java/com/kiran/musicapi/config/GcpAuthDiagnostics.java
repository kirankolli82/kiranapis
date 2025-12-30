package com.kiran.musicapi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Diagnostic utility to help debug Google Cloud authentication and Secret
 * Manager access. Enable by setting: gcp.diagnostics.enabled=true
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "gcp.diagnostics.enabled", havingValue = "true")
public class GcpAuthDiagnostics implements CommandLineRunner {

	private final QuoteClientProperties quoteClientProperties;

	public GcpAuthDiagnostics(QuoteClientProperties quoteClientProperties) {
		this.quoteClientProperties = quoteClientProperties;
	}

	@Override
	public void run(String... args) {
		log.info("========== GCP Authentication Diagnostics ==========");

		// Check environment variables
		checkEnvironmentVariables();

		// Check credentials
		checkCredentials();

		// Check Secret Manager API availability
		checkSecretManagerApi();

		log.info("========== End of Diagnostics ==========");
		log.info("");
		log.info("If you see PERMISSION_DENIED errors above:");
		log.info("  1. MOST COMMON: Billing is not enabled on the GCP project");
		log.info("     → Visit: https://console.cloud.google.com/billing?project={}",
				quoteClientProperties.gcpProjectId());
		log.info("  2. Run: gcloud auth application-default login");
		log.info("  3. Grant permissions to your account");
		log.info("");
		log.info("See TROUBLESHOOTING_SECRET_MANAGER.md for detailed steps");
	}

	private void checkEnvironmentVariables() {
		log.info("--- Environment Variables ---");
		String gcpProjectId = System.getenv("GCP_PROJECT_ID");
		String apiTokenSecretId = System.getenv("API_TOKEN_SECRET_ID");
		String googleAppCreds = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

		log.info("GCP_PROJECT_ID: {}",
				gcpProjectId != null
						? gcpProjectId
						: "(not set - using default: " + quoteClientProperties.gcpProjectId() + ")");
		log.info("API_TOKEN_SECRET_ID: {}",
				apiTokenSecretId != null
						? apiTokenSecretId
						: "(not set - using default: " + quoteClientProperties.apiTokenSecretId() + ")");
		log.info("GOOGLE_APPLICATION_CREDENTIALS: {}",
				googleAppCreds != null ? googleAppCreds : "(not set - will use Application Default Credentials)");

		log.info("Configured project ID: {}", quoteClientProperties.gcpProjectId());
		log.info("Configured secret ID: {}", quoteClientProperties.apiTokenSecretId());
	}

	private void checkCredentials() {
		log.info("--- Google Cloud Credentials ---");
		try {
			GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
			log.info("✓ Successfully loaded Application Default Credentials");
			log.info("Credential type: {}", credentials.getClass().getSimpleName());

			// Try to refresh to ensure they're valid
			credentials.refresh();
			log.info("✓ Credentials are valid and refreshable");

		} catch (IOException e) {
			log.error("✗ Failed to load Application Default Credentials", e);
			log.error("This usually means:");
			log.error("  1. You haven't run: gcloud auth application-default login");
			log.error("  2. Or GOOGLE_APPLICATION_CREDENTIALS env var is not set correctly");
			log.error("  3. Or you're on GCP but the service account lacks permissions");
		} catch (Exception e) {
			log.error("✗ Unexpected error with credentials", e);
		}
	}

	private void checkSecretManagerApi() {
		log.info("--- Secret Manager API Access ---");
		String projectId = quoteClientProperties.gcpProjectId();

		try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
			log.info("✓ Successfully created SecretManagerServiceClient");
			log.info("Project ID being used: {}", projectId);

			// Try to list secrets (this will fail if API is disabled or no permission)
			try {
				String parent = "projects/" + projectId;
				log.info("Attempting to list secrets in project: {}", parent);

				var secrets = client.listSecrets(parent);
				log.info("✓ Successfully connected to Secret Manager API");

				int count = 0;
				for (var secret : secrets.iterateAll()) {
					count++;
					String secretName = secret.getName();
					String secretId = secretName.substring(secretName.lastIndexOf('/') + 1);
					log.info("  Found secret: {}", secretId);
					if (count >= 5) {
						log.info("  ... (showing first 5 secrets only)");
						break;
					}
				}

				if (count == 0) {
					log.warn("⚠ No secrets found in project. You may need to create the secret first.");
					log.warn("  Expected secret ID: {}", quoteClientProperties.apiTokenSecretId());
				}

			} catch (com.google.api.gax.rpc.PermissionDeniedException e) {
				log.error("✗ PERMISSION_DENIED when listing secrets", e);

				// Check if it's a billing issue
				String errorMessage = e.getMessage();
				if (errorMessage != null && (errorMessage.contains("billing") || errorMessage.contains("BILLING"))) {
					log.error("");
					log.error("*** BILLING IS NOT ENABLED ON THIS PROJECT ***");
					log.error("This is the most common cause of PERMISSION_DENIED errors!");
					log.error("");
					log.error("FIX: Enable billing for project: {}", projectId);
					log.error("  1. Visit: https://console.cloud.google.com/billing?project={}", projectId);
					log.error("  2. Link a billing account to the project");
					log.error("  3. Wait 2-3 minutes for it to propagate");
					log.error(
							"  4. Retry enabling the API: gcloud services enable secretmanager.googleapis.com --project={}",
							projectId);
					log.error("");
				} else {
					log.error("This means:");
					log.error(
							"  1. Billing might not be enabled (MOST COMMON) - check: https://console.cloud.google.com/billing?project={}",
							projectId);
					log.error("  2. The Secret Manager API might not be enabled in project: {}", projectId);
					log.error("  3. Your credentials don't have permission to access Secret Manager");
					log.error("  4. You might be using the wrong project ID");
					log.error("To enable the API, visit:");
					log.error("  https://console.cloud.google.com/apis/library/secretmanager.googleapis.com?project={}",
							projectId);
				}
			} catch (com.google.api.gax.rpc.UnauthenticatedException e) {
				log.error("✗ UNAUTHENTICATED - credentials are invalid or expired", e);
				log.error("Run: gcloud auth application-default login");
			} catch (Exception e) {
				log.error("✗ Error accessing Secret Manager API", e);
				log.error("Error type: {}", e.getClass().getName());
			}

		} catch (IOException e) {
			log.error("✗ Failed to create SecretManagerServiceClient", e);
		}
	}
}
