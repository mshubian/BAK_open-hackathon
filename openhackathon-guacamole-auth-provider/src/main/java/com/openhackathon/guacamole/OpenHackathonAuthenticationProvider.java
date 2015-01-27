package com.openhackathon.guacamole;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnection;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnectionDirectory;
import org.glyptodon.guacamole.properties.StringGuacamoleProperty;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;



import java.util.*;


public class OpenHackathonAuthenticationProvider extends SimpleAuthenticationProvider {
	
    private Logger logger = LoggerFactory.getLogger(OpenHackathonAuthenticationProvider.class.getClass());
	
    private static final StringGuacamoleProperty AUTH_REQUEST_URL = new StringGuacamoleProperty() {
        @Override
        public String getName() { return "auth-request-url"; }
    };

    /*constructed functions*/
    
    public OpenHackathonAuthenticationProvider() {
    	logger.debug("==============================gucamole authentication jar log init =============================================");
    }

    @Override
    public Map<String, GuacamoleConfiguration> getAuthorizedConfigurations(Credentials credentials) throws GuacamoleException {

        GuacamoleConfiguration config = getGuacamoleConfiguration(credentials.getRequest());

        if (config == null) {
            return null;
        }
        Map<String, GuacamoleConfiguration> configs = new HashMap<String, GuacamoleConfiguration>();
        configs.put(config.getConnectionID(), config);
        logger.debug("======================put configuration into The getAuthorizedConfigurations");
        return configs;
    }

    
    @Override
    public UserContext updateUserContext(UserContext context, Credentials credentials) throws GuacamoleException {
        HttpServletRequest request = credentials.getRequest();
        GuacamoleConfiguration config = getGuacamoleConfiguration(request);
      
        if (config == null) {
            return null;
        }
        
        String name = config.getParameter("name");
        logger.debug(" CLASS is "+ context.getRootConnectionGroup().getConnectionDirectory().getClass().getName());
        SimpleConnectionDirectory connections = (SimpleConnectionDirectory) context.getRootConnectionGroup().getConnectionDirectory();
        logger.debug("======================get info from GuacamoleConfiguration name:"+ name);
        logger.info("protocal select :" + config.getProtocol() );
        SimpleConnection connection = new SimpleConnection(name, name, config);
        connections.putConnection(connection);
        return context;
    }

    private GuacamoleConfiguration getGuacamoleConfiguration(HttpServletRequest request) throws GuacamoleException {
    	
        GuacamoleConfiguration config ;
        String jsonString = null;
        
        String tokenString = request.getParameter("token");
        
        String connectionName = request.getParameter("id").substring(2);
        
        logger.info("tokenString is : |" + tokenString);
        
               
        /*check user valid or not*/
        try {
            
            String authRequestURL = GuacamoleProperties.getProperty(AUTH_REQUEST_URL);
            logger.debug("==============================OpenHackathon guacd Auth request URL is : " + authRequestURL);
			
            Connect2OpenHackathon conn = new Connect2OpenHackathon(authRequestURL);
            jsonString = conn.getGuacamoleJSONString(connectionName,tokenString);
            logger.debug("==============================get guacamole config json String :" + jsonString);
			
            String finalString = jsonString.substring(1, jsonString.length()-1).replace("\\", "");
            Trans2GuacdConfiguration trans = new Trans2GuacdConfiguration(finalString);
            config = trans.getConfiguration();
			
            return config ;
        } catch (Exception e) {
            logger.error("=============================Exception when connect with open-hackathon to check User login");
            e.printStackTrace();
            return null;
        }

    }
}
