# =============================================================================
# Main Terraform Configuration for Event RSVP & Attendance Manager
# =============================================================================
# This file defines the core networking infrastructure on AWS including
# the VPC, subnets, internet gateway, and routing configuration.
# All resources are deployed in the eu-west-1 (Ireland) region.
# =============================================================================

# -----------------------------------------------------------------------------
# Terraform Settings & Provider Configuration
# -----------------------------------------------------------------------------
# Specifies the required Terraform version and configures the AWS provider
# to deploy resources in the eu-west-1 region.
# -----------------------------------------------------------------------------
terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# AWS Provider - configures the region for all resources in this configuration
provider "aws" {
  region = var.aws_region
}

# -----------------------------------------------------------------------------
# VPC (Virtual Private Cloud)
# -----------------------------------------------------------------------------
# The VPC provides an isolated virtual network for the Event RSVP application.
# CIDR block 10.0.0.0/16 gives us 65,536 IP addresses to work with,
# allowing room for multiple subnets and future expansion.
# DNS support and hostnames are enabled so EC2 instances receive DNS names.
# -----------------------------------------------------------------------------
resource "aws_vpc" "event_rsvp_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name        = "event-rsvp-vpc"
    Environment = var.environment
    Project     = "EventRSVPAndAttendanceManager"
  }
}

# -----------------------------------------------------------------------------
# Public Subnet
# -----------------------------------------------------------------------------
# The public subnet hosts internet-facing resources such as the EC2 instance
# running the Spring Boot backend. Instances launched here automatically
# receive a public IP address for direct internet access.
# Uses CIDR block 10.0.1.0/24 (256 addresses) in availability zone "a".
# -----------------------------------------------------------------------------
resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.event_rsvp_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name        = "event-rsvp-public-subnet"
    Environment = var.environment
    Type        = "Public"
  }
}

# -----------------------------------------------------------------------------
# Private Subnet
# -----------------------------------------------------------------------------
# The private subnet hosts resources that should NOT be directly accessible
# from the internet, such as the RDS MySQL database. This provides an
# additional layer of security by isolating the database from public traffic.
# Uses CIDR block 10.0.2.0/24 (256 addresses) in availability zone "b".
# -----------------------------------------------------------------------------
resource "aws_subnet" "private_subnet" {
  vpc_id            = aws_vpc.event_rsvp_vpc.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = "${var.aws_region}b"

  tags = {
    Name        = "event-rsvp-private-subnet"
    Environment = var.environment
    Type        = "Private"
  }
}

# -----------------------------------------------------------------------------
# Internet Gateway
# -----------------------------------------------------------------------------
# The Internet Gateway enables communication between resources in the VPC
# and the public internet. It is required for the EC2 instance in the public
# subnet to serve HTTP/HTTPS traffic and accept SSH connections.
# -----------------------------------------------------------------------------
resource "aws_internet_gateway" "event_rsvp_igw" {
  vpc_id = aws_vpc.event_rsvp_vpc.id

  tags = {
    Name        = "event-rsvp-igw"
    Environment = var.environment
  }
}

# -----------------------------------------------------------------------------
# Public Route Table
# -----------------------------------------------------------------------------
# This route table directs all outbound traffic (0.0.0.0/0) from the public
# subnet through the Internet Gateway, enabling internet access for resources
# like the EC2 instance. Without this route, instances in the public subnet
# would not be able to reach or be reached from the internet.
# -----------------------------------------------------------------------------
resource "aws_route_table" "public_route_table" {
  vpc_id = aws_vpc.event_rsvp_vpc.id

  # Default route - sends all non-local traffic to the Internet Gateway
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.event_rsvp_igw.id
  }

  tags = {
    Name        = "event-rsvp-public-rt"
    Environment = var.environment
  }
}

# -----------------------------------------------------------------------------
# Route Table Association
# -----------------------------------------------------------------------------
# Associates the public route table with the public subnet so that instances
# in the public subnet use the Internet Gateway for outbound traffic.
# Without this association, the subnet would use the VPC's default (main)
# route table, which does not include a route to the Internet Gateway.
# -----------------------------------------------------------------------------
resource "aws_route_table_association" "public_rt_association" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_route_table.id
}
