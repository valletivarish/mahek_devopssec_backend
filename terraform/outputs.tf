# =============================================================================
# Output Values for Event RSVP & Attendance Manager Infrastructure
# =============================================================================
# This file defines the output values that are displayed after a successful
# terraform apply. These outputs provide the essential connection information
# needed to access and configure the deployed infrastructure, including
# the backend server IP, database endpoint, and frontend website URL.
# =============================================================================

# -----------------------------------------------------------------------------
# EC2 Instance Public IP Address
# -----------------------------------------------------------------------------
# The public IPv4 address assigned to the EC2 backend instance.
# Use this IP to access the Spring Boot API (e.g., http://<ip>:8080)
# or to SSH into the instance (e.g., ssh -i key.pem ec2-user@<ip>).
# Note: This IP may change if the instance is stopped and restarted.
# For a permanent IP, consider using an Elastic IP (EIP).
# -----------------------------------------------------------------------------
output "ec2_public_ip" {
  description = "Public IP address of the EC2 backend instance"
  value       = aws_instance.event_rsvp_backend.public_ip
}

# -----------------------------------------------------------------------------
# EC2 Instance Public DNS
# -----------------------------------------------------------------------------
# The public DNS hostname assigned to the EC2 backend instance.
# This auto-generated hostname resolves to the instance's public IP.
# Useful for accessing the backend API without memorizing the IP address.
# Example: http://ec2-xx-xx-xx-xx.eu-west-1.compute.amazonaws.com:8080
# -----------------------------------------------------------------------------
output "ec2_public_dns" {
  description = "Public DNS hostname of the EC2 backend instance"
  value       = aws_instance.event_rsvp_backend.public_dns
}

# -----------------------------------------------------------------------------
# RDS MySQL Endpoint
# -----------------------------------------------------------------------------
# The connection endpoint for the RDS MySQL database instance.
# This value should be used in the Spring Boot application's configuration
# (application.properties or application.yml) as the JDBC URL host.
# Example JDBC URL: jdbc:mysql://<endpoint>:3306/event_rsvp_db
# Note: The endpoint includes the port number (e.g., hostname:3306).
# -----------------------------------------------------------------------------
output "rds_endpoint" {
  description = "Connection endpoint for the RDS MySQL database (host:port)"
  value       = aws_db_instance.event_rsvp_database.endpoint
}

# -----------------------------------------------------------------------------
# S3 Static Website URL
# -----------------------------------------------------------------------------
# The URL of the S3 static website hosting the frontend application.
# This is the public URL where users can access the Event RSVP frontend.
# The URL follows the format: http://<bucket>.s3-website-<region>.amazonaws.com
# For a custom domain, configure Route 53 to point to this endpoint.
# -----------------------------------------------------------------------------
output "s3_website_url" {
  description = "URL of the S3-hosted frontend static website"
  value       = aws_s3_bucket_website_configuration.frontend_website.website_endpoint
}

# -----------------------------------------------------------------------------
# VPC ID
# -----------------------------------------------------------------------------
# The unique identifier of the VPC created for the Event RSVP application.
# This ID is useful for:
#   - Referencing the VPC in other Terraform configurations
#   - Troubleshooting network issues in the AWS console
#   - Setting up VPC peering or additional networking resources
# -----------------------------------------------------------------------------
output "vpc_id" {
  description = "ID of the VPC created for the Event RSVP infrastructure"
  value       = aws_vpc.event_rsvp_vpc.id
}
