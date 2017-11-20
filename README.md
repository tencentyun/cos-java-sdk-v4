# tencentyun-cos-java-sdk-v4

java sdk for [腾讯云对象存储服务](https://www.qcloud.com/product/cos.html)

sdk说明请参照[cos java sdk文档](https://www.qcloud.com/doc/product/436/6273)


## maven坐标

```xml
<groupId>com.qcloud</groupId>
<artifactId>cos_api</artifactId>
<version>4.6</version>
```

### 直接下载源码集成
从github下载源码装入到您的程序中
请参考示例Demo.java

## 使用范例
修改Demo.java内的appId, secretId, secretKey等信息为您的配置(可在控制台上查阅相关信息), 然后运行Demo.java。


### 常见问题:

1 引入SDK运行后，出现 java.lang.NoSuchMethodError的异常。

   原因: 一般是发生了JAR包冲突，比如用户的工程中的http的JAR包版本没有A方法，但是SDK依赖的JAR包有A方法。此时运行时加载顺序的问题，加载了用户工程中的http库，运行时便会抛出NoSuchMethodError的异常。

   解决方法:  将已包含的工程中引起NoSuchMethodError的包的版本和SDK中pom.xml里的对应库的版本改成一致。
