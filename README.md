# DPP Metadata Registry

An example implementation of a Digital Product Passport (DPP) Metadata Registry compliant with EU regulations.

## Overview

This application provides an HTTP-based registry service for persisting and managing DPP metadata. It offers flexible configuration options for database backends (PostgreSQL or MariaDB), customizable metadata schemas, and OpenID Connect authentication.

> **⚠️ Disclaimer**: This is only an example implementation based on subjective yet reasonable assumptions about DPP metadata registry requirements. The official standardization process for Digital Product Passport registries at the EU level is still ongoing and not yet finalized. This implementation serves as an example and experimental application, and may not reflect the final specifications once the standardization work is complete. Use this software with awareness that official requirements will probably differ from the approach taken here.

### Key Features

- **RESTful API** for metadata management and schema configuration
- **Flexible metadata schema** with runtime customization support
- **Multiple database backends** (PostgreSQL, MariaDB)
- **OpenID Connect authentication** with role-based access control
- **Autocompletion capabilities** for metadata fields
- **Configurable update strategies** for handling duplicate UPI entries

## Table of Contents

- [Quick Start](#quick-start)
- [Configuration](#configuration)
  - [Configuration Variables Reference](#configuration-variables-reference)
  - [Configuration Examples](#configuration-examples)
- [Metadata Schema](#metadata-schema)
  - [Default Schema](#default-schema)
  - [Schema Customization](#schema-customization)
  - [Schema Resolution Order](#schema-resolution-order)
- [REST API](#rest-api)
  - [Metadata Endpoints](#metadata-endpoints)
  - [Schema Management Endpoints](#schema-management-endpoints)
- [Authentication & Authorization](#authentication--authorization)

## Quick Start

### Build the Application

```bash
mvn clean install
```

### Run the Application

After building, you can run the application using the Quarkus runner:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### Run with Custom Configuration

You can provide a custom `application.properties` file at startup:

```bash
java -jar target/quarkus-app/quarkus-run.jar -Dquarkus.config.locations=/path/to/application.properties
```

Or use environment variables:

```bash
REGISTRY_DB_HOST=localhost \
REGISTRY_DB_PORT=5432 \
REGISTRY_DB_NAME=registry_db \
REGISTRY_DB_USERNAME=db_user \
REGISTRY_DB_PASSWORD=db_password \
REGISTRY_OIDC_BASE_URL=https://your-idp.com/realms/your-realm \
REGISTRY_OIDC_CLIENT_ID=your-client-id \
REGISTRY_OIDC_SECRET=your-secret \
java -jar target/quarkus-app/quarkus-run.jar
```

### Using Docker

See the [Docker](#docker-compose) and [Docker Compose](#docker-compose) examples in the configuration section.

## Configuration

### Configuration Variables Reference

#### Database Configuration

| Variable                | Environment Variable    | Description          | Default |
|-------------------------|-------------------------|----------------------|---------|
| `registry.db-host`      | `REGISTRY_DB_HOST`      | Database hostname    | -       |
| `registry.db-port`      | `REGISTRY_DB_PORT`      | Database port        | -       |
| `registry.db-name`      | `REGISTRY_DB_NAME`      | Database name        | -       |
| `registry.db-username`  | `REGISTRY_DB_USERNAME`  | Database username    | -       |
| `registry.db-password`  | `REGISTRY_DB_PASSWORD`  | Database password    | -       |
| `registry.db.pool-size` | `REGISTRY_DB_POOL_SIZE` | Connection pool size | `16`    |

#### OpenID Connect Configuration

| Variable                        | Environment Variable            | Description                                         | Default |
|---------------------------------|---------------------------------|-----------------------------------------------------|---------|
| `registry.oidc.base-url`        | `REGISTRY_OIDC_BASE_URL`        | OIDC server base URL                                | -       |
| `registry.oidc.client-id`       | `REGISTRY_OIDC_CLIENT_ID`       | OIDC client ID                                      | -       |
| `registry.oidc.secret`          | `REGISTRY_OIDC_SECRET`          | OIDC client secret                                  | -       |
| `registry.oidc.role-claim-path` | `REGISTRY_OIDC_ROLE_CLAIM_PATH` | Comma-separated JWT claim paths for role extraction | `group` |

#### Application Configuration

| Variable                              | Environment Variable            | Description                                                           | Default |
|---------------------------------------|---------------------------------|-----------------------------------------------------------------------|---------|
| `registry.autocompletion-enabled-for` | `AUTOCOMPLETION_ENABLED_FOR`    | Comma-separated list of fields eligible for autocompletion            | -       |
| `registry.update-strategy`            | `REGISTRY_UPDATE_STRATEGY`      | Strategy for handling duplicate UPI: `UPDATE` or `APPEND_WITH_NEW_ID` | -       |
| `registry.upi-field-name`             | `REGISTRY_UPI_FIELD_NAME`       | Custom name for the UPI field in the schema                           | `upi`   |
| `registry.role-mappings`              | `REGISTRY_ROLE_MAPPINGS`        | Comma-separated mappings between external and internal roles          | -       |
| `registry.json-schema-location`       | `REGISTRY_JSON_SCHEMA_LOCATION` | Location of custom JSON schema (URL, file URI, or absolute path)      | -       |

#### Configuration Notes

**Update Strategy**
- `UPDATE`: Overwrites existing metadata when the same UPI is submitted
- `APPEND_WITH_NEW_ID`: Creates a new entry even if the UPI already exists

**Role Mappings**
- Maps external Identity Provider roles to internal application roles
- Internal roles: `admin`, `eo` (Economic Operator), `eu` (End User)
- Format: `external_role:internal_role,another_external:another_internal`
- Example: `keycloak_admin:admin,keycloak_operator:eo`

**OIDC Role Claim Path**
- Supports multiple paths separated by commas
- The system searches for roles in the JWT token at each specified path in order
- Example: `group,realm_access.roles`

**JSON Schema Location**
- Supports multiple formats:
  - HTTP URL: `https://example.com/schema.json`
  - File URI: `file:///path/to/schema.json`
  - Absolute path: `/etc/registry/schema.json`

### Configuration Examples

#### Application Properties

```properties
# Database Configuration
registry.db-host=localhost
registry.db-port=5432
registry.db-name=mydb
registry.db-username=dbuser
registry.db-password=dbpass
registry.db.pool-size=20

# OIDC Configuration
registry.oidc.base-url=https://keycloak.example.com/realms/myrealm
registry.oidc.client-id=my-client
registry.oidc.secret=my-secret
registry.oidc.role-claim-path=group,realm_access.roles

# Application Configuration
registry.autocompletion-enabled-for=commodityCode,facilitiesId,dataCarrierTypes
registry.update-strategy=UPDATE
registry.upi-field-name=upi
registry.role-mappings=keycloak_admin:admin,keycloak_eu:eu,keycloak_operator:eo
registry.json-schema-location=/etc/registry/custom-schema.json
```

#### Docker Compose

```yaml
version: '3.8'

services:
  registry:
    image: dpp-metadata-registry:latest
    ports:
      - "8080:8080"
    environment:
      # Database Configuration
      REGISTRY_DB_HOST: postgres
      REGISTRY_DB_PORT: 5432
      REGISTRY_DB_NAME: mydb
      REGISTRY_DB_USERNAME: dbuser
      REGISTRY_DB_PASSWORD: ${DB_PASSWORD}
      REGISTRY_DB_POOL_SIZE: 20
      
      # OIDC Configuration
      REGISTRY_OIDC_BASE_URL: https://keycloak:8443/realms/myrealm
      REGISTRY_OIDC_CLIENT_ID: my-client
      REGISTRY_OIDC_SECRET: ${OIDC_SECRET}
      REGISTRY_OIDC_ROLE_CLAIM_PATH: group,realm_access.roles
      
      # Application Configuration
      AUTOCOMPLETION_ENABLED_FOR: commodityCode,facilitiesId,dataCarrierTypes
      REGISTRY_UPDATE_STRATEGY: UPDATE
      REGISTRY_UPI_FIELD_NAME: upi
      REGISTRY_ROLE_MAPPINGS: keycloak_admin:admin,keycloak_eu:eu,keycloak_operator:eo
      REGISTRY_JSON_SCHEMA_LOCATION: /etc/registry/custom-schema.json
    depends_on:
      - postgres
    volumes:
      - ./custom-schema.json:/etc/registry/custom-schema.json:ro

  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: dbuser
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  postgres-data:
```

#### Kubernetes Deployment

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: registry-config
data:
  REGISTRY_DB_HOST: "postgres-service"
  REGISTRY_DB_PORT: "5432"
  REGISTRY_DB_NAME: "mydb"
  REGISTRY_DB_USERNAME: "dbuser"
  REGISTRY_DB_POOL_SIZE: "20"
  REGISTRY_OIDC_BASE_URL: "https://keycloak.example.com/realms/myrealm"
  REGISTRY_OIDC_CLIENT_ID: "my-client"
  REGISTRY_OIDC_ROLE_CLAIM_PATH: "group,realm_access.roles"
  AUTOCOMPLETION_ENABLED_FOR: "commodityCode,facilitiesId,dataCarrierTypes"
  REGISTRY_UPDATE_STRATEGY: "UPDATE"
  REGISTRY_UPI_FIELD_NAME: "upi"
  REGISTRY_ROLE_MAPPINGS: "keycloak_admin:admin,keycloak_user:eu,keycloak_operator:eo"
  REGISTRY_JSON_SCHEMA_LOCATION: "/etc/registry/custom-schema.json"

---
apiVersion: v1
kind: Secret
metadata:
  name: registry-secrets
type: Opaque
stringData:
  REGISTRY_DB_PASSWORD: "dbpass"
  REGISTRY_OIDC_SECRET: "my-secret"

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: registry-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: registry
  template:
    metadata:
      labels:
        app: registry
    spec:
      containers:
      - name: registry
        image: dpp-metadata-registry:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: registry-config
        - secretRef:
            name: registry-secrets
        volumeMounts:
        - name: schema-volume
          mountPath: /etc/registry
          readOnly: true
      volumes:
      - name: schema-volume
        configMap:
          name: json-schema-config
```

## Metadata Schema

The application uses JSON Schema (draft 2020-12) to validate and manage DPP metadata structure.

### Default Schema

The application ships with a comprehensive default schema that includes:

- **UPI** (Unique Product Identifier): Required identifier for the product
- **REO ID** (Responsible Economic Operator ID): Required operator identifier
- **URLs**: Live and backup URLs for DPP data retrieval
- **Facility IDs**: Array of facility identifiers
- **Commodity Code**: Product classification codes (HS Code, TARIC)
- **Data Carrier Types**: Physical carriers (QR codes, RFID, NFC, etc.)
- **Granularity Level**: Product scope (MODEL, BATCH, ITEM)

<details>
<summary>View complete default schema</summary>

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "EU DPP Registry Metadata",
  "description": "Schema for Digital Product Passport registration metadata in the EU Registry",
  "type": "object",
  "required": [
    "upi",
    "reoId",
    "liveURL",
    "granularityLevel"
  ],
  "properties": {
    "upi": {
      "type": "string",
      "description": "Unique Product Identifier - the unique identifier of the product",
      "minLength": 1,
      "maxLength": 200,
      "examples": [
        "urn:epc:id:sgtin:0614141.107346.2017"
      ]
    },
    "reoId": {
      "type": "string",
      "description": "Responsible Economic Operator ID",
      "minLength": 1,
      "maxLength": 50,
      "examples": [
        "LEI-529900T8BM49AURSDO55",
        "EORI-IT123456789"
      ]
    },
    "facilitiesId": {
      "type": "array",
      "uniqueItems": true,
      "minItems": 1,
      "items": {
        "type": "string"
      },
      "description": "Facility ID where the product is manufactured/assembled",
      "maxLength": 250,
      "examples": [
        "GLN-5412345000013",
        "FAC-MILANO-001"
      ]
    },
    "liveURL": {
      "type": "string",
      "format": "uri",
      "description": "Primary URL to retrieve the related DPP data",
      "pattern": "^https://",
      "examples": [
        "https://dpp.example.com/product/12345"
      ]
    },
    "backupURL": {
      "type": "string",
      "format": "uri",
      "description": "Backup URL to retrieve the related DPP data",
      "pattern": "^https://",
      "examples": [
        "https://backup-dpp.example.com/product/12345"
      ]
    },
    "commodityCode": {
      "type": [
        "string",
        "null"
      ],
      "description": "The commodity code of the product (e.g., HS Code, TARIC)",
      "pattern": "^[0-9]{4,10}$",
      "examples": [
        "85176200",
        "8517620090"
      ]
    },
    "dataCarrierTypes": {
      "type": "array",
      "description": "Types of data carriers associated with the product",
      "uniqueItems": true,
      "minItems": 1,
      "examples": [
        ["QR_CODE","RFID"]
      ],
      "items": {
        "type": "string",
        "enum": [
          "QR_CODE",
          "DATA_MATRIX",
          "BARCODE_EAN",
          "BARCODE_UPC",
          "BARCODE_GS1",
          "RFID",
          "NFC",
          "AZTEC_CODE",
          "PDF417"
        ]
      }
    },
    "granularityLevel": {
      "type": "string",
      "description": "The granularity level of the DPP",
      "enum": [
        "MODEL",
        "BATCH",
        "ITEM"
      ],
      "examples": [
        "MODEL"
      ]
    }
  }
}
```

</details>

### Schema Customization

The application provides two methods to override the default schema:

#### 1. Startup Configuration

Provide a schema location via the `registry.json-schema-location` configuration parameter. This schema will be loaded at application startup.

```properties
registry.json-schema-location=/path/to/custom-schema.json
```

#### 2. Runtime API Submission

Submit a new schema via the [POST /schema/v1](#post-schemav1) endpoint. This allows dynamic schema updates without restarting the application.

### Schema Resolution Order

The application resolves which schema to use following this priority order:

1. **Most recent schema submitted via API** (if any exists)
2. **Schema from configured file path** (if `registry.json-schema-location` is set)
3. **Default embedded schema** (fallback)

### Schema Validation Rules

When a custom schema is submitted (via API or file), it must satisfy these requirements:

- Must contain a **UPI property** (by default named `upi`, customizable via `registry.upi-field-name`)
- All properties must be either **primitive types** or **arrays of primitive types**
- If autocompletion is configured, all fields in `registry.autocompletion-enabled-for` must exist in the schema

## REST API

The application exposes two main API groups:

1. **Metadata API**: For creating and managing product metadata entries
2. **Schema API**: For runtime schema configuration and retrieval

To obtain the OpenAPI document start the application and issue a `GET` request targeting the path `/q/openapi`. Use the `Accept`
header to negotiate the media type (either `JSON` or `YAML`). The endpoint will always return an OpenAPI document aligned with
the current JSON schema in use by the application.
### Metadata Endpoints

#### POST /metadata/v1

Creates or updates a metadata entry in the registry.

**Behavior:**
- If the UPI does not exist: creates a new entry
- If the UPI already exists: behavior depends on the configured [update strategy](#configuration-notes)
  - `UPDATE`: overwrites the existing entry
  - `APPEND_WITH_NEW_ID`: creates a new entry with a different registry ID

**Query Parameters:**

| Parameter        | Type   | Description                                                                                                                                                                                                                              |
|------------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `autocompleteBy` | string | Comma-separated list of fields used to search previously added metadata for autocompletion. Fields specified in `registry.autocompletion-enabled-for` that are null or missing in the request will be auto-filled from matching records. |

**Example Request:**

```http
POST /metadata/v1?autocompleteBy=reoId,commodityCode
Content-Type: application/json

{
  "reoId": "LEI-529900T8BM49AURSDO55",
  "upi": "urn:epc:id:sgtin:0614141.107346.2017",
  "liveURL": "https://dpp.example.com/product/12345",
  "commodityCode": "85176200",
  "dataCarrierTypes": ["QR_CODE", "RFID"],
  "granularityLevel": "ITEM"
}
```

**Example Response:**

```json
{
  "registryId": "7f3e9c2a-5b8d-4e1f-a6c3-9d4b2e7f8a1c",
  "metadata": {
    "reoId": "LEI-529900T8BM49AURSDO55",
    "upi": "urn:epc:id:sgtin:0614141.107346.2017",
    "liveURL": "https://dpp.example.com/product/12345",
    "commodityCode": "85176200",
    "dataCarrierTypes": ["QR_CODE", "RFID"],
    "granularityLevel": "ITEM"
  }
}
```

### Schema Management Endpoints

#### POST /schema/v1

Submits a new DPP metadata JSON schema. The newly submitted schema immediately becomes the active schema used for validation and metadata management.

**Request Body:** A valid JSON Schema (draft 2020-12) document

**Example Request:**

```http
POST /schema/v1
Content-Type: application/json

{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "Custom DPP Registry Metadata",
  "description": "Customized schema for DPP registration",
  "type": "object",
  "required": ["upi", "reoId", "liveURL"],
  "properties": {
    "upi": {
      "type": "string",
      "description": "Unique Product Identifier",
      "minLength": 1,
      "maxLength": 200
    },
    "reoId": {
      "type": "string",
      "description": "Responsible Economic Operator ID",
      "minLength": 1,
      "maxLength": 50
    },
    "liveURL": {
      "type": "string",
      "format": "uri",
      "description": "Primary URL to retrieve the DPP data",
      "pattern": "^https://"
    }
  }
}
```

#### GET /schema/v1/current

Retrieves the currently active DPP metadata JSON schema (the most recently submitted schema, or the configured/default schema if none has been submitted).

**Example Response:**

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "Custom DPP Registry Metadata",
  "description": "Customized schema for DPP registration",
  "type": "object",
  "required": ["upi", "reoId", "liveURL"],
  "properties": {
    "upi": {
      "type": "string",
      "description": "Unique Product Identifier",
      "minLength": 1,
      "maxLength": 200
    },
    "reoId": {
      "type": "string",
      "description": "Responsible Economic Operator ID",
      "minLength": 1,
      "maxLength": 50
    },
    "liveURL": {
      "type": "string",
      "format": "uri",
      "description": "Primary URL to retrieve the DPP data",
      "pattern": "^https://"
    }
  }
}
```

#### DELETE /schema/v1/current

Removes the currently active schema. After deletion, the system reverts to the previous schema in the [resolution order](#schema-resolution-order):

1. If other schemas were submitted via API, the second-most-recent becomes active
2. Otherwise, reverts to the configured schema file (if `registry.json-schema-location` is set)
3. Otherwise, reverts to the default embedded schema

## Authentication & Authorization

The application uses **OpenID Connect (OIDC)** for authentication and implements role-based access control.

### Supported Roles

- **`admin`**: Full administrative access
- **`eo`** (Economic Operator): Operator-level access
- **`eu`** (End User): End-user level access

### Role Mapping

External Identity Provider roles can be mapped to internal roles using the `registry.role-mappings` configuration:

```properties
registry.role-mappings=keycloak_admin:admin,idp_operator:eo,idp_user:eu
```

### JWT Role Claims

The application extracts roles from JWT tokens using the paths specified in `registry.oidc.role-claim-path`. Multiple paths can be specified:

```properties
registry.oidc.role-claim-path=group,realm_access.roles,resource_access.my-client.roles
```

The system searches each path in order until roles are found.

---

## License

This project is licensed under the Apache License 2.0 - see below for details.

```
Copyright 2025-2026 ExtraRed

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contributing

We welcome contributions to this project! To contribute:

1. **Open a Pull Request** on GitHub with your changes
2. **Include tests** for all modifications:
   - Bug fixes must include tests that verify the fix
   - New functionalities must include comprehensive test coverage
   - Improvements should include tests where applicable
3. **Request a review** from the maintainers
4. Ensure all existing tests pass and code follows the project's coding standards

All contributions will be reviewed before being merged.

## Support

For questions, issues, or support requests, please contact:

**marco.volpini@extrared.it**