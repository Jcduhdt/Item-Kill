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
    还没解决  