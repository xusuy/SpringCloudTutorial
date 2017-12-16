# SpringCloud（第 053 篇）CentOS7 中用 Docker 部署一个简单的基于 Eureka 服务治理发现的项目
-

## 一、大致介绍

``` 
1、纠结了一下下，这么简单的部署流程是否需要详细的贴出来，然而纠结了一下还是将这个简单的部署流程补充完整了；
2、经过上节的讲解，相信大家已经对docker的命令操作都有了一定的了解，这里我就暂且默认大家都拥有了可操作的环境以及了解操作指令；
3、本章节基于docker来操作部署SpringCloud项目；
4、注意在利用idea打包生成jar文件时，里面的localhost请改成你的宿主机的ip地址，但是如果统一部署到测试或者生产环境的话，请改为动态配置，方便动态修改，因此我这里做测试的话，首先将代码中的localhost就直接改成宿主机的ip地址，然后再打出jar包；
```


## 二、针对简单用户微服务进行docker部署
### 2.1 利用 idea 对 springms-simple-provider-user 项目进行打包

``` 
1、这个打包就看大家喜好，用命令打包也好，用鼠标操作打包也好，反正目的就是在项目的target目录下生成jar文件即可；

2、本人这里使用ideaIDE开发工具，就沿着 Maven Project -> springms-simple-provider-user -> Lifecycle -> package 路径双击 package 即可；

3、然后就会看到项目中 springms-simple-provider-user/target 下多了一个 springms-simple-provider-user-1.0-SNAPSHOT.jar 文件；
```

### 2.2 上传 springms-simple-provider-user-1.0-SNAPSHOT.jar 至 linux 服务器

``` 
1、本人在Linux下喜欢操作命令，所以这里就给大家演示一下怎么用SecureCRT执行命令将文件传到服务器；

2、首先登录上服务器就不用说了，然后在SecureCRT中操作 “alt+p” 快捷键操作，此时将会打开一个新的页签，页签的第一行则是 “sftp>” 这个字样，说明已经进入了上传文件的命令窗口了；

3、找到需要将文件放置到服务器的位置；
	sftp> cd /home/docker/demo
	sftp> put D:\ANDROID\Code\Spring-Cloud\SpringCloudTutorial\springms-simple-provider-user\target\springms-simple-provider-user-1.0-SNAPSHOT.jar
	Uploading springms-simple-provider-user-1.0-SNAPSHOT.jar to /home/docker/demo/springms-simple-provider-user-1.0-SNAPSHOT.jar
	  100% 31995KB   1333KB/s 00:00:24     
	D:/ANDROID/Code/Spring-Cloud/SpringCloudTutorial/springms-simple-provider-user/target/springms-simple-provider-user-1.0-SNAPSHOT.jar: 32763475 bytes transferred in 24 seconds (1333 KB/s)
	sftp> 

4、然后切换到刚刚敲 “alt+p” 快捷键的那个窗口，执行命令查看文件是否上传成功
	[root@svr01 ~]#  ls /home/docker/demo
	springms-simple-provider-user-1.0-SNAPSHOT.jar
```


### 2.3 编写并保存 Dockerfile 文件

``` 
1、进入 /home/docker/demo
	[root@svr01 ~]#  cd /home/docker/demo

2、采用 vim或者vi 命令创建 Dockerfile 文件，并输入文件内容
	[root@svr01 demo]#  vim Dockerfile
	FROM frolvlad/alpine-oraclejdk8:slim
	VOLUME /tmp
	ADD springms-simple-provider-user-1.0-SNAPSHOT.jar app.jar
	RUN sh -c 'touch /app.jar'
	ENV JAVA_OPTS=""
	ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

3、保存 Dockerfile 文件内容，执行命令顺序如下，命令的先后执行顺序为：
	Esc
	Shift+:
	wq
	Enter
```

### 2.4 用 docker build 命令将给定的Dockerfile和上下文以构建Docker镜像

