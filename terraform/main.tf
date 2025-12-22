terraform {
  required_version = ">= 1.6.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

# -------------------------------
# Provider
# -------------------------------
provider "google" {
  project = var.project_id
  region  = var.region
}

# -------------------------------
# Artifact Registry (Container)
# -------------------------------
resource "google_artifact_registry_repository" "app_repo" {
  location       = var.region
  repository_id  = "app-repo"
  description    = "Minimal app container repo"
  format         = "DOCKER"
}

# -------------------------------
# Service Account
# -------------------------------
resource "google_service_account" "run_sa" {
  account_id   = "cloudrun-sa"
  display_name = "Cloud Run Service Account"
}

resource "google_project_iam_member" "run_sa_storage_reader" {
  project = var.project_id
  role    = "roles/storage.objectViewer"
  member  = "serviceAccount:${google_service_account.run_sa.email}"
}

resource "google_secret_manager_secret" "firebase_sa" {
  secret_id = "firebase-service-account"

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_iam_member" "firebase_access" {
  secret_id = google_secret_manager_secret.firebase_sa.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.run_sa.email}"
}

resource "google_project_iam_member" "firebase_auth_admin" {
  project = var.project_id
  role    = "roles/firebaseauth.admin"
  member  = "serviceAccount:${google_service_account.run_sa.email}"
}

resource "google_project_iam_member" "firestore_user" {
  project = var.project_id
  role    = "roles/datastore.user"
  member  = "serviceAccount:${google_service_account.run_sa.email}"
}
# resource "null_resource" "run_after_apply" {
#   triggers = {
#     job_id = google_cloud_run_v2_job.csv_job.id
#   }
#
#   provisioner "local-exec" {
#     command = "gcloud run jobs execute ${google_cloud_run_v2_job.csv_job.name} --region=${var.region} --project=${var.project_id}"
#   }
# }

# -------------------------------
# IAM: Allow all-users (optional)
# -------------------------------
# resource "google_cloud_run_v2_job_iam_member" "invoker" {
#   name     = google_cloud_run_v2_job.csv_job.name
#   location = var.region
#   role     = "roles/run.invoker"
#   member   = "allUsers"  # remove for private service
# }
