# DevOps (Git, Docker, Kubernetes, Ansible, CI/CD) — Complete Line-by-Line Guide

This guide is built strictly from your course materials in
`Courseware/08-devops/` and four real project folders you have on disk:
**Git Github** (the forked EMS Spring Boot lab repo), **Docker Kubernetes**
(a tiny `docker-demo` Node app plus the full `acme-ems-docker` project — Spring
Boot + Postgres + Nginx + Kubernetes manifests), **Ansible** (a minimal
Express app deployed via an idempotent Ansible playbook that itself calls
`kubectl`, driven by its own Jenkinsfile), and **Jenkins** (the same Express
app deployed straight from a Jenkinsfile with no Ansible in between). Every
command, YAML field, and code line below is reproduced from what is actually
in those folders — nothing invented.

The five parts below (1–5) walk the taught concepts module by module. Part 6
covers assessment gotchas pulled from the MCQ bank and discussion Q&A. Part 7
walks every real file in the four project folders line by line. Part 8 ties
the whole toolchain together into one CI/CD story, with notes on how it maps
to a homelab (k3s/microk8s + a self-hosted Jenkins).

---

# PART 1 — GIT & GITHUB

## 1. Version Control Fundamentals and Git vs GitHub

Git is the local, offline version-control **tool** (created by Linus
Torvalds, 2005); GitHub is a **cloud platform** that hosts Git repositories
and layers on collaboration features (PRs, issue tracking, code review).
"Git is the engine; GitHub is the garage where you park your code."

## 2. The Three Areas of Git

Every Git operation moves a file between three areas:

| Area | What it is |
|------|-----------|
| Working Directory | files you're currently editing |
| Staging Area (index) | files marked "ready to commit" via `git add` |
| Repository (`.git/`) | permanent history of snapshots (commits) |

Flow: edit → `git add` (stage) → `git commit` (snapshot into history).

## 3. First-Time Setup and Starting a Repository

```bash
git config --global user.name  "Alice Johnson"
git config --global user.email "alice@acme.com"
git config --global core.editor "code --wait"
git config --list

git init            # start fresh — creates hidden .git/, never delete it
git clone <url>     # copy an existing remote repo locally
```

## 4. Core Daily Workflow

```bash
git status
git diff                    # unstaged changes
git diff --staged           # staged changes
git add server.js | src/ | .
git commit -m "Add employee search endpoint"
git log / git log --oneline / git log --oneline --graph
git show abc1234
```

Commit message convention: imperative mood, under 72 characters, explains
**what** changed — `"Add pagination to GET /employees endpoint"`, not `"fix"`
or `"stuff"`.

## 5. `.gitignore`

A file of patterns Git should never track — for a Node project:
`node_modules/`, `dist/`, `.env`, `.env.local`, `*.log`, `.DS_Store`,
`coverage/`. Verify with `git status` — ignored paths should never show as
untracked.

## 6. Undoing Things — The Seven Scenarios

| Scenario | Command |
|----------|---------|
| Discard unstaged edits | `git restore <file>` / `git restore .` (irreversible) |
| Unstage a file | `git restore --staged <file>` (old: `git reset HEAD <file>`) |
| Fix last commit (not pushed) | `git commit --amend -m "msg"` |
| Undo a pushed commit safely | `git revert <hash>` — adds a new commit, doesn't rewrite history |
| Rewind to a commit | `git reset --soft/--mixed/--hard <hash>` — soft keeps changes staged, mixed (default) unstages them, hard destroys them |
| Recover a deleted file | `git restore <file>` or `git checkout <hash> -- <file>` |
| Park work in progress | `git stash` → `git stash pop` (apply+remove) or `git stash apply` (keep); `git stash list` |

## 7. Connecting to GitHub and Authentication

```bash
git remote add origin https://github.com/alice-johnson/acme-ems-api.git
git remote -v
git push -u origin main       # -u remembers origin/main for future bare `git push`
```

SSH auth (recommended over HTTPS):
```bash
ssh-keygen -t ed25519 -C "alice@acme.com"
cat ~/.ssh/id_ed25519.pub          # paste into GitHub → Settings → SSH keys
ssh -T git@github.com              # test
git remote set-url origin git@github.com:alice-johnson/acme-ems-api.git
```

Push/pull cycle: `git push`, `git pull` (= `fetch` + `merge`), `git fetch`
(download only, no merge — always the safer first move), then
`git diff main origin/main` to preview before merging.

## 8. Branching

```bash
git branch / git branch -r / git branch -a
git checkout -b feature/employee-search   # or: git switch -c feature/employee-search
git checkout main / git switch main
git branch -m old new                      # rename current branch
git branch -d name   # safe delete (warns if unmerged)
git branch -D name   # force delete
```

Naming convention: `feature/employee-search`, `bugfix/salary-validation`,
`hotfix/jwt-token-expiry`, `release/1.2.0`, `chore/update-dependencies`,
`docs/api-documentation`.

## 9. Merging and Conflict Resolution

```bash
git checkout feature/employee-search
git add . && git commit -m "..."
git checkout main
git merge feature/employee-search
git branch -d feature/employee-search
```

A conflict marks the file:
```
<<<<<<< HEAD
router.get('/employees', getAll)
=======
router.get('/employees', searchEmployees)
>>>>>>> feature/employee-search
```
Edit to the correct version, delete the markers, then `git add <file>` and
`git commit`. Search for stray `<<<<<<<`/`=======`/`>>>>>>>` before
committing — leftover markers mean the conflict wasn't fully resolved.

## 10. Rebase — Linear History (from the hands-on lab, Exercise 7.5)

`git rebase origin/main` detaches your commits, fast-forwards your branch to
`origin/main`, then replays your commits on top one at a time — same goal as
merging (bring your branch up to date) but produces a straight-line history
instead of a merge commit. Conflicts during rebase are resolved per-commit:

```bash
git add <file>
git rebase --continue      # after resolving
git rebase --abort         # bail out cleanly
```

Interactive rebase squashes commits before opening a PR:
```bash
git rebase -i HEAD~3       # pick first, squash (s) the other two
```

Since hashes change, a normal push is rejected — force-push safely:
```bash
git push --force-with-lease origin feature/<branch>
```
`--force-with-lease` refuses to overwrite the remote if someone else pushed
since your last fetch — always prefer it over plain `--force`.

> **Golden rule:** only rebase branches that are still local/yours. Never
> rebase or force-push `main` or any branch teammates have already pulled —
> it rewrites hashes and breaks their history. Rebase is a
> before-you-share tool; merge is the shared-history tool.

## 11. Forking and the Contribution Workflow

```bash
git clone git@github.com:alice-johnson/acme-ems-api.git   # your fork
cd acme-ems-api
git remote add upstream git@github.com:acme-team/acme-ems-api.git
git checkout -b feature/department-filter
git add . && git commit -m "Add department filter"
git push origin feature/department-filter
# → open PR on GitHub: your fork's branch → original repo's main
```

Keeping a fork current:
```bash
git fetch upstream
git checkout main
git merge upstream/main
git push origin main
```

## 12. Collaboration — Pull Requests, Code Review, Protected Branches

**Good PR habits:** small and focused, description of what/why, link issues
(`Closes #42`), request specific reviewers, respond promptly.

**Code review:** comment on specific lines (`+` button), **Request changes**
for blockers, **Approve** when ready, never approve code you don't
understand.

**Branch protection** (GitHub → Settings → Branches): require PR before
merging, require ≥1 approving review, require status checks (CI) to pass, do
not allow bypassing.

## 13. Git Flow vs GitHub Flow

| Branch | Purpose (Git Flow) |
|--------|---------|
| `main` | production-ready only |
| `develop` | integration branch |
| `feature/*` | individual feature dev |
| `release/*` | release prep/bugfixes |
| `hotfix/*` | emergency production fixes |

**GitHub Flow** (simpler, for small teams/solo): `main` always deployable,
branch per feature, PR back to `main`, deploy immediately after merge. The
hands-on lab explicitly follows GitHub Flow — no `develop`/`release`
branches, just `main` + short-lived `feature/*` branches merged via reviewed
PRs.

## 14. Command Reference

| Command | What it does |
|---------|-------------|
| `git init` / `git clone <url>` | new repo / copy a remote repo |
| `git status` / `git diff` | inspect changes |
| `git add` / `git commit -m` | stage / snapshot |
| `git log --oneline --graph` | history |
| `git push` / `git pull` | upload / download+merge |
| `git branch -b <name>` / `git switch -c <name>` | create+switch branch |
| `git merge <branch>` | merge into current |
| `git rebase <branch>` | replay commits on top |
| `git restore <file>` | discard working-dir changes |
| `git stash` / `git stash pop` | park / restore WIP |
| `git revert <hash>` | safe undo (new commit) |
| `git reset --hard <hash>` | destructive rewind |
| `git tag -a v1.0 -m "msg"` | tag a release |


---

# PART 2 — DOCKER

## 1. Why Docker — Containers vs VMs

Docker packages an app together with its runtime, dependencies, and config
into a **container** that runs identically everywhere, solving the "works on
my machine" problem. Containers share the host OS kernel (lightweight,
start in seconds) versus a VM's full guest OS per instance (heavy, slow).

| Term | Meaning |
|------|---------|
| Image | read-only blueprint — like a class |
| Container | running instance of an image — like an object |
| Dockerfile | instructions to build a custom image |
| Registry | image storage (Docker Hub, ECR, ACR) |
| Docker Compose | tool to run multiple containers together |

## 2. Installing and Verifying

