variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "region" {
  description = "Deploy region"
  type        = string
  default     = "us-central1"
}

variable "vpc_connector" {
  description = "Cloud Run VPC connector name"
  type        = string
  default = "run-connector"
}

variable "csv_container_image" {
  description = "Csv container image url for Cloud Run"
  type        = string
}

variable "app_container_image" {
  description = "App container image url for Cloud Run"
  type        = string
}

variable "author_csv_path" {
  description = "GCP project ID"
  type        = string
}

variable "author_csv_charset" {
  description = "GCP project ID"
  type        = string
}

variable "book_csv_path" {
  description = "GCP project ID"
  type        = string
}

variable "book_csv_charset" {
  description = "GCP project ID"
  type        = string
}

variable "magazine_csv_path" {
  description = "GCP project ID"
  type        = string
}

variable "magazine_csv_charset" {
  description = "GCP project ID"
  type        = string
}

variable "kafka_bootstrap" {
  description = "GCP project ID"
  type        = string
}

variable "kafka_schema" {
  description = "GCP project ID"
  type        = string
}

variable "kafka_security_protocol" {
  description = "GCP project ID"
  type        = string
}

variable "kafka_sasl_mechanism" {
  description = "GCP project ID"
  type        = string
}

variable "kafka_sasl_jaas_config" {
  description = "GCP project ID"
  type        = string
}

variable "log_level" {
  description = "GCP project ID"
  type        = string
}

variable "is_from_volume" {
  description = "GCP project ID"
  type        = string
}

variable "jdbc_url" {
  description = "JDBC url"
  type        = string
}

variable "db_host" {
  description = "db host"
  type        = string
}

variable "db_port" {
  description = "db port"
  type        = string
}