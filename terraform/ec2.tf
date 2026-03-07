# =============================================================================
# EC2 Instance Configuration for Event RSVP & Attendance Manager
# =============================================================================
# This file provisions an EC2 instance to host the Spring Boot backend
# application. It includes the AMI data source, security group rules,
# and a user data script that installs Java 17 and configures a systemd
# service for automated application startup.
# =============================================================================

# -----------------------------------------------------------------------------
# Data Source: Latest Amazon Linux 2023 AMI
# -----------------------------------------------------------------------------
# Dynamically fetches the most recent Amazon Linux 2023 AMI from AWS.
# This ensures we always use an up-to-date, patched base image without
# hardcoding AMI IDs that vary by region and become outdated over time.
# Filters ensure we get the correct architecture (x86_64) and HVM type.
# -----------------------------------------------------------------------------
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}

# -----------------------------------------------------------------------------
# Security Group for EC2 Instance
# -----------------------------------------------------------------------------
# Controls inbound and outbound network traffic to the EC2 instance.
# Inbound rules allow:
#   - SSH (port 22)   : Remote administration and deployment
#   - HTTP (port 80)  : Standard web traffic
#   - HTTPS (port 443): Encrypted web traffic
#   - Spring Boot (port 8080): The backend application's default port
# Outbound: All traffic is allowed so the instance can download packages,
# communicate with RDS, and reach external services.
# -----------------------------------------------------------------------------
resource "aws_security_group" "ec2_security_group" {
  name        = "event-rsvp-ec2-sg"
  description = "Security group for the Event RSVP backend EC2 instance"
  vpc_id      = aws_vpc.event_rsvp_vpc.id

  # SSH access - allows remote administration via SSH
  ingress {
    description = "Allow SSH access for remote administration"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTP access - allows standard unencrypted web traffic
  ingress {
    description = "Allow HTTP traffic"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS access - allows encrypted web traffic
  ingress {
    description = "Allow HTTPS traffic"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Spring Boot application port - the backend API listens on 8080
  ingress {
    description = "Allow Spring Boot application traffic on port 8080"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Outbound traffic - allow all outbound connections so the instance
  # can download packages, connect to RDS, and reach external APIs
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "event-rsvp-ec2-sg"
    Environment = var.environment
  }
}

# -----------------------------------------------------------------------------
# EC2 Instance
# -----------------------------------------------------------------------------
# The main compute instance running the Spring Boot backend application.
# - Uses a t2.micro instance type (free-tier eligible) for cost efficiency
# - Deployed in the public subnet so it can serve API requests
# - The user data script automates the setup of Java 17 and creates a
#   systemd service so the Spring Boot JAR runs as a managed background
#   process that starts automatically on boot.
# - depends_on ensures the Internet Gateway is ready before the instance
#   launches, so the user data script can download packages.
# -----------------------------------------------------------------------------
resource "aws_instance" "event_rsvp_backend" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public_subnet.id
  vpc_security_group_ids      = [aws_security_group.ec2_security_group.id]
  key_name                    = var.key_pair_name
  associate_public_ip_address = true

  # User data script - runs on first boot to configure the instance
  # Installs Java 17 (Amazon Corretto), creates application directories,
  # and sets up a systemd service for the Spring Boot application
  user_data = <<-EOF
              #!/bin/bash
              # =============================================================
              # EC2 Instance Bootstrap Script
              # Purpose: Install Java 17 and configure systemd service for
              #          the Event RSVP Spring Boot backend application
              # =============================================================

              # Update all system packages to the latest versions
              sudo yum update -y

              # Install Amazon Corretto 17 (Java 17 JDK) - required runtime
              # for the Spring Boot backend application
              sudo yum install -y java-17-amazon-corretto-devel

              # Create a dedicated directory for the application JAR file
              sudo mkdir -p /opt/event-rsvp

              # Create a dedicated system user to run the application
              # (security best practice - avoid running as root)
              sudo useradd -r -s /sbin/nologin eventrsvp || true
              sudo chown -R eventrsvp:eventrsvp /opt/event-rsvp

              # Create a systemd service unit file for the Spring Boot app
              # This allows the application to:
              #   - Start automatically on system boot
              #   - Be managed with systemctl (start/stop/restart/status)
              #   - Restart automatically if it crashes
              sudo tee /etc/systemd/system/event-rsvp.service > /dev/null <<'SERVICE'
              [Unit]
              Description=Event RSVP and Attendance Manager - Spring Boot Backend
              After=network.target

              [Service]
              Type=simple
              User=eventrsvp
              Group=eventrsvp
              WorkingDirectory=/opt/event-rsvp
              ExecStart=/usr/bin/java -jar /opt/event-rsvp/event-rsvp-backend.jar --server.port=8080
              Restart=on-failure
              RestartSec=10
              StandardOutput=journal
              StandardError=journal
              SyslogIdentifier=event-rsvp

              # Environment variables for the Spring Boot application
              Environment=SPRING_PROFILES_ACTIVE=production
              Environment=SERVER_PORT=8080

              [Install]
              WantedBy=multi-user.target
              SERVICE

              # Reload systemd to recognize the new service file
              sudo systemctl daemon-reload

              # Enable the service to start on boot (will start once JAR is deployed)
              sudo systemctl enable event-rsvp.service

              echo "EC2 bootstrap complete - Java 17 installed and systemd service configured"
              EOF

  tags = {
    Name        = "event-rsvp-backend"
    Environment = var.environment
    Project     = "EventRSVPAndAttendanceManager"
  }

  # Ensure the Internet Gateway exists before launching the instance,
  # so the user data script can access the internet to download packages
  depends_on = [aws_internet_gateway.event_rsvp_igw]
}