```bash
sudo apt-get install docker.io
sudo systemctl start docker && sudo systemctl enable docker
sudo usermod -aG docker $USER && newgrp docker
docker --version
docker run hello-world
```

## 3. Image and Container Commands

```bash
docker images / docker image ls
docker pull node:20-alpine
docker rmi node:20-alpine
docker search node
docker inspect node:20-alpine
docker history node:20-alpine

docker ps / docker ps -a
docker stop <id> / docker start <id> / docker rm <id>
docker container prune
docker logs <id> / docker logs -f <id> / docker logs --tail 50 <id>
docker exec -it <id> bash        # or sh on Alpine
docker cp localfile.txt <id>:/app/

docker system df / docker system prune [-a]
docker info / docker version
```

## 4. `docker run` — Flags That Matter

```bash
docker run -it ubuntu bash             # interactive + tty
docker run -d nginx                    # detached
docker run -d --name my-nginx nginx
docker run -d -p 8080:80 nginx         # host:container port mapping
docker run -d -e NODE_ENV=production -p 3000:3000 my-ems-api
docker run -d -v /home/alice/data:/data/db mongo:7
docker run --rm ubuntu echo "Clean up after me"
docker run -d --memory="512m" --cpus="0.5" my-ems-api
docker run -d --name ems-api -p 3000:3000 -e NODE_ENV=production \
  --restart unless-stopped acme-ems/api:latest
```

`--restart` policies: `no` (default), `always`, `unless-stopped`,
`on-failure`.

## 5. Dockerfile Instructions and Layer Caching

```dockerfile
FROM node:20-alpine          # base image — starts a new layer
WORKDIR /app                 # working dir inside container
COPY package*.json ./        # copy package files FIRST — for cache
RUN npm ci                   # install deps (cached unless package.json changed)
COPY . .                     # copy the rest of the source
EXPOSE 3000                  # documents the port — doesn't publish it
CMD ["node", "server.js"]    # default command at container start
```

Order matters for caching: copying source before `npm ci` busts the cache on
every code change; copying `package*.json` first means `npm ci` only re-runs
when dependencies actually change.

**CMD vs ENTRYPOINT** (from the discussion Q&A): `ENTRYPOINT` is the
executable that always runs and can't be overridden by `docker run`
arguments (only `--entrypoint`); `CMD` supplies default arguments to
`ENTRYPOINT`, or is the default command if no `ENTRYPOINT` is set, and *can*
be overridden. Use `ENTRYPOINT` for the main executable, `CMD` for default
args.

### Multi-Stage Builds

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci                     # includes devDependencies for building
COPY . .
RUN npm run build

FROM node:20-alpine AS production
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production   # no devDependencies
COPY --from=builder /app/dist ./dist
EXPOSE 3000
CMD ["node", "dist/server.js"]
```
Earlier stages hold build tools; the final stage copies only the build
output — much smaller image (course example: 420MB single-stage vs 95MB
multi-stage).

### `.dockerignore`

Same idea as `.gitignore` — keeps unneeded files out of the build context:
`node_modules`, `npm-debug.log`, `.git`, `.gitignore`, `*.md`, `.env`,
`coverage/`, `dist/`.

### Building

```bash
docker build -t acme-ems/api:1.0.0 .
docker build -f Dockerfile.prod -t acme-ems/api:prod .
docker images | grep acme-ems
```

## 6. Docker Compose

`docker-compose.yml` defines a multi-container app in one YAML file. The
course's EMS example wires a `mongo` service (with a named volume for
persistence), an `api` service that `build`s from the local Dockerfile,
depends on `mongo`, and gets its `MONGO_URI` pointed at the `mongo` service
name, and an optional `mongo-express` UI. Key fields: `build.context` /
`build.dockerfile`, `environment`, `ports` (`host:container`), `depends_on`
(start ordering only, not a readiness wait), `volumes` (named volume
persists `/data/db`; bind mount `./logs:/app/logs` persists logs on host),
and a top-level `volumes:` block declaring the named volume.

```bash
docker compose up [-d] [--build]
docker compose stop
docker compose down            # removes containers+networks, KEEPS named volumes
docker compose down -v         # also deletes named volumes (deletes DB data!)
docker compose ps
docker compose logs [-f] [service]
docker compose exec api sh
docker compose up -d --scale api=3
docker compose run api npm test
```

Dev vs prod compose files are combined with `-f`:
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up
```

## 7. Docker Registry

```bash
docker login
docker tag acme-ems/api:1.0.0 yourusername/acme-ems-api:1.0.0
docker push yourusername/acme-ems-api:1.0.0
docker pull yourusername/acme-ems-api:latest
```
Private registry: `docker run -d -p 5000:5000 --name registry registry:2`,
then tag/push to `localhost:5000/...`. Tagging strategy: semantic version
tags (`1.0.0`, `1.0`, `1`, `latest`) or a Git SHA
(`registry/acme-ems-api:$(git rev-parse --short HEAD)`) for traceability.

## 8. Storage and Networking

**Named volume** (Docker-managed, survives container recreation, best for
DB data) vs **bind mount** (maps a host directory in, best for live-reload
dev) vs **tmpfs** (in-memory, not persisted):
```bash
docker volume create ems-data
docker run -v ems-data:/data/db mongo:7
docker run -v $(pwd):/app node:20-alpine
docker run --tmpfs /tmp node:20-alpine
```

**Networking:** containers on the same Docker network reach each other by
**container/service name**, not IP.
```bash
docker network create ems-network
docker run -d --name mongo --network ems-network mongo:7
docker run -d --name ems-api --network ems-network \
  -e MONGO_URI=mongodb://mongo:27017/ems acme-ems/api:latest
```
Default networks: `bridge` (default, internet+inter-container), `host`
(shares host's network stack), `none`, and custom bridge (adds automatic
DNS — Compose puts all services on one of these automatically, which is why
`api` can reach `mongo` by name with zero extra config).

## 9. Orchestration Intro — Docker Swarm

```bash
docker swarm init --advertise-addr <manager-ip>
docker swarm join --token <token> <manager-ip>:2377
docker stack deploy -c docker-compose.yml ems-stack
docker service ls / docker service ps ems-stack_api
docker service scale ems-stack_api=3
docker service update --image acme-ems/api:1.1.0 ems-stack_api
docker stack rm ems-stack
```
Swarm is simpler but less powerful than Kubernetes, which is the
industry-standard choice for production-scale orchestration (Module 03).

## 10. Command Summary

| Concept | Key command |
|---------|------------|
| Run | `docker run -d -p 3000:3000 myimage` |
| Build | `docker build -t myimage:tag .` |
| List containers | `docker ps` / `docker ps -a` |
| Logs | `docker logs -f <container>` |
| Shell in | `docker exec -it <container> sh` |
| Compose up/down | `docker compose up -d` / `docker compose down` |
| Push | `docker push registry/image:tag` |
| Volume/network | `docker volume create` / `docker network create` |


---

# PART 3 — KUBERNETES

## 1. Why Kubernetes and the Control-Plane/Worker Model

Docker Compose runs containers on **one machine** — if it dies, the app
dies, and it can't auto-scale. **Kubernetes (K8s)** orchestrates containers
across a **cluster**: it auto-restarts failed containers, scales up/down on
load, does zero-downtime rolling updates, load balances traffic, and
centrally manages config/secrets. "Docker runs containers. Kubernetes
manages containers."

| Component | Role |
|-----------|------|
| API Server | front door — `kubectl` talks to this |
| etcd | distributed key-value store, cluster's source of truth |
| Scheduler | assigns Pods to nodes |
| Controller Manager | reconciles actual state to desired state |
| kubelet | per-node agent, ensures containers are running |
| kube-proxy | per-node networking rules |

## 2. Cluster Setup and kubectl

```bash
minikube start [--driver=docker]
minikube status / minikube dashboard / minikube stop / minikube delete
kind create cluster --name ems-cluster

kubectl version --client
kubectl config get-contexts / kubectl config use-context minikube
kubectl cluster-info / kubectl get nodes
```
The simplified module's audience note: on Windows 11 + Docker Desktop,
Kubernetes is enabled directly in Docker Desktop settings rather than via
Minikube — verify with `kubectl version`, `kubectl cluster-info`,
`kubectl get nodes`.

## 3. YAML and the Standard Manifest Shape

Every K8s resource YAML has the same top-level shape:
```yaml
apiVersion: apps/v1     # API group/version this resource belongs to
kind: Deployment         # resource type
metadata:
  name: ems-api
  namespace: default
  labels:
    app: ems-api
spec:                     # desired state
  # ...
```
`apiVersion` = version, `kind` = resource type, `metadata` = name/labels,
`spec` = configuration. Common mistakes: inconsistent indentation (YAML uses
spaces only, never tabs) and unquoted strings containing a colon (`message:
"Error: connection refused"` must be quoted).

## 4. Pods

The smallest deployable unit — one or more containers sharing network and
storage, usually **1 Pod = 1 container** in practice. Rarely created
directly — normally managed via a Deployment.
```bash
kubectl apply -f pod.yaml
kubectl get pods [-o wide]
kubectl describe pod ems-api-pod
kubectl logs [-f] ems-api-pod [-c container-name]
kubectl exec -it ems-api-pod -- sh
kubectl delete pod ems-api-pod   # or: kubectl delete -f pod.yaml
```
A Pod spec includes `containers[].image`, `.ports[].containerPort`,
`.env[]`, and `.resources.requests/limits` (CPU/memory).

## 5. ReplicaSet and Deployment

A **ReplicaSet** keeps N identical Pods running — if one crashes, it
creates a replacement (`spec.replicas`, `spec.selector.matchLabels`,
`spec.template`). You rarely create ReplicaSets directly.

