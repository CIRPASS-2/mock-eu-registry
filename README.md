# DPP Metadata Registry

An example implementation of a Digital Product Passport (DPP) Metadata Registry.

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

The application provides two maven profiles:
- `pgsql-oidc` profile builds an application using postgresql as a database and oidc as the authentication method.
- `mariadb-oidc` profile builds an application using mariadb as a database and oidc as the authentication method.

Artifacts and docker images are available [here](https://github.com/cirpass-2/dpp-metadata-registry/releases)
### Build the Application
```bash
mvn clean install -P pgsql-oidc
```
or
```bash
mvn clean install -P mariadb-oidc
```

### Run the Application

After building, you can run the application using the Quarkus runner.

Create an `application.properties` with your configuration parameters and specify its location.

***Run with PGSQL***
```bash
java -Dquarkus.config.locations=file://path/to/application.properties -Dquarkus.profile=pgql,oidc -jar target/quarkus-app/quarkus-run.jar
```

***Run with MARIADB***
```bash
java -Dquarkus.config.locations=file://path/to/application.properties -Dquarkus.profile=mariadb,oidc -jar target/quarkus-app/quarkus-run.jar
```

Instead of an `application.properties`, environment variables can be used:
```bash
QUARKUS_DATASOURCE_REACTIVE_URL=vertx-reactive:postgresql://localhost:5432/registry_db \
QUARKUS_DATASOURCE_USERNAME=db_user \
QUARKUS_DATASOURCE_PASSWORD=db_password \
QUARKUS_OIDC_AUTH_SERVER_URL=https://your-idp.com/realms/your-realm \
QUARKUS_OIDC_CLIENT_ID=your-client-id \
QUARKUS_OIDC_CREDENTIALS_SECRET=your-secret \
java -jar target/quarkus-app/quarkus-run.jar
```

### Using Docker

See the [Docker Compose](#docker-compose) examples in the configuration section.

## Configuration

### Configuration Variables Reference

#### Database Configuration

| Variable                                   | Environment Variable                          | Description                    | Default |
|--------------------------------------------|-----------------------------------------------|--------------------------------|---------|
| `quarkus.datasource.reactive.url`          | `QUARKUS_DATASOURCE_REACTIVE_URL`             | Reactive datasource URL        | -       |
| `quarkus.datasource.username`              | `QUARKUS_DATASOURCE_USERNAME`                 | Database username              | -       |
| `quarkus.datasource.password`              | `QUARKUS_DATASOURCE_PASSWORD`                 | Database password              | -       |
| `quarkus.datasource.reactive.max-size`     | `QUARKUS_DATASOURCE_REACTIVE_MAX_SIZE`        | Maximum connection pool size   | `16`    |

**PostgreSQL Reactive URL format:**
```
vertx-reactive:postgresql://hostname:port/database_name
```
Example: `vertx-reactive:postgresql://localhost:5432/registry_db`

**MariaDB Reactive URL format:**
```
vertx-reactive:mysql://hostname:port/database_name
```
Example: `vertx-reactive:mysql://localhost:3306/registry_db`

> **Note**: For MariaDB, the reactive driver uses the `mysql` protocol identifier.

**PostgreSQL Schema Script:**
```sql
CREATE SEQUENCE IF NOT EXISTS dpp_metadata_seq;

CREATE TABLE IF NOT EXISTS dpp_metadata (
id BIGINT PRIMARY KEY DEFAULT nextval('dpp_metadata_seq'),
registry_id VARCHAR(36) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
metadata JSONB NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS json_schema_seq;

CREATE TABLE IF NOT EXISTS json_schemas (
id BIGINT PRIMARY KEY DEFAULT nextval('json_schema_seq'),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
data_schema JSONB NOT NULL
);
```

**MariaDB Schema Script:**
```sql
CREATE TABLE IF NOT EXISTS dpp_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registry_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    metadata JSON NOT NULL
);

CREATE TABLE IF NOT EXISTS json_schemas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_schema JSON NOT NULL
);
```

#### OpenID Connect Configuration

| Variable                             | Environment Variable                 | Description                                         | Default |
|--------------------------------------|--------------------------------------|-----------------------------------------------------|---------|
| `quarkus.oidc.auth-server-url`       | `QUARKUS_OIDC_AUTH_SERVER_URL`       | OIDC server URL (realm URL for Keycloak)            | -       |
| `quarkus.oidc.client-id`             | `QUARKUS_OIDC_CLIENT_ID`             | OIDC client ID                                      | -       |
| `quarkus.oidc.credentials.secret`    | `QUARKUS_OIDC_CREDENTIALS_SECRET`    | OIDC client secret                                  | -       |
| `registry-auth.oidc.role-claim-path` | `REGISTRY_AUTH_OIDC_ROLE_CLAIM_PATH` | Comma-separated JWT claim paths for role extraction | `group` |

#### Application Configuration

| Variable                              | Environment Variable            | Description                                                           | Default |
|---------------------------------------|---------------------------------|-----------------------------------------------------------------------|---------|
| `registry.autocompletion-enabled-for` | `AUTOCOMPLETION_ENABLED_FOR`    | Comma-separated list of fields eligible for autocompletion            | -       |
| `registry.update-strategy`            | `REGISTRY_UPDATE_STRATEGY`      | Strategy for handling duplicate UPI: `MODIFY` or `APPEND_WITH_NEW_ID` | -       |
| `registry.upi-field-name`             | `REGISTRY_UPI_FIELD_NAME`       | Custom name for the unique product identifier field in the schema     | `upi`   |
| `registry.reoid-field-name`           | `REGISTRY_REOID_FIELD_NAME`     | Custom name for the responsible economic operator field in the schema | `reoId` |
| `registry.role-mappings`              | `REGISTRY_ROLE_MAPPINGS`        | Comma-separated mappings between external and internal roles          | -       |
| `registry.json-schema-location`       | `REGISTRY_JSON_SCHEMA_LOCATION` | Location of custom JSON schema (URL, file URI, or absolute path)      | -       |

#### HTTP Configuration


| Variable            | Environment Variable | Description              | Default |
|---------------------|----------------------|--------------------------|---------|
| `quarkus.http.port` | `QUARKUS_HTTP_PORT`  | HTTP port of the service | 8080    |


#### Configuration Notes

**Update Strategy**
- `MODIFY`: Overwrites existing metadata when the same UPI is submitted
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

#### Application Properties (PostgreSQL)
```properties
# Database Configuration
quarkus.datasource.reactive.url=vertx-reactive:postgresql://localhost:5432/registry_db
quarkus.datasource.username=dbuser
quarkus.datasource.password=dbpass
quarkus.datasource.reactive.max-size=20

# OIDC Configuration
quarkus.oidc.auth-server-url=https://keycloak.example.com/realms/myrealm
quarkus.oidc.client-id=my-client
quarkus.oidc.credentials.secret=my-secret
registry-auth.oidc.role-claim-path=group,realm_access.roles

# Application Configuration
registry.autocompletion-enabled-for=commodityCode,facilitiesId,dataCarrierTypes
registry.update-strategy=MODIFY
registry.upi-field-name=upi
registry.role-mappings=keycloak_admin:admin,keycloak_eu:eu,keycloak_operator:eo
registry.json-schema-location=/etc/registry/custom-schema.json
```

#### Application Properties (MariaDB)
```properties
# Database Configuration
quarkus.datasource.reactive.url=vertx-reactive:mysql://localhost:3306/registry_db
quarkus.datasource.username=dbuser
quarkus.datasource.password=dbpass
quarkus.datasource.reactive.max-size=20

# OIDC Configuration
quarkus.oidc.auth-server-url=https://keycloak.example.com/realms/myrealm
quarkus.oidc.client-id=my-client
quarkus.oidc.credentials.secret=my-secret
registry-auth.oidc.role-claim-path=group,realm_access.roles

# Application Configuration
registry.autocompletion-enabled-for=commodityCode,facilitiesId,dataCarrierTypes
registry.update-strategy=MODIFY
registry.upi-field-name=upi
registry.role-mappings=keycloak_admin:admin,keycloak_eu:eu,keycloak_operator:eo
registry.json-schema-location=/etc/registry/custom-schema.json
```

#### Docker Compose (PostgreSQL)
```yaml
version: '3.8'

services:
  registry:
    image: ghcr.io/cirpass-2/dpp-metadata-registry-pgsql-oidc:latest
    ports:
      - "8080:8080"
    environment:
      # Database Configuration
      QUARKUS_DATASOURCE_REACTIVE_URL: vertx-reactive:postgresql://postgres:5432/registry_db
      QUARKUS_DATASOURCE_USERNAME: dbuser
      QUARKUS_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      QUARKUS_DATASOURCE_REACTIVE_MAX_SIZE: 20
      
      # OIDC Configuration
      QUARKUS_OIDC_AUTH_SERVER_URL: https://keycloak:8443/realms/myrealm
      QUARKUS_OIDC_CLIENT_ID: my-client
      QUARKUS_OIDC_CREDENTIALS_SECRET: ${OIDC_SECRET}
      REGISTRY_AUTH_OIDC_ROLE_CLAIM_PATH: group,realm_access.roles
      
      # Application Configuration
      AUTOCOMPLETION_ENABLED_FOR: commodityCode,facilitiesId,dataCarrierTypes
      REGISTRY_UPDATE_STRATEGY: MODIFY
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
      POSTGRES_DB: registry_db
      POSTGRES_USER: dbuser
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  postgres-data:
```

#### Docker Compose (MariaDB)
```yaml
version: '3.8'

services:
  registry:
    image: ghcr.io/cirpass-2/dpp-metadata-registry-mariadb-oidc:latest
    ports:
      - "8080:8080"
    environment:
      # Database Configuration
      QUARKUS_DATASOURCE_REACTIVE_URL: vertx-reactive:mysql://mariadb:3306/registry_db
      QUARKUS_DATASOURCE_USERNAME: dbuser
      QUARKUS_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      QUARKUS_DATASOURCE_REACTIVE_MAX_SIZE: 20
      
      # OIDC Configuration
      QUARKUS_OIDC_AUTH_SERVER_URL: https://keycloak:8443/realms/myrealm
      QUARKUS_OIDC_CLIENT_ID: my-client
      QUARKUS_OIDC_CREDENTIALS_SECRET: ${OIDC_SECRET}
      REGISTRY_AUTH_OIDC_ROLE_CLAIM_PATH: group,realm_access.roles
      
      # Application Configuration
      AUTOCOMPLETION_ENABLED_FOR: commodityCode,facilitiesId,dataCarrierTypes
      REGISTRY_UPDATE_STRATEGY: MODIFY
      REGISTRY_UPI_FIELD_NAME: upi
      REGISTRY_ROLE_MAPPINGS: keycloak_admin:admin,keycloak_eu:eu,keycloak_operator:eo
      REGISTRY_JSON_SCHEMA_LOCATION: /etc/registry/custom-schema.json
    depends_on:
      - mariadb
    volumes:
      - ./custom-schema.json:/etc/registry/custom-schema.json:ro

  mariadb:
    image: mariadb:11
    environment:
      MARIADB_DATABASE: registry_db
      MARIADB_USER: dbuser
      MARIADB_PASSWORD: ${DB_PASSWORD}
      MARIADB_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    volumes:
      - mariadb-data:/var/lib/mysql
    ports:
      - "3306:3306"

volumes:
  mariadb-data:
```

#### Kubernetes Deployment
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: registry-config
data:
  QUARKUS_DATASOURCE_REACTIVE_URL: "vertx-reactive:postgresql://postgres-service:5432/registry_db"
  QUARKUS_DATASOURCE_USERNAME: "dbuser"
  QUARKUS_DATASOURCE_REACTIVE_MAX_SIZE: "20"
  QUARKUS_OIDC_AUTH_SERVER_URL: "https://keycloak.example.com/realms/myrealm"
  QUARKUS_OIDC_CLIENT_ID: "my-client"
  REGISTRY_AUTH_OIDC_ROLE_CLAIM_PATH: "group,realm_access.roles"
  AUTOCOMPLETION_ENABLED_FOR: "commodityCode,facilitiesId,dataCarrierTypes"
  REGISTRY_UPDATE_STRATEGY: "MODIFY"
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
  QUARKUS_DATASOURCE_PASSWORD: "dbpass"
  QUARKUS_OIDC_CREDENTIALS_SECRET: "my-secret"

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
        image: ghcr.io/cirpass-2/dpp-metadata-registry-pgsql-oidc:latest
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
    "backupURL",
    "commodityCode",
    "dataCarrierTypes",
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
        "type": "string",
        "maxLength": 250
      },
      "description": "Facility ID where the product is manufactured/assembled",
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
      "type": "string",
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
    },
    "deactivated": {
      "type": ["boolean", "null"],
      "description": "True if the DPP has been deactivated, false/null otherwise. The property must be present if the granularityLevel equals = ITEM",
      "examples": [true, false, null]
    }
  },
  "if": {
    "properties": {
      "granularityLevel": { "const": "ITEM" }
    }
  },
  "then": {
    "required": ["deactivated"]
  },
  "else": {
    "not": {
      "required": ["deactivated"]
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
  - `MODIFY`: overwrites the existing entry
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
  "backupURL": "https://dpp.example.com/product/backup/12345",
  "commodityCode": "85176200",
  "dataCarrierTypes": ["QR_CODE", "RFID"],
  "granularityLevel": "ITEM",
  "deactivated": false
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
    "backupURL": "https://dpp.example.com/product/backup/12345",
    "commodityCode": "85176200",
    "dataCarrierTypes": ["QR_CODE", "RFID"],
    "granularityLevel": "ITEM",
    "deactivated": false
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