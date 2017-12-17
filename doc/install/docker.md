# SpringCloud（第 052 篇）CentOS7 安装 Docker 以及常用操作命令讲解
-

## 一、大致介绍

``` 
本章节主要带入大家初步进入 Docker，体验一下docker的安装步骤以及操作命令。
```


## 二、安装步骤
### 2.1 Docker环境部署要求

``` 
1、Docker 需要运行在 64-bit 的操作系统上并且要求Linux 内核版本不小于 3.10,OS7满足这个要求；
2、其余低版本的可以使用yum update 命令对操作系统内核进行升级! 
3、系统内核版本查看命令：uname -r

[root@svr01 ~]# uname -r
3.10.0-514.6.2.el7.x86_64
```


### 2.2 yum 命令安装 Docker

``` 
[root@svr01 ~]# yum -y install docker

安装完了会看到如下的打印信息：
Installed:
  docker.x86_64 2:1.12.6-68.gitec8512b.el7.centos                                                                                      

Dependency Installed:
  audit-libs-python.x86_64 0:2.7.6-3.el7                                checkpolicy.x86_64 0:2.5-4.el7                                 
  container-selinux.noarch 2:2.33-1.git86f33cd.el7                      container-storage-setup.noarch 0:0.8.0-3.git1d27ecf.el7        
  device-mapper-event.x86_64 7:1.02.140-8.el7                           device-mapper-event-libs.x86_64 7:1.02.140-8.el7               
  device-mapper-persistent-data.x86_64 0:0.7.0-0.1.rc6.el7              docker-client.x86_64 2:1.12.6-68.gitec8512b.el7.centos         
  docker-common.x86_64 2:1.12.6-68.gitec8512b.el7.centos                libcgroup.x86_64 0:0.41-13.el7                                 
  libseccomp.x86_64 0:2.3.1-3.el7                                       libsemanage-python.x86_64 0:2.5-8.el7                          
  lvm2.x86_64 7:2.02.171-8.el7                                          lvm2-libs.x86_64 7:2.02.171-8.el7                              
  oci-register-machine.x86_64 1:0-3.13.gitcd1e331.el7                   oci-systemd-hook.x86_64 1:0.1.14-1.git1ba44c6.el7              
  oci-umount.x86_64 2:2.3.0-1.git51e7c50.el7                            policycoreutils-python.x86_64 0:2.5-17.1.el7                   
  python-IPy.noarch 0:0.75-6.el7                                        setools-libs.x86_64 0:3.3.8-1.1.el7                            
  skopeo-containers.x86_64 1:0.1.26-2.dev.git2e8377a.el7.centos         yajl.x86_64 0:2.0.4-4.el7                                      

Dependency Updated:
  audit.x86_64 0:2.7.6-3.el7                        audit-libs.x86_64 0:2.7.6-3.el7        device-mapper.x86_64 7:1.02.140-8.el7       
  device-mapper-libs.x86_64 7:1.02.140-8.el7        libsemanage.x86_64 0:2.5-8.el7         policycoreutils.x86_64 0:2.5-17.1.el7       

Complete!
```

### 2.3 检测 Docker 是否安装成功

``` 
[root@svr01 ~]# docker images
Cannot connect to the Docker daemon. Is the docker daemon running on this host?

打印该信息说明docker已经安装了，只是没有启动docker而已；
```

### 2.4 启动 docker 后台服务

``` 
[root@svr01 ~]# service docker start
Redirecting to /bin/systemctl start  docker.service
```

### 2.5 查看 docker 的一些相关信息

``` 
1、查看镜像列表
[root@svr01 ~]# docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE

2、查看运行的镜像列表
[root@svr01 ~]# docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

### 2.6 测试下载 hello-world 镜像，本地没有的话则会从docker.io的远端镜像库下载

``` 
1、下载 hello-world 镜像
[root@svr01 ~]# docker run hello-world
Unable to find image 'hello-world:latest' locally
Trying to pull repository docker.io/library/hello-world ... 
latest: Pulling from docker.io/library/hello-world

ca4f61b1923c: Pull complete 
Digest: sha256:be0cd392e45be79ffeffa6b05338b98ebb16c87b255f48e297ec7f98e123905c

Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://cloud.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/

2、再次查看 docker 现有的资源：
[root@svr01 ~]# docker images
REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
docker.io/hello-world   latest              f2a91732366c        3 weeks ago         1.848 kB

[root@svr01 ~]# docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

