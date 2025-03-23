for local testing fill local.settings.json with values.

1. First, create an Azure Storage Account:

-    Go to Azure Portal (portal.azure.com)
-    Click "Create a resource"
-    Search for "Storage account"
-    Click "Create"

2. Get the Storage Connection String:

-   Once created, go to your storage account
-   In the left menu, find "Access keys" under "Security + networking"
-   Click "Show keys"
-   Copy the "Connection string" value - this is your AzureWebJobsStorage value

3. Create a Blob Container:

-    In your storage account, go to "Containers" under "Data storage"
-    Click "+ Container"
-    Name: Enter a name (e.g., "orders") - this will be your BLOB_CONTAINER_NAME
-    Public access level: Private
-    Click "Create"
