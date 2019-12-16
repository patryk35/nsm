variable "cluster-name" {
  default = "nsm-k8s"
  type    = "string"
}

variable "aws-secrets-manager-id" {
  description = ""
  default = ""
}

variable "aws-secrets-manager-key" {
  description = ""
  default = ""
}
variable "dockerfilepath" {
  description = ""
  default = "~/.docker/config.json"
}

variable "nsm-access-token" {
  default = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwiaWQiOiIxN2I4NjExMi04MzU2LTQ4YWEtYjRlYy04MTEwNDM2NjViMWEiLCJuW1lIjoiS3ViZXJuZXRlc1Rva2VuIiwiYWxsb3dlZE1ldGhvZHMiOiJHRVQsE9TVCQQVRDSCxERUxFVEUsIiwiYWxsb3dlZEVuZHBvaW50cyI6Ii9hcGkvdjEvYWdlbnQsIiwiaWF0IjoxNTc2MzQ3MDAwLCJleHAiOjE1Nzc4MTkzODJ9.tQ-rWGzbKuimYs9kJGp6k5-bvPUwoBsexRIMCN04FNBaTYB811-6ZXrGDJ6KmWv4TpcsZ8wkGKG2L5UiEOJsnw"
}