package io.wahid.publication;

import io.wahid.publication.controller.CsvParserServlet;
import io.wahid.publication.util.KafkaUtil;
import org.apache.kafka.clients.admin.AdminClient;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.Map;
import java.util.logging.Logger;

public class CsvMain {
    private static final Logger LOGGER = Logger.getLogger(CsvMain.class.getName());
    public static final String KAFKA_ADDR = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
    public static final String KAFKA_HOST = KAFKA_ADDR.split(":")[0];
    public static final String KAFKA_PORT = KAFKA_ADDR.split(":")[1];
    public static final String FILE_SOURCE_VOLUME = System.getenv("fromVolume");
    private static final String PORT = System.getenv().getOrDefault("WEB_PORT", "8080");

    public static void main(String[] args) throws Exception {
        waitForKafka();
        Map<String, Handler> handlerMap = SecurityMain.initiateSecurity();
        ServletContextHandler servletContextHandler =
                (ServletContextHandler) handlerMap.get("servlet");

        if (servletContextHandler == null) {
            servletContextHandler =
                    new ServletContextHandler(ServletContextHandler.SESSIONS);
        }
        servletContextHandler.addServlet(CsvParserServlet.class, "/api/*");

        int port = Integer.parseInt(PORT);
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.addHandler(servletContextHandler);
        Server server = new Server(port);
        server.setHandler(handlers);
        System.out.println("starting csv web server at: " + PORT);
        server.start();
        server.join();
        /*
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("0.0.0.0");
        connector.setPort(port);

        server.setConnectors(new Connector[]{connector});
         */
    }

    private static void waitForKafka() {
        int retries = 0;
        while (retries < 30) {
            try (AdminClient admin = AdminClient.create(KafkaUtil.getProducerProperties(KAFKA_ADDR))) {
                admin.listTopics().names().get();
                LOGGER.info("✅ Kafka connection established.");
                return;
            } catch (Exception ex) {
                retries++;
                LOGGER.info("⏳ Waiting for Kafka... at " + KAFKA_ADDR + ", attempt " + retries);
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }

        throw new RuntimeException("Kafka not reachable via Admin Client after 30 attempts");
    }
}