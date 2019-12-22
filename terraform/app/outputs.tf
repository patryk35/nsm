output "app_server_address" {
  description = ""
  value       = "${kubernetes_service.nsm_app_server.load_balancer_ingress.0.hostname}"
}

output "client_address" {
  description = ""
  value       = "${kubernetes_service.nsm_app_client.load_balancer_ingress.0.hostname}"
}