``` 
1、构建镜像
	[root@svr01 demo]# docker build -t springms-simple-provider-user .
	Sending build context to Docker daemon 32.77 MB
	Step 1 : FROM frolvlad/alpine-oraclejdk8:slim
	 ---> 4f03dc990224
	Step 2 : VOLUME /tmp
	 ---> Running in dfd09d567767
	 ---> f76843b950c7
	Removing intermediate container dfd09d567767
	Step 3 : ADD springms-simple-provider-user-1.0-SNAPSHOT.jar app.jar
	 ---> e009af0cc3bc
	Removing intermediate container 0884992be7c0
	Step 4 : RUN sh -c 'touch /app.jar'
	 ---> Running in 4fb67225860f
	 ---> 06320de95ea3
	Removing intermediate container 4fb67225860f
	Step 5 : ENV JAVA_OPTS ""
	 ---> Running in 519c55360fda
	 ---> bb2377cac425
	Removing intermediate container 519c55360fda
	Step 6 : ENTRYPOINT sh -c java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar
	 ---> Running in 95eb606d0e57
	 ---> 2aaa88be3d65
	Removing intermediate container 95eb606d0e57
	Successfully built 2aaa88be3d65

2、查看产生的镜像
	[root@svr01 demo]# docker images
	REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
	springms-simple-provider-user          latest              2aaa88be3d65        6 seconds ago       235.6 MB
	docker.io/centos                       v2                  11efb35f320c        7 hours ago         307.5 MB
	docker.io/frolvlad/alpine-oraclejdk8   latest              4f03dc990224        12 days ago         170.1 MB
	frolvlad/alpine-oraclejdk8             slim                4f03dc990224        12 days ago         170.1 MB
	docker.io/centos                       latest              3fa822599e10        2 weeks ago         203.5 MB
	docker.io/hello-world                  latest              f2a91732366c        3 weeks ago         1.848 kB
```

### 2.5 启动刚刚构建好的 docker 镜像

``` 
[root@svr01 demo]# docker run -p 8000:8000 -t springms-simple-provider-user

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.4.1.RELEASE)
 。。。。
 【【【【【【 简单用户微服务 】】】】】】已启动.
```

### 2.6 测试docker部署springcloud项目是否成功

``` 
1、还记得我么在电脑本机测试的地址么，地址为：http://localhost:8000/simple/1 ?
2、现在目前我在阿里云上部署操作，所以这个localhost我就需要换成阿里云的ip地址，总而言之这个localhost换成你docker所在服务器的宿主机ip地址准没错；

```

### 2.7 尝试停止刚才docker部署的项目

``` 
[root@svr01 docker]# docker ps -l
CONTAINER ID        IMAGE                           COMMAND                  CREATED             STATUS              PORTS                    NAMES
3a6393ce51b8        springms-simple-provider-user   "sh -c 'java $JAVA_OP"   8 minutes ago       Up 8 minutes        0.0.0.0:8000->8000/tcp   sick_shannon
[root@svr01 docker]# docker stop 3a6393ce51b8
3a6393ce51b8
[root@svr01 docker]# docker ps -l
CONTAINER ID        IMAGE                           COMMAND                  CREATED             STATUS                       PORTS               NAMES
3a6393ce51b8        springms-simple-provider-user   "sh -c 'java $JAVA_OP"   8 minutes ago       Exited (137) 3 seconds ago                       sick_shannon
[root@svr01 docker]# 
```

### 2.8 小结

``` 
到此为止，我们已经将我们打包好的一个镜像成功部署了，并且可以成功在浏览器访问到输出数据；
```




## 三、用docker部署一个基于Eureka服务治理发现的项目
### 3.1 从 [SpringCloudTutorial](https://git.oschina.net/ylimhhmily/SpringCloudTutorial.git) 中挑选项目

``` 
1、springms-discovery-eureka 
2、springms-provider-user
3、springms-consumer-movie-feign
```

