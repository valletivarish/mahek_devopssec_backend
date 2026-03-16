# =============================================================================
# S3 Bucket Configuration for Event RSVP & Attendance Manager Frontend
# =============================================================================
# This file provisions an S3 bucket configured for static website hosting.
# The React/Angular frontend application is built into static assets (HTML,
# CSS, JS) and served directly from S3, providing a cost-effective and
# highly available hosting solution without managing web servers.
# =============================================================================

# -----------------------------------------------------------------------------
# S3 Bucket
# -----------------------------------------------------------------------------
# The primary bucket that stores the frontend static website files.
# The bucket name must be globally unique across all AWS accounts.
# It is sourced from the s3_bucket_name variable.
# -----------------------------------------------------------------------------
resource "aws_s3_bucket" "frontend_bucket" {
  bucket = var.s3_bucket_name

  tags = {
    Name        = "event-rsvp-frontend"
    Environment = var.environment
    Project     = "EventRSVPAndAttendanceManager"
  }
}

# -----------------------------------------------------------------------------
# S3 Bucket Website Configuration
# -----------------------------------------------------------------------------
# Enables static website hosting on the S3 bucket. This configures S3 to
# serve files as a website with:
#   - index_document: The default page served when visiting the root URL
#   - error_document: The page served for 404 errors (also set to index.html
#     for single-page applications that handle routing client-side)
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_website_configuration" "frontend_website" {
  bucket = aws_s3_bucket.frontend_bucket.id

  # Default page - served when users visit the root URL of the website
  index_document {
    suffix = "index.html"
  }

  # Error page - served for 404 and other error responses
  # Using index.html supports client-side routing in SPAs
  error_document {
    key = "index.html"
  }
}

# -----------------------------------------------------------------------------
# S3 Bucket Public Access Block Configuration
# -----------------------------------------------------------------------------
# By default, S3 blocks all public access for security. For a static website,
# we need to allow public read access so visitors can view the frontend.
# This configuration disables the public access blocks to permit the bucket
# policy (below) to grant public read access.
# NOTE: In production, consider using CloudFront with an OAI instead of
# direct S3 public access for better security and performance.
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_public_access_block" "frontend_public_access" {
  bucket = aws_s3_bucket.frontend_bucket.id

  # Disable all public access blocks to allow the bucket policy
  # to grant public read access for static website hosting
  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# -----------------------------------------------------------------------------
# S3 Bucket Ownership Controls
# -----------------------------------------------------------------------------
# Sets the bucket ownership to BucketOwnerPreferred, which ensures the
# bucket owner has full control over all objects. This is required when
# allowing public access via ACLs or bucket policies.
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_ownership_controls" "frontend_ownership" {
  bucket = aws_s3_bucket.frontend_bucket.id

  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

# -----------------------------------------------------------------------------
# S3 Bucket Policy - Public Read Access
# -----------------------------------------------------------------------------
# This bucket policy grants public read access (s3:GetObject) to all objects
# in the bucket. This is necessary for static website hosting so that
# visitors' browsers can download HTML, CSS, JS, and image files.
# The policy uses the wildcard principal ("*") to allow unauthenticated access.
# depends_on ensures the public access block is configured first, otherwise
# the policy application will fail due to default public access restrictions.
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_policy" "frontend_bucket_policy" {
  bucket = aws_s3_bucket.frontend_bucket.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.frontend_bucket.arn}/*"
      }
    ]
  })

  # The public access block must be configured before applying this policy,
  # otherwise AWS will reject the policy as it conflicts with default blocks
  depends_on = [aws_s3_bucket_public_access_block.frontend_public_access]
}
