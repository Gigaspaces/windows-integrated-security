Windows Integrated Security Server
==================================

Build
=====

To build the server run `mvn package`

> Will produce new jar name `win-integrated-security-[version]-SNAPSHOT.jar` at <project>/target   
  and copy all dependencies to `<project>/target/lib`


Configure XAP to use Windows Integrated Security
================================================

to use windows integration security server:

1. Enable XAP security
2. Copy the following jar to `<Gigaspaces Home>\Runtime\lib\optional\security`

        win-integrated-security-[version]-SNAPSHOT.jar
        commons-cli-1.2.jar
        guava-13.0.1.jar
        jna-3.5.0.jar
        platform-3.5.0.jar
        slf4j-api-1.7.2.jar
        spring-security-config-3.1.4.RELEASE.jar
        spring-security-core-3.1.4.RELEASE.jar
        spring-security-web-3.1.4.RELEASE.jar
        waffle-jna-1.5.jar

3. copy spring security template configuration:
    * copy `<project>\src\main\resources\wis-security-config.xml` `<Gigaspaces Home>\Runtime\config\security`
    * optionally rename the `win-security-config.xml` to `security-config.xml`
4. edit `security.properties` file and make it point to step 3 config file
    * security.properties example:   
    ```
        com.gs.security.security-manager.class=org.openspaces.security.spring.SpringSecurityManager
        spring-security-config-location=../../Runtime/config/security/security-config.xml
    ``` 
5. run the windows integrated security server:
    * copy `<project>\src\main\resources\server to the location where your server should be reside
    * copy the required jars to `server\lib` see the README at lib folder
    * to run the server: `server\server.bat -p <port> -sp <security package>`   
    Example:   
        `server\server.bat -p 8888 -sp Negotiate`   
        `server\server.bat -p 8888 -sp NTLM`
    

Spring Configuration File
=========================

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN 2.0//EN" "http://www.springframework.org/dtd/spring-beans-2.0.dtd">

<!--  
     Spring Security sample configuration file.
     For detailed information, refer to the GigaSpaces documentation section on Spring-based Security.
 -->

<beans>

    <bean id="authenticationManager"
          class="org.springframework.security.authentication.ProviderManager">
        <constructor-arg>
            <list>
                <ref bean="windowsAuthenticationProvider"/>
            </list>
        </constructor-arg>
    </bean>

    <bean id="windowsAuthenticationProvider"
          class="com.gigaspaces.wis.WindowsIntegratedSecurityAuthenticationProvider">
        <constructor-arg value="127.0.0.1" />
        <constructor-arg value="8889"/>
        <!--
            Roles map
             key - Name of Windows Group
             value - comma delimited list of Roles role1,role2,...,roleN
        -->
        <constructor-arg>
            <props>
                <prop key="Administrators">GridPrivilege MANAGE_GRID,GridPrivilege MANAGE_PU,GridPrivilege PROVISION_PU</prop>
                <prop key="Users">SpacePrivilege READ</prop>
            </props>
        </constructor-arg>
    </bean>
</beans>
```