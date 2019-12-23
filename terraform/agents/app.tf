resource "kubernetes_service" "nsm_app_agent_proxy" {
  metadata {
    name = "nsm-app-agent-proxy"
    labels = {
      app = "nsm-app-agent-proxy"
    }
  }
  spec {
    selector = {
      app = "nsm-app-agent-proxy"
    }
    port {
      target_port = 9999
      port = 9999
    }
    type = "NodePort"
  }
}

resource "kubernetes_stateful_set" "nsm_app_agent_proxy" {
  metadata {
    name = "nsm-app-agent-proxy"
    labels = {
      app = kubernetes_service.nsm_app_agent_proxy.spec[0].selector.app
    }
  }

  spec {
    replicas = 2
    service_name = "nsm-app-agent-proxy"

    selector {
      match_labels = {
        app = "nsm-app-agent-proxy"
      }
    }

    template {
      metadata {
        labels = {
          app = "nsm-app-agent-proxy"
        }
      }

      spec {
        container {
          image = var.agent_image_tag
          name  = "nsm-app-agent-proxy"
          port {
            container_port = 9999
          }
          env {
            name = "MONITOR_IP"
            value = var.app_server_address
          }
          env {
            name = "MONITOR_PORT"
            value = 8443
          }
          env {
            name = "ACCESS_TOKEN"
            value = var.nsm_access_token
          }
          env {
            name = "AGENT_PROXY"
            value = "true"
          }
          env {
            name = "APP_SERVER_IP"
            value = var.app_server_address
          }
          env {
            name = "APP_SERVER_PORT"
            value = 8443
          }
        }
        image_pull_secrets {
          name = "gitlab-registry-key"
        }
      }
    }
  }
}

resource "kubernetes_stateful_set" "nsm_app_agent_proxy_0_agents" {
  metadata {
    name = "nsm-app-agent-proxy-0-agents"
    labels = {
      app = "nsm-app-agent-proxy-0-agents"
    }
  }

  spec {
    replicas = 2

    service_name = "nsm-app-agent-proxy-0-agents"

    selector {
      match_labels = {
        app = "nsm-app-agent-proxy-0-agents"
      }
    }

    template {
      metadata {
        labels = {
          app = "nsm-app-agent-proxy-0-agents"
        }
      }

      spec {
        container {
          image = var.agent_image_tag
          name  = "nsm-app-agent-proxy-agents"
          port {
            container_port = 9999
          }
          env {
            name = "MONITOR_IP"
            value = format("%s-0.%s.default.svc.cluster.local", kubernetes_stateful_set.nsm_app_agent_proxy.metadata[0].labels.app,kubernetes_service.nsm_app_agent_proxy.metadata[0].labels.app)
          }
          env {
            name = "MONITOR_PORT"
            value = 9999
          }
          env {
            name = "ACCESS_TOKEN"
            value = var.nsm_access_token
          }
          env {
            name = "AGENT_PROXY"
            value = "false"
          }
          env {
            name = "APP_SERVER_IP"
            value = var.app_server_address
          }
          env {
            name = "APP_SERVER_PORT"
            value = 8443
          }
        }
        image_pull_secrets {
          name = "gitlab-registry-key"
        }
      }
    }
  }
}

resource "kubernetes_stateful_set" "nsm_app_agent_proxy_1_agents" {
  metadata {
    name = "nsm-app-agent-proxy-1-agents"
    labels = {
      app = "nsm-app-agent-proxy-1-agents"
    }
  }

  spec {
    replicas = 3

    service_name = "nsm-app-agent-proxy-1-agents"

    selector {
      match_labels = {
        app = "nsm-app-agent-proxy-1-agents"
      }
    }

    template {
      metadata {
        labels = {
          app = "nsm-app-agent-proxy-1-agents"
        }
      }

      spec {
        container {
          image = var.agent_image_tag
          name  = "nsm-app-agent-proxy-agents"
          port {
            container_port = 9999
          }
          env {
            name = "MONITOR_IP"
            value = format("%s-1.%s.default.svc.cluster.local", kubernetes_stateful_set.nsm_app_agent_proxy.metadata[0].labels.app,kubernetes_service.nsm_app_agent_proxy.metadata[0].labels.app)
          }
          env {
            name = "MONITOR_PORT"
            value = 9999
          }
          env {
            name = "ACCESS_TOKEN"
            value = var.nsm_access_token
          }
          env {
            name = "AGENT_PROXY"
            value = "false"
          }
          env {
            name = "APP_SERVER_IP"
            value = var.app_server_address
          }
          env {
            name = "APP_SERVER_PORT"
            value = 8443
          }
        }
        image_pull_secrets {
          name = "gitlab-registry-key"
        }
      }
    }
  }
}

resource "kubernetes_stateful_set" "nsm_app_agent" {
  metadata {
    name = "nsm-app-agent"
    labels = {
      app = "nsm-app-agent"
    }
  }

  spec {
    replicas = 2
    service_name = "nsm-app-agent"

    selector {
      match_labels = {
        app = "nsm-app-agent"
      }
    }

    template {
      metadata {
        labels = {
          app = "nsm-app-agent"
        }
      }

      spec {
        container {
          image = var.agent_image_tag
          name  = "nsm-app-agent"
          port {
            container_port = 9999
          }
          env {
            name = "MONITOR_IP"
            value = var.app_server_address
          }
          env {
            name = "MONITOR_PORT"
            value = 8443
          }
          env {
            name = "ACCESS_TOKEN"
            value = var.nsm_access_token
          }
          env {
            name = "AGENT_PROXY"
            value = "false"
          }
          env {
            name = "APP_SERVER_IP"
            value = var.app_server_address
          }
          env {
            name = "APP_SERVER_PORT"
            value = 8443
          }
        }
        image_pull_secrets {
          name = "gitlab-registry-key"
        }
      }
    }
  }
}
