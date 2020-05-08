1、Tomcat是一个HTTP服务器；它既按照Servlet规范的要求实现了Servlet容器，同时它也具有Http服务器的功能。

Tomcat服务器接受客户请求并做出响应的过程如下：
1）客户端（通常都是浏览器）访问Web服务器，发送HTTP请求。
2）Web服务器接收到请求后，传递给Servlet容器。
3）Servlet容器加载Servlet，产生Servlet实例后，向其传递表示请求和响应的对象。
4）Servlet实例使用请求对象得到客户端的请求信息，然后进行相应的处理。
5）Servlet实例将处理结果通过响应对象发送回客户端，容器负责确保响应正确送出，同时
将控制返回给Web服务器。

Tomcat的体系结构：
Tomcat服务器是由一系列可配置的组件构成的，其中核心组件是Catalina Servlet容器，
它是所有其他Tomcat组件的顶层容器。

(1) Server
Server表示整个的Catalina Servlet容器。Tomcat提供了Server接口的一个默认实现，这通
常不需要用户自己去实现。在Server容器中，可以包含一个或多个Service组件。
(2) Service
Service是存活在Server内部的中间组件，它将一个或多个连接器（Connector）组件绑定到
一个单独的引擎（Engine）上。在Server中，可以包含一个或多个Service组件。Service也
很少由用户定制，Tomcat提供了Service接口的默认实现，而这种实现既简单又能满足应
用。
(3) Connector
连接器（Connector）处理与客户端的通信，它负责接收客户请求，以及向客户返回响应结
果。在Tomcat中，有多个连接器可以使用。
(4) Engine
在Tomcat中，每个Service只能包含一个Servlet引擎（Engine）。引擎表示一个特定的
Service的请求处理流水线。作为一个Service可以有多个连接器，引擎从连接器接收和处理
所有的请求，将响应返回给适合的连接器，通过连接器传输给用户。用户允许通过实现
Engine接口提供自定义的引擎，但通常不需要这么做。
(5) Host
Host表示一个虚拟主机，一个引擎可以包含多个Host。用户通常不需要创建自定义的
Host，因为Tomcat给出的Host接口的实现（类StandardHost）提供了重要的附加功能。
(6) Context
一个Context表示了一个Web应用程序，运行在特定的虚拟主机中。