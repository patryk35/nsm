resource "aws_iam_role" "nsm_k8s_node" {
  name = "nsm-k8s-node"

  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy_attachment" "nsm_k8s_node_AmazonEKSWorkerNodePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = "${aws_iam_role.nsm_k8s_node.name}"
}

resource "aws_iam_role_policy_attachment" "nsm_k8s_node_AmazonEKS_CNI_Policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = "${aws_iam_role.nsm_k8s_node.name}"
}

resource "aws_iam_role_policy_attachment" "nsm_k8s_node_AmazonEC2ContainerRegistryReadOnly" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = "${aws_iam_role.nsm_k8s_node.name}"
}

resource "aws_eks_node_group" "nsm_k8s" {
  cluster_name    = "${aws_eks_cluster.nsm_k8s.name}"
  node_group_name = "nsm-k8s"
  node_role_arn   = "${aws_iam_role.nsm_k8s_node.arn}"
  subnet_ids      = "${aws_subnet.nsm_k8s[*].id}"

  scaling_config {
    desired_size = 1
    max_size     = 1
    min_size     = 1
  }

  depends_on = [
    "aws_iam_role_policy_attachment.nsm_k8s_node_AmazonEKSWorkerNodePolicy",
    "aws_iam_role_policy_attachment.nsm_k8s_node_AmazonEKS_CNI_Policy",
    "aws_iam_role_policy_attachment.nsm_k8s_node_AmazonEC2ContainerRegistryReadOnly",
  ]
}
