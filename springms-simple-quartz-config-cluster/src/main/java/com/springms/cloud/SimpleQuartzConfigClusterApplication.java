package com.springms.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 简单 Quartz-Cluster 微服务，采用注解配置 Quartz 分布式集群。
 *
 * @author hmilyylimh
 *
 * @version 0.0.1
 *
 * @date 2017/12/17
 *
 */
@SpringBootApplication
public class SimpleQuartzConfigClusterApplication {

	private static final Logger Logger = LoggerFactory.getLogger(SimpleQuartzConfigClusterApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SimpleQuartzConfigClusterApplication.class, args);

		System.out.println("【【【【【【 简单Quartz-Config-Cluster微服务 】】】】】】已启动.");
	}
}




/****************************************************************************************
 一、简单 Quartz-Cluster 微服务，采用注解配置 Quartz 分布式集群：

 1、添加 Quartz 相关配置文件；
 2、启动 springms-simple-quartz-config-cluster 模块服务，启动3个端口（8405、8406、8407）；
 3、然后看到 3 台服务器只有 1 台服务器调用了 Job1 类中的方法，因此 Quartz 的集群分布式也算是部署成功了；
 4、然后关闭 1 台活跃 Quartz 服务器；
 5、再等一会儿就看到 2 台服务器中的 1 台服务器每隔一定的时间调用 Job1 类中的方法；
 ****************************************************************************************/


