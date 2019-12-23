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
      port        = 8443
      target_port = 8443
    }

    type = "LoadBalancer"
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
      port        = 443
      target_port = 443
    }

    type = "LoadBalancer"
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
          image = var.app_server_image_tag
          name  = "nsm-app-server"
          port {
            container_port = 8443
          }
          env {
            name = "APP_SERVER_ADDRESS"
            value = format("https://%s:8443", kubernetes_service.nsm_app_server.load_balancer_ingress.0.hostname)
          }
          env {
            name = "CLIENT_SERVER_ADDRESS"
            value = format("https://%s", kubernetes_service.nsm_app_client.load_balancer_ingress.0.hostname)
          }
          env {
            name = "AWS_ACCESS_KEY_ID"
            value = var.aws_secrets_manager_id
          }
          env {
            name = "AWS_SECRET_KEY"
            value = var.aws_secrets_manager_key
          }
          env {
            name = "AWS_DEFAULT_REGION"
            value = var.aws_region
          }
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
          image = var.app_client_image_tag
          name  = "nsm-app-client"
          port {
            container_port = 443
            host_port = 443
          }
          env {
            name = "REACT_APP_API_URL"
            value = format("https://%s:8443/api/v1", kubernetes_service.nsm_app_server.load_balancer_ingress.0.hostname)
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