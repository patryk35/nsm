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
-aws - access to secure groups
-dockerfile - access to registry

## Usage
Step 0
 
Set appropriate values for region, aws_secrets_manager_id and aws_secrets_manager_key in file variables.tf

Step 1
```
terraform apply -target=module.aws_k8s_cluster -auto-approve
terraform apply -target=module.app -auto-approve
```
Step 2

a) Find client address in terraform output (e.g. client_address = ...)

b) Create account

c) Create auth token with access to agent configuration (methods: GET, POST)


Step 3 

```
terraform apply -target=module.agents -auto-approve -var 'nsm_access_token=[token_value]'
```

## Cleaning
```
terraform apply destroy -auto-approve
```