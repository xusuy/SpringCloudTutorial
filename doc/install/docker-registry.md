# SpringCloud（第 055 篇）CentOS7 搭建 docker-registry 私有库及管理界面
-

## 一、大致介绍

``` 
1、基于前面docker的部署，容器一多非常不便于管理，于是急需一个自己的docker私有库；
2、而目前市面上大多数的私有库基本上都是后台服务加前台ui构成，于是选来选去，最后选择了portainer管理界面；
3、之所以选择portainer这款管理界面，我就简述阐述一下，基于以下几点综合考虑而为之：
	3.1 DockerUI 无登录验证，无法权限分配，不支持多主机；
	3.2 Shipyard 支持集群，支持权限分配，启动容器较多，占用每个节点的一部分资源；
	3.3 Portainer 轻量级著称，消耗资源少，虽然功能没有Shipyard强大，但麻雀虽小五脏俱全，满足基本需求；
	3.4 Daocloud 功能强大，部分高级功能收费，安装稍微难度大点，土豪公司无外乎都会选择此项；
4、注意：下面的 my_host_ip 字符串，请大家换成你们自己的宿主机ip地址即可；
```


## 二、docker-registry 安装步骤
### 2.1 搜索docker-registry镜像

``` 
// 利用docker搜索命令，看看能搜索包含registry字段的东西，能出来个啥东西
[root@svr01 ~]# docker search registry
INDEX       NAME                                              DESCRIPTION                                     STARS     OFFICIAL   AUTOMATED
docker.io   docker.io/registry                                The Docker Registry 2.0 implementation for...   1847      [OK]       
docker.io   docker.io/konradkleine/docker-registry-frontend   Browse and modify your Docker registry in ...   177                  [OK]
docker.io   docker.io/hyper/docker-registry-web               Web UI, authentication service and event r...   123                  [OK]
。。。。。。
docker.io   docker.io/convox/registry                                                                         0                    
docker.io   docker.io/kontena/registry                        Kontena Registry                                0                    
docker.io   docker.io/lorieri/registry-ceph                   Ceph Rados Gateway (and any other S3 compa...   0                    
docker.io   docker.io/mattford63/registry                     The officail docker-registry with python-m...   0  
```


### 2.2 拉取docker.io/registry镜像

``` 
// 随便拉取了一个指定版本，也可以拉取下来
[root@svr01 ~]# docker pull docker.io/registry:2.3.1
Trying to pull repository docker.io/library/registry ... 
2.3.1: Pulling from docker.io/library/registry
fdd5d7827f33: Pull complete 
a3ed95caeb02: Pull complete 
a79b4a92697e: Pull complete 
6cbb75c7cc30: Pull complete 
4831699594bc: Pull complete 
Digest: sha256:9bd58f43fdf3c378ee7f19ec6e355a5ecbfad8eab82c77079b974b5a78b59e4d
[root@svr01 ~]#
[root@svr01 ~]#
[root@svr01 ~]#
[root@svr01 ~]#
[root@svr01 ~]#

// 获取一个最新的版本，只要网络畅通，基本上都能获取下来
[root@svr01 ~]# docker pull docker.io/registry
Using default tag: latest
Trying to pull repository docker.io/library/registry ... 
latest: Pulling from docker.io/library/registry
81033e7c1d6a: Pull complete 
b235084c2315: Pull complete 
c692f3a6894b: Pull complete 
ba2177f3a70e: Pull complete 
a8d793620947: Pull complete 
Digest: sha256:672d519d7fd7bbc7a448d17956ebeefe225d5eb27509d8dc5ce67ecb4a0bce54
[root@svr01 ~]#
[root@svr01 ~]#
[root@svr01 ~]#
[root@svr01 ~]#
[root@svr01 ~]#

// 查看我们拉取的镜像是否都已经存在本地了
[root@svr01 ~]# docker images
REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
docker.io/registry                     latest              d1fd7d86a825        3 weeks ago         33.26 MB
springms/gateway-zuul                  latest              f3825f14878c        6 weeks ago         248.5 MB
springms/provider-user                 latest              5f8a95ffddae        6 weeks ago         270.6 MB
springms/discovery-eureka              latest              825e3f54be46        6 weeks ago         252.5 MB
springms-simple-provider-user          latest              7ccdcdd5270f        6 weeks ago         235.6 MB
springms-user                          latest              4799ed153086        6 weeks ago         235.6 MB
docker.io/centos                       v2                  11efb35f320c        6 weeks ago         307.5 MB
docker.io/frolvlad/alpine-oraclejdk8   latest              4f03dc990224        8 weeks ago         170.1 MB
frolvlad/alpine-oraclejdk8             slim                4f03dc990224        8 weeks ago         170.1 MB
docker.io/centos                       latest              3fa822599e10        8 weeks ago         203.5 MB
docker.io/hello-world                  latest              f2a91732366c        10 weeks ago        1.848 kB
docker.io/registry                     2.3.1               83139345d017        23 months ago       165.8 MB
```

