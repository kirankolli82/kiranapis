# Google Cloud Secret Manager - Troubleshooting Guide

## Error: "PERMISSION_DENIED: Secret Manager API has not been used in project..."

This error occurs even after enabling the API. Here's how to debug and fix it:

## Common Causes and Solutions

### 1. **Billing Not Enabled (MOST CRITICAL)**

**This is the #1 cause of the PERMISSION_DENIED error!**

The Secret Manager API requires billing to be enabled on your GCP project. Even though the error message says "API has not been used" or "is disabled", the real issue is often that **billing is not configured**.

#### Check Billing Status

```powershell
# Try to enable the API - this will reveal billing issues
gcloud services enable secretmanager.googleapis.com --project=kiran-stock-api-project
```

If you see an error like:
```
FAILED_PRECONDITION: Billing account for project is not found. 
Billing must be enabled for activation of service(s) 'secretmanager.googleapis.com'
```

Then billing is NOT enabled.

#### Solution: Enable Billing

1. **Go to the GCP Console:**
   - Visit: https://console.cloud.google.com/billing?project=kiran-stock-api-project

2. **Link a billing account:**
   - If you don't have a billing account, create one (you may get free credits)
   - Link the billing account to your project `kiran-stock-api-project`

3. **Wait 2-3 minutes** for billing to propagate

4. **Enable the API again:**
   ```powershell
   gcloud services enable secretmanager.googleapis.com --project=kiran-stock-api-project
   ```

5. **Verify it's enabled:**
   ```powershell
   gcloud services list --enabled --project=kiran-stock-api-project | Select-String "secretmanager"
   ```

**Without billing enabled, NO Google Cloud APIs will work, regardless of permissions or authentication!**

---

### 2. **Authentication Issue**

The error message says the API is disabled, but it can also be an **authentication problem**.

#### Solution: Authenticate with `gcloud`

```powershell
# Authenticate your local machine
gcloud auth application-default login

# Verify which account is authenticated
gcloud auth list

# Verify the project
gcloud config get-value project

# Set the correct project if needed
gcloud config set project kiran-stock-api-project
```

**After running these commands, wait 2-3 minutes and restart your application.**

---

### 3. **Using the Wrong Project ID**

The error occurs in project `kiran-stock-api-project`. Verify this is the correct project.

#### Check Your Configuration

In `application.properties`:
```properties
quote-client.gcp-project-id=${GCP_PROJECT_ID:kiran-stock-api-project}
```

#### Verify Project Exists
```powershell
gcloud projects list
```

Look for `kiran-stock-api-project` in the output.

---

### 3. **API Not Actually Enabled**

Even though you enabled it, verify it's really enabled:

```powershell
# Check if Secret Manager API is enabled
gcloud services list --enabled --project=kiran-stock-api-project | Select-String "secretmanager"

# If not shown, enable it explicitly
gcloud services enable secretmanager.googleapis.com --project=kiran-stock-api-project
```

---

### 4. **Missing IAM Permissions**

Your authenticated user/service account needs the right permissions.

#### For Local Development (Your User Account)

```powershell
# Get your authenticated email
gcloud auth list

# Grant yourself Secret Manager access
gcloud projects add-iam-policy-binding kiran-stock-api-project `
    --member="user:YOUR_EMAIL@gmail.com" `
    --role="roles/secretmanager.secretAccessor"
```

#### For Service Accounts

```powershell
# If using a service account
gcloud projects add-iam-policy-binding kiran-stock-api-project `
    --member="serviceAccount:YOUR_SERVICE_ACCOUNT@kiran-stock-api-project.iam.gserviceaccount.com" `
    --role="roles/secretmanager.secretAccessor"
```

---

### 5. **Secret Doesn't Exist**

Create the secret if it doesn't exist:

```powershell
# Check if secret exists
gcloud secrets list --project=kiran-stock-api-project

# Create the secret if needed
gcloud secrets create stockdata_org_token `
    --replication-policy="automatic" `
    --project=kiran-stock-api-project

# Add the actual API token value
echo -n "YOUR_ACTUAL_API_TOKEN_VALUE" | gcloud secrets versions add stockdata_org_token `
    --data-file=- `
    --project=kiran-stock-api-project
```

**Note:** The secret name should match `quote-client.api-token-secret-id` in your `application.properties`.

---

## Using the Diagnostic Tool

The application now includes a diagnostic tool that runs on startup when enabled.

### Enable Diagnostics

In `application.properties`:
```properties
gcp.diagnostics.enabled=true
```

### Run the Application

```powershell
./gradlew.bat bootRun
```

### What the Diagnostics Check

