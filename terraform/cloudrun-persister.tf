# -------------------------------
# Cloud Run Service
# -------------------------------
resource "google_cloud_run_v2_service" "csv_persister" {
  name     = "csv-persister"
  location = var.region

  template {
    revision = "csv-persister-${substr(md5(timestamp()), 0, 8)}"
    # FREE TIER SAFE — scale to zero
    scaling {
      min_instance_count = 0
      max_instance_count = 1
    }
    volumes {
      name = "firebase-secret"

      secret {
        secret = google_secret_manager_secret.firebase_sa.secret_id

        items {
          path    = "firebase"
          version = "latest"
        }
      }
    }
    containers {
      image = "us-central1-docker.pkg.dev/alert-cursor-476219-s1/publication-repo/publication-persister@sha256:5b510f069d5a6a4f9f56e15419b5959bd3c1543e6b4fc4f9792a4b990469f2f9"
      volume_mounts {
        name       = "firebase-secret"
        mount_path = "/run/secrets"
      }
      # --------------------------------------
      # Environment variables for your app
      # --------------------------------------
      env {
        name  = "GOOGLE_APPLICATION_CREDENTIALS"
        value = "/run/secrets/firebase"
      }
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
        name = "WEB3_PRIVATE_KEY"
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
        name  = "JPA_JDBC_URL"
        value = var.jdbc_url
      }
      env {
        name  = "DB_HOST"
        value = var.db_host
      }
      env {
        name  = "DB_PORT"
        value = var.db_port
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