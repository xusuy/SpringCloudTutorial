# SpringCloud（第 057 篇）CentOS7 安装 maven 编译工具
-

## 一、大致介绍

``` 
1、maven 相信大家一点都不陌生，由于jenkins的需要，所以这不就来了一篇maven的安装环节；
2、注意：下面的 my_host_ip 字符串，请大家换成你们自己的宿主机ip地址即可；
```


## 二、安装步骤
### 2.1 下载maven安装包

``` 
// 创建存放安装包的文件夹目录
[root@svr01 ~]# mkdir -p /home/install/maven
[root@svr01 ~]# cd /home/install/maven/

// 利用wget命令下载安装包
[root@svr01 maven]# wget http://mirrors.shu.edu.cn/apache/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz
--2018-02-01 08:57:29--  http://mirrors.shu.edu.cn/apache/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz
Resolving mirrors.shu.edu.cn (mirrors.shu.edu.cn)... 202.121.199.235
Connecting to mirrors.shu.edu.cn (mirrors.shu.edu.cn)|202.121.199.235|:80... connected.
HTTP request sent, awaiting response... 200 OK
Length: 8738691 (8.3M) [application/x-gzip]
Saving to: ‘apache-maven-3.5.2-bin.tar.gz’

100%[===========================================================================================================================>] 8,738,691   7.04MB/s   in 1.2s   

2018-02-01 08:57:31 (7.04 MB/s) - ‘apache-maven-3.5.2-bin.tar.gz’ saved [8738691/8738691]
```


### 2.2 解压maven

``` 
[root@svr01 maven]# tar -zxvf apache-maven-3.5.2-bin.tar.gz 
apache-maven-3.5.2/README.txt
apache-maven-3.5.2/LICENSE
。。。。。
。。。。。
[root@svr01 maven]# ls
apache-maven-3.5.2  apache-maven-3.5.2-bin.tar.gz
[root@svr01 maven]# ls ../../src/
jdk  maven  mysql  nginx-1.12.0  openssl-1.0.2l  pcre-8.41  redis-3.2.9  zlib-1.2.11
[root@svr01 maven]# mv apache-maven-3.5.2 ../../src/
[root@svr01 maven]# cd ../../src/
[root@svr01 src]# ls
apache-maven-3.5.2  jdk  maven  mysql  nginx-1.12.0  openssl-1.0.2l  pcre-8.41  redis-3.2.9  zlib-1.2.11
[root@svr01 src]# 
```

### 2.3 添加环境变量

``` 
// 索性我就将涉及到的环境变量全部粘贴出来
[root@svr01 src]# vim /etc/profile

// 修改的变量如下
JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64
MAVEN_HOME=/home/src/apache-maven-3.5.2
JRE_HOME=$JAVA_HOME/jre

CLASS_PATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
PATH=$PATH:$MAVEN_HOME/bin
export JAVA_HOME JRE_HOME CLASS_PATH PATH MAVEN_HOME

// 使修改的环境变量生效
[root@svr01 src]# source /etc/profile
```

### 2.4 检测maven是否安装好了

``` 
[root@svr01 ~]# mvn -v
Apache Maven 3.5.2 (138edd61fd100ec658bfa2d307c43b76940a5d7d; 2017-10-18T15:58:13+08:00)
Maven home: /home/src/apache-maven-3.5.2
Java version: 1.8.0_161, vendor: Oracle Corporation
Java home: /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "3.10.0-514.6.2.el7.x86_64", arch: "amd64", family: "unix"
```




## 三、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























