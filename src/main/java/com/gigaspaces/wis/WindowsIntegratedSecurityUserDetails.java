package com.gigaspaces.wis;

import com.gigaspaces.security.Authority;
import com.gigaspaces.security.directory.UserDetails;

/**
 * Created by shadim on 9/14/2014.
 */
public class WindowsIntegratedSecurityUserDetails implements UserDetails {

    private String passoword;
    private String userName;

    @Override
    public Authority[] getAuthorities() {
        return new Authority[0];
    }

    @Override
    public String getPassword() {
        return passoword;
    }

    @Override
    public String getUsername() {
        return userName;
    }
}
