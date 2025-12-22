# -------------------------------
# Cloud Run Service
# -------------------------------
resource "google_cloud_run_v2_service" "csv_producer" {
  name     = "csv-parser"
  location = var.region

  template {
    revision = "csv-parser-${substr(md5(timestamp()), 0, 8)}"
    # FREE TIER SAFE — scale to zero
    scaling {
      min_instance_count = 0
      max_instance_count = 1
    }
    containers {
      image = "us-central1-docker.pkg.dev/alert-cursor-476219-s1/publication-repo/csv-producer@sha256:423291011936dc1bc368f83ac09683901cb2716d058e2f58bcb634aab28981a1"
      # --------------------------------------
      # Environment variables for your app
      # --------------------------------------
      env {
        name = "SEPOLIA_RPC_URL"
        value_source {
          secret_key_ref {
            secret  = "SEPOLIA_RPC_URL" # existing secret name
            version = "latest"
          }
        }
      }
      env {
        name = "PRIVATE_KEY"
        value_source {
          secret_key_ref {
            secret  = "WEB3_PRIVATE_KEY" # existing secret name
            version = "latest"
          }
        }
      }
      env {
        name = "CONTRACT_ADDRESS_V2"
        value_source {
          secret_key_ref {
            secret  = "CONTRACT_ADDRESS_V2" # existing secret name
            version = "latest"
          }
        }
      }
      env {
        name = "WEB_PORT"
        value = "8080"
      }
      env {
        name  = "AUTHOR_CSV_PATH"
        value = var.author_csv_path
      }
      env {
        name  = "AUTHOR_CSV_CHARSET"
        value = var.author_csv_charset
      }
      env {
        name  = "BOOK_CSV_PATH"
        value = var.book_csv_path
      }
      env {
        name  = "BOOK_CSV_CHARSET"
        value = var.book_csv_charset
      }
      env {
        name  = "MAGAZINE_CSV_PATH"
        value = var.magazine_csv_path
      }
      env {
        name  = "MAGAZINE_CSV_CHARSET"
        value = var.magazine_csv_charset
      }
      env {
        name  = "KAFKA_BOOTSTRAP_SERVERS"
        value = var.kafka_bootstrap
      }
      env {
        name  = "KAFKA_SCHEMA_REGISTRY"
        value = var.kafka_schema
      }
      env {
        name  = "KAFKA_SECURITY_PROTOCOL"
        value = var.kafka_security_protocol
      }
      env {
        name  = "KAFKA_SASL_MECHANISM"
        value = var.kafka_sasl_mechanism
      }
      env {
        name  = "KAFKA_SASL_JAAS_CONFIG"
        value = var.kafka_sasl_jaas_config
      }
      env {
        name  = "LOG_LEVEL"
        value = var.log_level
      }
      env {
        name  = "FROM_VOLUME"
        value = var.is_from_volume
      }
      env {
        name  = "JPA_JDBC_URL"
        value = var.jdbc_url
      }
      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"   # FREE TIER SAFE (you can bump to 1Gi if needed)
        }
      }
    }
    # ------------ VPC ACCESS (VALID) ------------
    vpc_access {
      connector = "projects/${var.project_id}/locations/${var.region}/connectors/${var.vpc_connector}"
      egress    = "PRIVATE_RANGES_ONLY"   # or PRIVATE_RANGES_ONLY
    }
    service_account = google_service_account.run_sa.email
    timeout = "30s" # microservice should respond fast
    #     ingress = "INGRESS_ALL"
  }
}