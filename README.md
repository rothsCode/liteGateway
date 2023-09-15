# liteGateway

##网关介绍
liteGateway是一款异步,较高性能的轻量级网关。具有较低的学习成本，可作为学习开发用途。

##网关功能
> 1. 路由转发：支持Apache Dubbo，Http路由转发
> 2. 注册中心:支持从Nacos注册中心进行服务发现
> 3. 流量管控: 支持单机和redis分布式限流,限流维度包括全局,url,ip,user等细粒度多级限流
> 4. 日志监控: 网关日志支持kafka,es进行日志上报
> 5. 配置中心: 支持接入Nacos配置中心,可即时刷新配置
> 6. 性能metrics: 支持prometheus进行指标统计
> 7. 服务鉴权: 可通过插件实现自定义接口鉴权
> 8. 黑白名单：支持远程配置通过绑定黑、白名单限制访问
> 9. HTTP反向代理: 支持对接口进行反向代理
##网关架构设计
网关核心主要由HandlerEvent和Plugin两个基类作为底座
网关请求采用责任链设计模式处理请求
HandlerEvent: 作请求事件处理基类,具体事件通过继承该类实现自定义逻辑，则可对请求执行相应处理。
Plugin: 插件基类,基于spi机制以及指定插件名即可获取指定插件实现类
高性能实现点: 请求异步调用 对配置及相关数据基于caffeine缓存化,对请求增加
disruptor高性能缓存队列，对netty设置了batchFlush优化写性能,高水位校验以及相关参数

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