### 2.7 搜索镜像文件

``` 
[root@svr01 ~]# docker search centos
INDEX       NAME                                         DESCRIPTION                                     STARS     OFFICIAL   AUTOMATED
docker.io   docker.io/centos                             The official build of CentOS.                   3876      [OK]       
docker.io   docker.io/ansible/centos7-ansible            Ansible on Centos7                              103                  [OK]
docker.io   docker.io/jdeathe/centos-ssh                 CentOS-6 6.9 x86_64 / CentOS-7 7.4.1708 x8...   90                   [OK]
docker.io   docker.io/consol/centos-xfce-vnc             Centos container with "headless" VNC sessi...   37                   [OK]
docker.io   docker.io/imagine10255/centos6-lnmp-php56    centos6-lnmp-php56                              34                   [OK]
docker.io   docker.io/tutum/centos                       Simple CentOS docker image with SSH access      34                   
docker.io   docker.io/gluster/gluster-centos             Official GlusterFS Image [ CentOS-7 +  Glu...   21                   [OK]
docker.io   docker.io/kinogmt/centos-ssh                 CentOS with SSH                                 17                   [OK]
docker.io   docker.io/centos/python-35-centos7           Platform for building and running Python 3...   14                   
docker.io   docker.io/openshift/base-centos7             A Centos7 derived base image for Source-To...   13                   
docker.io   docker.io/centos/php-56-centos7              Platform for building and running PHP 5.6 ...   10                   
docker.io   docker.io/openshift/jenkins-2-centos7        A Centos7 based Jenkins v2.x image for use...   7                    
docker.io   docker.io/openshift/mysql-55-centos7         DEPRECATED: A Centos7 based MySQL v5.5 ima...   6                    
docker.io   docker.io/darksheer/centos                   Base Centos Image -- Updated hourly             3                    [OK]
docker.io   docker.io/openshift/ruby-20-centos7          DEPRECATED: A Centos7 based Ruby v2.0 imag...   3                    
docker.io   docker.io/blacklabelops/centos               CentOS Base Image! Built and Updates Daily!     1                    [OK]
docker.io   docker.io/miko2u/centos6                     CentOS6 鏃ユ湰瑾炵挵澧                                  1                    [OK]
docker.io   docker.io/openshift/php-55-centos7           DEPRECATED: A Centos7 based PHP v5.5 image...   1                    
docker.io   docker.io/pivotaldata/centos-gpdb-dev        CentOS image for GPDB development. Tag nam...   1                    
docker.io   docker.io/pivotaldata/centos-mingw           Using the mingw toolchain to cross-compile...   1                    
docker.io   docker.io/jameseckersall/sonarr-centos       Sonarr on CentOS 7                              0                    [OK]
docker.io   docker.io/openshift/wildfly-101-centos7      A Centos7 based WildFly v10.1 image for us...   0                    
docker.io   docker.io/pivotaldata/centos                 Base centos, freshened up a little with a ...   0                    
docker.io   docker.io/pivotaldata/centos-gcc-toolchain   CentOS with a toolchain, but unaffiliated ...   0                    
docker.io   docker.io/smartentry/centos                  centos with smartentry                          0                    [OK]
```

### 2.8 拉取镜像文件

``` 
[root@svr01 ~]# docker pull docker.io/centos
```

### 2.9 运行刚刚下载的 docker.io/centos 镜像

``` 
[root@svr01 ~]# docker run -i -t docker.io/centos /bin/bash
[root@9f053696bedb /]#

1、运行 docker run 命令后会看到已经进入的镜像文件内部，因此会看到 [root@9f053696bedb /]# 这样的命令展示信息。

2、进入镜像随便使用看看
[root@9f053696bedb /]# ls
anaconda-post.log  bin  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
[root@9f053696bedb /]# cd /home/
[root@9f053696bedb home]# mkdir hmilyylimh
[root@9f053696bedb home]# mkdir -p hmilyylimh/docker
[root@9f053696bedb home]# cd hmilyylimh/docker/
[root@9f053696bedb docker]# 

3、退出镜像
[root@9f053696bedb docker]# exit
exit
[root@svr01 ~]# 

4、参数说明：
	-a stdin: 指定标准输入输出内容类型，可选 STDIN/STDOUT/STDERR 三项；
	-d: 后台运行容器，并返回容器ID；
	-i: 以交互模式运行容器，通常与 -t 同时使用；
	-t: 为容器重新分配一个伪输入终端，通常与 -i 同时使用；
	--name="nginx-lb": 为容器指定一个名称；
	--dns 8.8.8.8: 指定容器使用的DNS服务器，默认和宿主一致；
	--dns-search example.com: 指定容器DNS搜索域名，默认和宿主一致；
	-h "mars": 指定容器的hostname；
	-e username="ritchie": 设置环境变量；
	--env-file=[]: 从指定文件读入环境变量；
	--cpuset="0-2" or --cpuset="0,1,2": 绑定容器到指定CPU运行；
	-m :设置容器使用内存最大值；
	--net="bridge": 指定容器的网络连接类型，支持 bridge/host/none/container: 四种类型；
	--link=[]: 添加链接到另一个容器；
	--expose=[]: 开放一个端口或一组端口；
```

