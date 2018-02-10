# SpringCloud（第 056 篇）CentOS7 安装 jenkins 持续集成工具
-

## 一、大致介绍

``` 
1、jenkins 的作用相信大家也耳熟能详了，为开发过程的持续交付提供了莫大的帮助；
2、本章节我们就尝试着自己安装一套持续集成工具，建立一套持续交付的平台工具；
3、注意：下面的 my_host_ip 字符串，请大家换成你们自己的宿主机ip地址即可；
```


## 二、安装步骤
### 2.1 jenins 下载

``` 
// 进入官网 https://jenkins.io/

// 找到安装包下载地址 https://pkg.jenkins.io/redhat/jenkins-2.104-1.1.noarch.rpm

// 创建一个存放下载包的文件夹
[root@svr01 ~]# mkdir -p /home/install/jenkins 
[root@svr01 ~]# cd /home/install/jenkins/

// 下载rpm包
[root@svr01 jenkins]# wget https://pkg.jenkins.io/redhat/jenkins-2.104-1.1.noarch.rpm
--2018-01-31 22:25:38--  https://pkg.jenkins.io/redhat/jenkins-2.104-1.1.noarch.rpm
Resolving pkg.jenkins.io (pkg.jenkins.io)... 52.202.51.185
Connecting to pkg.jenkins.io (pkg.jenkins.io)|52.202.51.185|:443... connected.
HTTP request sent, awaiting response... 302 Found
Location: https://prodjenkinsreleases.blob.core.windows.net/redhat/jenkins-2.104-1.1.noarch.rpm [following]
--2018-01-31 22:25:39--  https://prodjenkinsreleases.blob.core.windows.net/redhat/jenkins-2.104-1.1.noarch.rpm
Resolving prodjenkinsreleases.blob.core.windows.net (prodjenkinsreleases.blob.core.windows.net)... 104.208.128.30
Connecting to prodjenkinsreleases.blob.core.windows.net (prodjenkinsreleases.blob.core.windows.net)|104.208.128.30|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 74195529 (71M) [application/x-redhat-package-manager]
Saving to: ‘jenkins-2.104-1.1.noarch.rpm’

100%[=================================================================================================================>] 74,195,529  5.53MB/s   in 16s    

2018-01-31 22:25:56 (4.56 MB/s) - ‘jenkins-2.104-1.1.noarch.rpm’ saved [74195529/74195529]

[root@svr01 jenkins]# 
```


### 2.2 安装jenkins

``` 
[root@svr01 jenkins]# rpm -ivh jenkins-2.104-1.1.noarch.rpm 
warning: jenkins-2.104-1.1.noarch.rpm: Header V4 DSA/SHA1 Signature, key ID d50582e6: NOKEY
Preparing...                          ################################# [100%]
Updating / installing...
   1:jenkins-2.104-1.1                ################################# [100%]
```

### 2.3 修改默认端口

``` 
// 由于jenkins的 JENKINS_PORT 默认端口是8080，而本人的 8080端口已被其他应用占据，因此修改端口；

// 如果大家觉得要换端口的话，则执行 vi /etc/sysconfig/jenkins，然后 JENKINS_PORT、JENKENS_AJP_PORT 值修改；

修改默认端口8080为8880
[root@svr01 jenkins]# vim /etc/sysconfig/jenkins 
JENKINS_PORT="8880"
```

### 2.4 jdk测试及安装

``` 
// 如果有的童鞋没有安装jdk环境的话，那么我就在这里推荐大家简单操作安装一下jdk；

// 检索1.8的列表
yum list java-1.8* 

// 安装1.8.0的所有文件
yum install java-1.8.0-openjdk* -y
```

### 2.5 启动jenkins

``` 
[root@svr01 jenkins]# service jenkins start        
Starting jenkins (via systemctl):                          [  OK  ]
[root@svr01 jenkins]# 
```

### 2.6 进入jenkins管理界面

``` 
// 浏览器输入地址：http://my_host_ip:8880

// 选择安装建议插件，

// 视网络情况而定，建议插件安装完成之后，出现创建管理员的界面，

// 输入账户信息即可完成Jenkins的安装和第一个管理员的设置。

// 注意：点击“Continue as admin”链接则跳过管理员设置，管理员账户为“admin”，管理员密码为/root/.jenkins/secrets/initialAdminPassword中的内容；点击“Saveand Finish”则保存当前页面填写的内容
```

### 2.7 后续交付

``` 
后序再结合前面的SpringCloud、Docker小结搭建一套自己玩的整套流程框架；
```




## 三、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