### 3.2 依次对刚刚选出来的3个项目进行打包，并产生的文件如下，打包前记得将localhost的记得改为宿主机ip地址

``` 
1、springms-discovery-eureka-1.0-SNAPSHOT.jar
2、springms-provider-user-1.0-SNAPSHOT.jar
3、springms-consumer-movie-feign-1.0-SNAPSHOT.jar
```

### 3.3 在服务器上构建docker项目目录

``` 
[root@svr01 ~]# cd /home/docker/
[root@svr01 docker]# mkdir -p springms-consumer-movie-feign springms-discovery-eureka springms-provider-user
[root@svr01 docker]# ll
total 3
drwxr-xr-x 2 root root   76 Dec 15 12:59 demo
drwxr-xr-x 2 root root   76 Dec 15 13:44 springms-consumer-movie-feign
drwxr-xr-x 2 root root   72 Dec 15 13:09 springms-discovery-eureka
drwxr-xr-x 2 root root   69 Dec 15 13:43 springms-provider-user
[root@svr01 docker]# 
```

### 3.4 将步骤 3.2 的三个jar文件按照步骤 2.2 的方式上传到对应服务器目录去

``` 
sftp> cd /home/docker/springms-discovery-eureka
sftp> put D:\ANDROID\Code\Spring-Cloud\SpringCloudTutorial\springms-discovery-eureka\target\springms-discovery-eureka-1.0-SNAPSHOT.jar
Uploading springms-discovery-eureka-1.0-SNAPSHOT.jar to /home/docker/springms-discovery-eureka/springms-discovery-eureka-1.0-SNAPSHOT.jar
  100% 31595KB   1333KB/s 00:00:24     
D:/ANDROID/Code/Spring-Cloud/SpringCloudTutorial/springms-discovery-eureka/target/springms-discovery-eureka-1.0-SNAPSHOT.jar: 32776474 bytes transferred in 28 seconds (1383 KB/s)
sftp> 
sftp> 
sftp> 
sftp> cd /home/docker/springms-provider-user
sftp> put D:\ANDROID\Code\Spring-Cloud\SpringCloudTutorial\springms-provider-user\target\springms-provider-user-1.0-SNAPSHOT.jar
Uploading springms-discovery-eureka-1.0-SNAPSHOT.jar to /home/docker/springms-provider-user/springms-provider-user-1.0-SNAPSHOT.jar
  100% 31565KB   1333KB/s 00:00:24     
D:/ANDROID/Code/Spring-Cloud/SpringCloudTutorial/springms-provider-user/target/springms-provider-user-1.0-SNAPSHOT.jar: 32763789 bytes transferred in 27 seconds (1313 KB/s)
sftp> 
sftp> 
sftp> 
sftp> cd /home/docker/springms-consumer-movie-feign
sftp> put D:\ANDROID\Code\Spring-Cloud\SpringCloudTutorial\springms-consumer-movie-feign\target\springms-consumer-movie-feign-1.0-SNAPSHOT.jar
Uploading springms-discovery-eureka-1.0-SNAPSHOT.jar to /home/docker/springms-consumer-movie-feign/springms-consumer-movie-feign-1.0-SNAPSHOT.jar
  100% 32195KB   1333KB/s 00:00:24     
D:/ANDROID/Code/Spring-Cloud/SpringCloudTutorial/springms-consumer-movie-feign/target/springms-consumer-movie-feign-1.0-SNAPSHOT.jar: 32712375 bytes transferred in 25 seconds (1413 KB/s)
```

### 3.5 查看三个文件是否传至服务器成功