### 2.10 在 docker.io/centos 镜像中随便装个东西，举例装个json

``` 
[root@svr01 ~]# docker run -i -t docker.io/centos /bin/bash
[root@497905f140a6 /]# gem install json
bash: gem: command not found
[root@497905f140a6 /]# yum install gem
。。。。
Installed:
  rubygems.noarch 0:2.0.14.1-30.el7                                                                                                                                    

Dependency Installed:
  libyaml.x86_64 0:0.1.4-11.el7_0             ruby.x86_64 0:2.0.0.648-30.el7              ruby-irb.noarch 0:2.0.0.648-30.el7    ruby-libs.x86_64 0:2.0.0.648-30.el7   
  rubygem-bigdecimal.x86_64 0:1.2.0-30.el7    rubygem-io-console.x86_64 0:0.4.2-30.el7    rubygem-json.x86_64 0:1.7.7-30.el7    rubygem-psych.x86_64 0:2.0.0-30.el7   
  rubygem-rdoc.noarch 0:4.0.0-30.el7         

Complete!
[root@497905f140a6 /]# gem install json 
Fetching: json-2.1.0.gem (100%)
Building native extensions.  This could take a while...
ERROR:  Error installing json:
        ERROR: Failed to build gem native extension.

    /usr/bin/ruby extconf.rb
mkmf.rb can't find header files for ruby at /usr/share/include/ruby.h


Gem files will remain installed in /usr/local/share/gems/gems/json-2.1.0 for inspection.
Results logged to /usr/local/share/gems/gems/json-2.1.0/ext/json/ext/generator/gem_make.out
```

### 2.11 针对已经安装json的docker.io/centos镜像库打包，tag命名为v2

``` 
[root@497905f140a6 /]# docker commit -m ="add new image" -a="hmilyylimh" 497905f140a6 docker.io/centos:v2
bash: docker: command not found
[root@497905f140a6 /]# exit
exit
[root@svr01 ~]# docker commit -m ="add new image" -a="HEHUI231" 497905f140a6 docker.io/centos:v2
sha256:11efb35f320cec46c83bc4dcbc184c8d45dcb3e369105251d70e2336fd261c92
[root@svr01 ~]# docker images;
REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
docker.io/centos        v2                  11efb35f320c        17 seconds ago      307.5 MB
docker.io/centos        latest              3fa822599e10        2 weeks ago         203.5 MB
docker.io/hello-world   latest              f2a91732366c        3 weeks ago         1.848 kB
```


### 2.12 给刚刚打包的 docker.io/centos:v2 镜像设置镜像标签

``` 
[root@svr01 ~]# docker tag 11efb35f320c docker.io/centos:v22
[root@svr01 ~]# docker images;
REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
docker.io/centos        v2                  11efb35f320c        36 minutes ago      307.5 MB
docker.io/centos        v22                 11efb35f320c        36 minutes ago      307.5 MB
docker.io/centos        latest              3fa822599e10        2 weeks ago         203.5 MB
docker.io/hello-world   latest              f2a91732366c        3 weeks ago         1.848 kB
```

### 2.13 删除 docker.io/centos:v2 镜像文件

``` 
[root@svr01 ~]# docker rmi docker.io/centos:v22
Untagged: docker.io/centos:v22
[root@svr01 ~]# docker images;
REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
docker.io/centos        v2                  11efb35f320c        37 minutes ago      307.5 MB
docker.io/centos        latest              3fa822599e10        2 weeks ago         203.5 MB
docker.io/hello-world   latest              f2a91732366c        3 weeks ago         1.848 kB
```

### 2.14 删除单个已经停止的容器

