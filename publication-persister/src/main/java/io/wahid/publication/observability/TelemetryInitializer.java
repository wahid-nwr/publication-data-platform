package io.wahid.publication.observability;

//import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;


public class TelemetryInitializer {
    /*public static OpenTelemetry initTelemetry() {
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .setResource(Resource.getDefault())
                .build();

        PrometheusHttpServer prometheusServer =
                PrometheusHttpServer.builder()
                        .setPort(9464)
                        .setHost("0.0.0.0")
                        .setMeterProvider(meterProvider)
                        .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();

        System.out.println("✅ Prometheus metrics exposed at http://localhost:9464/metrics");
        return openTelemetry;
    }*/
}