A **Deployment** wraps a ReplicaSet and adds rolling updates, rollback, and
pause/resume — it's the standard way to run an app. Its
`spec.strategy.rollingUpdate.maxSurge`/`maxUnavailable` control how many
extra/short pods are allowed mid-rollout. It also carries
`livenessProbe` (restarts the container if it fails) and `readinessProbe`
(stops routing traffic to the pod if it fails), each with `httpGet.path`,
`.port`, `initialDelaySeconds`, `periodSeconds`.

```bash
kubectl apply -f deployment.yaml
kubectl get deploy
kubectl rollout status deployment/ems-api
kubectl rollout history deployment/ems-api
kubectl set image deployment/ems-api ems-api=yourusername/acme-ems-api:1.1.0
kubectl rollout undo deployment/ems-api [--to-revision=2]
kubectl scale deployment ems-api --replicas=5
```

## 6. Services

Pods are ephemeral and get new IPs when recreated. A **Service** gives a
stable network endpoint that load-balances to whatever Pods match its
`selector`.

| Type | Reachable from | Use case |
|------|----------------|---------|
| `ClusterIP` (default) | inside cluster only | internal service-to-service |
| `NodePort` | outside, via `<node-ip>:<nodePort>` (30000-32767) | dev/testing |
| `LoadBalancer` | outside, via cloud LB | production on cloud |
| `ExternalName` | aliases external DNS | connecting to external services |

`spec.port` is what clients connect to; `spec.targetPort` is the port the
Pod actually listens on — they can differ.

## 7. ConfigMaps and Secrets

`ConfigMap` holds non-sensitive key/value config, consumed via
`envFrom.configMapRef.name`. `Secret` holds sensitive values — created
ad-hoc (`kubectl create secret generic ... --from-literal=...`) or declared
in YAML under `data:` with **base64-encoded** values (`echo -n "..." |
base64`), consumed per-key via `env[].valueFrom.secretKeyRef.{name,key}`.
Note: base64 is encoding, not encryption — Secrets are not encrypted at
rest by default.

## 8. Networking, DNS, and Ingress

```
Internet → Ingress (optional, host/path routing) → Service (stable DNS, load-balances) → Pods (ephemeral)
```
Services get automatic DNS: `<service>.<namespace>.svc.cluster.local`, so
the API can reach Mongo via `mongodb://mongo-service:27017/ems` inside the
same namespace. An `Ingress` resource (needs an ingress controller such as
nginx-ingress) routes HTTP by hostname/path to different backend Services
via `spec.rules[].http.paths[].backend.service.{name,port}`.

## 9. PersistentVolumeClaim

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mongo-pvc
spec:
  accessModes: [ReadWriteOnce]   # one node can mount at a time
  resources:
    requests:
      storage: 2Gi
```
Referenced from a Deployment's `volumes[].persistentVolumeClaim.claimName`
and mounted into the container via `volumeMounts[].mountPath`.

## 10. Namespaces and Deploying Everything

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mongo/
kubectl apply -f k8s/api/
kubectl apply -R -f k8s/           # recursively apply a whole folder
kubectl get all -n ems
kubectl port-forward service/ems-api-service 3000:80 -n ems
```

## 11. Multi-Node Cluster with kubeadm (Appendix)

For a production-like local multi-VM setup: install `kubelet`/`kubeadm`/
`kubectl` on all nodes, `kubeadm init --pod-network-cidr=...` on the master
only, copy `/etc/kubernetes/admin.conf` to `~/.kube/config`, install a CNI
plugin (Flannel), then `kubeadm join` from each worker using the token the
master printed.

## 12. Summary Table

| Resource | Purpose | Create with |
|----------|---------|-------------|
| Pod | containers, shared network | rarely directly — use Deployment |
| ReplicaSet | maintain N replicas | rarely directly — use Deployment |
| Deployment | ReplicaSets + rolling updates | `kubectl apply -f deployment.yaml` |
| Service (ClusterIP/NodePort/LoadBalancer) | stable endpoint | `kubectl apply -f service.yaml` |
| ConfigMap / Secret | config / sensitive config | `kubectl create configmap/secret` |
| PVC | persistent storage | `kubectl apply -f pvc.yaml` |
| Namespace | logical isolation | `kubectl create namespace` |
| Ingress | HTTP routing by host/path | `kubectl apply -f ingress.yaml` |


---

# PART 4 — ANSIBLE

## 1. What Problem Ansible Solves — Infrastructure as Code

Manually configuring N servers by hand produces "snowflake servers" — each
one slightly different, impossible to reproduce. Ansible lets you write a
playbook once and run it against all servers, producing identical,
reproducible, documented infrastructure. This is **Infrastructure as Code
(IaC)** — infra config treated like application code: versioned, reviewed,
testable, repeatable.

| Tool | Approach | Language | Agent needed? |
|------|----------|---------|---------------|
| Ansible | procedural/declarative | YAML | agentless (SSH) |
| Chef | procedural | Ruby | needs agent |
| Puppet | declarative | Puppet DSL | needs agent |
| Terraform | declarative | HCL | agentless |

Ansible advantages: agentless (only needs Python + SSH on the target),
readable YAML, **idempotent** (running twice gives the same result), huge
community (Ansible Galaxy roles).

## 2. Architecture — Control Node and Managed Nodes

The control node (your laptop or a CI server) holds the inventory,
playbooks, roles, and variables, and pushes changes to managed nodes over
SSH. **No agent is ever installed on the managed nodes** — they just need
Python and SSH.

```bash
sudo apt install ansible     # or: pip install ansible
ansible --version
```

## 3. Inventory

```ini
# inventory.ini
192.168.1.100
server01.acme.local

[web]
web01.acme.local
web02.acme.local

[db]
db01.acme.local  ansible_user=ubuntu  ansible_port=22

[backend:children]
web
db

[web:vars]
http_port=80
```
```bash
ansible all -i inventory.ini -m ping
ansible all -i inventory.ini -m shell -a "uptime"
ansible web -i inventory.ini -m shell -a "node --version"
```

## 4. Modules

