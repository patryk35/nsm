#resource "kubernetes_config_map" "aws_auth" {
#  metadata {
#    name = "aws-auth"
#    namespace = "kube-system"
#  }
#  data = {
#    mapRoles = <<EOF
#- rolearn: ${aws_iam_role.nsm_k8s_node.arn}
#  username: system:node:{{EC2PrivateDNSName}}
#  groups:
#    - system:bootstrappers
#    - system:nodes
#EOF
#  }
#  depends_on = [
#    "aws_eks_node_group.nsm_k8s"
#  ]
#}


resource "kubernetes_secret" "gitlab_registry_key" {
  metadata {
    name = "gitlab-registry-key"
  }

  data = {
    ".dockerconfigjson" = "${file("${var.dockerfile_path}")}"
  }

  type = "kubernetes.io/dockerconfigjson"
  #depends_on = [
  #  "kubernetes_config_map.aws_auth"
  #]
}