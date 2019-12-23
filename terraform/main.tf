// todo - use config
// terraform apply -target=module.client_and_server -auto-approve
provider "aws" {
  region  = var.region
  version = "~> 2.0"
}

data "external" "aws_iam_authenticator" {
  program = ["sh", "-c", "aws-iam-authenticator token -i nsm-k8s | jq -r -c .status"]
}
provider "kubernetes" {
  host                      = "${module.aws_k8s_cluster.cluster_host}"
  cluster_ca_certificate    = "${base64decode(module.aws_k8s_cluster.cluster_ca_certificate)}"
  token                     = "${data.external.aws_iam_authenticator.result.token}"
  load_config_file          = false
  version = "~> 1.5"
}

module "aws_k8s_cluster" {
  source              = "./aws"
  cluster_name        = "${var.cluster_name}"
}

module "app" {
  source                        = "./app"
  aws_secrets_manager_id        = "${var.aws_secrets_manager_id}"
  aws_secrets_manager_key       = "${var.aws_secrets_manager_key}"
  app_client_image_tag          = "${var.app_client_image_tag}"
  app_server_image_tag          = "${var.app_server_image_tag}"
  aws_region                    = "${var.region}"
}

module "agents" {
  source                  = "./agents"
  app_server_address      = "${module.app.app_server_address}"
  nsm_access_token        = "${var.nsm_access_token}"
  agent_image_tag         = "${var.agent_image_tag}"
}

output "kubeconfig" {
  value = "${module.aws_k8s_cluster.kubeconfig}"
}
output "app_server_address" {
  value = "${module.app.app_server_address}"
}
output "client_address" {
  value = "${module.app.client_address}"
}