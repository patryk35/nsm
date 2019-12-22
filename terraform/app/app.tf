resource "kubernetes_service" "nsm_app_server" {
  metadata {
    name = "nsm-app-server"
    labels = {
      app = "nsm-app-server"
    }
  }
  spec {
    selector = {
      app = "nsm-app-server"
    }
    port {
      port        = 80
      target_port = 5000
    }

    type = "LoadBalancer"
    #port {
    #  port = 5000
    #  node_port = 30100
    #}
    #type = "NodePort"
  }
}

resource "kubernetes_service" "nsm_app_client" {
  metadata {
    name = "nsm-app-client"
    labels = {
      app = "nsm-app-client"
    }
  }
  spec {
    selector = {
      app = "nsm-app-client"
    }
    port {
      port        = 80
      target_port = 80
    }

    type = "LoadBalancer"
    #port {
    #  port = 5000
    #  node_port = 30100
    #}
    #type = "NodePort"
  }
}

resource "kubernetes_stateful_set" "nsm_app_server" {
  metadata {
    name = "nsm-app-server"
    labels = {
      app = kubernetes_service.nsm_app_server.spec[0].selector.app
    }
  }

  spec {
    replicas = 1
    service_name = "nsm-app-server"

    selector {
      match_labels = {
        app = "nsm-app-server"
      }
    }

    template {
      metadata {
        labels = {
          app = "nsm-app-server"
        }
      }

      spec {
        container {
          image = "registry.gitlab.com/orion17/network-services-monitor/app-server:1.0.2-2"
          name  = "nsm-app-server"
          port {
            container_port = 5000
          }
          env {
            name = "APP_SERVER_ADDRESS"
            value = kubernetes_service.nsm_app_server.load_balancer_ingress.0.hostname
          }
          env {
            name = "CLIENT_SERVER_ADDRESS"
            value = kubernetes_service.nsm_app_client.load_balancer_ingress.0.hostname
          }
          env {
            name = "AWS_ACCESS_KEY_ID"
            value = var.aws_secrets_manager_id
          }
          env {
            name = "AWS_SECRET_KEY"
            value = var.aws_secrets_manager_key
          }
          #resources {
          #  limits {
          #    cpu    = "0.5"
          #    memory = "512Mi"
          #  }
          #  requests {
          #    cpu    = "250m"
          #    memory = "50Mi"
          #  }
          #}

          #liveness_probe {
          #  http_get {
          #    path = "/nginx_status"
          #    port = 80

          #    http_header {
          #      name  = "X-Custom-Header"
          #      value = "Awesome"
          #    }
          #  }

          #  initial_delay_seconds = 3
          #  period_seconds        = 3
        }
        image_pull_secrets {
          name = "gitlab-registry-key"
        }
      }
    }
  }
  depends_on = [
    "kubernetes_service.nsm_app_server"
  ]
}

resource "kubernetes_stateful_set" "nsm_app_client" {
  metadata {
    name = "nsm-app-client"
    labels = {
      app = kubernetes_service.nsm_app_client.spec[0].selector.app
    }
  }

  spec {
    replicas = 1
    service_name = "nsm-app-client"

    selector {
      match_labels = {
        app = "nsm-app-client"
      }
    }

    template {
      metadata {
        labels = {
          app = "nsm-app-client"
        }
      }

      spec {
        container {
          image = "registry.gitlab.com/orion17/network-services-monitor/app-client:1.0.5"
          name  = "nsm-app-client"
          port {
            container_port = 80
            host_port = 80
          }
          env {
            name = "REACT_APP_API_URL"
            value = format("http://%s", kubernetes_service.nsm_app_server.load_balancer_ingress.0.hostname)
          }
        }
        image_pull_secrets {
          name = "gitlab-registry-key"
        }
      }
    }
  }
  depends_on = [
    "kubernetes_service.nsm_app_client"
  ]
}