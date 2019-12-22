resource "aws_vpc" "nsm_k8s" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "nsm-k8s-vpc"
    "kubernetes.io/cluster/${var.cluster_name}" =  "shared"
  }
  #tags = "${
  #  map(
  #    "Name", "terraform-eks-demo-node",
  #    "kubernetes.io/cluster/${var.cluster-name}", "shared",
   # )
  #}"
}

resource "aws_subnet" "nsm_k8s" {
  count = 2

  availability_zone = "${data.aws_availability_zones.available.names[count.index]}"
  cidr_block        = "10.0.${count.index}.0/24"
  vpc_id            = "${aws_vpc.nsm_k8s.id}"

  tags = {
    Name = "nsm-k8s-node"
    "kubernetes.io/cluster/${var.cluster_name}" =  "shared"
  }
}

resource "aws_internet_gateway" "nsm_k8s" {
  vpc_id = "${aws_vpc.nsm_k8s.id}"

  tags = {
    Name = "nsm_k8s"
  }
}

resource "aws_route_table" "nsm_k8s" {
  vpc_id = "${aws_vpc.nsm_k8s.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.nsm_k8s.id}"
  }
}

resource "aws_route_table_association" "nsm_k8s" {
  count = 2

  subnet_id      = "${aws_subnet.nsm_k8s.*.id[count.index]}"
  route_table_id = "${aws_route_table.nsm_k8s.id}"
}
