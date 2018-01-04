#Springboot集成urule
最近公司准备启动一个风险系统,架构中用到urule与flowable,捣鼓了好几天,踩了很多坑,算是初步写出一个demo,顺手记录一下

-------------------
## Urule2
URule是一款基于RETE算法纯Java的开源规则引擎产品，提供了向导式规则集、脚本式规则集、决策表、决策树、评分卡及决策流共六种类型的规则定义方式，配合基于WEB的设计器，可快速实现规则的定义、维护与发布。
用来替换原有的drools规则引擎,有一部分原因是因为URule自带了配置规则的UI界面
本例中采用urule客户端与服务端分离的设计

### Urule Server
urule的Server端,用来配置规则(知识包),并暴露给客户端,本例中知识库存储在mysql数据库中
springboot的配置可以详见[URule官方文档](http://wiki.bsdn.org/pages/viewpage.action?pageId=75071960"  target="_blank) 

####1.配置Urule Servlet
#####URuleServletRegistration.java
```java
@Component
public class URuleServletRegistration {
	@Bean
	public ServletRegistrationBean registerURuleServlet(){
		return new ServletRegistrationBean(new URuleServlet(),"/urule/*");
	}
} 
```
####2.配置urule知识库数据源、导入配置文件
#####application.yml
```yml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/world?useUnicode=true&characterEncoding=utf-8
    driver-class-name: com.mysql.jdbc.Driver
    username: root
    password: 1234
  jackson:
    default-property-inclusion: non_null

urule:
  repository:
    databasetype: mysql
    datasourcename: datasource

server:
  port: 8787
```
#####Config.java
```java
@Configuration
@ImportResource({"classpath:urule-console-context.xml"})
@PropertySource(value = {"classpath:urule-console-context.properties"})
public class Config {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourceLoader() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setOrder(1);
        return configurer;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource datasource() {
        return DataSourceBuilder.create().build();
    }
}
```
####3.启动Application类
#####Application.java
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
访问地址:http://localhost:8787/urule/frame 即可看到urule的规则配置页面

![](https://raw.githubusercontent.com/worstEzreal/mdpic/master/1.png)

### Urule Client
Urule的客户端,即调用规则的一方

####1.配置urule知识库地址
#####application.yml
```yml
urule:
  resporityServerUrl: http://localhost:8787
  knowledgeUpdateCycle: 1

server:
  port: 7878
```

####2.引入urule配置文件
#####RuleConfig.java
```java
@Configuration
@ImportResource({"classpath:urule-core-context.xml"})
public class RuleConfig {
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourceLoader() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setOrder(1);
        return configurer;
    }
}
```

####3.配置KnowledgePackageReceiverServlet

此Servlet用于接收Urule服务端发布的知识包(不想用这个功能可以不配)

#####URuleServletRegistration.java
```java
@Component
public class URuleServletRegistration {
	@Bean
	public ServletRegistrationBean registerURuleServlet(){
		return new ServletRegistrationBean(new KnowledgePackageReceiverServlet(),"/knowledgepackagereceiver");
	}
}
```

####4.启动Application类
#####RuleApplication.java
```java
@SpringBootApplication
public class RuleApplication {
	public static void main(String[] args) {
		SpringApplication.run(RuleApplication.class, args);
	}
}
```

到这里Urule的服务端和客户端就都配置完了。

### 测试规则

由于嫌官方文档的sample太麻烦,这里我自己写了个简单的规则作为测试用途

####1.添加规则&发布

#####配置参数
![](https://raw.githubusercontent.com/worstEzreal/mdpic/master/2.png)

#####配置规则
大于50的数会乘以10,小于50的数会除以2
![](https://raw.githubusercontent.com/worstEzreal/mdpic/master/3.png)

#####配置推送客户端
![](https://raw.githubusercontent.com/worstEzreal/mdpic/master/4.png)

#####发布知识包
![](https://raw.githubusercontent.com/worstEzreal/mdpic/master/5.png)

####2.编写Controller测试
#####RuleController.java
```java
@RestController
public class RuleController {
    @RequestMapping("rule")
    public String rule(@RequestParam String data) throws IOException {
        //创建一个KnowledgeSession对象
        KnowledgeService knowledgeService = (KnowledgeService) Utils.getApplicationContext().getBean(KnowledgeService.BEAN_ID);
        KnowledgePackage knowledgePackage = knowledgeService.getKnowledge("aaa/bag");
        KnowledgeSession session = KnowledgeSessionFactory.newKnowledgeSession(knowledgePackage);

        Integer integer = Integer.valueOf(data);
        Map<String, Object> param = new HashMap();
        param.put("var", integer);
        session.fireRules(param);

        Integer result = (Integer) session.getParameter("var");
        return String.valueOf(result);
    }
}
```
访问  http://localhost:7878/rule?data=50 和 http://localhost:7878/rule?data=40 
可以看到页面上分别打印500和20,执行规则成功

-------------------

