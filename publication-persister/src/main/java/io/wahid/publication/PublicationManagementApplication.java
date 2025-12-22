package io.wahid.publication;

import io.wahid.publication.controller.PersisterServlet;
import io.wahid.publication.exception.GlobalExceptionFilter;
import io.wahid.publication.observability.MetricsServer;
import io.wahid.publication.security.JwtFilter;
import io.wahid.publication.util.AppConfig;
import io.wahid.publication.util.KafkaUtil;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import jakarta.servlet.DispatcherType;
import org.apache.kafka.clients.admin.AdminClient;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicationManagementApplication {

    private static final Logger LOGGER = Logger.getLogger(PublicationManagementApplication.class.getName());
    public static final String ZOOKEEPER_HOST = AppConfig.getProperty("zookeeper.host");
    public static final int ZOOKEEPER_PORT = AppConfig.getIntProperty("zookeeper.port");
    public static final String KAFKA_BOOTSTRAP_SERVERS = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
    public static final String ZOOKEEPER_ADDR = ZOOKEEPER_HOST + ":" + ZOOKEEPER_PORT;
    private static final String PORT = System.getenv().getOrDefault("WEB_PORT", "8080");

    public static void main(String[] args) throws Exception {
        LOGGER.log(Level.INFO, "Starting metric server.");
        new MetricsServer().start(9494);

        // ... your existing Kafka + DB logic here
        new ClassLoaderMetrics().bindTo(MetricsServer.MetricsRegistry.REGISTRY);
        new JvmMemoryMetrics().bindTo(MetricsServer.MetricsRegistry.REGISTRY);
        new JvmThreadMetrics().bindTo(MetricsServer.MetricsRegistry.REGISTRY);
        new ProcessorMetrics().bindTo(MetricsServer.MetricsRegistry.REGISTRY);
        LOGGER.log(Level.INFO, "Lookout for kafka.");
        waitForKafka();

        Map<String, Handler> handlerMap = SecurityMain.initiateSecurity();
        LOGGER.log(Level.INFO, "Initializing servlet context for web server.");
        ServletContextHandler servletContextHandler = (ServletContextHandler) handlerMap.get("servlet");

        if (servletContextHandler == null) {
            servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        }

        servletContextHandler.addServlet(PersisterServlet.class, "/api/*");

        FilterHolder exceptionFilterHolder = new FilterHolder(new GlobalExceptionFilter());
        servletContextHandler.addFilter(exceptionFilterHolder, "/*", EnumSet.of(
                DispatcherType.REQUEST,
                DispatcherType.ASYNC,
                DispatcherType.ERROR
        ));
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.addHandler(servletContextHandler);
        int port = Integer.parseInt(PORT);
        Server server = new Server(port);
        server.setHandler(handlers);
        LOGGER.log(Level.INFO, "starting persister web server at: {0}", PORT);
        server.start();
        server.join();
    }

    private static void waitForKafka() {
        int retries = 0;
        while (retries < 30) {
            try (AdminClient admin = AdminClient.create(KafkaUtil.getProducerProperties(KAFKA_BOOTSTRAP_SERVERS))) {
                admin.listTopics().names().get();
                LOGGER.info("✅ Kafka connection established.");
                return;
            } catch (Exception ex) {
                retries++;
                LOGGER.info("⏳ Waiting for Kafka... at " + KAFKA_BOOTSTRAP_SERVERS + ", attempt " + retries);
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }

        throw new RuntimeException("Kafka not reachable via Admin Client after 30 attempts");
    }
}
