resource "aws_iam_role" "nsm_k8s_cluster" {
  name = "nsm-k8s-cluster"

  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "eks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy_attachment" "nsm_k8s_cluster_AmazonEKSClusterPolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = "${aws_iam_role.nsm_k8s_cluster.name}"
}

resource "aws_iam_role_policy_attachment" "nsm_k8s_cluster_AmazonEKSServicePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSServicePolicy"
  role       = "${aws_iam_role.nsm_k8s_cluster.name}"
}

resource "aws_security_group" "nsm_k8s_cluster" {
  name        = "nsm-k8s-cluster"
  description = "Cluster communication with worker nodes"
  vpc_id      = "${aws_vpc.nsm_k8s.id}"

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "nsm-k8s"
  }
}

resource "aws_security_group_rule" "nsm_k8s_cluster_ingress_https" {
  cidr_blocks       = ["${local.workstation-external-cidr}"]
  description       = "Allow workstation to communicate with the cluster API Server"
  from_port         = 443
  protocol          = "tcp"
  security_group_id = "${aws_security_group.nsm_k8s_cluster.id}"
  to_port           = 443
  type              = "ingress"
}

resource "aws_eks_cluster" "nsm_k8s" {
  name     = "${var.cluster_name}"
  role_arn = "${aws_iam_role.nsm_k8s_cluster.arn}"

  vpc_config {
    security_group_ids = ["${aws_security_group.nsm_k8s_cluster.id}"]
    subnet_ids         = "${aws_subnet.nsm_k8s[*].id}"
  }

  depends_on = [
    "aws_iam_role_policy_attachment.nsm_k8s_cluster_AmazonEKSClusterPolicy",
    "aws_iam_role_policy_attachment.nsm_k8s_cluster_AmazonEKSServicePolicy",
  ]
}
