import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.ProviderManager;

import java.io.IOException;

/**
 * Created by shadim on 9/14/2014.
 */
public class Main {
    public static void main(String[] args) throws IOException {

        AbstractApplicationContext context = new ClassPathXmlApplicationContext("wis-security-config.xml");

        ProviderManager provider = (ProviderManager) context.getBean("authenticationManager");

        System.in.read();
    }
}