### 2.3 启动docker-registry

``` 
[root@svr01 ~]# docker run -d -p 5000:5000 --restart=always -v /tmp/docker-var/:/var/lib/registry registry:2.3.1
```

### 2.4 访问搭建的镜像仓库

``` 
// 通过 curl 命令测试, 正常情况能打印些东西出来
[root@svr01 ~]# curl http://my_host_ip:5000/v2
<a href="/v2/">Moved Permanently</a>.

// 通过浏览器输入 http://my_host_ip:5000/v2 地址试试，正常情况下回输出{}一个空的大括号内容。
```

### 2.5 上传测试镜像到镜像仓库中去

``` 
// 打一个tag文件
[root@svr01 ~]# docker tag busybox my_host_ip:5000/busybox
[root@svr01 ~]# docker images
REPOSITORY                                        TAG                 IMAGE ID            CREATED             SIZE
my_host_ip:5000/busybox                       latest              5b0d59026729        7 days ago          1.146 MB
docker.io/busybox                                 latest              5b0d59026729        7 days ago          1.146 MB
docker.io/registry                                latest              d1fd7d86a825        3 weeks ago         33.26 MB
springms/gateway-zuul                             latest              f3825f14878c        6 weeks ago         248.5 MB
springms/provider-user                            latest              5f8a95ffddae        6 weeks ago         270.6 MB
springms/discovery-eureka                         latest              825e3f54be46        6 weeks ago         252.5 MB
springms-simple-provider-user                     latest              7ccdcdd5270f        6 weeks ago         235.6 MB
springms-user                                     latest              4799ed153086        6 weeks ago         235.6 MB
docker.io/centos                                  v2                  11efb35f320c        6 weeks ago         307.5 MB
docker.io/frolvlad/alpine-oraclejdk8              latest              4f03dc990224        8 weeks ago         170.1 MB
frolvlad/alpine-oraclejdk8                        slim                4f03dc990224        8 weeks ago         170.1 MB
docker.io/centos                                  latest              3fa822599e10        8 weeks ago         203.5 MB
docker.io/hello-world                             latest              f2a91732366c        10 weeks ago        1.848 kB
docker.io/registry                                2.3.1               83139345d017        23 months ago       165.8 MB
docker.io/konradkleine/docker-registry-frontend   latest              7621ed3504d4        23 months ago       234.9 MB

// push文件到仓库
[root@svr01 ~]# docker push ip:5000/busybox
The push refers to a repository [my_host_ip:5000/busybox]
Get https://my_host_ip:5000/v1/_ping: http: server gave HTTP response to HTTPS client
[root@svr01 ~]# docker push my_host_ip:5000/busybox
The push refers to a repository [my_host_ip:5000/busybox]
Get https://my_host_ip:5000/v1/_ping: http: server gave HTTP response to HTTPS client

// 结果发现出错了, 修改 /etc/docker/daemon.json 文件，支持文件推送
文件内容为：{"insecure-registries":["my_host_ip:5000"]}

// 重启docker
[root@svr01 ~]# service docker restart
Redirecting to /bin/systemctl restart  docker.service
[root@svr01 ~]# 

// 推送文件
[root@svr01 ~]# docker push my_host_ip:5000/busybox
The push refers to a repository [my_host_ip:5000/busybox]
4febd3792a1f: Pushed 
latest: digest: sha256:4cee1979ba0bf7db9fc5d28fb7b798ca69ae95a47c5fecf46327720df4ff352d size: 527
[root@svr01 ~]# 
```