1. **Environment Variables**: Shows which GCP-related env vars are set
2. **Credentials**: Validates that Application Default Credentials are working
3. **Secret Manager API**: Tests connection and lists available secrets

### Expected Output

```
========== GCP Authentication Diagnostics ==========
--- Environment Variables ---
GCP_PROJECT_ID: kiran-stock-api-project
API_TOKEN_SECRET_ID: stockdata_org_token
GOOGLE_APPLICATION_CREDENTIALS: (not set - will use Application Default Credentials)
...
--- Google Cloud Credentials ---
✓ Successfully loaded Application Default Credentials
Credential type: UserCredentials
✓ Credentials are valid and refreshable
...
--- Secret Manager API Access ---
✓ Successfully created SecretManagerServiceClient
✓ Successfully connected to Secret Manager API
  Found secret: stockdata_org_token
...
========== End of Diagnostics ==========
```

---

## Step-by-Step Debugging Process

### Step 1: Run the Diagnostic Tool
Enable `gcp.diagnostics.enabled=true` and run the app. Look for ✗ (failure) markers.

### Step 2: Check Authentication
```powershell
# Re-authenticate
gcloud auth application-default login

# Verify
gcloud auth application-default print-access-token
```

If this command fails, your credentials are not set up.

### Step 3: Verify Project Access
```powershell
# List your projects
gcloud projects list

# Set the correct project
gcloud config set project kiran-stock-api-project
```

### Step 4: Enable API (Again)
```powershell
gcloud services enable secretmanager.googleapis.com --project=kiran-stock-api-project
```

Wait 5 minutes, then test again.

### Step 5: Grant Permissions
```powershell
# Get your email
$USER_EMAIL = gcloud auth list --filter=status:ACTIVE --format="value(account)"

# Grant access
gcloud projects add-iam-policy-binding kiran-stock-api-project `
    --member="user:$USER_EMAIL" `
    --role="roles/secretmanager.secretAccessor"
```

### Step 6: Test Secret Access Directly
```powershell
# Try to access the secret via gcloud
gcloud secrets versions access latest --secret="stockdata_org_token" --project=kiran-stock-api-project
```

If this works, the API and permissions are correct. If the app still fails, it's an authentication issue in the app.

---

## Understanding `apiTokenSecretId`

### Question: What should `apiTokenSecretId` be?

**Answer:** It's the **Secret ID** (not the full resource name).

### Example:

In Google Cloud Console:
- **Secret ID**: `stockdata_org_token` ← Use this in `application.properties`
- **Resource Name**: `projects/kiran-stock-api-project/secrets/stockdata_org_token` ← Don't use this

Your configuration:
```properties
quote-client.api-token-secret-id=${API_TOKEN_SECRET_ID:stockdata_org_token}
```

The application code constructs the full resource name automatically:
```java
SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
// Result: projects/kiran-stock-api-project/secrets/stockdata_org_token/versions/latest
```

---

## Quick Fix Checklist

- [ ] **CRITICAL: Enable billing** - Visit https://console.cloud.google.com/billing?project=kiran-stock-api-project
- [ ] Run `gcloud auth application-default login`
- [ ] Run `gcloud config set project kiran-stock-api-project`
- [ ] Enable API: `gcloud services enable secretmanager.googleapis.com --project=kiran-stock-api-project`
- [ ] Wait 5 minutes
- [ ] Grant yourself access: `gcloud projects add-iam-policy-binding kiran-stock-api-project --member="user:YOUR_EMAIL" --role="roles/secretmanager.secretAccessor"`
- [ ] Create secret if needed: `gcloud secrets create stockdata_org_token --replication-policy="automatic" --project=kiran-stock-api-project`
- [ ] Test: `gcloud secrets versions access latest --secret="stockdata_org_token" --project=kiran-stock-api-project`
- [ ] Enable diagnostics: `gcp.diagnostics.enabled=true`
- [ ] Run application: `./gradlew.bat bootRun`
- [ ] Check logs for diagnostic output

---

## Still Not Working?

If you've tried everything above:

1. **Check the diagnostic output** - It will pinpoint the exact issue
2. **Check application logs** - Look for detailed error messages from `SecretManagerService`
3. **Verify you're in the right GCP organization** - Run `gcloud organizations list`
4. **Try a different secret** - Create a test secret and try accessing it
5. **Check billing** - Ensure the project has billing enabled
6. **Contact support** - Share the diagnostic output

---

## Additional Resources

- [Secret Manager Documentation](https://cloud.google.com/secret-manager/docs)
- [Application Default Credentials](https://cloud.google.com/docs/authentication/provide-credentials-adc)
- [IAM Permissions for Secret Manager](https://cloud.google.com/secret-manager/docs/access-control)

