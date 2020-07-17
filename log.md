# day 07.10
1. 视频2-2的时候执行Maven的clean、install操作报错  
    java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTags  
    编译器的环境使用过高，但是依赖过低导致  
    解决方案：jdk降级或者依赖升级，该项目是采用了jdk降级的办法 
    原本是jdk12，改成了8，屈服了   
    [错误解决](https://blog.csdn.net/liubenlong007/article/details/86139598)  
2. maven使用install中文乱码  
    setting->maven->runner->vm options添加-Dfile.encoding=gbk  
    因为乱码的中文其编码是windows下的本地编码(GBK)，在idea 整合的 maven中使得默认vm 的编码是utf-8.所以出现控制台乱码  
3. 更改数据库中item_kill的start与end日期，使之符合秒杀时间  
    因为这是根据当前时间来的，应当start<当前时间<end，且total字段大于0，才符合秒杀商品
4. MySqlValidConnectionChecker: Cannot resolve com.mysq.jdbc.Connection.ping method.  Will use 'SELECT 1' instead.
   java.lang.NullPointerException
   因为druid与mysql版本不兼容，这里保留mysql8.0.16，将druid升至1.1.10  
5. @Autowired对Mapper注入报错，标红  
    取消标红：改为@Autowired(required = false)  
    在视频3-2哪里，若一直是抛异常，取看看你的主启动类哪里的MapperScan是不是被你注销了  
6. 发现直接在数据库查询与用idea查询得到的商品时间不一致  
    mysql读取时间与系统时间一致，但是通过IDEA得到的时间就不对了，快了8小时  
    通过IDEA写回Mysql的时间又慢了8小时  
    解决方案在application.properties连接mysql的url的serverTimezone的值写成Asia/Shanghai  
    但是之前一直都写成UTC也没出现过这种问题，可能是版本原因吧，毕竟JDK降级了  
# day 07.11 
从3-3开始   
1. 视频使用了postman  
    我使用IDEA 的REST Client
    位置：Tools->Http Client->Test RESTFul Web Service  
    在本项目的设置需要在请求头添加content-type：application/json  
    具体自己慢慢摸索
2. 使用REST Client测试通过，但是根据视频结合jsp测试不通过  
    看看info.jsp的75~81行，使用哪个data，两个同时使用，后面个会把前面的覆盖导致userId为null  
    所以会返回fail界面，因为这是之后完整的jsp，但视频是一步步来的，要分析现阶段哪些使用了，哪些没用
3. rabbitMQ的安装  
    [参考](https://www.jianshu.com/p/c7726ba4b046)  
    默认用户名和密码都是guest  
day 07.12
1. 压测的时候出现Too many connections错误  
   因为自己设置的mysql最大连接数太小了，但是这个又是直接操纵数据库，所以就调大一点就行  
   将mysql安装目录下的my.ini文件中的max_connection设置成期望的连接大小  
   使用cmd或powershell在管理员模式下执行net stop mysql,然后执行net start mysql即可重启mysql
2. redis分布式压测的时候出现只有10条记录但是书籍却减少了大于10的数字  
    比如库存减少了18本，但是出售记录只有10本
    找到的原因是redis版本问题，redis不推荐在windows下运行，windows目前主动支持到了3.2版本  
    而我就是在window进行的且用的2.8  
    我说看网上redis都到6了，我怎么还在用这个版本  
    将redis改成3.1或3.2都行，问题就解决了，不过我没有深究，到底是哪里的问题  
3. java.io.IOException: 远程主机强迫关闭了一个现有的连接  
    还是redis的原因，改了conf的tcp-keepalive也不行，应该就是并发量太大  
4. The version of ZooKeeper being used doesn't support Container nodes. CreateMode.PERSISTENT will be used instead.  
    emmm
5. 添加了shiro做登录管理，使用的md5盐值加密  
    所以在更改info的data字段的数据后，要把数据库中的密码替换为盐值加密后的密码
# day 7.17
1. github提示我在server的pom.xml中的commons-fileupload模块有安全漏洞  
    按照他的提示将1.3.1改为了1.3.3，测试了一下功能正常  
    
