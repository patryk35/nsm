variable "cluster_name" {
  description = "Name of k8s cluster"
}

variable "dockerfile_path" {
  description = "Path of Dockerfile used for pulling images"
  default = "~/.docker/config.json"
}
