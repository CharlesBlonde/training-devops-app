/*
 * Copyright 2008-2009 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.productionready;

import fr.xebia.springframework.security.core.userdetails.ExtendedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Initialize the application (add rows in the database, etc).
 * </p>
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 */
public class Initializer implements InitializingBean {

    private DataSource dataSource;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UserDetailsManager userDetailsManager;

    /**
     * Loads spring Security users in the database.
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Connection connection = dataSource.getConnection();
        try {
            String createUsersTable = "create table users(username varchar(256), password varchar(256), enabled int, allowedRemoteAddresses varchar(256), comments varchar(256))";
            connection.createStatement().execute(createUsersTable);

            String createAuthoritiesTable = "create table authorities(username varchar(256), authority varchar(256))";
            connection.createStatement().execute(createAuthoritiesTable);

        } finally {
            connection.close();
        }

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        ExtendedUser user = new ExtendedUser("admin", "admin", true, true, true, true, authorities);
        user.setComments("my first comment");
        user.setAllowedRemoteAddresses("10\\..*, 127\\..*, 0:0:0:0:0:0:0:1%0");

        userDetailsManager.createUser(user);

        logger.warn("Application successfully initialized");
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public UserDetailsManager getUserDetailsManager() {
        return userDetailsManager;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setUserDetailsManager(UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
    }

}