### 2.6 查看刚刚推送的busybox文件

``` 
[root@svr01 ~]# curl http://my_host_ip:5000/v2/_catalog
{"repositories":["busybox"]}
[root@svr01 ~]# 
```

### 2.7 下载刚刚上传的镜像

``` 
// 删除已经存在的镜像
[root@svr01 ~]# docker rmi my_host_ip:5000/busybox:latest
Untagged: my_host_ip:5000/busybox:latest
Untagged: my_host_ip:5000/busybox@sha256:4cee1979ba0bf7db9fc5d28fb7b798ca69ae95a47c5fecf46327720df4ff352d
[root@svr01 ~]# 

// 下载镜像
[root@svr01 ~]# docker pull my_host_ip:5000/busybox
Using default tag: latest
Trying to pull repository my_host_ip:5000/busybox ... 
latest: Pulling from my_host_ip:5000/busybox
Digest: sha256:4cee1979ba0bf7db9fc5d28fb7b798ca69ae95a47c5fecf46327720df4ff352d

// 查看一下，my_host_ip:5000/busybox 镜像已经下载下来了
[root@svr01 ~]# docker images
REPOSITORY                                        TAG                 IMAGE ID            CREATED             SIZE
docker.io/busybox                                 latest              5b0d59026729        7 days ago          1.146 MB
my_host_ip:5000/busybox                           latest              5b0d59026729        7 days ago          1.146 MB
docker.io/registry                                latest              d1fd7d86a825        3 weeks ago         33.26 MB
springms/gateway-zuul                             latest              f3825f14878c        6 weeks ago         248.5 MB
springms/provider-user                            latest              5f8a95ffddae        6 weeks ago         270.6 MB
springms/discovery-eureka                         latest              825e3f54be46        6 weeks ago         252.5 MB
springms-simple-provider-user                     latest              7ccdcdd5270f        6 weeks ago         235.6 MB
springms-user                                     latest              4799ed153086        6 weeks ago         235.6 MB
docker.io/centos                                  v2                  11efb35f320c        6 weeks ago         307.5 MB
frolvlad/alpine-oraclejdk8                        slim                4f03dc990224        8 weeks ago         170.1 MB
docker.io/frolvlad/alpine-oraclejdk8              latest              4f03dc990224        8 weeks ago         170.1 MB
docker.io/centos                                  latest              3fa822599e10        8 weeks ago         203.5 MB
docker.io/hello-world                             latest              f2a91732366c        10 weeks ago        1.848 kB
docker.io/registry                                2.3.1               83139345d017        23 months ago       165.8 MB
docker.io/konradkleine/docker-registry-frontend   latest              7621ed3504d4        23 months ago       234.9 MB
[root@svr01 ~]# 

```

## 三、portainer管理界面安装
### 3.1 下载轻量级的管理界面

``` 
[root@svr01 ~]# docker pull docker.io/portainer/portainer
Using default tag: latest
Trying to pull repository docker.io/portainer/portainer ... 
latest: Pulling from docker.io/portainer/portainer
d1e017099d17: Pull complete 
d63e75e16ec8: Pull complete 
Digest: sha256:232742dcb04faeb109f1086241f290cb89ad4c0576e75197e902ca6e3bf3a9fc
[root@svr01 ~]# 
```

### 3.2 启动 portainer

``` 
[root@svr01 ~]# docker run -d -p 9000:9000 --restart=always -v /var/run/docker.sock:/var/run/docker.sock --name parainer-test docker.io/portainer/portainer
```


### 3.3 访问管理界面

``` 
浏览器输入 http://my_host_ip:9000/ 然后即可看到UI管理界面；
```






## 四、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























