
* [Backstage QShift Showcase](#backstage-qshift-showcase)
  * [Prerequisites](#prerequisites)
  * [Provision an ocp cluster](#provision-an-ocp-cluster)
    * [Kubevirt](#kubevirt)
    * [GitOps](#gitops)
    * [Tekton](#tekton)
  * [Backstage instructions](#backstage-instructions)
    * [First steps](#first-steps)
    * [Deploy and use Backstage on OCP](#deploy-and-use-backstage-on-ocp)
    * [Run backstage locally](#run-backstage-locally)
  * [Curl backstage](#curl-backstage)
  * [Clean up](#clean-up)

# Backstage QShift Showcase

The backstage QShift application has been designed to showcase QShift (Quarkus on OpenShift). It is composed of the following plugins and integrated with different backend systems:

| Backstage plugin                                                                                                                                                                                                                                          | Backend system                                   | 
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| [Kubernetes](https://backstage.io/docs/features/kubernetes/)                                                                                                                                                                                              | OpenShift 4.14                                   |
| [Quarkus front & backend](https://github.com/q-shift/backstage-plugins)                                                                                                                                                                                   | code.quarkus.io, OpenShift Virtualization 4.14.3 |
| [Quarkus Console](https://github.com/q-shift/backstage-plugins?tab=readme-ov-file#quarkus-console)                                                                                                                                                        | OpenShift 4.14                                   |
| ArgoCD [front](https://github.com/RoadieHQ/roadie-backstage-plugins/tree/main/plugins/frontend/backstage-plugin-argo-cd) & [backend](https://github.com/RoadieHQ/roadie-backstage-plugins/tree/main/plugins/scaffolder-actions/scaffolder-backend-argocd) | OpenShift GitOps 1.11.1                          |
| [Tekton](https://github.com/janus-idp/backstage-plugins/tree/main/plugins/tekton)                                                                                                                                                                         | OpenShift Pipelines 1.13.1                       |
| [Topology](https://github.com/janus-idp/backstage-plugins/tree/main/plugins/topology)                                                                                                                                                                     | OpenShift 4.14                                   |

**Note**: This backstage application is based on the backstage's version: 1.24.x

## Prerequisites

- [Node.js](https://nodejs.org/en) (18 or 20)
- [nvm](https://github.com/nvm-sh/nvm), npm and [yarn](https://classic.yarnpkg.com/lang/en/docs/install/#mac-stable) installed
- Read this blog post: https://medium.com/@chrisschneider/build-a-developer-portal-with-backstage-on-openshift-d2a97aca91ee
- [GitHub client](https://cli.github.com/) (optional)
- [argocd client](https://argo-cd.readthedocs.io/en/stable/getting_started/#2-download-argo-cd-cli) (optional)

**Important**: If you need to provision an OpenShift cluster with the required backend systems: ArgoCD, Tekton, etc, then go to the next section [Provision an ocp cluster](#provision-an-ocp-cluster), otherwise move to [Backstage instructions](#backstage-instructions)

## Provision an ocp cluster

The following section details the different commands to be used to deploy the backend systems needed by QShift on a new OCP cluster (e.g. 4.14.10)

#### Kubevirt

https://github.com/q-shift/openshift-vm-playground?tab=readme-ov-file#instructions-to-create-a-vm-and-to-ssh-to-it

To subscribe to the operator and create the needed CR

**Note**: The version of the operator could be different according to the ocp cluster version used but the platform will in this case bump the version for you. Take care as this project could take time !

```bash
cd manifest/installation/virt
kubectl create ns openshift-cnv
kubectl apply -f subscription-kubevirt-hyperconverged.yml
kubectl apply -f hyperConverged.yml
```

To install the customized fedora image packaging podman and socat, create now a `DataVolume` CR and wait till the image will be imported
```bash
kubectl -n openshift-virtualization-os-images apply -f quay-to-pvc-datavolume.yml
```

#### GitOps

To subscribe to the operator and create the needed CR

```bash
cd manifest/installation/gitops
kubectl create ns openshift-gitops-operator
kubectl apply -f subscription-gitops.yml
```

To use ArgoCD with QShift, it is needed to delete the existing `ArgoCD` CR and to deploy our `ArgoCD` CR.

**Note**: Our CR includes different changes needed to work with QShift: `sourceNamespaces`, `extraConfig` and `tls.termination: reencrypt` and `resourceExclusions`

**Todo**: The previous note should be documented to explain the changes needed !

```bash
kubectl delete argocd/openshift-gitops -n openshift-gitops
```
Substitute within the `ArgoCD` CR the <MY_NAMESPACE> to be used using this command
```bash
cat argocd.tmpl | NAMESPACE=<MY_NAMESPACE> envsubst > argocd.yml
kubectl apply -f argocd.yml
```
**Todo**: Instead of deleting and recreating a new ArgoCD CR, we should patch it or install it using kustomize, helm chart. Example: https://github.com/redhat-cop/agnosticd/blob/development/ansible/roles_ocp_workloads/ocp4_workload_openshift_gitops/templates/openshift-gitops.yaml.j2

Patch the `AppProject` CR to support to deploy the `Applications` CR in [different namespaces](https://github.com/q-shift/backstage-playground/issues/39#issuecomment-1938403564).
```bash
kubectl get AppProject/default -n openshift-gitops -o json | jq '.spec.sourceNamespaces += ["*"]' | kubectl apply -f -
```

Finally, create a new ClusterRoleBinding to give the `Admin` role to the ServiceAccount `openshift-gitops-argocd-application-controller`. That will allow it to manage the `Applications` CR deployed in any namespace of the cluster.

```bash
cat << EOF | kubectl apply -f -
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: argocd-controller-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: admin
subjects:
- kind: ServiceAccount
  name: openshift-gitops-argocd-application-controller
  namespace: openshift-gitops
EOF
```

#### Tekton

To subscribe to the operator, execute this command

```bash
cd manifest/installation/tekton
kubectl apply -f subscription-pipelines.yml
```

## Backstage instructions

This section explains how to use Backstage:
- [Deployed](#deploy-and-use-backstage-on-ocp) on an OpenShift cluster
- Running [locally](#run-backstage-locally) or 


### First steps

Before to install and use our Backstage application, it is needed to perform some steps such as:
- Create an OpenShift project 
- Provide your registry credentials (quay.io, docker, etc) as a `config.json` file

The commands described hereafter will help you to set up what it is needed:

- Start first by cloning this project locally
  ```bash
  git clone https://github.com/q-shift/backstage-playground.git
  cd backstage-playground
  ```
- Log on to the ocp cluster `oc login --token=sha256 ...` which has been provisioned
- Create an OpenShift project: 
  ```bash
  oc new-project <MY_NAMESPACE>
  ```
  **Important**: The commands documented hereafter assume that your use the project created: `oc project <MY_NAMESPACE>`

- Next create the following registry `config.json` file (or use yours). Provide the following registry: quay.io and docker as they are needed to build/push the image of the Quarkus container or to pull images from docker registry without the hassle of the `docker limit`.
  ```bash
  QUAY_CREDS=$(echo -n "<QUAY_USER>:<QUAY_TOKEN>" | base64)
  DOCKER_CREDS=$(echo -n "<DOCKER_USER>:<DOCKER_PWD>" | base64)
  QUAY_ORG=<QUAY_ORG>
  
  cat <<EOF > config.json
  {
    "auths": {
      "quay.io/${QUAY_ORG}": {
        "auth": "$QUAY_CREDS"
      },
      "https://index.docker.io/v1/": {
        "auth": "$DOCKER_CREDS"
      }
    }
  }
  EOF
  ```

  **Important**: The Git Org to define here should be the same as the one you will use when you scaffold a Quarkus application.

- Deploy it using this command:

  ```bash
  kubectl create secret generic dockerconfig-secret --from-file=config.json
  ```
- **Warning**: To let ArgoCD to handle the `Applications` CR within your namespace, it is needed to patch the resource `kind: ArgoCD` to add your namespace using the field: `.spec.sourceNamespaces`. When patched, the ArgoCD operator will roll out automatically the ArgoCD server.
  ```bash
  kubectl get argocd/openshift-gitops -n openshift-gitops -o json \
    | jq '.spec.sourceNamespaces += ["<MY_NAMESPACE>"]' | kubectl apply -f -
  ```
- And finally, create the service account `my-backstage`. 
  ```bash
  kubectl create sa my-backstage
  ```
  **Note**: This is needed to create the SA in order to get the secret generated and containing the token that we will use at the step `Deploy and use Backstage on OCP`

- Next, it is needed to create a VM using the following commands:
  ```bash
  oc project <MY_NAMESPACE>
  kubectl create secret generic quarkus-dev-ssh-key --from-file=key=$HOME/.ssh/id_rsa.pub
  kubectl apply -f quarkus-dev-virtualmachine.yml
  ```
- You can verify if the VMI is well running if you check its status:
  ```bash
  kubectl get vm -n <MY_NAMESPACE>
  NAMESPACE       NAME          AGE   STATUS    READY
  <MY_NAMESPACE>  quarkus-dev   32s   Running   True
  ```

We are now ready to deploy and use backstage within your project as documented at the following section.

### Deploy and use Backstage on OCP

A Backstage application uses an app-config.yaml [configuration](https://backstage.io/docs/conf/writing) file to configure its front and backend application like the plugins accessing the backend systems.

As we cannot use a local config file as this is the case when you start backstage locally (`yarn dev`), then we will use for ocp a `configMap` and
define the sensitive information in a kubernetes `secret`. 

This kubernetes secret, which contains k=v pairs, will be mounted as a volume within the backstage's pod and will override the `appo-config.yaml` file mounted also as a volume from a ConfigMap.

**Trick**: The [backstage_env_secret.tmpl](manifest/templates/backstage_env_secret.tmpl) file contains what you need to get or set the sensitive information :-)

- Copy the template and save it: `backstage_env_secret.env`:
  ```bash
  cp manifest/templates/backstage_env_secret.tmpl backstage_env_secret.env
  ```
- Edit the file `backstage_env_secret.env` and set the different values using the commands or information between `<command or trick>`  
- Create the kubernetes secret using the env file: 
  ```bash
  kubectl create secret generic my-backstage-secrets --from-env-file=backstage_env_secret.env
  ```
- To deploy backstage, create from the template `manifest/templates/argocd.tmpl` the  argocd.yaml file and pass env variables to be substituted:
  ```bash
  cat manifest/templates/argocd.tmpl | NAMESPACE=<MY_NAMESPACE> DOMAIN=<OCP_CLUSTER_DOMAIN> envsubst > argocd.yaml
  kubectl apply -f argocd.yaml
  ```

Verify if backstage is alive using the URL: `https://backstage-<MY_NAMESPACE>.<OCP_CLUSTER_DOMAIN>` and start to play with the template `Create Quarkus Application`

![scaffold-templates-page.png](docs%2Fscaffold-templates-page.png)

Quarkus console

![quarkus-console-1.png](docs%2Fquarkus-console-1.png)

### Run backstage locally

Create your `app-config.qshift.yaml` file using the [app-config.qshift.tmpl](manifest%2Ftemplates%2Fapp-config.qshift.tmpl) file and set the different 
url/password/tokens using the env [backstage_env_secret.tmpl](manifest%2Ftemplates%2Fbackstage_env_secret.tmpl) likle this

```bash
cp manifest/templates/backstage_env_secret.tmpl backstage_env_secret.env

# Edit the backstage_env_secret.env and set the different url/password/tokens !!

export $(grep -v '^#' backstage_env_secret.env | xargs)
envsubst < manifest/templates/app-config.qshift.tmpl > app-config.local.yaml
```

**Warning**: If you use node 20, then export the following env var `export NODE_OPTIONS=--no-node-snapshot` as documented [here](https://backstage.io/docs/getting-started/configuration/#create-a-new-component-using-a-software-template).

Next run the following commands to start the front and backend:

```sh
yarn install
yarn start --config ../../app-config.qshift.yaml
yarn start-backend --config ../../app-config.qshift.yaml
```

You can now open the backstage URL `http://localhodt:3000`, select from the left menu `/create` and scaffold a new project using the template `Create a Quarkus application`

## Curl backstage

If you would like to automate the process to scaffold a project without the need to use the UI, then create a JSON file containing the parameters to be passed

```bash
cat <<EOF > req.json
{
  "templateRef": "template:default/quarkus-application",
  "values": {
    "component_id": "my-quarkus-app",
    "native": false,
    "owner": "user:default/guest",
    "groupId": "io.quarkus",
    "artifactId": "my-quarkus-app",
    "version": "1.0.0-SNAPSHOT",
    "java_package_name": "io.quarkus.demo",
    "description": "A cool quarkus app",
    "javaVersion": "17",
    "buildTool": "MAVEN",
    "database": "quarkus-jdbc-postgresql",
    "healthEndpoint": true,
    "metricsEndpoint": true,
    "infoEndpoint": true,
    "extensions": [
      "io.quarkus:quarkus-resteasy-reactive",
      "io.quarkus:quarkus-resteasy-reactive-jackson",
      "io.quarkus:quarkus-hibernate-orm-rest-data-panache"
    ],
    "repo": {
      "host": "github.com",
      "org": "ch007m"
    },
    "namespace": "cmoullia",
    "imageRepository": "quay.io",
    "virtualMachineName": "quarkus-dev",
    "virtualMachineNamespace": "cmoullia",
    "imageUrl": "quay.io/ch007m/my-quarkus-app"
  }
}
EOF
```

and next issue a POST request

```bash
URL=http://localhost:7007
curl $URL/api/scaffolder/v2/tasks \
  -H 'Content-Type: application/json' \
  -d @req.json
```

## Clean up

To delete the different artefacts created, review the following commands:

- ArgoCD
```bash
ARGOCD_SERVER=openshift-gitops-server-openshift-gitops.apps.qshift.snowdrop.dev
ARGOCD_PWD=<ARGOCD_PWD>
ARGOCD_USER=admin
argocd login --insecure $ARGOCD_SERVER --username $ARGOCD_USER --password $ARGOCD_PWD --grpc-web

argocd app delete <MY_NAMESPACE>/$app-bootstrap --grpc-web -y
argocd app list --grpc-web -N <MY_NAMESPACE>
```

- GitHub repository
```bash
app=my-quarkus-app
gh repo delete github.com/<GIT_ORG>/$app --yes
```

- Backstage location/component
```bash
URL=http://localhost:7007
ID=$(curl -s $URL/api/catalog/locations | jq -r '.[] | .data.id')
curl -X 'DELETE' $URL/api/catalog/locations/$ID
```
**Note**: If you created x backstage components, then iterate through the list of the ID returned as response !
