variable "region" {
  description = "AWS region"
  default = "eu-west-3"
}

variable "cluster_name" {
  default = "nsm-k8s"
}

variable "aws_secrets_manager_id" {
  default = "provide"
}

variable "aws_secrets_manager_key" {
  default = "provide"
}
variable "dockerfile_path" {
  default = "~/.docker/config.json"
}

variable "nsm_access_token" {
  default = "provide"
}

variable "agent_image_tag" {
  description = "Docker image tag for agent appliction - with registration"
  default = "registry.gitlab.com/orion17/network-services-monitor/app-agent:1.1.4-reg"
}

variable "app_server_image_tag" {
  description = "Docker image tag for server appliction"
  default = "registry.gitlab.com/orion17/network-services-monitor/app-server:1.1.0"
}

variable "app_client_image_tag" {
  description = "Docker image tag for client appliction"
  default = "registry.gitlab.com/orion17/network-services-monitor/app-client:1.1.0"
}