``` 
[root@svr01 ~]# docker rm 47de3399a0ab
47de3399a0ab

[root@svr01 ~]# docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

### 2.15 删除所有已经停止的容器

``` 
[root@svr01 ~]# docker rm $(docker ps -a -q)

ca94bd87299f
43a27d23f3ee
497905f140a6
fb9512c00a84
9f053696bedb
565b5d3e5139
9c2518ba47c8
1abaaef82836
e5b39fc724ab

[root@svr01 ~]# docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

### 2.16 杀掉单个正在执行的容器

``` 
[root@svr01 ~]# docker kill 47de3399a0ab
47de3399a0ab

[root@svr01 ~]# docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

### 2.17 杀掉所有正在执行的容器

``` 
[root@svr01 ~]# docker kill $(docker ps -q)

ca94bd87299f
43a27d23f3ee
497905f140a6
fb9512c00a84
9f053696bedb
565b5d3e5139
9c2518ba47c8
1abaaef82836
e5b39fc724ab

[root@svr01 ~]# docker ps -l
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

### 2.18 将image文件保存到磁盘目录

``` 
[root@svr01 ~]# docker images;
REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
docker.io/centos                       v2                  11efb35f320c        5 hours ago         307.5 MB
docker.io/frolvlad/alpine-oraclejdk8   latest              4f03dc990224        12 days ago         170.1 MB
docker.io/centos                       latest              3fa822599e10        2 weeks ago         203.5 MB
docker.io/hello-world                  latest              f2a91732366c        3 weeks ago         1.848 kB
[root@svr01 ~]# docker save 4f03dc990224 > /home/install/alpine-oraclejdk8.tar
[root@svr01 ~]# ls /home/install/
alpine-oraclejdk8.tar          hive                     MySQL-5.6.27-1.el7.x86_64.rpm-bundle.tar  openssl-1.0.2l.tar.gz  redis-3.2.9.tar.gz
apache-flume-1.7.0-bin.tar.gz  httpd-2.4.26.tar.gz      mysql-5.7.18-1.el7.x86_64.rpm-bundle.tar  pcre-8.41.tar.gz       zlib-1.2.11.tar.gz
hadoop                         memcached-1.4.39.tar.gz  nginx-1.12.0.tar.gz                       php-5.6.31.tar.gz      zookeeper-3.4.10.tar.gz
```

### 2.19 将磁盘的镜像文件导入到docker中，并且通过 docker tag 修改名称和tag

``` 
[root@svr01 ~]# docker load < /home/install/centos-v2.tar
e9613a472968: Loading layer [==================================================>]   105 MB/105 MB
Loaded image ID: sha256:11efb35f320cec46c83bc4dcbc184c8d45dcb3e369105251d70e2336fd261c92
[root@svr01 ~]# docker images
REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
<none>                                 <none>              11efb35f320c        5 hours ago         307.5 MB
docker.io/frolvlad/alpine-oraclejdk8   latest              4f03dc990224        12 days ago         170.1 MB
docker.io/centos                       latest              3fa822599e10        2 weeks ago         203.5 MB
docker.io/hello-world                  latest              f2a91732366c        3 weeks ago         1.848 kB
[root@svr01 ~]# docker tag 11efb35f320c docker.io/centos:v2
[root@svr01 ~]# docker images
REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
docker.io/centos                       v2                  11efb35f320c        5 hours ago         307.5 MB
docker.io/frolvlad/alpine-oraclejdk8   latest              4f03dc990224        12 days ago         170.1 MB
docker.io/centos                       latest              3fa822599e10        2 weeks ago         203.5 MB
docker.io/hello-world                  latest              f2a91732366c        3 weeks ago         1.848 kB
```

### 2.20 验证刚刚加载的镜像能否成功运行，能进入的话，并且成功exit则导入成功

``` 
[root@svr01 ~]# docker images
REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
docker.io/centos                       v2                  11efb35f320c        5 hours ago         307.5 MB
docker.io/frolvlad/alpine-oraclejdk8   latest              4f03dc990224        12 days ago         170.1 MB
docker.io/centos                       latest              3fa822599e10        2 weeks ago         203.5 MB
docker.io/hello-world                  latest              f2a91732366c        3 weeks ago         1.848 kB
[root@svr01 ~]# docker run -it docker.io/centos:v2
[root@0a87728d1798 /]# ls
anaconda-post.log  bin  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
[root@0a87728d1798 /]# exit
exit
[root@svr01 ~]#
```




## 三、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























