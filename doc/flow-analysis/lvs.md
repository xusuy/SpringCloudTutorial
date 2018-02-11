# LVS 工作原理分析
-

## 一、大致介绍

``` 
1、官方站点：http://www.linuxvirtualserver.org；
2、用过LVS的童鞋，其实大家的目的性很明确，就是需要通过LVS提供的负载均衡技术和Linux操作系统实现一个高性能，高可用的服务器群集；
3、并且这个集群具有良好的可靠性、可扩展性和可操作性，从而以低廉的成本实现最优的服务性能，这也是大多数中小型公司青睐的架构；
```


## 二、LVS体系架构

### 2.1 LVS相关术语

``` 
1、 DS：Director Server。指的是前端负载均衡器节点。
2、 RS：Real Server。后端真实的工作服务器。
3、 VIP：向外部直接面向用户请求，作为用户请求的目标的IP地址。
4、 DIP：Director Server IP，主要用于和内部主机通讯的IP地址。
5、 RIP：Real Server IP，后端服务器的IP地址。
6、 CIP：Client IP，访问客户端的IP地址。
```

### 2.2 请求通讯路径

``` 
用户在终端发起请求 
--> 负载均衡层（Load Balancer）
--> 服务器群组层（Server Arrary）
--> 共享存储层（Shared Storage）
```


### 2.3 负载均衡层（Load Balancer）

``` 
1、处于集群最前端，一台或多台构成负载调度，俗称负载调度器（Director Server）；
2、分发请求给服务器集群组层的应用服务器（Real Server）；
3、监控应用服务器健康状况，动态从LVS路由表中剔除、添加；
4、也可以兼职Real Server的身份；
```


### 2.4 服务器群组层（Server Arrary）

``` 
1、一台或多台实际运行的应用服务器构成；
2、每个Real Server关联时通过有效网络互连；
```


### 2.5 共享存储层（Shared Storage）

``` 
1、提供共享存储空间和内容一致性的存储区域；
```




## 三、LVS模式: NAT
### 3.1 原理

``` 
多目标IP的DNAT，通过将请求报文中的目标地址和目标端口修改为选出来的RS的RIP和PORT实现转发。
```


### 3.2 流程分析

``` 
1.当用户请求到达DS后，此时请求的数据报文会先到内核空间的PREROUTING链，此时报文的源IP为CIP，目标IP为VIP；
|源地址|目的地址|
---------------
|CIP  |VIP    |

2、PREROUTING检查发现数据包的目标IP是本机，将数据包送至INPUT链；

3、IPVS比对数据包请求的服务是否为集群服务，若是，修改数据包的目标IP地址为后端服务器IP，后将数据包发至POSTROUTING链。 此时报文的源IP为CIP，目标IP为RIP；
|源地址|目的地址|
---------------
|CIP  |RIP    |

4、POSTROUTING链通过选路，将数据包发送给RS；

5、RS比对发现目标为自己的IP，开始构建响应报文发回给DS，此时报文的源IP为RIP，目标IP为CIP；
|源地址|目的地址|
---------------
|RIP  |CIP    |

6、Director Server在响应客户端前，此时会将源IP地址修改为自己的VIP地址，然后响应给客户端，此时报文的源IP为VIP，目标IP为CIP；
|源地址|目的地址|
---------------
|VIP  |CIP    |
```


### 3.3 特性

``` 
1、要求DS具备双网卡，VIP应对公网，而DIP必须和RIP在同一个网段内；
2、RIP、DIP应该使用私网地址，同在一个网段中，且RS的网关要指向DIP；
3、请求和响应报文都要经由DS转发，极高负载中，DS可能会成为系统瓶颈；
4、RS可以使用任意OS；
```


### 3.4 配置大致步骤讲解（代码就不详细粘贴了）

``` 
// lvs-server
1、配置路由转发；
2、配置NAT模式；
3、配置转发到RIP规则；

// lvs-client
1、配置RS网关执行DIP；
```





## 四、LVS模式: TUN
### 4.1 原理

``` 
在原有的IP报文外再次封装多一层IP首部，内部IP首部(源地址为CIP，目标IIP为VIP)，外层IP首部(源地址为DIP，目标IP为RIP)。
```


### 4.2 流程分析

