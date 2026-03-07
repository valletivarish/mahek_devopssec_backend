# =============================================================================
# Input Variables for Event RSVP & Attendance Manager Infrastructure
# =============================================================================
# This file defines all input variables used across the Terraform configuration.
# Variables allow the infrastructure to be customized without modifying the
# resource definitions, supporting different environments (dev, staging, prod).
# Sensitive values like db_password have no defaults and must be provided
# at plan/apply time via -var, .tfvars files, or environment variables.
# =============================================================================

# -----------------------------------------------------------------------------
# AWS Region
# -----------------------------------------------------------------------------
# The AWS region where all infrastructure resources will be deployed.
# Default is eu-west-1 (Ireland) which provides low latency for European
# users and has full service availability.
# -----------------------------------------------------------------------------
variable "aws_region" {
  description = "The AWS region where all resources will be provisioned"
  type        = string
  default     = "eu-west-1"
}

# -----------------------------------------------------------------------------
# EC2 Instance Type
# -----------------------------------------------------------------------------
# The instance type for the EC2 backend server. t2.micro is free-tier
# eligible and suitable for development/testing workloads. For production,
# consider t3.small or larger based on traffic and processing requirements.
# -----------------------------------------------------------------------------
variable "instance_type" {
  description = "EC2 instance type for the backend application server"
  type        = string
  default     = "t2.micro"
}

# -----------------------------------------------------------------------------
# RDS Instance Class
# -----------------------------------------------------------------------------
# The instance class for the RDS MySQL database. db.t3.micro is the smallest
# available class and is suitable for development workloads. For production,
# consider db.t3.medium or larger based on query volume and data size.
# -----------------------------------------------------------------------------
variable "db_instance_class" {
  description = "RDS instance class for the MySQL database"
  type        = string
  default     = "db.t3.micro"
}

# -----------------------------------------------------------------------------
# Database Username
# -----------------------------------------------------------------------------
# The master username for the RDS MySQL instance. Default is "admin".
# This user has full administrative privileges on the database.
# For production, consider using a more specific username and creating
# separate application-level database users with limited permissions.
# -----------------------------------------------------------------------------
variable "db_username" {
  description = "Master username for the RDS MySQL database"
  type        = string
  default     = "admin"
}

# -----------------------------------------------------------------------------
# Database Password (Sensitive)
# -----------------------------------------------------------------------------
# The master password for the RDS MySQL instance. This variable is marked
# as sensitive so its value will not appear in Terraform plan output or
# state logs. No default is provided - it MUST be supplied at runtime
# via -var flag, terraform.tfvars file, or TF_VAR_db_password env variable.
# Password must meet MySQL requirements (min 8 characters).
# -----------------------------------------------------------------------------
variable "db_password" {
  description = "Master password for the RDS MySQL database (must be at least 8 characters)"
  type        = string
  sensitive   = true
}

# -----------------------------------------------------------------------------
# EC2 Key Pair Name
# -----------------------------------------------------------------------------
# The name of an existing AWS EC2 key pair used for SSH access to the
# backend instance. The key pair must already exist in the target AWS
# region before running terraform apply. No default is provided - it
# MUST be supplied to enable SSH access to the EC2 instance.
# -----------------------------------------------------------------------------
variable "key_pair_name" {
  description = "Name of an existing EC2 key pair for SSH access to the backend instance"
  type        = string
}

# -----------------------------------------------------------------------------
# S3 Bucket Name
# -----------------------------------------------------------------------------
# The globally unique name for the S3 bucket hosting the frontend static
# website. S3 bucket names must be unique across ALL AWS accounts worldwide.
# No default is provided - choose a unique name like
# "event-rsvp-frontend-<your-identifier>". Must comply with S3 naming rules:
# lowercase, 3-63 characters, no underscores or uppercase letters.
# -----------------------------------------------------------------------------
variable "s3_bucket_name" {
  description = "Globally unique name for the S3 bucket hosting the frontend static website"
  type        = string
}

# -----------------------------------------------------------------------------
# Environment
# -----------------------------------------------------------------------------
# The deployment environment name used for tagging and resource naming.
# Helps identify resources belonging to different environments (development,
# staging, production) in the AWS console and for cost allocation.
# Default is "development" for local/dev deployments.
# -----------------------------------------------------------------------------
variable "environment" {
  description = "Deployment environment name (e.g., development, staging, production)"
  type        = string
  default     = "development"
}
