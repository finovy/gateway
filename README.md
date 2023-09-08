Gateway

English | [中文版](README_CN.md)

- [Project Overview](#project-overview)
- [Runtime Environment](#runtime-environment)
- [Key Features](#key-features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration Reference](#configuration-reference)
    - [1. `gateways.yaml`](#1-gatewaysyaml)
    - [2. `gateway-routes.json`](#2-gateway-routesjson)
    - [3. gateway-skip-url.json (Optional)](#3-gateway-skip-urljson-optional)
    - [4. gateway-host.json (Optional)](#4-gateway-hostjson-optional)
    - [5. gateway-degrade.json (optional)](#5-gateway-degradejson-optional)
- [Frequently Asked Questions](#frequently-asked-questions)

## Project Overview

Welcome to our project—an enhanced high-performance gateway built on Spring Cloud Gateway. This gateway serves as the prime entry point for your system's traffic, offering an array of powerful functionalities including proxying, routing, load balancing, health checks, and robust authentication. Notably, it empowers you to tailor your route configurations through our dynamic configuration center, enabling seamless and real-time updates to your routes.

## Runtime Environment

- Java 17+
- Spring Cloud 4.0.4
- Spring Cloud Alibaba 2022
- Disruptor 3.4.4
- Sentinel 1.8.6
- Nacos 2.2.2

## Key Features

- **Customizable Routing**: Flexibility to define routes that perfectly match your requirements.
- **User-Defined TraceId Generation**: Empower your trace identification process with custom prefixes.
- **Hot-Reloadable Routes via Configuration Center**: Swiftly update your routes on-the-fly using the configuration center.
- **Multi-Tenant Configuration Support**: Seamlessly manage configurations for multiple tenants.
- **Integrated with Sentinel for Flow Control and Circuit Breaking**: Harness the power of Sentinel for seamless flow control and circuit breaking capabilities.
- **Integrated with Swagger for Aggregating Downstream API Documentation**: Effortlessly aggregate and document downstream APIs with Swagger.

## Project Structure

```txt
gateway
├─ gateway-bootstrap            : Project startup class
├─ gateway-config               : Configuration Center
│   └─ gateway-config-nacos     : Configuration Center Nacos implementation
├─ gateway-core                 : Gateway Core Implementation Layer
├─ gateway-discovery            : Service Discovery
│   └─ gateway-discovery-nacos  : Service Discovery Nacos implementation
├─ gateway-disruptor            : Disruptor Extension
├─ gateway-transfer             : Gateway Transport Layer
│   └─ gateway-http             : Gateway Transport HTTP implementation
└─ script                       : Collection of Scripts
    └─ docker                   : Docker Image Build Scripts
```

## Getting Started

1. **Configure Application Files**: Start by configuring application files in the configuration center, such as `gateway.yaml`. These files allow you to manage dynamic parameters post-service startup.
2. **Customize Your Routes**: Tailor your routing experience by configuring the `gateway-routes.json` file in the configuration center. Dive into the Configuration Reference for detailed guidance.
3. **Seamless Integration**: Clone our repository and smoothly integrate your configuration center details into `gateway-bootstrap/src/main/resources/environment/application.yaml`. Alternatively, configure these details within your project's environment variables.
4. **Launch and Thrive**: Fire up your `tech.finovy.gateway.GatewayBackendApplication` and embark on your high-performance journey.

## Configuration Reference

#### 1. `gateways.yaml`

```yaml
gateway:
  route:
    data-id:
      # Control the files read by the router.
      route: gateway-routes.json
      skip-url: gateway-skip-url.json
  auth:
    default-appid: GATEWAY-01
  exception:
    # Control whether downstream anomalies are thrown to the page.
    throw-error-to-page: true
  trace:
    # TraceId prefix.
    trace-id-prefix: K
    # Open trace.
    enable: true
  api-doc:
    # Open Swagger documentation.
    enable: true
```

#### 2. `gateway-routes.json`

**Location:** Configuration Center

**Function:** Used for configuring dynamic service routing.

Example:

```json
{
    "globalFilters": [
        {
            "apply": [
                "default_filter"
            ],
            "args": {
                "methods": [
                    "GET",
                    "DELETE"
                ],
                "retries": "5",
                "statuses": [
                    "BAD_GATEWAY",
                    "GATEWAY_TIMEOUT"
                ]
            },
            "name": "Retry"
        }
    ],
    "globalpredicates": {
        "apply": [
            "common-user"
        ],
        "notApply": []
    },
    "routeList": [
        {
            "filters": [
                {
                    "name": "PreserveHostHeader"
                }
            ],
            "id": "common-user",
            "metadata": {
                "optionName": "OptionValue"
            },
            "order": 10,
            "predicates": [
                {
                    "name": "Path",
                    "values": [
                        "/**"
                    ]
                }
            ],
            "uri": "lb://common-user"
        }
    ]
}
```
**Configuration Details:**

| Parameter              | Description                                          | Required | Type   |
| ---------------------- | --------------------------------------------------- | -------- | ------ |
| globalFilter.apply     | Enabled filters                                     | No       | string |
| globalFilter.notApply  | Disabled filters                                    | No       | string |
| globalFilter.args      | Filter parameters                                   | No       | Map    |
| globalpredicates.apply | Enabled route list (all enabled if empty)           | No       | Set    |
| globalpredicates.notApply | Disabled route list                              | No       | Set    |
| routeList.id           | Unique identifier for this route configuration     | Yes      | string |
| routeList.predicates   | Predicate configuration (e.g., "After", "Before")  | Yes      | List   |
| routeList.filters      | Filter configuration (refer to [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-header-route-predicate-factory)) | No | List |
| routeList.metadata     | Metadata                                            | No       | Map    |
| routeList.uri          | Downstream resource address (http, https, lb)       | Yes      | string |
| routeList.order        | Priority (lower value indicates higher priority)    | Yes      | int    |


#### 3. gateway-skip-url.json (Optional)

**Location:** Configuration Center

**Functionality:** Determines whether a service should skip request authentication.

**Example:**

```json
{
  "directPass": false,
  "skipList": [
    {"url": "*.ico"},
    {"url": "*.js", "skipLog": true, "skipAuth": true},
    {"url": "*.css", "skipLog": true, "skipAuth": true}
  ],
  "contentTypes": [
    {"contentType": "text/html", "skipResponse": false},
    {"contentType": "text/plain", "skipResponse": false},
    {"contentType": "text/css", "skipResponse": true},
    {"contentType": "application/javascript", "skipResponse": true},
    {"contentType": "font", "skipResponse": true},
    {"contentType": "image", "skipResponse": true}
  ]
}
```

**Configuration Details:**

| Parameter               | Description              | Data Type | Default Value | Required |
| ----------------------- | ------------------------ | --------- | ------------- | -------- |
| skips.url               | Request resource address | string    |             | No       |
| skips.directPass        | Directly pass (Not yet implemented) | boolean | false | No       |
| skips.skipLog           | Skip log recording       | boolean   | false         | No       |
| skips.skipRefreshListener | Skip token refresh (Not yet implemented) | boolean | false | No       |
| skips.skipAuth          | Skip authentication     | boolean   | false         | No       |
| skips.skipRequest       | Skip request            | boolean   | false         | No       |
| skips.skipEncry         | Skip encryption (Not yet implemented) | boolean | false | No       |
| skips.skipDecry         | Skip decryption (Not yet implemented) | boolean | false | No       |
| skips.decryType         | Decryption type (Not yet implemented) | string  |       | No       |
| contentType.contentType | Request content type    | string    |             | No       |
| contentType.skipLog     | Skip log recording      | boolean   | false         | No       |
| contentType.skipResponse | Skip response (Not yet implemented) | boolean | false | No       |


#### 4. gateway-host.json (Optional)

**Configuration Location:** Configuration Center

**Functionality:** Determines if authentication is required for a specific Host.

**Example:**

```json
{
    "directPass": false,
    "hosts": [
        {
            "appId": "GATEWAY-01",
            "directPass": false,
            "exists": false,
            "host": "https://google.com",
            "order": 0,
            "validatorToken": true
        }
    ],
    "validatorToken": true
}
```
**Configuration Details:**

| Parameter             | Description         | Data Type | Default Value | Required |
| -------------------- | ------------------- | --------- | ------------- | -------- |
| directPass           | Direct pass         | boolean   | false         | No       |
| hosts.appId          | Direct pass (not yet implemented) | boolean   | false         | No       |
| hosts.directPass     | Direct pass         | boolean   | false         | No       |
| hosts.host           | Control address     | string    |             | No       |
| hosts.order          | Order, smaller values indicate higher priority | int       |             | No       |
| hosts.validatorToken | Validate token      | boolean   | false         | No       |
| validatorToken       | Skip log records    | boolean   | false         | No       |

#### 5. gateway-degrade.json (optional)

**Location:** Configuration Center

**Function:** Custom response for Sentinel flow control.

**Example:**

```json
{
   "config": [
      {
         "degradeApi": "lb://common-user",
         "degradeCode": 430,
         "degradeMessage": "Too many requests Wraning",
         "routeId": "common-user"
      }
   ],
   "degradeApi": "lb://common-user-degrade",
   "degradeCode": 429,
   "degradeMessage": "Too many requests"
}
```

**Configuration Details:**

| Parameter            | Description           | Data Type | Default Value | Required |
| --------------------- | --------------------- | --------- | ------------- | -------- |
| degradeCode          | Default degradation response code | int       | 0             | No       |
| degradeMessage       | Default degradation response message | string    |               | No       |
| degradeApi           | Default degradation response API | string    |               | No       |
| paramFlowCode        | Flow control response code | int       | 0             | No       |
| paramFlowMessage     | Flow control response message | string    |               | No       |
| systemBlockCode      | System interruption response code | int       | 0             | No       |
| systemBlockMessage   | System interruption response message | string    |               | No       |
| paramFlowApi         | Flow control degradation request API | string    |               | No       |
| systemBlockApi       | System interruption degradation request API | string    |               | No       |
| config.degradeCode   | Custom degradation response code | int       | 0             | No       |
| config.degradeMessage| Custom degradation message | string    |               | No       |
| config.routeId       | Bind degradation route | string    |               | No       |
| config.degradeApi    | Degradation request API | string    |               | No       |

## Frequently Asked Questions
