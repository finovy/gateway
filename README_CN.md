Gateway

中文版 | [English](README.md)

- [项目介绍](#项目介绍)
- [运行环境](#运行环境)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置参考](#配置参考)
    - [1. gateways.yaml](#1-gatewaysyaml)
    - [2. gateway-routes.json](#2-gateway-routesjson)
    - [3. gateway-skip-url.json(可选)](#3-gateway-skip-urljson可选)
    - [4. gateway-host.json(可选)](#4-gateway-hostjson可选)
    - [5. gateway-degrade.json(可选)](#5-gateway-degradejson可选)
- [常见问题](#常见问题)

## 项目介绍

该项目在Spring Cloud Gateway的基础上进行了拓展，构建成了一个高性能网关。它不仅提供了代理、路由、负载平衡、健康检查、身份验证等功能，还可以作为系统流量的入口层。同时，我们还实现了在配置中心自定义路由文件的功能，从而实现了路由的动态刷新。

## 运行环境

- Java 17 +
- Spring Cloud 4.0.4
- Spring Cloud Alibaba 2022
- Disruptor 3.4.4
- Sentinel 1.8.6
- Nacos 2.2.2

## 功能特性

- 自定义路由

- 自定义生成TraceId

- 支持配置中心热更新路由

- 支持多租户配置

- 集成Sentinel，支持限流熔断

- 集成Swagger，可聚合下游接口文档

## 项目结构

```txt
gateway
├─ gateway-bootstrap            : 项目启动类
├─ gateway-config               : 配置中心
│   └─ gateway-config-nacos     : 配置中心nacos实现
├─ gateway-core                 : 网关核心实现层
├─ gateway-discovery            : 注册中心
│   └─ gateway-discovery-nacos  : 注册中心nacos实现
├─ gateway-disruptor            : Disruptor扩展
├─ gateway-transfer             : 网关传输层
│   └─ gateway-http             : 网关传输http实现
└─ script                       : 脚本集合
    └─ docker                   : docker镜像构建脚本
```

## 快速开始

1. 在配置中心配置应用文件, 例如: gateway.yaml, 用于控制服务启动后的动态参数。

2. 在配置中心配置 gateway-routes.json 文件，内容根据 配置参考 填写。

3. 克隆该项目，将 gateway-bootstrap/src/main/resource/enviroment/application.yaml 相关配置中心信息替换，或者在项目环境变量配置对应的信息。

4. 启动 tech.finovy.gateway.GatewayBackendApplication。

## 配置参考

#### 1. gateways.yaml

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
    # Open swagger doc.
    enable: true
```

#### 2. gateway-routes.json

配置位置: 配置中心

功能说明: 用于服务动态路由配置。

举例:

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

配置说明:

| 参数                        | 描述                                                                                                                                            | 是否必填 | 类型     |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- | ---- | ------ |
| globalFilter.apply        | 启用的filter                                                                                                                                     | 否    | string |
| globalFilter.notApply     | 禁用的filter                                                                                                                                     | 否    | string |
| globalFilter.args         | filter参数                                                                                                                                      | 否    | Map    |
| globalpredicates.apply    | 启用路由列表，为空默认全部启用                                                                                                                               | 否    | Set    |
| globalpredicates.notApply | 禁用路由列表                                                                                                                                        | 否    | Set    |
| routeList.id              | 该路由配置的唯一标识                                                                                                                                    | 是    | string |
| routeList.predicates      | 断言配置，可选 "After", "Before", "Between", "Cookie", "Header", "Host", "Method", "Path", "Query", "RemoteAddr"                                     | 是    | List   |
| routeList.filters         | 过滤器配置 参考: [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-header-route-predicate-factory) | 否    | List   |
| routeList.metadata        |                                                                                                                                               | 否    | Map    |
| routeList.uri             | 下游资源地址, 可选http，https，lb(使用注册中心服务发现)                                                                                                           | 是    | string |
| routeList.order           | 优先级，数字越低优先级越高                                                                                                                                 | 是    | int    |

#### 3. gateway-skip-url.json(可选)

配置位置: 配置中心

功能说明: 用于服务是否跳过请求鉴权配置。

举例:

```json
{
     // default
    "directPass": false,
    "skipAuth": false,
    "skipDecry": false,
    "skipEncry": false,
    "skipLog": false,
    "skipRequest": false,
    "skipResponse": false,
    "skips":[
        {"url":"*.ico"},
        {"url":"*.js","skipLog":true,"skipAuth":true},
        {"url":"*.css","skipLog":true,"skipAuth":true}
    ],
    "contentTypes":[
        {"contentType":"text/html","skipResponse":false},
        {"contentType":"text/plain","skipResponse":false},
        {"contentType":"text/css","skipResponse":true},
        {"contentType":"application/javascript","skipResponse":true},
        {"contentType":"font","skipResponse":true},
        {"contentType":"image","skipResponse":true}
    ]
}

```
配置说明:

| 参数                        | 描述                | 数据类型    | 默认值   | 是否必填 |
| ------------------------- | ----------------- | ------- | ----- | ---- |
| skips.url                 | 请求资源地址            | string  |       | 否    |
| skips.directPass          | 直接放行(暂未实现)        | boolean | false | 否    |
| skips.skipLog             | 跳过日志记录            | boolean | false | 否    |
| skips.skipRefreshListener | 是否跳过刷新token(暂未实现) | boolean | false | 否    |
| skips.skipAuth            | 跳过鉴权              | boolean | false | 否    |
| skips.skipRequest         | 跳过请求              | boolean | false | 否    |
| skips.skipEncry           | 跳过加密(暂未实现)        | boolean | false | 否    |
| skips.skipDecry           | 跳过解密(暂未实现)        | boolean | false | 否    |
| skips.decryType           | 解密方式(暂未实现)        | string  | false | 否    |
| contentType.contentType   | 请求内容类型            | string  |       | 否    |
| contentType.skipLog       | 跳过日志记录            | boolean | false | 否    |
| contentType.skipResponse  | 跳过响应(暂未实现)        | boolean | false | 否    |

#### 4. gateway-host.json(可选)

配置位置: 配置中心

功能说明: 判断某Host是否需要鉴权。

举例:

```json
{
    "directPass": false,
    "hosts": [
        {
            "appId": "GATEWAY-01",
            "directPass": false,
            "exists": false,
            "host": "https://google.com",
            "order": "0",
            "validatorToken": true
        }
    ],
    "validatorToken": true
}
    }
]
```
配置说明:

| 参数                   | 描述         | 数据类型    | 默认值   | 是否必填 |
| -------------------- | ---------- | ------- | ----- | ---- |
| directPass           | 直接放行       | boolean | false | 否    |
| hosts.appId          | 直接放行(暂未实现) | boolean | false | 否    |
| hosts.directPass     | 直接放行       | boolean | false | 否    |
| hosts.host           | 控制地址       | string  |       | 否    |
| hosts.order          | 顺序，越小优先级越高 | int     |       | 否    |
| hosts.validatorToken | 校验token    | boolean | false | 否    |
| validatorToken       | 跳过日志记录     | boolean | false | 否    |

#### 5. gateway-degrade.json(可选)

配置位置: 配置中心

功能说明: Sentinel 流控自定义响应。

举例:

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

配置说明:

| 参数                    | 描述          | 数据类型   | 默认值 | 是否必填 |
| --------------------- | ----------- | ------ | --- | ---- |
| degradeCode           | 默认降级响应码     | int    | 0   | 否    |
| degradeMessage        | 默认降级响应消息    | string |     | 否    |
| degradeApi            | 默认降级响应API   | string |     | 否    |
| paramFlowCode         | 流控响应码       | int    | 0   | 否    |
| paramFlowMessage      | 流控响应消息      | string |     | 否    |
| systemBlockCode       | 系统中断响应码     | int    | 0   | 否    |
| systemBlockMessage    | 系统中断响应消息    | string |     | 否    |
| paramFlowApi          | 流控降级请求API   | string |     | 否    |
| systemBlockApi        | 系统中断降级请求API | string |     | 否    |
| config.degradeCode    | 自定义降级响应码    | int    | 0   | 否    |
| config.degradeMessage | 自定义降级消息     | string |     | 否    |
| config.routeId        | 绑定降级路由      | string |     | 否    |
| config.degradeApi     | 降级请求API     | string |     | 否    |

## 常见问题
