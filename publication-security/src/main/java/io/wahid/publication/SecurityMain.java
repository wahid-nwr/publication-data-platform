package io.wahid.publication;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import io.wahid.publication.security.*;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Map;

public class SecurityMain {

    public static Map<String, Handler> initiateSecurity() throws MalformedURLException {
        System.out.println("Hello world from security!");

        FirebaseInit.initialize();

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setWelcomeFiles(new String[]{"login.html"});
        URL resourceUrl = SecurityMain.class.getClassLoader().getResource("public");
        resourceHandler.setBaseResource(Resource.newResource(resourceUrl));
        ContextHandler staticContentHandler = new ContextHandler("/");
        staticContentHandler.setHandler(resourceHandler);

        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/");
        servletContext.addServlet(new ServletHolder(new HelloServlet()), "/api/hello");
        servletContext.addServlet(new ServletHolder(new LoginServlet()), "/auth/login");
        JwtConfig cfg = new JwtConfig(
                "https://www.googleapis.com/oauth2/v3/certs",
                "https://securetoken.google.com/alert-cursor-476219-s1"
        );
        JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(new URL(cfg.getJwksUri())).build();
        FilterHolder jwtFilterHolder = new FilterHolder(new JwtFilter(cfg, jwkSource));
        servletContext.addFilter(jwtFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

        return Map.of("servlet", servletContext, "static", staticContentHandler);
    }

    public static void main(String[] args) {
        System.out.println("Security main!!");
    }
}