``` 
[root@svr01 docker]# ll
total 4
drwxr-xr-x 2 root root   76 Dec 15 12:59 demo
drwxr-xr-x 2 root root   76 Dec 15 13:44 springms-consumer-movie-feign
drwxr-xr-x 2 root root   72 Dec 15 13:09 springms-discovery-eureka
drwxr-xr-x 2 root root   69 Dec 15 13:43 springms-provider-user
[root@svr01 docker]# ls springms-discovery-eureka/
springms-discovery-eureka-1.0-SNAPSHOT.jar
[root@svr01 docker]# ls springms-provider-user/
springms-provider-user-1.0-SNAPSHOT.jar
[root@svr01 docker]# ls springms-consumer-movie-feign/
springms-consumer-movie-feign-1.0-SNAPSHOT.jar
[root@svr01 docker]# 
```

### 3.6 分别给3个模块构建Dockfile文件

``` 
[root@svr01 ~]#  cd /home/docker/springms-discovery-eureka
[root@svr01 springms-discovery-eureka]#  vim Dockerfile
FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD springms-discovery-eureka-1.0-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
保存Dockfile文件

[root@svr01 springms-discovery-eureka]#
[root@svr01 springms-discovery-eureka]# cd /home/docker/springms-provider-user
[root@svr01 springms-provider-user]#  vim Dockerfile
FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD springms-provider-user-1.0-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
保存Dockfile文件

[root@svr01 springms-provider-user]# cd /home/docker/springms-consumer-movie-feign
[root@svr01 springms-consumer-movie-feign]# vim Dockerfile
FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD springms-consumer-movie-feign-1.0-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
保存Dockfile文件

[root@svr01 springms-consumer-movie-feign]# cd ..
[root@svr01 docker]# ls springms-discovery-eureka/
Dockerfile  springms-discovery-eureka-1.0-SNAPSHOT.jar
[root@svr01 docker]# ls springms-provider-user/
Dockerfile  springms-provider-user-1.0-SNAPSHOT.jar
[root@svr01 docker]# ls springms-consumer-movie-feign/
Dockerfile  springms-consumer-movie-feign-1.0-SNAPSHOT.jar
[root@svr01 docker]# 
```

### 3.7 分别对3个模块进行镜像构建

``` 
[root@svr01 docker]# cd springms-discovery-eureka
[root@svr01 springms-discovery-eureka]# docker build -t springms/discovery-eureka .
[root@svr01 springms-discovery-eureka]# cd ../springms-provider-user
[root@svr01 springms-provider-user]# docker build -t springms/provider-user .
[root@svr01 springms-provider-user]# cd ../springms-consumer-movie-feign
[root@svr01 springms-consumer-movie-feign]# docker build -t springms/consumer-movie-feign .
[root@svr01 springms-consumer-movie-feign]# cd ..
[root@svr01 docker]# docker images
REPOSITORY                            	TAG                 IMAGE ID            CREATED             SIZE
springms/consumer-movie-feign           latest              f6e9ac5c27a7        19 hours ago        507.5 MB
springms/provider-user                  latest              d2b16dc2df77        19 hours ago        535.2 MB
springms/discovery-eureka            	latest              f0f8555dcd0b        19 hours ago        517.1 MB

```

### 3.8 分别手动启动3个模块

``` 
[root@svr01 docker]# docker run -p 8761:8761 -t -d springms/discovery-eureka
[root@svr01 docker]# docker run -p 7900:7900 -t -d springms/provider-user
[root@svr01 docker]# docker run -p 7910:7910 -t -d springms/consumer-movie-feign
```

### 3.9 测试

``` 
1、首先进入eureka注册中心：http://宿主机IP地址:8761，结果看到2个微服务已经注册上来了，若还没出现的话，稍微等待一下下；
2、测试 http://宿主机IP地址:7910/movie/4，结果可以看到信息输出；
3、说明上述相关的docker部署操作准确无误，一切正常；
```

### 3.10 后续展望

``` 
既然上面提到了手动部署操作，那么服务多的话，一个个手动部署，岂不是累死去啊，那么在后续的章节中，陆续给大家讲解采用docker服务编排部署操作；
```


## 四、下载地址

[https://git.oschina.net/ylimhhmily/SpringCloudTutorial.git](https://git.oschina.net/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