``` 
1、当用户请求到达DS后，此时请求的数据报文会先到内核空间的PREROUTING链，此时报文的源IP为CIP，目标IP为VIP；
|源地址|目的地址|
---------------
|CIP  |VIP    |

2、PREROUTING检查发现数据包的目标IP是本机，将数据包送至INPUT链；

3、IPVS比对数据包请求的服务是否为集群服务，若是，在请求报文的首部再次封装一层IP报文，封装源IP为为DIP，目标IP为RIP。然后发至POSTROUTING链。 此时源IP为DIP，目标IP为RIP；
|IP首部源地址|IP首部目的地址|源地址|目的地址|
---------------------------------------
|DIP       |RIP         |CIP  |VIP    |

4、POSTROUTING链根据最新封装的IP报文，将数据包发至RS（因为在外层封装多了一层IP首部，所以可以理解为此时通过隧道传输），此时源IP为DIP，目标IP为RIP；

5、RS接收到报文后发现是自己的IP地址，就将报文接收下来，拆除掉最外层的IP后，会发现里面还有一层IP首部，而且目标是自己的tun0接口VIP，那么此时RS开始处理此请求，处理完成之后，通过tun0接口送出去向外传递，此时的源IP地址为VIP，目标IP为CIP；
|源地址|目的地址|
---------------
|VIP  |CIP    |

6、响应报文最终送达至客户端；
```


### 4.3 特性

``` 
1、DIP、VIP、RIP都应该是公网地址；
2、RS的网关不能，也不可能指向DIP；
3、RS必须支持IP隧道；
```


### 4.4 配置大致步骤讲解（代码就不详细粘贴了）

``` 
// lvs-server
1、将VIP配置到tun0网卡上，并配置tun0隧道0网卡为独立网段；
2、添加隧道ip路由表，防止路由短缺；
3、配置TUN模式；
4、配置转发到RIP规则；

// lvs-client
1、将VIP配置到tun0网卡上，配置tun0隧道0网卡为独立网段；
2、添加隧道ip路由表，防止路由短缺；
3、配置tun0/all忽略arp_ignore,宣告arp_announce；
```





## 五、LVS模式: DR
### 5.1 原理

``` 
通过为请求报文重新封装一个MAC首部进行转发，源MAC是DIP所在的接口的MAC，目标MAC是某挑选出的RS的RIP所在接口的MAC地址；源IP/PORT，以及目标IP/PORT均保持不变；
```


### 5.2 流程分析

``` 
1、当用户请求到达DS后，此时请求的数据报文会先到内核空间的PREROUTING链，此时报文的源IP为CIP，目标IP为VIP；
|源地址   |目的地址   |
---------------------
|CIP     |VIP       |
---------------------
|源MAC地址|目的MAC地址|
---------------------
|CIP-MAC |VIP-MAC   |

2、PREROUTING检查发现数据包的目标IP是本机，将数据包送至INPUT链；

3、IPVS比对数据包请求的服务是否为集群服务，若是，将请求报文中的源MAC地址修改为DIP的MAC地址，将目标MAC地址修改RIP的MAC地址，然后将数据包发至POSTROUTING链。 此时的源IP和目的IP均未修改，仅修改了源MAC地址为DIP的MAC地址，目标MAC地址为RIP的MAC地址；
|源地址   |目的地址   |
---------------------
|CIP     |VIP       |
---------------------
|源MAC地址|目的MAC地址|
---------------------
|DIP-MAC |RIP-MAC   |

4、由于DS和RS在同一个网络中，所以是通过二层来传输。POSTROUTING链检查目标MAC地址为RIP的MAC地址，那么此时数据包将会发至RS。

5、RS发现请求报文的MAC地址是自己的MAC地址，就接收此报文。处理完成之后，将响应报文通过lo接口传送给eth0网卡然后向外发出，此时的源IP地址为VIP，目标IP为CIP；
|源地址|目的地址|
---------------
|VIP  |CIP    |

6、 响应报文最终送达至客户端；
```


### 5.3 特性

``` 
1、RIP可以是公网地址，也可以是私网地址；
2、RIP、DIP需要在同一个IP网段；
3、RIP的网关不能指向DIP，以确保响应报文不会经由DS，而是直接通过RS发往Client；
```


### 5.4 配置大致步骤讲解（代码就不详细粘贴了）

``` 
// lvs-server
1、网卡可配置多个ip，将VIP配置到调度器eth0:0的一个端口上；
2、添加eth0:0路由表，防止路由短缺；
3、配置DR模式；
4、配置转发到RIP规则；

// lvs-client
1、将VIP配置到回环接口的一个端口上；
2、添加lo:0路由表，防止路由短缺；
3、配置lo/all忽略arp_ignore,宣告arp_announce；
```




## 六、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























