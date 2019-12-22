output "cluster_host" {
  description = "IP of k8s cluster"
  value       = "${aws_eks_cluster.nsm_k8s.endpoint}"
}

output "cluster_ca_certificate" {
  description = "Cluster CA certificate"
  value       = "${aws_eks_cluster.nsm_k8s.certificate_authority.0.data}"
}

locals {
  kubeconfig = <<KUBECONFIG


apiVersion: v1
clusters:
- cluster:
    server: ${aws_eks_cluster.nsm_k8s.endpoint}
    certificate-authority-data: ${aws_eks_cluster.nsm_k8s.certificate_authority.0.data}
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: aws
  name: aws
current-context: aws
kind: Config
preferences: {}
users:
- name: aws
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1alpha1
      command: aws-iam-authenticator
      args:
        - "token"
        - "-i"
        - "${var.cluster_name}"
KUBECONFIG
}

output "kubeconfig" {
  value = "${local.kubeconfig}"
}