Modules are the unit of work Ansible executes — thousands exist. Ad-hoc
examples: `ping` (connectivity), `shell`/`command` (run a command — prefer
`command` when possible, it's safer/more predictable than `shell`), `copy`,
`file` (create dirs/files with state), `apt`/`yum` (install packages),
`service`/`systemd` (manage services), `git` (clone/checkout a repo),
`template` (render a Jinja2 file), `user` (manage OS users),
`community.docker.docker_container`/`docker_image` (manage Docker from
Ansible).

## 5. Playbooks — Anatomy

```yaml
---
- name: Configure web servers      # a "play" — targets a group
  hosts: web
  become: true                     # sudo for privileged tasks
  vars:
    app_dir: /opt/ems-api
  tasks:
    - name: Update apt cache
      apt:
        update_cache: true
        cache_valid_time: 3600
    - name: Install Node.js via NodeSource
      shell: |
        curl -fsSL https://deb.nodesource.com/setup_{{ node_version }}.x | bash -
        apt-get install -y nodejs
      args:
        creates: /usr/bin/node    # skip if already present — idempotency
    - name: Verify Node.js installation
      command: node --version
      register: node_version_output
    - name: Print Node.js version
      debug:
        msg: "Node.js version: {{ node_version_output.stdout }}"
```
`args.creates` and dedicated modules (`apt`, `service`) are how Ansible
achieves idempotency — they check current state before acting instead of
blindly re-running a command.

```bash
ansible-playbook -i inventory.ini site.yaml
ansible-playbook -i inventory.ini site.yaml --check       # dry run
ansible-playbook -i inventory.ini site.yaml --diff
ansible-playbook -i inventory.ini site.yaml --tags "nodejs"
ansible-playbook -i inventory.ini site.yaml --extra-vars "node_version=20"
ansible-playbook -i inventory.ini site.yaml -v / -vvv
ansible-playbook -i inventory.ini site.yaml --limit web01.acme.local
```

## 6. Variables and Jinja2 Templates

Precedence (highest → lowest): `--extra-vars` → task vars → role defaults →
`host_vars` → `group_vars` → inventory vars → role vars → playbook vars.

`group_vars/all.yaml`, `group_vars/web.yaml`, `host_vars/web01....yaml` scope
variables to all hosts / a group / one host. A `template` task renders a
Jinja2 file with variables substituted:
```
NODE_ENV={{ node_env | default('production') }}
MONGO_URI=mongodb://{{ mongo_user }}:{{ mongo_password }}@{{ mongo_host }}:27017/ems
```

## 7. Conditionals, Loops, Handlers

```yaml
- name: Install nginx (Ubuntu only)
  apt: {name: nginx, state: present}
  when: ansible_os_family == "Debian"

- name: Install multiple packages
  apt: {name: "{{ item }}", state: present}
  loop: [nodejs, npm, git, curl]
```
**Handlers** run once, at the end of a play, only if a task that `notify`s
them actually reports `changed`:
```yaml
tasks:
  - name: Copy nginx config
    copy: {src: nginx.conf, dest: /etc/nginx/nginx.conf}
    notify: Restart nginx
handlers:
  - name: Restart nginx
    service: {name: nginx, state: restarted}
```
The `notify:` value and the handler's `name:` must match **exactly**
(case and spacing) — they're matched as plain strings.

## 8. Roles

A structured, reusable bundle of tasks/handlers/templates/files/vars for one
concern:
```
roles/ems-api/
├── tasks/main.yaml       ← entry point
├── handlers/main.yaml
├── templates/env.j2
├── files/ems.service     ← static files
├── vars/main.yaml        ← high-priority
├── defaults/main.yaml    ← low-priority, easy to override
└── meta/main.yaml
```
```bash
ansible-galaxy role init roles/ems-api
```
Used in a playbook via `roles: [{role: nodejs, vars: {...}}, {role: ems-api, vars: {...}}]`.

## 9. Ansible Vault — Encrypting Secrets

```bash
ansible-vault create group_vars/all/vault.yaml
ansible-vault edit group_vars/all/vault.yaml
ansible-vault encrypt group_vars/all/vault.yaml
ansible-playbook site.yaml --ask-vault-pass
ansible-playbook site.yaml --vault-password-file ~/.vault_pass.txt
```
Encrypted vars (`vault_mongo_password`) live in the vault file; a plain
`vars.yaml` references them (`mongo_password: "{{ vault_mongo_password }}"`)
so non-secret files stay diff-able while secrets stay encrypted at rest.

## 10. Summary

| Concept | Description |
|---------|-------------|
| Inventory | list of managed servers, grouped |
| Module | unit of work (`apt`, `copy`, `shell`, `service`, `git`...) |
| Task | one module invocation |
| Play | tasks targeting a host group |
| Playbook | YAML file of one or more plays |
| Handler | task that only runs when `notify`d by a change |
| Role | reusable structured bundle of tasks/vars/templates |
| Vault | encrypted secret storage |
| Idempotency | running the same playbook repeatedly = same end state |


---

# PART 5 — CI/CD WITH JENKINS

## 1. What CI/CD Is

Without CI/CD: code gets pushed Friday, "works on my machine," QA finds 50
bugs Monday, an integration nightmare follows, someone fixes it at 2am. With
CI/CD, every push automatically checks out code, installs deps, runs tests,
checks quality, builds the image, pushes it, deploys to staging, runs smoke
tests, deploys to production — done in minutes, every time.

| Term | Meaning |
|------|---------|
| CI — Continuous Integration | auto build+test on every commit |
| CD — Continuous Delivery | auto-prepare a release, deploy to staging |
| CD — Continuous Deployment | auto-deploy to production |

The EMS pipeline the module builds: Checkout → Install → Test → Build
(Docker) → Push → Deploy (`kubectl apply` or `ansible-playbook`).

## 2. Installing Jenkins

Native (Ubuntu, needs Java 17+):
```bash
sudo apt install -y openjdk-17-jdk
# add pkg.jenkins.io repo, then:
sudo apt install -y jenkins
sudo systemctl start jenkins && sudo systemctl enable jenkins
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```
Or in Docker, for learning:
```bash
docker run -d --name jenkins -p 8080:8080 -p 50000:50000 \
  -v jenkins-data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17
```
Essential plugins: Git, Pipeline, Docker Pipeline, Kubernetes CLI, Ansible,
Blue Ocean (optional), Credentials Binding.

## 3. Jenkins Fundamentals

| Concept | Meaning |
|---------|-----------|
| Job/Project | a task Jenkins runs |
| Build | one execution of a job |
| Pipeline | sequence of stages defined in code (Jenkinsfile) |
| Stage | logical phase (Build/Test/Deploy) |
| Step | individual action within a stage |
| Agent | where the pipeline runs |
| Workspace | folder Jenkins checks code out into |
| Credentials | stored secrets |

Credentials kinds (Manage Jenkins → Credentials): Username with password
(Docker Hub/NPM), Secret text (API tokens), SSH Username with private key,
Certificate. **Never hardcode secrets** in a Jenkinsfile — always reference
a credentials ID.

## 4. Declarative Pipeline Syntax — CI Stage by Stage

```groovy
pipeline {
    agent any
    environment {
        NODE_VERSION = '20'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/acme-team/acme-ems-api.git'
            }
        }
        stage('Install Dependencies') { steps { sh 'npm ci' } }
        stage('Lint')  { steps { sh 'npm run lint' } }
        stage('Test')  {
            steps { sh 'npm test' }
            post { always { junit 'test-results/*.xml' } }
        }
        stage('Security Audit') { steps { sh 'npm audit --audit-level=high' } }
    }
    post {
        success { echo "Pipeline passed! Build #${env.BUILD_NUMBER}" }
        failure { echo "Pipeline failed" }
        always  { cleanWs() }
    }
}
```
Creating the job: New Item → Pipeline → Pipeline script from SCM → Git repo
URL, branch `*/main`, Script Path `Jenkinsfile` → Save → Build Now.
Auto-trigger on push: enable **GitHub hook trigger for GITScm polling** in
the job, add a GitHub webhook to
`http://<jenkins-server>:8080/github-webhook/` for the push event.

## 5. Integrating a Deploy Server over SSH (Tomcat Reference)

The course syllabus references Tomcat (Java web server) as the deploy target;
in the Node.js EMS context the pattern is identical over SSH:
```groovy
stage('Deploy to Server') {
    steps {
        sshagent(['server-ssh-key']) {
            sh """
                ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOY_HOST} '
                    cd /opt/ems-api && git pull origin main &&
                    npm ci --only=production && pm2 restart ems-api
                '
            """
        }
    }
}
```

## 6. Integrating Docker

```groovy
environment {
    DOCKER_IMAGE       = 'yourusername/acme-ems-api'
    DOCKER_CREDENTIALS = 'dockerhub-credentials'
    IMAGE_TAG          = "${env.BUILD_NUMBER}"
}
stages {
    stage('Build Docker Image') {
        steps {
            script { dockerImage = docker.build("${DOCKER_IMAGE}:${IMAGE_TAG}") }
            sh "docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} ${DOCKER_IMAGE}:latest"
        }
    }
    stage('Push Docker Image') {
        steps {
            script {
                docker.withRegistry("https://docker.io", DOCKER_CREDENTIALS) {
                    dockerImage.push("${IMAGE_TAG}")
                    dockerImage.push("latest")
                }
            }
        }
    }
}
```
The Jenkins server itself needs Docker access:
`sudo usermod -aG docker jenkins && sudo systemctl restart jenkins`.

## 7. Integrating Ansible

```groovy
stage('Deploy with Ansible') {
    steps {
        writeFile file: '.vault_pass', text: "${ANSIBLE_VAULT_PASS}"
        ansiblePlaybook(
            playbook: 'ansible/deploy.yaml',
            inventory: 'ansible/inventory/production.ini',
            vaultCredentialsId: 'ansible-vault-password',
            extras: "--extra-vars \"image_tag=${IMAGE_TAG}\""
        )
    }
    post { always { sh 'rm -f .vault_pass' } }
}
```
The called playbook pulls the new image, stops the old container, starts
the new one with `community.docker.docker_container`, then polls
`/api/health` with `retries`/`delay`/`until` before declaring success.

## 8. Integrating Kubernetes — The Full Pipeline

Stages: Checkout (captures `GIT_SHORT_SHA`) → Install Dependencies → Run
Tests (`junit` report) → Build Docker Image (tagged with build number, git
SHA, and `latest`) → Push to Registry (`withCredentials` for Docker Hub
login) → Deploy to Staging (`withKubeConfig`, `kubectl set image` +
`kubectl rollout status --timeout=120s`) → Smoke Test (Staging) (curl the
LoadBalancer IP's `/api/health`) → Deploy to Production, gated by
`when { branch 'main' }` **and** a manual `input` approval step
(`message`, `ok`, `submitter: "jenkins-admins"`).
`post.failure` automatically runs `kubectl rollout undo deployment/ems-api`
— an automatic rollback on pipeline failure. `post.always` cleans up the
local Docker image and the workspace.

## 9. Best Practices

- **Pipeline as code**: store `Jenkinsfile` in the repo root, version it,
  review changes via PR.
- **Never hardcode credentials** — use `credentials('id')` or
  `withCredentials([...])`.
- **Fail fast**: order stages cheapest/fastest-to-fail first (Checkout →
  Install → Lint → Test → Build → Push → Deploy).
- **Parallel stages** for independent checks:
  ```groovy
  stage('Test & Lint in Parallel') {
      parallel {
          stage('Unit Tests') { steps { sh 'npm test' } }
          stage('Lint')        { steps { sh 'npm run lint' } }
      }
  }
  ```
- **Environment-specific deploys**: branch off `env.BRANCH_NAME` to pick a
  namespace/target dynamically.

## 10. Summary

| Stage | Tool | What happens |
|-------|------|-------------|
| Source control | Git + GitHub | versioned code, webhook triggers pipeline |
| CI server | Jenkins | orchestrates all stages |
| Build tool | npm/Maven | install, compile, package |
| Testing | npm test | unit/integration/lint |
| Containerisation | Docker | build + tag image |
| Registry | Docker Hub | store/distribute images |
| Server deploy | Ansible | pull image, restart container on VMs |
| K8s deploy | kubectl | rolling update across the cluster |
| Monitoring | health probes | verify post-deploy health |

---

# PART 6 — ASSESSMENT GOTCHAS (from the MCQ bank and discussion Q&A)

The 75-question MCQ bank (`06-mcq-assessment.md`) covers Git/GitHub
(Q1–18), Docker (Q19–36), Kubernetes (Q37–52), Ansible (Q53–62), and CI/CD
(Q63–75) — matching the five modules above 1:1. A few precise distinctions
worth memorizing verbatim from the discussion Q&A (`07-devops_discussion_qa.md`):

- **`git fetch` vs `git pull`**: `fetch` downloads but does not merge — safe
  to run anytime; `pull` = `fetch` + `merge`. Prefer `fetch` then `git diff`
  before merging when you want to review first.
- **`git reset` vs `git revert`**: `revert` creates a new commit undoing a
  previous one — safe on shared branches; `reset` moves the branch pointer
  and rewrites history — never use on pushed shared branches.
- **`git clone` vs fork**: `clone` copies a repo locally; fork (a GitHub
  concept, not a Git command) copies someone else's repo to your own GitHub
  account first, so you can PR back without write access to the original.
- **`CMD` vs `ENTRYPOINT`**: `ENTRYPOINT` is fixed at container start;
  `CMD` supplies overridable default arguments (or the whole default
  command if no `ENTRYPOINT`).
- **Named volume vs bind mount**: volumes are Docker-managed and portable
  (use for DB data in production); bind mounts map a host path directly in
  (use for live-reload dev).
- **`docker compose down` vs `down -v`**: plain `down` keeps named volumes
  (data survives); `-v` deletes them too.
- **`docker stop` vs `docker kill`**: `stop` sends `SIGTERM` then
  `SIGKILL` after a grace period (graceful shutdown); `kill` sends
  `SIGKILL` immediately (abrupt). Always prefer `stop`.


---

# PART 7 — WALKING THROUGH THE REAL CODE

## 7.1 Git/GitHub Project — `Code/Git Github/`

This folder is the **starter project** trainees fork for the Git & GitHub
hands-on lab — a Java 17 / Spring Boot 3 Maven app (`pom.xml`,
`EmsApplication.java`) split into four entity packages (`employee`,
`department`, `project`, `job`), each with `model` / `repository` /
`service` / `controller` layers, matching the Team → Package → Ownership
Map in the lab guide exactly. Its `README.md` states the project's real
purpose: "give your team real files to practice Git and GitHub on... the
real assignment is the workflow." `changelogs/employee-CHANGELOG.md` (and
one per entity) is the deliberately **shared** file every team member edits
in Exercise 4 — its content is literally an `## Unreleased` list with a
one-line comment instructing each of the four members to add one entry,
which is the mechanism that reliably produces the real merge conflict in
Exercise 7 (all four branch from the same commit and touch the same file).
`scratch.txt` and a `.gitignore` are present at the root, matching Exercise
5's "Oops Lab" scratch-file workflow. No further line-by-line treatment is
needed here — the file *is* the exercise, not a system to reverse-engineer.

## 7.2 Docker — `docker-demo/` (the simple case)

### `docker-demo/Dockerfile`

```dockerfile
FROM node:22-alpine
WORKDIR /app
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
```

Alpine base for a small footprint. `COPY . .` copies the **entire build
context** in one shot — no separate `COPY package*.json ./` + `RUN npm ci`
layer-caching step first, so any source change busts every layer above it.
The course explicitly calls this "inefficient," but it's acceptable here
since this is a two-file demo with zero dependencies (`package.json` has
no `dependencies` block, so there's nothing to cache anyway). `EXPOSE 3000`
only documents the port — `docker run -p` is what actually publishes it.
`CMD` (exec form) delegates to the `start` script defined in `package.json`.

### `docker-demo/app.js`

```javascript
import { createServer } from "http";
const server = createServer((req, res) => {
  res.writeHead(200, { "Content-Type": "text/plain" });
  res.end("Hello from Docker!\n");
});
server.listen(3000, () => console.log("Server running on port 3000"));
```
Uses Node's built-in `http` module — no Express dependency at all, the
leanest possible demo server — to answer every request with a plain-text
`200`, just enough to prove the container is reachable.

### `docker-demo/package.json`

`package.json`'s `"start": "node app.js"` script is what
`CMD ["npm", "start"]` ultimately invokes; there's no `dependencies` block
at all, which is also why the Dockerfile skips the layer-caching
`COPY package*.json ./` step entirely — nothing to cache.


## 7.3 Docker — `acme-ems-docker/` (the full EMS deployment)

This is the completed Docker lab: a Spring Boot 3 / Java 17 / Maven app
(same four-entity EMS structure as the Git lab) fronted by Postgres and
Adminer, built with a real multi-stage Dockerfile.

### `docker/Dockerfile` vs `docker/Dockerfile.starter` — what changed

```dockerfile
# ---------- STAGE 1: build ----------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- STAGE 2: run ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

This is the same layer-caching multi-stage pattern as `docker-demo`, applied
to a Maven/Java build instead of a dependency-free Node script: `COPY
pom.xml .` + `mvn dependency:go-offline -B` (batch mode) downloads and
caches dependencies in their own layer *before* `COPY src ./src`, so
source-only changes skip re-downloading the whole dependency tree.
`-DskipTests` keeps the image build fast. Stage 2 switches to a **JRE-only**
Alpine base (no compiler, no Maven) and `COPY --from=build` pulls out only
the compiled jar by name — none of the source, `pom.xml`, or `.m2` cache
crosses into the final image, keeping it small. `ENTRYPOINT` (exec form,
not shell form) is used instead of `CMD` so the startup command can't be
silently overridden by stray `docker run` arguments — appropriate for a
"this container only ever does one thing" production image.

The finished file pins `maven:3.9.8-eclipse-temurin-17`; the `.starter`
template only specified the floating `maven:3.9-eclipse-temurin-17` — a
minor version-pinning improvement made while filling in the lab. All the
`.starter` file's `????` blanks (base image, `-DskipTests`, `EXPOSE`) were
correctly resolved in the finished version above.

### `docker/docker-compose.yml`

```yaml
services:
  app:
    build:
      context: ..
      dockerfile: docker/Dockerfile
    container_name: ems-app
    ports:
      - "8080:8080"
    depends_on:
      - db
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/emsdb
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres

  db:
    image: postgres:16-alpine
    container_name: ems-db
    restart: always
    environment:
      POSTGRES_DB: emsdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql

  adminer:
    image: adminer
    container_name: ems-adminer
    ports:
      - "8081:8080"
    depends_on:
      - db

volumes:
  postgres-data:
```

`context: ..` builds from the **repo root**, one level above `docker/`
(where this file lives), because the Dockerfile needs `pom.xml` and `src/`
from the project root; `dockerfile:` then points to `docker/Dockerfile`
relative to that root context. `depends_on: [db]` controls **start order
only** -- it does not wait for Postgres to actually be ready to accept
connections, a known Compose limitation. The app's `SPRING_DATASOURCE_*`
vars point at the **service name** `db` (resolved by Docker's internal DNS
on the default network Compose creates), never `localhost` or a hardcoded
IP -- and the DB name/user/password there must match `db`'s own
`POSTGRES_*` values exactly, a coupling that's why moving both into a
shared `.env` file (as `docker/db/.env.example` shows) is the safer
real-world pattern. `restart: always` is the strongest of the four restart
policies (Part 2 Section 4). The `db` service mixes a **named volume**
(`postgres-data:/var/lib/postgresql/data`, so data survives `docker compose
down` without `-v`) with a **bind mount** of `init.sql` into Postgres's
official `/docker-entrypoint-initdb.d/` directory -- any `.sql`/`.sh` file
found there runs **once, only on first startup** (empty data directory).
`adminer` (a single-file DB-browser UI) maps to host port `8081`, distinct
from the app's `8080`.

**Discrepancy worth knowing for the exam:** `docker-compose.yml.starter`
scaffolds a **fourth** service block, `proxy` (Nginx, forwarding `:80` to
`app:8080` via `docker/proxy/nginx.conf`), with hints identical in spirit
to the `docker-demo` proxy exercise. The **finished** `docker-compose.yml`
above does **not** include a `proxy` service at all — the trainee completed
`app`, `db`, and `adminer` but left the reverse-proxy piece out. If asked
"does this EMS stack use Nginx," the honest answer based on the actual
compose file is **no** — Nginx only exists as a `nginx.conf.starter`
exercise file, never wired into the running stack.

### `docker/proxy/nginx.conf.starter` (unfinished)

```nginx
events {}

http {
    server {
        listen 80;

        location / {
            # TODO (Lab): set proxy_pass to the app service.
            # Hint: proxy_pass http://????:8080;

            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
```
The unresolved TODO (`proxy_pass http://????:8080;`) should become
`proxy_pass http://app:8080;`, using the Compose service name `app` --
exactly the "containers reach each other by service name, not IP"
principle from Part 2 Section 8. `proxy_set_header` forwards the original
`Host` header and the client's real IP through to the upstream app,
standard reverse-proxy hygiene so the backend doesn't just see
`127.0.0.1` for every request. This file was never actually renamed to
`nginx.conf` or wired into `docker-compose.yml`.

### `docker/db/init.sql`

```sql
-- Optional seed data. Hibernate creates the tables automatically on first
-- app startup (ddl-auto=update), so this script is OPTIONAL -- it just
-- gives you rows to look at in Adminer before the app writes any data.
-- Table names come from each entity's @Table annotation: employees,
-- departments, projects, jobs.

-- INSERT INTO departments (name, location, head_count)
-- VALUES ('Engineering', 'Bengaluru', 42);
```
The seed `INSERT` is left commented out as a TODO -- never uncommented in
this project, so the DB starts empty aside from whatever Hibernate/the app
itself writes.

### `docker/db/.env.example`

```ini
# Copy this file to docker/db/.env and adjust as needed.
# Never commit the real .env file -- it's already in .gitignore.

POSTGRES_DB=emsdb
POSTGRES_USER=ems_user
POSTGRES_PASSWORD=change_me
```
Note this template's suggested user is `ems_user`, but the finished
`docker-compose.yml` above actually hardcodes `POSTGRES_USER: postgres`
directly rather than sourcing it from a real `.env` -- a sign the
`.env`-file indirection from the starter kit wasn't carried through to the
final, checked-in compose file.


### `k8s/pod-solo.yaml` (bare Pod primitive, Exercise 2)

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: acme-ems-api-solo
  labels:
    app: acme-ems-api-solo
spec:
  containers:
    - name: acme-ems-api
      image: vamandeshmukh/acme-ems-api:1.0
      ports:
        - containerPort: 8080
```
`Pod` is a core (`v1`) resource, not part of the `apps/` API group like
Deployments -- this is the raw, bare-metal primitive you rarely deploy
directly in real work (Exercise 3 moves to a Deployment, which manages
Pods for you). `image` was filled in with the trainee's own pushed Docker
Hub image from the Docker lab -- the handoff artifact between the two
labs. `containerPort: 8080` matches the Dockerfile's `EXPOSE 8080`.

### `k8s/app-deployment.yaml` (Exercise 3 — self-healing demo)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ems-app
  labels:
    app: ems-app
spec:
  replicas: 4
  selector:
    matchLabels:
      app: ems-app
  template:
    metadata:
      labels:
        app: ems-app
    spec:
      containers:
        - name: ems-app
          image: vamandeshmukh/acme-ems-api:1.0
          ports:
            - containerPort: 8080
```
A Deployment (`apps/v1`, unlike the bare `v1` Pod above) manages a
ReplicaSet, which manages Pods -- you describe desired state and
Kubernetes continuously reconciles reality to match, which is what makes
the self-healing demo work (delete a Pod and watch it get recreated).
`spec.selector.matchLabels` **must** match
`spec.template.metadata.labels` -- this is the live wiring between the
Deployment and the Pods it manages; if they diverge, `kubectl apply` fails
validation. The trainee chose `replicas: 4` rather than the suggested 2,
using the same image reference as `pod-solo.yaml`.

### `k8s/app-service.yaml` (Exercise 4)

```yaml
apiVersion: v1
kind: Service
metadata:
  name: ems-app
spec:
  type: ????
  selector:
    app: ems-app
  ports:
    - port: 8080
      targetPort: ????
```
A Service gives Pods (disposable, new IPs on every recreation) one stable
name/IP that routes to whatever currently matches its `selector` --- which
**must** match the Pod template labels from `app-deployment.yaml`, the
live routing link; get it wrong and the Service has zero endpoints. This
file was left **unfinished**: `type: ????` and `targetPort: ????` were
never resolved. Correct values (Part 3 Section 6): `type: NodePort` (or
`LoadBalancer` on a real cloud cluster) for "reachable from outside the
cluster" -- `ClusterIP`, the default, would **not** work here -- and
`targetPort: 8080` to match `containerPort`. As checked in, this file is
**not directly applyable**: `kubectl apply -f` fails YAML/schema
validation on the literal `????` tokens.

### `k8s/db-secret.yaml`, `db-pvc.yaml`, `db-deployment.yaml`, `db-service.yaml` (Exercise 5)

All four database manifests are likewise left as **unfinished starters**
with `????` placeholders still in place. Key concepts and intended values:

- **`db-secret.yaml`** (`type: Opaque`, generic key/value Secret) uses
  `stringData` (not `data`) deliberately -- Kubernetes accepts **plain-text**
  values under `stringData` and base64-encodes them automatically on
  write, sparing hand-encoding, unlike the `data:` field shown elsewhere
  in the courseware (Part 3 Section 7) which requires pre-encoded base64.
  `POSTGRES_DB`/`_USER`/`_PASSWORD` are left as `????`, meant to mirror
  `docker/db/.env`.
- **`db-pvc.yaml`** leaves `accessModes` (should be `ReadWriteOnce` -- a
  single Postgres Pod only ever mounts its own volume from one node at a
  time) and `storage` (should be a size like `1Gi`) as `????`.
- **`db-deployment.yaml`** hardcodes `replicas: 1` **intentionally, not as
  a TODO** -- a plain Postgres container has no built-in clustering, so
  2+ replicas would mean two independent instances writing to the *same*
  PVC and corrupting data (real multi-replica DBs need a StatefulSet plus
  DB-level replication, out of scope here). Left as `????`:
  `containerPort` (should be `5432`), `envFrom.secretRef.name` (should
  reference `ems-db-secret`, injecting the three `POSTGRES_*` keys as env
  vars without duplicating values), `volumeMounts.mountPath` (should be
  `/var/lib/postgresql/data`), and `claimName` (should reference
  `ems-db-pvc`).
- **`db-service.yaml`** hardcodes `type: ClusterIP` correctly (**not** a
  TODO) -- unlike `app-service.yaml`, the database must stay
  internal-only, never reachable from outside the cluster. `targetPort` is
  left as `????` (should be `5432`).

**Takeaway for the exam:** the `acme-ems-docker/k8s/` folder is a **mixed
state** -- `pod-solo.yaml` and `app-deployment.yaml` are fully completed
and would `kubectl apply` cleanly; `app-service.yaml` and all four
`db-*.yaml` files still contain literal `????` placeholders and would fail
to apply as-is. Know both the intended correct values and the fact that
this checked-in copy is unfinished -- useful if asked to debug "why won't
this manifest apply."


## 7.4 Ansible — `Code/Ansible/` (playbook-driven deploy with its own Jenkinsfile)

This project is a minimal Express app (`acme-ansible-demo`) whose deployment
is delegated to an Ansible playbook that itself shells out to `kubectl`
against a **local** Kubernetes cluster (Docker Desktop's built-in cluster)
-- i.e., Ansible here is used as an orchestration/idempotency wrapper around
`kubectl`, not for provisioning VMs.

### `ansible/inventory.ini`

```ini
[local]
localhost ansible_connection=local
```
A single group, `[local]`, whose only host is `localhost` with
`ansible_connection=local` -- this tells Ansible to run tasks as **local
shell commands** rather than opening an SSH connection to itself, the
correct choice since the "target" is the same Windows/WSL machine Jenkins
and `kubectl` already run on, not a remote server.

### `ansible/deploy-playbook.yml`

```yaml
---
- name: Deploy acme-ansible-demo to Kubernetes
  hosts: local
  gather_facts: false

  vars:
    image_name: "vamandeshmukh/acme-ansible-demo"
    deployment_name: "acme-ansible-demo"
    container_name: "acme-ansible-demo"
    image_tag: "latest"

  tasks:

    - name: Ensure the Deployment and Service exist (creates on first run, no-op after)
      ansible.builtin.command:
        cmd: "kubectl apply -f {{ playbook_dir }}/../k8s/"
      register: apply_result
      changed_when: "'unchanged' not in apply_result.stdout"

    - name: Ensure Kubernetes uses the pushed image (registry pull allowed)
      ansible.builtin.command:
        cmd: >
          kubectl patch deployment {{ deployment_name }}
          -p "{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"{{ container_name }}\",\"imagePullPolicy\":\"IfNotPresent\"}]}}}}"
      register: patch_result
      changed_when: "'no change' not in patch_result.stdout"

    - name: Point the deployment at the new image tag
      ansible.builtin.command:
        cmd: "kubectl set image deployment/{{ deployment_name }} {{ container_name }}={{ image_name }}:{{ image_tag }}"
      register: set_image_result
      changed_when: "'image updated' in set_image_result.stdout"

    - name: Wait for the rollout to finish
      ansible.builtin.command:
        cmd: "kubectl rollout status deployment/{{ deployment_name }} --timeout=90s"
      register: rollout_result
      changed_when: false

    - name: Show rollout result
      ansible.builtin.debug:
        msg: "{{ rollout_result.stdout }}"
```
`hosts: local` targets the `[local]` group; `gather_facts: false` skips
Ansible's Setup module fact-gathering (unneeded since every task is just a
`kubectl` call). Play-level `vars` parameterize image name,
Deployment/container name, and `image_tag` (defaults to `"latest"`,
overridden via `--extra-vars "image_tag=..."` by the Jenkinsfile below).

Every task uses the generic `ansible.builtin.command` module rather than a
purpose-built one, which means Ansible has no innate way to know whether
anything "changed" -- `kubectl apply`/`patch`/`set image` all exit 0
regardless. Each task therefore `register`s its output and sets
`changed_when` by inspecting `kubectl`'s own **stdout text** (e.g.
`"unchanged" not in apply_result.stdout`) to manually recover idempotency
reporting -- this is the `command`/`shell`-module idempotency gap called
out in the Ansible lab's FAQ, unlike purpose-built modules such as `apt` or
`service` which detect changes natively. `{{ playbook_dir }}/../k8s/`
resolves relative to the playbook file itself, so it works regardless of
the caller's working directory (important since the Jenkinsfile invokes it
via `wsl` from a different cwd than a human running it manually). The four
tasks in order: (1) `kubectl apply -f k8s/` creates-or-updates the
Deployment/Service; (2) `kubectl patch` forces `imagePullPolicy:
IfNotPresent` so the cluster pulls the registry image rather than assuming
a locally-built one is cached; (3) `kubectl set image` is the actual
deployment trigger -- the same primitive the courseware's Kubernetes-
integration Jenkinsfile (Part 5 Section 8) calls directly, wrapped in
Ansible here instead; (4) `kubectl rollout status --timeout=90s` blocks
until the rollout finishes, with `changed_when: false` marking it a pure
observability/wait step (same pattern as the courseware's `command: node
--version` task). The final `ansible.builtin.debug` task prints the
captured rollout output to the Jenkins console log.

### `app/Dockerfile`

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --production
COPY server.js ./
EXPOSE 3000
CMD ["node", "server.js"]
```
Same layer-caching discipline as Part 2 Section 5 (manifests copied first,
then `npm install --production`, skipping `devDependencies` like
`jest`/`supertest`), and only `server.js` is copied in -- test files never
enter the image. Plain `CMD`, no `ENTRYPOINT`, since this simple demo has
no need to lock the startup command against override.

### `app/server.js`

```javascript
import express from 'express';
import os from 'os';
import { fileURLToPath } from 'url';

const app = express();
const PORT = process.env.PORT || 3000;
const MESSAGE = 'Hello from the CI/CD Demo!';

app.get('/', (req, res) => {
  res.json({
    message: MESSAGE,
    version: process.env.APP_VERSION || '1.0.0',
    hostname: os.hostname()
  });
});

app.get('/health', (req, res) => {
  res.json({ status: 'UP' });
});

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
}

export default app;
```
`PORT` reads from the environment first, falling back to `3000` -- matches
`k8s/deployment.yaml`'s `containerPort: 3000`. The root route returns JSON
including `os.hostname()`, which inside a Pod resolves to the **Pod's
name** -- a visual way to prove load-balancing across a Deployment's
replicas (different `curl`s may return different hostnames). `/health` is
the **exact URL** both the Deployment's `readinessProbe` and the
Jenkinsfile's `Verify` stage check. The "only listen if this file is the
entry point" guard (comparing `process.argv[1]` to this module's own
resolved file URL) is what lets `app/test/basic.test.js` `import app from
'../server.js'` and drive it with `supertest` **without** triggering a real
`app.listen()` -- a clean pattern for making an Express app both a real
server and a directly-testable module; `export default app` is what makes
that guard meaningful.

### `app/test/basic.test.js`

```javascript
import request from 'supertest';
import app from '../server.js';

describe('Simple CI/CD Demo App', () => {
  it('GET / returns a welcome message', async () => {
    const res = await request(app).get('/');
    expect(res.body.message).toBeDefined();
  });
  it('GET / returns status 200', async () => { /* ... */ });
  it('GET / does not return status 404', async () => { /* ... */ });
  it('GET /health returns UP status', async () => { /* ... */ });
  it('GET /health returns status 200', async () => { /* ... */ });
  it('GET /health does not return status 404', async () => { /* ... */ });
});
```
`supertest` drives HTTP assertions directly against the exported Express
`app` object **in-process**, with no real socket/port bound. Each route
(`/` and `/health`) gets three checks: expected body/status, status 200,
and explicitly *not* 404 -- proving the route actually matched rather than
falling through to Express's default 404 handler. This is what the
Jenkinsfile's `Install & Test` stage runs via `npm test` (Part 5 Section
9's "fail fast" principle, before any Docker build).

### `k8s/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: acme-ansible-demo
  labels:
    app: acme-ansible-demo
spec:
  replicas: 2
  selector:
    matchLabels:
      app: acme-ansible-demo
  template:
    metadata:
      labels:
        app: acme-ansible-demo
    spec:
      containers:
        - name: acme-ansible-demo
          image: vamandeshmukh/acme-ansible-demo:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 3000
          env:
            - name: APP_VERSION
              value: "1.0.0"
          readinessProbe:
            httpGet:
              path: /health
              port: 3000
            initialDelaySeconds: 3
            periodSeconds: 5
```
Same selector/template label-matching pattern as every Deployment (Part
7.3). The image tag is hardcoded to `:latest` in the checked-in manifest
but is **overridden live** by the playbook's task 3 (`kubectl set image
... {{ image_tag }}`), passed the Jenkins build number -- so this file on
disk is just the "first apply" bootstrap state; the tag actually running is
whatever the pipeline last set. `imagePullPolicy: IfNotPresent` is checked
into the manifest directly here (unlike `acme-ems-docker`, where the
equivalent is a live `kubectl patch` by the playbook). `readinessProbe`
hits `/health` on port 3000, waiting 3s before the first check
(`initialDelaySeconds`) then re-checking every 5s (`periodSeconds`) -- a
Pod failing this probe is taken out of Service rotation without being
killed (that's what `livenessProbe` would do, which this manifest doesn't
define).

### `k8s/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: acme-ansible-demo-svc
spec:
  type: NodePort
  selector:
    app: acme-ansible-demo
  ports:
    - port: 3000
      targetPort: 3000
      nodePort: 30081
```
`NodePort` is correct for reaching this Service from outside the cluster on
Docker Desktop's local Kubernetes without a cloud load balancer.
`nodePort: 30081` is a fixed, explicit port in the 30000-32767 valid range
rather than a random assignment, letting the Jenkinsfile's `Verify` stage
curl a known, stable URL every build.

### `Jenkinsfile`

```groovy
// Ansible-driven CI/CD demo pipeline
// Flow: Checkout -> Test -> Docker Build -> Docker Push (DockerHub) -> Deploy (Ansible -> Kubernetes) -> Verify

pipeline {
    agent any

    environment {
        IMAGE_NAME = "vamandeshmukh/acme-ansible-demo"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/dyesmuk/acme-ansible-demo-4-jun-2026.git'
            }
        }
        stage('Install & Test') {
            steps {
                dir('app') {
                    bat 'npm install'
                    bat 'npm test'
                }
            }
        }
        stage('Docker Build') {
            steps {
                dir('app') {
                    bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                }
            }
        }
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_CREDENTIALS', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW')]) {
                    bat 'powershell -Command "$env:DOCKERHUB_CREDENTIALS_PSW | docker login -u $env:DOCKERHUB_CREDENTIALS_USR --password-stdin"'
                    bat "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    bat "docker push ${IMAGE_NAME}:latest"
                }
            }
        }
        stage('Deploy to Kubernetes (Ansible)') {
            steps {
                dir('ansible') {
                    bat "wsl ansible-playbook -i inventory.ini deploy-playbook.yml --extra-vars \"image_tag=${IMAGE_TAG}\""
                }
            }
        }
        stage('Verify') {
            steps {
                bat "kubectl get pods -l app=acme-ansible-demo"
                bat "curl -s http://localhost:30081/health || true"
            }
        }
    }

    post {
        success { echo "Pipeline succeeded -- ${IMAGE_NAME}:${IMAGE_TAG} is live on Kubernetes" }
        failure { echo "Pipeline failed -- check the stage logs above" }
    }
}
```
The file's header comments (omitted above) are effectively its own
runbook: the exact Jenkins credential ID expected
(`DOCKERHUB_CREDENTIALS`), the manual `kubectl apply -f k8s/` bootstrap
that must happen once before the pipeline can `set image` on a Deployment
that doesn't exist yet, and -- unique to this project -- the requirement
that **Ansible runs inside WSL2**, since Ansible has no native Windows
control node, while `kubectl` still runs natively on Windows and shares
Docker Desktop's `docker-desktop` context with WSL. `agent any` means
Jenkins runs **natively** on the training machine (not containerized
itself), per the courseware's design note that this lab's CI/CD stage runs
Jenkins natively "to avoid cross-platform config headaches." `IMAGE_TAG =
"${env.BUILD_NUMBER}"` gives every build a unique, traceable tag (Part 5
Section 8's pattern). `dir('app')` scopes the working directory so
relative paths resolve; `bat` (not `sh`) is used throughout since this is a
**native Windows** Jenkins agent -- the one exception is the Ansible stage,
which shells out **into** WSL via `bat "wsl ansible-playbook ..."` since
Ansible has no Windows-native binary. `withCredentials` binds the stored
credential to `_USR`/`_PSW` env vars (Jenkins' `usernamePassword` binding
convention), piping the password into `docker login --password-stdin` via
PowerShell so it's never echoed to a plain log line -- though this
Jenkinsfile never calls `docker logout` explicitly, unlike the courseware's
Part 5 Section 8 example, a minor hygiene gap worth noting. The Deploy
stage is what differentiates this project from the plain `Code/Jenkins`
one below: instead of calling `kubectl` directly, it calls the Ansible
playbook, passing the build's own `IMAGE_TAG` straight into the playbook's
`image_tag` variable via `--extra-vars` (Part 5 Section 7's pattern). The
`Verify` stage uses `|| true` on its curl, a soft check rather than a hard
gate (unlike the courseware's Part 5 Section 8 smoke test, which uses
`curl -f ... || exit 1` to hard-fail); and the `post` block has no
automatic rollback on failure (unlike the fuller courseware pipeline's
`post.failure { kubectl rollout undo }`), only echoing success/failure
messages.

## 7.5 Jenkins — `Code/Jenkins/` (straight kubectl deploy, no Ansible)

Same shape as the Ansible project's app (an Express server), same
Dockerfile pattern (`app/Dockerfile`, `package.json`, `test/basic.test.js`
are byte-for-byte the same pattern), but here the Jenkinsfile talks to
Kubernetes **directly via `kubectl`** rather than through an Ansible
playbook -- the cleanest side-by-side comparison of "CI/CD deploying via
raw `kubectl`" vs. "CI/CD deploying via an Ansible wrapper around
`kubectl`." Same as before, except:

### `app/server.js` (near-identical to the Ansible app, one line different)

Near-identical to the Ansible app's `server.js`, with one real difference:
its `/` handler adds `console.log(req)`, logging the **entire raw request
object** on every hit. This is debug-grade logging left in (dumping a huge
circular Node.js request object to stdout on every request is noisy and
not something you'd want in production) -- worth flagging as a "what would
you fix in code review" item.

### `k8s/deployment.yaml` and `k8s/service.yaml`

`deployment.yaml` is structurally identical to the Ansible project's (same
replicas, probe timing, env var; resource named `acme-cicd-demo`), with
one deliberate omission: **no `imagePullPolicy` field at all**.
Kubernetes' default `imagePullPolicy` is `IfNotPresent` for any tag other
than `:latest`, but `Always` when the tag **is** `:latest` (which this
manifest uses) -- meaning the kubelet will, by default, always attempt to
re-pull the image on every Pod (re)start. The Ansible project handles this
explicitly (a live `kubectl patch` forcing `IfNotPresent`); this
plain-Jenkins project instead relies entirely on the Jenkinsfile's own
`kubectl set image` (below) pointing at a build-numbered tag rather than
`:latest` at deploy time. `service.yaml` is the same `NodePort` pattern as
the Ansible project, on a **different** fixed port (`30080` here vs.
`30081` there) -- the two demo projects were clearly designed to run
side-by-side on the same Docker Desktop cluster without port collisions.

### `Jenkinsfile`

```groovy
// Simple Node.js CI/CD demo pipeline
// Flow: Checkout -> Test -> Docker Build -> Docker Push (DockerHub) -> Deploy (Kubernetes/Minikube) -> Verify

