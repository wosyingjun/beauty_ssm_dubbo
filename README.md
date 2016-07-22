#将"优雅的SSM框架"拆分为分布式架构(基于dubbo+zookeeper)
- Maven（模块化构建）
- Spring（IOC DI 声明式事务处理）
- SpringMVC（支持Restful风格）
- Hibernate Validate（参数校验）
- Mybatis（最少配置方案）
- Quartz时间调度
- Redis缓存（ProtoStuff序列化）
- [Redis Sentinel主从高可用方案](http://wosyingjun.iteye.com/blog/2289593)
- [Druid（数据源配置 sql防注入 sql性能监控)](http://wosyingjun.iteye.com/blog/2306139)
- **Dubbo+Zookeeper分布式服务框架**
- **合理的分布式服务划分（common+api+service+web）**
- **资源调度和治理中心(SOA)（dubbo-admin）**
- **服务监控方案(dubbo-monitor)**
- **分布式系统中的异常处理**
- **分布式事务（待完善）**
- **分布式锁（待完善）**
- **dubbo服务集群、负载均衡策略（待完善）**
- **Redis集群高可用方案（待完善）**
- **Zookeeper集群高可用方案（待完善）**
- **消息中间件 ActiveMQ 的引入（待完善）**
- **ActiveMQ 集群高可用方案（待完善）**
- **分布式文件系统（FastDFS）的引入（待完善）**
- **FastDFS集群高可用方案（待完善）**

###**架构图：**
![](http://i.imgur.com/q46ieis.png)