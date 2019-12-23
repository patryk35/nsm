variable "aws_secrets_manager_id" {
}

variable "aws_secrets_manager_key" {
}

variable "app_server_image_tag" {
  description = "Docker image tag for server appliction"
}

variable "app_client_image_tag" {
  description = "Docker image tag for client appliction"
}

variable "aws_region" {
  description = "AWS region"
}