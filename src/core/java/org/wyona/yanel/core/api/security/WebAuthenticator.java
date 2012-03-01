package org.wyona.yanel.core.api.security;

import org.wyona.yanel.core.map.Map;
import org.wyona.yanel.core.map.Realm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Web authentication interface, which allows each realm to have its custom web authenticator if necessary
 */
public interface WebAuthenticator {

    /**
     *
     */
    public void init(org.w3c.dom.Document configuration, javax.xml.transform.URIResolver resolver) throws Exception;

    /**
     * Allows to do custom user authentication and to generate a custom HTTP response if authentication failed
     * @return HTTP response if authentication failed, whereas if authentication successful, then return null
     */
    public HttpServletResponse doAuthenticate(HttpServletRequest request, HttpServletResponse response, Map map, String reservedPrefix, String xsltLoginScreenDefault, String servletContextRealPath, String sslPort) throws ServletException, IOException;

    /**
     *
     */
     public void getXHTMLAuthenticationForm(HttpServletRequest request, HttpServletResponse response, Realm realm, String message, String reservedPrefix, String xsltLoginScreenDefault, String servletContextRealPath, String sslPort, Map map) throws ServletException, IOException;
}
