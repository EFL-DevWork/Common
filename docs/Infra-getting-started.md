# Setting up clusters, infra services, and CI/ CD 

This sections in this doc describes the steps to set up clusters, infra services, and CI/ CD as precondition to run the starter-kit service. Follow each section in order to complete your setup

- [Creating Clusters Using Infra Catalyst](#creating-clusters-using-infra-catalyst)
- [Setting up ingress controller for each cluster](#setting-up-ingress-controller-for-each-cluster)
- [Setting Up Infra for Service-Starter](#setting-up-infra-for-service-starter)
- [Setting Up Jenkins](#setting-up-jenkins)
- [Setup Additional Steps For Each Node In The Cluster](#setup-additional-steps-for-each-node-in-the-cluster)

## Creating Clusters Using Infra Catalyst

**Steps to setup:**
- Clone the repo of [devops-catalyst](https://github.com/DevOpsByExample/devops-catalyst).
- cd devops-catalyst 
- Follow README.md file of [devops-catalyst](https://github.com/DevOpsByExample/devops-catalyst) to set up three clusters

**Expected output:**
- If you successfully used the devops-catalyst by following the readme.md then you will have now three clusters (systems, qa, staging)


## Setting up ingress controller for each cluster
To access the services using domain name URL and utilise the same load balancer, we need ingress controller enabled. 

**Steps to setup:**
- By default, system cluster will have ingress controller enabled
Verify this by running the following command :
```
kubectl get svc -n ingress-nginx
```
If the setup is successful you will see external-ip for ingress controller coming up like below for each of the clusters: 
```
NAME            TYPE           CLUSTER-IP      EXTERNAL-IP    PORT(S)                      AGE
ingress-nginx   LoadBalancer   10.13.208.104   64.225.85.61   80:31859/TCP,443:32331/TCP   31d
``` 
- Staging and qa clusters will not have the ingress controller addon enabled by default- To set this up follow the below steps

(We will do the actual setting up of domain name URLs for required services in later steps)

### Enabling nginx-ingress controller addon :
**Note : Repeat the below steps for both staing and qa cluster by replacing $path with 'qa' and 'staging'**

- Path of ingress addon : devops-catalyst/$path/k8s/terragrunt.hcl
- In terragrunt.hcl file check for addons inside ' inputs ' and enable ingress and ccm like given below :
```
addons = {
      ingress      = {
        enabled    = true
      }
      ccm          = {
        enabled    = true
      }
      external_dns = null
      cert_manager = null
      ebs          = null

      csi = {
        enabled    = true
        upgrade    = false
      }
    }
```

- After doing changes use commands below for respective clusters in devops-catalyst/$path: 
```
terragrunt plan-all
terragrunt apply-all
```

**Expected output:**

- Get list of services using command below for each of the clusters: 
```
kubectl get svc -n ingress-nginx
```
If the setup is successful you will see external-ip for ingress controller coming up like below for each of the clusters: 
```
NAME            TYPE           CLUSTER-IP      EXTERNAL-IP    PORT(S)                      AGE
ingress-nginx   LoadBalancer   10.13.208.104   64.225.85.61   80:31859/TCP,443:32331/TCP   31d
``` 
## Setting Up Infra for Service-Starter 
- Git clone [infra-helm-starter](https://github.com/Regional-IT-India/infra-helm-starter.git)
- Go inside infra-helm-starter folder
```
cd infra-helm-starter
```
- #### Creating Namespaces And Pulling Repositories
    - Run following commands to pull repositories and creating namespaces : 
    ```
    . ./create-namespaces.sh
    . ./pull-repos.sh
    ```
- #### Install And Setup Nexus In Systems Cluster

    - Export into Systems Cluster to set up nexus 
    ```
    export KUBECONFIG=/Users/<username>/.kube/configs/devops-catalyst-systems.conf
    ```
    - Follow [NexusReadme.md](https://github.com/Regional-IT-India/infra-helm-starter/blob/master/NexusReadme.md) file
    
- #### Setting up Nexus Secret

    - Encode nexus username and password to base64 should be in following format : 
    ```
    <nexususername>:<nexuspassword>
    ```
    - Give encoded string of username and password in ' auth '
    Example: 
    ```
    {
      "auths": {
        "registry.demo/repository/service-starter-docker": {
          "auth": "YWRtaW46VGhvdWqqdEBjYXRhbHlzdA=="
        }
      }
    }
    ``` 
    - Take entire json object from above and encode it to base 64 
    - Add encoded string in .dockerconfigjson of nexus-secret/nexus-secret.yml
    Example :
    ```
    data:
      .dockerconfigjson: ewogICJffXRocyI6IHsKICAgICJyZWdpc3RyeS5kZW1vL3JlcG9zaXRvcnkvc2VydmljZS1zdGFydGVyLWRvY2tlciI6IHsKICAgICAgImF1dGgiOiAiWVdSdGFXNDZWR2h2ZFdkb2RFQmpZWFJoYkhsemRBPT0iCiAgICB9CiAwwQp9
    ```
    -Export into Staging cluster to set nexus secret
    ```
        export KUBECONFIG=/Users/<username>/.kube/configs/devops-catalyst-staging.conf
    ```
    - Run Follow command to set secret :
    ```
    . ./nexussecret-execute.sh
    ```
    -Export into QA cluster to set nexus secret
     ```
        export KUBECONFIG=/Users/<username>/.kube/configs/devops-catalyst-qa.conf
    ```
    - Run Follow command to set secret :
    ```
    . ./nexussecret-execute.sh
    ```

- #### Install Postgres Staging and QA Cluster 

    - Export into Staging Cluster to set up postgres 
    ```
    export KUBECONFIG=/Users/<username>/.kube/configs/devops-catalyst-staging.conf
    ```
    - Run following command to install postgres :
    ```
    . ./postgres-execute.sh
    ``` 
    - Repeat same command in QA cluster 

- #### Install Postgres Client For Staging and QA Cluster
    
    - Export into Staging Cluster to set up postgres 
    ```
    export KUBECONFIG=/Users/<username>/.kube/configs/devops-catalyst-staging.conf
    ```
    - Run following command to install postgres :
    ```
    . ./postgreclient-execute.sh
    ``` 
    - Run below command get postgresclient pod name : 
    ```
    kubectl get pods -n infra
    ```
    - After getting postgres client pod name replace it in below command and run it to enter inside the pod : 
    ```
    kubectl exec -it <postgresclientdeployment pod name> -n infra -- /bin/sh
    ```
    - Run initdbjava.sh script to create user and db for java using below command :
    ```
    . ./initdbjava.sh
    ``` 
    - Repeat same command in QA cluster 
   
## Setting Up Jenkins
Note : systems-cluster by default comes up with jenkins  
- Follow [JenkinsReadme.md](https://github.com/Regional-IT-India/ci-jenkins-starter/blob/master/Readme.md) file

## Setup To Access Docker Registry

Since we are using http instead of https for host address, we need docker registry insecurity access.
We can use following steps to access docker registry.

- Export into a $ClusterName
    - We have three clusters to export into one of the cluster, we have to give the path of the $cluster_config_file
```
export KUBECONFIG=/Users/<username>/.kube/configs/$cluster_config_file
```
- How to get Host name and address :
    - After applying below command we can get host names and addresses :
    ```
    kubectl get ingress -A
    ```
    - Output will be shown like below :
    ```
    NAMESPACE   NAME                                         CLASS    HOSTS                              ADDRESS        PORTS   AGE
    ci          jenkins                                      <none>   jenkins.catalyst.com               64.225.85.61   80      21d
    nexus       nexus-artifactory-nexus-repository-manager   <none>   nexus.catalyst.com,registry.demo   64.225.85.61   80      11d
  ``` 
    Note : Save ' HOSTS ' and their respective ' ADDRESS ' somewhere.
- Get the list of nodes present in $ClusterName using following command :
```
kubectl get nodes -A
```
- Describe one the node (eg. devops-catalyst-systems-primary-01)
```
kubectl describe node devops-catalyst-systems-primary-01 
```
- Check for Public-ip in ' Annotations ' (eg. 167.71.236.124)
```html
Annotations:        flannel.alpha.coreos.com/backend-data: {"VtepMAC":"a2:0a:75:1b:b4:f8"}
                    flannel.alpha.coreos.com/backend-type: vxlan
                    flannel.alpha.coreos.com/kube-subnet-manager: true
                    flannel.alpha.coreos.com/public-ip: 167.71.236.124 <==== "Public Ip"
                    kubeadm.alpha.kubernetes.io/cri-socket: /var/run/dockershim.sock
                    node.alpha.kubernetes.io/ttl: 0
                    projectcalico.org/IPv4IPIPTunnelAddr: 10.12.0.1
                    volumes.kubernetes.io/controller-managed-attach-detach: true
```

- Use ssh command to go inside the node by using following command :
```
ssh root@167.71.236.124 -i id_rsa
```

#### Adding Insecure Registry to Docker
- Go to /etc/docker/daemon.json file 
```
nano /etc/docker/daemon.json
```
- After ' "storage-driver": "overlay2" ' add insecurity registry as next line like given below :
```
"insecure-registries": ["registry.demo"]
```
- After adding insecure registry in daemon.json file restart the docker and check its status :
```
sudo service docker restart 
sudo service docker status 
``` 
- Make sure docker status restarted few seconds ago.

<b>Repeat the steps, adding docker insecure registry for each node in each cluster</b>

**Expected output:**
If you successfully follow above steps, run the following command :
```
docker login -u <nexus username> -p <nexus password> http://registry.demo/repository/service-starter-docker/
``` 
After running the command it will show following output :
```
Login Succeeded
```
Example : 
```
devops-catalyst-systems-primary-01, 
devops-catalyst-staging-node-6cd7w5s,
devops-catalyst-qa-primary-01, etc. 
```

