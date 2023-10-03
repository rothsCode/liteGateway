# liteGateway

##网关介绍
liteGateway是一款异步,较高性能的轻量级网关。具有较低的学习成本，可作为学习开发用途。

##网关功能
> 1. 路由转发：支持Apache Dubbo，Http路由转发
> 2. 注册中心:支持从Nacos注册中心进行服务发现
> 3. 流量管控: 支持单机和redis分布式限流,限流维度包括全局,url,ip等细粒度多级限流
> 4. 日志监控: 网关日志支持kafka,es进行日志上报
> 5. 配置中心: 支持接入Nacos配置中心,即时刷新配置
> 6. 性能metrics: 支持prometheus进行指标统计
> 7. 服务鉴权: 可通过插件实现自定义接口鉴权
> 8. 黑白名单：支持远程配置通过绑定黑、白名单限制访问
> 9. HTTP反向代理: 支持对接口进行反向代理
##网关架构设计
网关核心主要由HandlerEvent和Plugin两个基类作为底座
网关请求采用责任链设计模式处理请求,可针对业务灵活增减事件
> 1. HandlerEvent: 作请求事件处理基类,具体事件通过继承该类实现自定义逻辑，则可对请求执行相应处理。
> 2. Plugin: 插件基类,基于spi机制以及指定插件名即可获取指定插件实现类

##网关高性能之道
> 1. 对netty设置了batchFlush优化写性能,高水位校验以及优化相关参数
> 2. 请求异步化 配置数据缓存化 针对请求调用以及日志记录采用disruptor高性能内存队列
> 3. 针对在鉴权,限流，路由场景下url,ip循环匹配问题采用radixTree数据结构,优化了相应性能以及内存压力


##网关项目模块介绍
- **client 客户端模块** 后续实现
- **common 公共模块**
- **core 核心逻辑模块**
- **spring starter接入模块** 后续实现
- **test 单元测试模块**


## 基准测试
-8核16g服务器  wrk服务器
-8核16g服务器  网关应用服务器

|         分类          |         产品          | 1000并发<br/>QPS     | 1000并发<br/>90% Latency(ms) |
| :------------------ | :------------------  | :-------: | :-------: |

| 后端服务 | 直接访问后端服务    | 55597| 21 |

| 应用网关 | liteGateway      | 15318 | 64|

| 应用网关 | spring-cloud-gateway | 13604 | 126 |

<img src="https://github.com/rothsCode/liteGateway/blob/main/apiWrk.png" alt="liteGateway压测结果" width="50%" />

<img src="https://github.com/rothsCode/liteGateway/blob/main/clipboard.png" alt="springCloudGateway压测结果" width="50%" />

<img src="https://github.com/rothsCode/liteGateway/blob/main/apiWrk.png" alt="接口直接调用压测结果" width="50%" />

##开发指南

> 1. 路由:默认路由为http服务调用路由,即常规springCloud架构,默认路由规则取url第一个path作为服务名,即/order/xxx
取order作为服务名。从而进行服务发现。如果是dubbo路由则在请求头上加上routeType:dubbo标识信息,如果是url代理
则加上routeType:urlProxy。相应的路由规则在nacos动态配置配置,目前url匹配支持精确匹配以及/xxx/**匹配,ip匹配支持单个ip精确匹配以及
cidr ip段匹配例如192.168.2.0/24
> 2. 配置信息参照application-dev.properties,正式配置可命名为application-pro.properties,支持nacos动态配置,动态配置模板参见如下

> 1. whitePathList: url白名单
> 1. whiteIpList:   ip白名单
> 1. blackIpList:   ip黑名单
> 1. dubboRouteRules: dubbo路由规则
> 1. proxyRouteRules: url代理路由规则
> 1. httpServiceRouteRules:  http服务调用路由规则
> 1. flowRules: 流控规则

> 动态配置模板. {
    "whitePathList":[
        "/crm-dms/v1/capital/capital/**"
    ],
    "whiteIpList":[
        "10.2.9.17",
        "10.2.9.19"
    ],
    "blackIpList":[
        "10.2.9.17",
        "10.2.9.19"
    ],
    "dubboRouteRules":[
        {
            "apiPath":"dfffffff",
            "interfaceName":"com.wzb.service.TestService",
            "version":"1.0.0",
            "methodName":"ins",
            "paramTypes":[
                "java.lang.String",
                "java.lang.String"
            ]
        }
    ],
    "proxyRouteRules":[
        {
            "apiPath":"/wrktest/testGet/**",
            "hostName":"localhost",
            "port":8083
        }
    ],
     "httpServiceRouteRules":[
        {
            "apiPath":"/crm-xxx/**",
            "serviceName":"crm-xxx"
        }
    ],
    "flowRules":[
        {
            "resourceName":"global",
            "resourceValue":"global",
            "resourceType":"global",
            "rateLimitType":"memory",
            "maxPermits":100000,
            "maxWaitingRequests":1000
        },
        {
            "resourceName":"apiPath",
            "resourceValue":"/wrktest/**",
            "resourceType":"url",
            "rateLimitType":"memory",
            "maxPermits":2,
            "maxWaitingRequests":2
        },
           {
            "resourceName":"apiPath",
            "resourceValue":"/crm-xxx/**",
            "resourceType":"url",
            "rateLimitType":"redis",
            "maxPermits":2,
            "maxWaitingRequests":2
        },
        {
            "resourceName":"ip限流",
            "resourceValue":"10.2.9.17",
            "resourceType":"ip",
            "rateLimitType":"memory",
            "maxPermits":1,
            "maxWaitingRequests":1
        }
    ]
}

