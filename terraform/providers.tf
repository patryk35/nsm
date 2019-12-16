#
# Provider Configuration
#

provider "aws" {
  region  = "eu-west-3"
  version = "~> 2.0"
}

# Using these data sources allows the configuration to be
# generic for any region.
data "aws_region" "current" {}

data "aws_availability_zones" "available" {}

data "external" "aws_iam_authenticator" {
  program = ["sh", "-c", "aws-iam-authenticator token -i nsm-k8s | jq -r -c .status"]
}

provider "kubernetes" {
  host                      = "${aws_eks_cluster.nsm_k8s.endpoint}"
  cluster_ca_certificate    = "${base64decode(aws_eks_cluster.nsm_k8s.certificate_authority.0.data)}"
  token                     = "${data.external.aws_iam_authenticator.result.token}"
  load_config_file          = false
  version = "~> 1.5"
}

# Allow worker nodes to join cluster via config map
