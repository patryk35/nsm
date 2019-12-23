# Terraform For NSM

## Requirements

To use this Terraform configuration are required:

- Terraform, ~> 0.12
- Terraform providers (should be automatically installed with init): provider.aws, 
provider.external, provider.http, provider.kubernetes
- AWS CLI ~> 1.16
- aws-iam-authenticator
- kubectl
- Docker

Before run configure:
- aws cli - access to Secrets Manager (described in NSM Application Server README file)
- Docker - access to registry (with command docker login - it will create file ~/.docker/config.json)

## Usage
Step 0
 
Set appropriate values for region, aws_secrets_manager_id and aws_secrets_manager_key in file variables.tf

Create Docker images for server, client, agent(withRegistration). 
Then push images to registry and set tags of created images in variables.tf

Step 1 Infrastructure + client & server applications

Create AWS infrastructure and K8S cluster using module aws_k8s_cluster and then run client and server with module app.
Use provided commands: 
```
terraform apply -target=module.aws_k8s_cluster -auto-approve
terraform apply -target=module.app -auto-approve
```
Step 2 Create access token

a) Find client address in terraform output (e.g. client_address = ...)

b) Create account

c) Create auth token with access to agent configuration (methods: GET, POST)


Step 3 Run specified structure of agents applications

```
terraform apply -target=module.agents -auto-approve -var 'nsm_access_token=[token_value]'
```

## Cleaning
```
terraform destroy -auto-approve
```