pipeline {
    agent any

    environment {
        IMAGE_NAME = "vamandeshmukh/acme-cicd-demo"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    stages {
        // Checkout, Install & Test, Docker Build, Docker Push:
        // identical pattern to the Ansible project's Jenkinsfile above.

        stage('Deploy to Kubernetes') {
            steps {
                bat "kubectl patch deployment acme-cicd-demo -p \"{\\\"spec\\\":{\\\"template\\\":{\\\"spec\\\":{\\\"containers\\\":[{\\\"name\\\":\\\"acme-cicd-demo\\\",\\\"imagePullPolicy\\\":\\\"IfNotPresent\\\"}]}}}}\""
                bat "kubectl set image deployment/acme-cicd-demo acme-cicd-demo=${IMAGE_NAME}:${IMAGE_TAG} --record"
                bat "kubectl rollout status deployment/acme-cicd-demo --timeout=90s"
            }
        }
        stage('Verify') {
            steps {
                bat "kubectl get pods -l app=acme-cicd-demo"
                bat "curl -s http://localhost:30080/health || true"
            }
        }
    }

    post {
        success { echo "Pipeline succeeded -- ${IMAGE_NAME}:${IMAGE_TAG} is live on Kubernetes" }
        failure { echo "Pipeline failed -- check the stage logs above" }
    }
}
```
Through the Docker Push stage this is identical (even in prose) to the
Ansible project's Jenkinsfile -- same `dir('app')` scoping, same
`withCredentials` login pattern, same double-tag build -- confirming these
two demo repos are deliberately parallel implementations of the same
pipeline shape, differing only at the deploy stage. Here, the
`imagePullPolicy: IfNotPresent` patch that the Ansible project's playbook
does as a **registered, idempotency-checked task** is instead a **raw
inline `kubectl patch`** with manually hand-escaped JSON (three layers of
escaping: Groovy string + Windows `bat` shell + JSON) -- a good
illustration of exactly the fragile-but-functional shell escaping that
using Ansible (or a Kubernetes-native Jenkins plugin like
`withKubeConfig`, per Part 5 Section 8) is meant to abstract away.
`kubectl set image ... --record` (the `--record` flag, deprecated in
modern `kubectl` but still present here) annotates the rollout with the
command that caused it, visible later in `kubectl rollout history`. The
`Verify` and `post` stages are the same shape as the Ansible project, just
on port `30080` instead of `30081`, with no automatic rollback on failure
in either project.

### `demo-script.md` and `README.md`

Both are instructor-facing walkthroughs for live-demoing this exact
pipeline in a training session -- they don't introduce new technical
content beyond what's captured in the Jenkinsfile and manifests above.


---


# PART 8 — SYNTHESIS: ONE PUSH, ONE PIPELINE, ONE DEPLOYED APP

Every piece explained above is a segment of the same end-to-end flow. Using
the `Code/Jenkins` project as the concrete trace (it's the simpler of the
two full pipelines — no Ansible hop):

1. **`git push origin main`** — a developer commits a change to
   `app/server.js` or a manifest under `k8s/`, following the branching and
   commit-message discipline from Part 1, and pushes to the GitHub repo
   the Jenkinsfile's `Checkout` stage points at
   (`https://github.com/dyesmuk/acme-cicd-demo-4-jun-2026.git`).
2. **Trigger** — in a fully wired setup (Part 5 §4), a GitHub webhook hits
   `http://<jenkins-server>:8080/github-webhook/` and Jenkins starts a new
   build automatically; in this local/native setup, that trigger is
   typically manual ("Build Now") since nothing here is on a shared
   network (per `06-ems-devops-series-overview.md`'s design note that this
   lab intentionally collapsed to individual/local rather than a shared
   Jenkins instance).
3. **Checkout** — Jenkins clones the repo into its workspace
   (`git branch: 'main', url: '...'`).
4. **Install & Test** — `dir('app') { bat 'npm install'; bat 'npm test' }`
   runs the Jest/Supertest suite from `app/test/basic.test.js` against the
   in-process Express app (via the `server.js` "only listen if main
   module" guard) — this is the fail-fast gate: if `/` or `/health` broke,
   the pipeline stops here, before anything gets built or pushed (Part 5
   §9's "fail fast" principle).
5. **Docker Build** — `docker build -t <image>:${BUILD_NUMBER} -t
   <image>:latest .` builds from `app/Dockerfile`'s cached layer sequence
   (as before: manifests copied first, `npm install --production`, then
   the source), so unchanged dependencies skip re-installation.
6. **Docker Push** — `withCredentials` unlocks the `DOCKERHUB_CREDENTIALS`
   Jenkins secret, `docker login --password-stdin` authenticates, both
   tags get pushed to Docker Hub — this is the registry hand-off point:
   from here on, any machine with `docker pull` access and no access to
   the source code at all can run the exact same image (Part 2 §7,
   Exercise 8's "build once, run anywhere" checkpoint).
7. **Deploy** — either straight `kubectl patch` +
   `kubectl set image deployment/... = <image>:${BUILD_NUMBER}` +
   `kubectl rollout status` (the plain `Code/Jenkins` project), **or**
   the same three operations wrapped inside an idempotent, `changed_when`-
   aware Ansible playbook invoked via `wsl ansible-playbook ... --extra-vars
   "image_tag=${IMAGE_TAG}"` (the `Code/Ansible` project) — Kubernetes'
   Deployment controller then performs a rolling update: new Pods with the
   new image tag start, `readinessProbe` on `/health` (port 3000, checked
   every 5s after a 3s initial delay) gates when they start receiving
   traffic, and old Pods are only terminated once replacements are ready.
8. **Verify** — the pipeline curls the Service's fixed `NodePort`
   (`30080` or `30081`) at `/health` and lists Pods by label, closing the
   loop: code that was just pushed is now provably running and healthy.

**Where Ansible fits versus a direct `kubectl` call:** as both real
Jenkinsfiles above show, Ansible is not strictly required to get from image
to running Pods — `Code/Jenkins` proves a Jenkinsfile can do it with three
raw `kubectl` commands. What Ansible buys you, demonstrated concretely in
`deploy-playbook.yml`, is (a) **idempotency you don't have to hand-roll**
per pipeline (the `changed_when` pattern makes "did this actually change"
explicit and reusable instead of embedded ad hoc in shell/Groovy string
escaping like `Code/Jenkins`'s triple-escaped JSON patch), (b) a single
YAML description of "ensure this Deployment/Service/image state" that's
portable to a real fleet of VMs (the courseware's `04-ansible.md` roles/
inventory/vars machinery) rather than one-off `kubectl` calls baked into a
single Jenkinsfile, and (c) a natural seam for provisioning work
(installing Docker Engine, creating a deploy user — the Ansible hands-on
lab's Exercise 8) that has to happen *before* any pipeline can hand a
server a container at all.

**Homelab mapping (Aakash-specific):** every piece above has a direct
self-hosted equivalent worth running to get real practice reps beyond the
course's Docker-Desktop-local scope:

- **Jenkins**: instead of the native-install-to-avoid-cross-platform-
  headaches approach the course took, a homelab Jenkins is a natural fit
  for the `jenkins/jenkins:lts-jdk17` Docker container pattern from Part 5
  §2 — run it as a persistent container (or a Compose service) on a
  homelab box, with `-v /var/run/docker.sock:/var/run/docker.sock` so it
  can build images on the host's Docker daemon, and a GitHub webhook
  pointed at the homelab's public/tunnelled address instead of relying on
  manual "Build Now" clicks — this is exactly the gap the course's
  individual-not-shared Jenkins setup left open.
- **Kubernetes**: Docker Desktop's built-in single-node cluster (what both
  Jenkinsfiles' `kubectl` commands target) maps directly to **k3s** or
  **microk8s** on a homelab node — same `apiVersion`/`kind`/`spec` shapes
  from every manifest in Part 7.3–7.5 apply unchanged; only the `kubectl
  config use-context` target changes.
- **Docker Hub → self-hosted registry**: the `docker push
  <username>/<image>:<tag>` pattern from both Jenkinsfiles is a drop-in
  replacement for a homelab registry (`docker run -d -p 5000:5000
  registry:2`, per Part 2 §7), which avoids the Docker Hub rate limits and
  keeps images private without a paid plan.
- **Ansible control node**: the course's WSL2-hosted Ansible control node
  (needed only because the Jenkins agent itself is native Windows) has no
  such constraint on a Linux homelab box — Ansible would run natively
  alongside Jenkins, simplifying the `bat "wsl ansible-playbook ..."`
  indirection in `Code/Ansible/Jenkinsfile` line 66 down to a plain `sh
  "ansible-playbook ..."` call.

