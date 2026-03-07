# =============================================================================
# RDS MySQL Database Configuration for Event RSVP & Attendance Manager
# =============================================================================
# This file provisions an RDS MySQL instance to store application data
# including events, RSVPs, attendees, and attendance records. The database
# is placed in a private subnet for security, accessible only from the
# EC2 backend instance.
# =============================================================================

# -----------------------------------------------------------------------------
# DB Subnet Group
# -----------------------------------------------------------------------------
# A DB subnet group defines which subnets RDS can use for the database
# instance. RDS requires at least two subnets in different Availability
# Zones for Multi-AZ deployments. Even for single-AZ, AWS requires the
# subnet group to be defined. We include both the public and private
# subnets to satisfy this requirement, but the instance is placed in
# the private subnet for security.
# -----------------------------------------------------------------------------
resource "aws_db_subnet_group" "event_rsvp_db_subnet_group" {
  name        = "event-rsvp-db-subnet-group"
  description = "Subnet group for the Event RSVP RDS MySQL database"

  # Include both subnets to meet the multi-AZ requirement;
  # the actual instance will reside in the private subnet
  subnet_ids = [
    aws_subnet.public_subnet.id,
    aws_subnet.private_subnet.id
  ]

  tags = {
    Name        = "event-rsvp-db-subnet-group"
    Environment = var.environment
  }
}

# -----------------------------------------------------------------------------
# Security Group for RDS Instance
# -----------------------------------------------------------------------------
# This security group restricts database access to ONLY the EC2 backend
# instance. By referencing the EC2 security group as the source, we ensure
# that only traffic originating from the backend application server can
# reach the MySQL database on port 3306. This follows the principle of
# least privilege and prevents direct database access from the internet.
# -----------------------------------------------------------------------------
resource "aws_security_group" "rds_security_group" {
  name        = "event-rsvp-rds-sg"
  description = "Security group for the Event RSVP RDS MySQL instance - allows access only from EC2"
  vpc_id      = aws_vpc.event_rsvp_vpc.id

  # MySQL inbound rule - allow connections ONLY from the EC2 security group
  # This ensures the database is not accessible from the public internet
  ingress {
    description     = "Allow MySQL traffic from the EC2 backend instance only"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2_security_group.id]
  }

  # Outbound traffic - allow all outbound connections
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "event-rsvp-rds-sg"
    Environment = var.environment
  }
}

# -----------------------------------------------------------------------------
# RDS MySQL Instance
# -----------------------------------------------------------------------------
# The primary database for the Event RSVP application. Key design decisions:
#   - db.t3.micro: Cost-effective instance class for development workloads
#   - MySQL 8.0: Matches the Spring Boot application's expected DB engine
#   - 20 GB gp3 storage: Sufficient for development with good IOPS baseline
#   - Private subnet: Database is not publicly accessible for security
#   - skip_final_snapshot: Set to true for dev environment to allow easy
#     teardown; this should be false in production to prevent data loss
#   - Credentials are sourced from variables (db_password is sensitive)
# -----------------------------------------------------------------------------
resource "aws_db_instance" "event_rsvp_database" {
  identifier     = "event-rsvp-database"
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.db_instance_class

  # Storage configuration - 20 GB general purpose SSD (gp3)
  allocated_storage = 20
  storage_type      = "gp3"

  # Database credentials and name
  # The db_password variable is marked as sensitive in variables.tf
  db_name  = "event_rsvp_db"
  username = var.db_username
  password = var.db_password

  # Network configuration - place in private subnet, not publicly accessible
  db_subnet_group_name   = aws_db_subnet_group.event_rsvp_db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_security_group.id]
  publicly_accessible    = false

  # Backup and maintenance configuration
  # Skip final snapshot for development - enables easy terraform destroy
  # WARNING: Set skip_final_snapshot to false in production environments
  skip_final_snapshot       = true
  final_snapshot_identifier = "event-rsvp-db-final-snapshot"
  backup_retention_period   = 7

  # Parameter group - use default MySQL 8.0 parameters
  parameter_group_name = "default.mysql8.0"

  tags = {
    Name        = "event-rsvp-database"
    Environment = var.environment
    Project     = "EventRSVPAndAttendanceManager"
  }

  # Ensure the DB subnet group is fully created before provisioning RDS
  depends_on = [aws_db_subnet_group.event_rsvp_db_subnet_group]
}
