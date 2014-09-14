package com.gigaspaces.wis;

import com.gigaspaces.security.Authority;
import com.gigaspaces.security.AuthorityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.IWindowsIdentity;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shadim on 9/14/2014.
 * <p/>
 * Spring security @{AuthenticationProvider} windows integrated security implementation
 */
public class WindowsIntegratedSecurityAuthenticationProvider implements AuthenticationProvider {

    static {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("wis-security-config.xml");
    }
    private static final Logger log = LoggerFactory.getLogger(WindowsIntegratedSecurityAuthenticationProvider.class);
    // roles(group) map authorities
    private final Map<String, Collection<? extends GrantedAuthority>> roles = new ConcurrentHashMap<String, Collection<? extends GrantedAuthority>>();
    private final Server server;

//    public WindowsIntegratedSecurityAuthenticationProvider() throws IOException, JAXBException {
//        this(8889, "Negotiate", new Properties());
//    }

    public WindowsIntegratedSecurityAuthenticationProvider(Server server, Properties properties) throws IOException, JAXBException {

        log.info("initialize windows integrated security server.");

        populateRoles(properties);

        this.server = server;

//        this.server.bootstrap();
    }

    public WindowsIntegratedSecurityAuthenticationProvider(int port, String securityPackage, Properties properties) throws IOException, JAXBException {

        populateRoles(properties);

        server = new Server(port, securityPackage);

//        server.bootstrap();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String userName = (String) authentication.getPrincipal();

        String credentials = (String) authentication.getCredentials();

        IWindowsIdentity identity = server.authenticate(userName, credentials);

        if (identity == null)
            return null;

        Collection<? extends GrantedAuthority> authorities = getUserAuthorities(identity);

        return new UsernamePasswordAuthenticationToken(identity, credentials, authorities);
    }

    private void populateRoles(Properties rolesMaps) {
        roles.clear();

        Iterator<Map.Entry<Object, Object>> e = rolesMaps.entrySet().iterator();

        while (e.hasNext()) {
            Map.Entry<Object, Object> entry = e.next();

            String privileges = (String) entry.getValue();

            if (privileges == null) continue;

            String[] p = privileges.split(",");

            if (p.length <= 0) continue;

            ArrayList<GrantedAuthority> authrzs = new ArrayList<GrantedAuthority>();

            for (int j = 0; j < p.length; j++) {
                final Authority authority = AuthorityFactory.create(p[j]);
                log.info("authority: " + authority);
                if (authority == null) continue;

                authrzs.add(new GrantedAuthority() {
                    @Override
                    public String getAuthority() {
                        return authority.toString();
                    }
                });
            }

            log.info("Add role: " + entry.getKey());

            roles.put((String) entry.getKey(), authrzs);
        }
    }

    public Collection<? extends GrantedAuthority> getUserAuthorities(IWindowsIdentity identity) {

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        IWindowsAccount[] groups = identity.getGroups();

        if (groups != null) {

            for (int i = 0; i < groups.length; i++) {
                String g = groups[i].getName();

                Collection<? extends GrantedAuthority> a = roles.get(g);

                if (a == null) continue;

                authorities.addAll(a);
            }
        }
        return authorities;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }
}
