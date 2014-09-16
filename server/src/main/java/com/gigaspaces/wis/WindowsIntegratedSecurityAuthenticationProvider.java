package com.gigaspaces.wis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import javax.xml.bind.JAXBException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by shadim on 9/14/2014.
 * <p/>
 * Spring security @{AuthenticationProvider} windows integrated security implementation
 */
public class WindowsIntegratedSecurityAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(WindowsIntegratedSecurityAuthenticationProvider.class);
    // roles(group) map authorities
    private final Map<String, Collection<? extends GrantedAuthority>> roles = new ConcurrentHashMap<String, Collection<? extends GrantedAuthority>>();
    private final ExecutorService pool;
    // cache authenticated users
    private final Map<String, IdentityMessage> authenticatedUsers = new ConcurrentHashMap<String, IdentityMessage>();
    private final int port;
    private final String hostname;

//    private com.gigaspaces.wis.Server server;
//    private final JAXBContext context;

    public WindowsIntegratedSecurityAuthenticationProvider(String host, int port, Properties properties) throws JAXBException {
        log.info("initialize windows integrated security server.");
        this.hostname = host;
        this.port = port;
//        this.context = JAXBContext.newInstance(Message.class);
        this.pool = Executors.newCachedThreadPool();
        populateRoles(properties);
    }

//    public WindowsIntegratedSecurityAuthenticationProvider(com.gigaspaces.wis.Server server, Properties properties) throws IOException, JAXBException {
//
//        log.info("initialize windows integrated security server.");
//
//        populateRoles(properties);
//
//        this.server = server;
//    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String userName = (String) authentication.getPrincipal();

        String credentials = (String) authentication.getCredentials();

        IdentityMessage identity = authenticate(userName, credentials);

        if (identity == null)
            return null;

        Collection<? extends GrantedAuthority> authorities = getUserAuthorities(identity);

        return new UsernamePasswordAuthenticationToken(identity, credentials, authorities);
    }

    private IdentityMessage authenticate(String userName, final String credentials) {

        IdentityMessage identity = authenticatedUsers.get(credentials);

        if (identity == null) {

            try {
                Future<IdentityMessage> future = pool.submit(createWindowCallback(credentials));

                identity = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            if (identity != null) {
                authenticatedUsers.put(credentials, identity);
            } else
                return null;
        }

        if (!identity.getFqn().contains(userName))
            return null;

        return identity;
    }

    private Callable<IdentityMessage> createWindowCallback(String credentials) throws JAXBException {
        return new WindowsIdentityCallable(hostname, port, credentials);
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
                final String authority = p[j];

                log.info("fetch authority: " + authority);

                if (authority == null) continue;

                authrzs.add(new GrantedAuthority() {

                    @Override
                    public String getAuthority() {
                        return authority;
                    }
                });
            }

            log.info("Add role: " + entry.getKey());

            roles.put((String) entry.getKey(), authrzs);
        }
    }

    public Collection<? extends GrantedAuthority> getUserAuthorities(IdentityMessage identity) {

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();


        for (String group : identity.getGroups()) {

            Collection<? extends GrantedAuthority> a = roles.get(group);

            if (a == null) continue;

            authorities.addAll(a);
        }

        return authorities;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }


}
