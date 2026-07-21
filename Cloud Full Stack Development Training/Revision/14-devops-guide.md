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
1:  # Base Image
2:  FROM node:22-alpine
3:  
4:  # Create application directory
5:  WORKDIR /app
6:  
7:  # Copy project files
8:  COPY . .
9:  
10: # Application listens on port 3000
11: EXPOSE 3000
12: 
13: # Start application
14: CMD ["npm", "start"]
```

- **Line 2** — base image `node:22-alpine`: Alpine variant for a small
  footprint, Node 22 (newer than the course examples' `node:20-alpine`).
- **Line 5** — `WORKDIR /app` creates and switches into `/app` inside the
  image; every subsequent instruction runs relative to it.
- **Line 8** — `COPY . .` copies the **entire build context** (everything
  next to the Dockerfile) in one shot. This is the single-stage, no-cache-
  optimization version the course explicitly calls "inefficient" — there's
  no separate `COPY package*.json ./` + `RUN npm ci` step first, so any
  source change busts every layer above it. Acceptable here because this is
  a two-file demo app with a trivial dependency graph.
- **Line 11** — `EXPOSE 3000` documents the port; it does not publish it —
  publishing happens with `docker run -p`.
- **Line 14** — `CMD ["npm", "start"]` (exec form) is the default startup
  command, delegating to the `start` script in `package.json`.

### `docker-demo/app.js`

```javascript
1:  import { createServer } from "http";
2:  
3:  const PORT = 3000;
4:  
5:  const server = createServer((req, res) => {
6:      res.writeHead(200, {
7:          "Content-Type": "text/plain"
8:      });
9:  
10:     res.end("Hello from Docker!\n");
11: });
12: 
13: server.listen(PORT, () => {
14:     console.log(`Server running on port ${PORT}`);
15: });
```

- **Line 1** — ES module import of Node's built-in `http` module (no
  Express dependency at all — this is the leanest possible demo server).
- **Lines 5–11** — `createServer` takes a request handler; every request
  gets a `200` plain-text response reading `"Hello from Docker!\n"`,
  regardless of method or path — proves the container is reachable, nothing
  more.
- **Line 13** — binds and listens on port 3000, matching the Dockerfile's
  `EXPOSE 3000` and the app's own `PORT` constant.

### `docker-demo/package.json`

```json
1:  {
2:    "name": "docker-demo",
3:    "version": "1.0.0",
4:    "description": "Docker Demo",
5:    "main": "app.js",
6:    "scripts": {
7:      "start": "node app.js"
8:    }
9:  }
```
- **Line 7** — `"start": "node app.js"` is what `CMD ["npm", "start"]` in
  the Dockerfile ultimately invokes. No `dependencies` block at all — this
  app has zero npm packages to install, which is also why the Dockerfile
  skips the layer-caching `COPY package*.json ./` step entirely; there's
  nothing to cache.


## 7.3 Docker — `acme-ems-docker/` (the full EMS deployment)

This is the completed Docker lab: a Spring Boot 3 / Java 17 / Maven app
(same four-entity EMS structure as the Git lab) fronted by Postgres and
Adminer, built with a real multi-stage Dockerfile.

### `docker/Dockerfile` vs `docker/Dockerfile.starter` — what changed

```dockerfile
1:  # ---------- STAGE 1: build ----------
2:  
3:  FROM maven:3.9.8-eclipse-temurin-17 AS build
4:  
5:  WORKDIR /app
6:  
7:  COPY pom.xml .
8:  RUN mvn dependency:go-offline -B
9:  
10: COPY src ./src
11: 
12: RUN mvn clean package -DskipTests
13: 
14: # ---------- STAGE 2: run ----------
15: 
16: FROM eclipse-temurin:17-jre-alpine
17: 
18: WORKDIR /app
19: 
20: COPY --from=build /app/target/*.jar app.jar
21: 
22: EXPOSE 8080
23: 
24: ENTRYPOINT ["java","-jar","app.jar"]
```

- **Line 3** — build stage named `build`, based on `maven:3.9.8-eclipse-
  temurin-17` (the finished file pins `3.9.8`; the `.starter` template only
  specified the floating `maven:3.9-eclipse-temurin-17` — a real, if minor,
  version-pinning improvement made while filling in the lab).
- **Line 7** — `COPY pom.xml .` copies **only** the Maven manifest first.
  This is the layer-cache TODO from the starter: as long as `pom.xml`
  doesn't change, line 8's dependency download layer stays cached even when
  application source changes.
- **Line 8** — `mvn dependency:go-offline -B` pre-downloads all
  dependencies into the local `.m2` cache inside this layer, in batch mode
  (`-B`, no interactive prompts) — this is what makes line 7's caching
  trick actually pay off.
- **Line 10** — `COPY src ./src` copies the source **after** dependencies
  are cached — this is the starter's second TODO, resolved correctly (only
  `src/`, not the whole project, and not before line 8).
- **Line 12** — `mvn clean package -DskipTests` builds the jar, skipping
  tests to keep the image build fast (`-D????Tests` in the starter resolves
  to `-DskipTests`).
- **Line 16** — runtime stage `FROM eclipse-temurin:17-jre-alpine` — a
  **JRE-only** (no compiler, no Maven) Alpine base, resolving the starter's
  `FROM ???` blank exactly per its hint. This is what keeps the final image
  small — no build toolchain shipped to production.
- **Line 20** — `COPY --from=build /app/target/*.jar app.jar` pulls only
  the compiled jar out of the `build` stage by name — none of the source,
  `pom.xml`, or `.m2` cache crosses into the final image.
- **Line 22** — `EXPOSE 8080` — Spring Boot's default port, resolving the
  starter's `EXPOSE ????`.
- **Line 24** — `ENTRYPOINT ["java","-jar","app.jar"]` (exec form, not
  shell form) — fixed startup command that can't be silently overridden by
  stray `docker run` arguments, appropriate for a "this container only ever
  does one thing" production image.

### `docker/docker-compose.yml`

```yaml
1:  version: "3.9"
2:  
3:  services:
4:    # ---------- APP ----------
5:  
6:    app:
7:      build:
8:        context: ..
9:        dockerfile: docker/Dockerfile
10: 
11:     container_name: ems-app
12: 
13:     ports:
14:       - "8080:8080"
15: 
16:     depends_on:
17:       - db
18: 
19:     environment:
20:       SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/emsdb
21:       SPRING_DATASOURCE_USERNAME: postgres
22:       SPRING_DATASOURCE_PASSWORD: postgres
23: 
24:   # ---------- DATABASE ----------
25: 
26:   db:
27:     image: postgres:16-alpine
28: 
29:     container_name: ems-db
30: 
31:     restart: always
32: 
33:     environment:
34:       POSTGRES_DB: emsdb
35:       POSTGRES_USER: postgres
36:       POSTGRES_PASSWORD: postgres
37: 
38:     ports:
39:       - "5432:5432"
40: 
41:     volumes:
42:       - postgres-data:/var/lib/postgresql/data
43:       - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql
44: 
45:   # ---------- adminer ----------
46: 
47:   adminer:
48:     image: adminer
49: 
50:     container_name: ems-adminer
51: 
52:     ports:
53:       - "8081:8080"
54: 
55:     depends_on:
56:       - db
57: 
58: volumes:
59:   postgres-data:
```

- **Line 8** — `context: ..`: the build context is the **repo root**, one
  level above `docker/` (this compose file lives inside `docker/`), because
  the Dockerfile needs `pom.xml` and `src/` from the project root, not from
  inside the `docker/` folder.
- **Line 9** — `dockerfile: docker/Dockerfile` — path to the Dockerfile
  *relative to that context* (root), i.e. `<root>/docker/Dockerfile`.
- **Lines 13–14** — `"8080:8080"` maps host port 8080 to the container's
  Spring Boot port (matches the Dockerfile's `EXPOSE 8080`).
- **Line 16–17** — `depends_on: [db]` controls **start order only** (starts
  `db` before `app`) — it does not wait for Postgres to be ready to accept
  connections, which is a known Compose limitation the course doesn't
  paper over.
- **Lines 20–22** — Spring Boot's `SPRING_DATASOURCE_*` env vars are set to
  point at the **service name** `db` (Docker's internal DNS resolves
  `db` to the Postgres container's IP on the default network Compose
  creates), not `localhost` and not a hardcoded IP.
- **Line 27** — `postgres:16-alpine` — official lightweight Postgres image.
- **Line 31** — `restart: always` — always restart the DB container, even
  after a Docker daemon restart (this is the strongest of the four restart
  policies from Part 2 §4).
- **Lines 34–36** — DB name/user/password match what `app` expects in its
  JDBC URL and datasource credentials on lines 20–22 exactly — this
  coupling is why moving them into a `.env` file (as `docker/db/.env.
  example` shows) is the safer real-world pattern.
- **Line 39** — `"5432:5432"` exposes Postgres to the host too — useful for
  connecting a local DB client directly, optional in production.
- **Line 42** — `postgres-data:/var/lib/postgresql/data` — a **named
  volume** mounted at Postgres's actual data directory, so the database
  survives `docker compose down` (without `-v`) and container recreation.
- **Line 43** — a **bind mount** of the local `init.sql` file into
  Postgres's official init-script directory
  (`/docker-entrypoint-initdb.d/`) — the Postgres image auto-runs any
  `.sql`/`.sh` file found there **on first startup only** (when the data
  directory is empty).
- **Lines 47–56** — `adminer` is a single-file DB-browser UI; `8081:8080`
  maps the host's 8081 to Adminer's internal port 8080 (distinct from the
  app's own 8080), and it also depends on `db` being started first.
- **Line 58–59** — the top-level `volumes:` block declares `postgres-data`
  as a Docker-managed named volume (referenced by line 42).

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
1:  events {}
2:  
3:  http {
4:      server {
5:          listen 80;
6:  
7:          location / {
8:              # TODO (Lab): set proxy_pass to the app service.
9:              # Hint: proxy_pass http://????:8080;
10: 
11:             proxy_set_header Host $host;
12:             proxy_set_header X-Real-IP $remote_addr;
13:         }
14:     }
15: }
```
- **Line 1** — `events {}` — required top-level Nginx block, empty because
  no custom connection-handling tuning is needed for a lab.
- **Line 5** — the proxy listens on port 80.
- **Line 9** — the still-unresolved TODO: `proxy_pass http://????:8080;`
  should become `proxy_pass http://app:8080;`, using the Compose service
  name `app` — exactly the "containers reach each other by service name,
  not IP" principle from Part 2 §8, called out explicitly in the file's own
  header comment. As noted above, this file was never actually renamed to
  `nginx.conf` or wired into `docker-compose.yml`.
- **Lines 11–12** — forwards the original `Host` header and the client's
  real IP through to the upstream app, standard reverse-proxy hygiene so
  the backend doesn't just see `127.0.0.1` for every request.

### `docker/db/init.sql`

```sql
1:  -- ============================================================
2:  -- Optional seed data
3:  -- ============================================================
4:  -- Hibernate creates the tables automatically on first app startup
5:  -- (see application.properties: spring.jpa.hibernate.ddl-auto=update),
6:  -- so this script is OPTIONAL. Its only job is to give you a couple
7:  -- of rows to look at in Adminer before the app has written any data.
8:  --
9:  -- Table names come from the @Table annotation on each entity:
10: --   employees, departments, projects, jobs
11: --
12: -- TODO (Lab — optional): uncomment and adjust once the app has run
13: -- once and you can see the real column names in Adminer.
14: --
15: -- INSERT INTO departments (name, location, head_count)
16: -- VALUES ('Engineering', 'Bengaluru', 42);
```
- **Lines 4–5** — explains why this file is a no-op by default: Hibernate's
  `ddl-auto=update` already creates the schema at app startup, so this
  script exists purely as an optional seeding hook, not a schema
  definition.
- **Line 10** — table names are driven by the JPA `@Table` annotations on
  the four entities (`Employee`, `Department`, `Project`, `Job`), which is
  why the seed insert on line 15 targets `departments`.
- **Lines 15–16** — the actual seed `INSERT` is left commented out as a
  TODO — it was never uncommented in this project, so the DB starts empty
  aside from whatever Hibernate/the app itself writes.

### `docker/db/.env.example`

```ini
1:  # Copy this file to docker/db/.env and adjust as needed.
2:  # Never commit the real .env file — it's already in .gitignore.
3:  
4:  POSTGRES_DB=emsdb
5:  POSTGRES_USER=ems_user
6:  POSTGRES_PASSWORD=change_me
```
- **Line 5** — note this template's suggested user is `ems_user`, but the
  finished `docker-compose.yml` above actually hardcodes `POSTGRES_USER:
  postgres` directly in the compose file rather than sourcing it from a
  real `.env` — another sign the `.env`-file indirection from the starter
  kit wasn't carried through to the final, checked-in compose file.


### `k8s/pod-solo.yaml` (bare Pod primitive, Exercise 2)

```yaml
1:  
2:  # ============================================================
3:  # Your first Pod — STARTER (Exercise 2)
4:  # ============================================================
5:  # This is the raw, bare-metal Kubernetes primitive. You will NOT
6:  # normally deploy Pods directly like this in real work — Exercise 3
7:  # moves you to a Deployment, which manages Pods for you. But you
8:  # should see the bare primitive once before something else manages
9:  # it on your behalf.
10: #
11: # Apply with: kubectl apply -f k8s/pod-solo.yaml
12: # ============================================================
13: 
14: apiVersion: v1
15: kind: Pod
16: metadata:
17:   name: acme-ems-api-solo
18:   labels:
19:     app: acme-ems-api-solo
20: spec:
21:   containers:
22:     - name: acme-ems-api
23:       # TODO (Lab): use YOUR OWN image from the Docker lab.
24:       # Format: <your-dockerhub-username>/acme-ems-api:1.0
25:       image: vamandeshmukh/acme-ems-api:1.0
26: 
27:       # TODO (Lab): what port does Spring Boot listen on inside the container?
28:       ports:
29:         - containerPort: 8080
```
- **Line 14–15** — `apiVersion: v1`, `kind: Pod` — Pod is a core (`v1`)
  resource, not part of the `apps/` API group like Deployments.
- **Line 19** — `labels.app: acme-ems-api-solo` — this label is what a
  Service's `selector` would match against, though this bare Pod isn't
  fronted by one.
- **Line 25** — the image reference was filled in with the trainee's own
  pushed Docker Hub image (`vamandeshmukh/acme-ems-api:1.0`) from the Docker
  lab — the direct handoff artifact between the two labs.
- **Line 29** — `containerPort: 8080` matches the Spring Boot app's actual
  listen port and the Dockerfile's `EXPOSE 8080`.

### `k8s/app-deployment.yaml` (Exercise 3 — self-healing demo)

```yaml
1:  # ============================================================
2:  # App Deployment — STARTER (Exercise 3)
3:  # ============================================================
4:  # A Deployment manages a ReplicaSet for you, which in turn manages
5:  # Pods. You describe the DESIRED state (how many replicas, which
6:  # image); Kubernetes continuously works to keep reality matching
7:  # that description — that's what makes the self-healing demo in
8:  # Exercise 3 work.
9:  #
10: # Apply with: kubectl apply -f k8s/app-deployment.yaml
11: # ============================================================
12: 
13: apiVersion: apps/v1
14: kind: Deployment
15: metadata:
16:   name: ems-app
17:   labels:
18:     app: ems-app
19: spec:
20:   # TODO (Lab): how many Pod copies do you want running at once?
21:   # Try 2 — you'll watch Kubernetes recreate one if you delete it.
22:   replicas: 4
23: 
24:   selector:
25:     matchLabels:
26:       app: ems-app
27: 
28:   template:
29:     metadata:
30:       labels:
31:         app: ems-app
32:     spec:
33:       containers:
34:         - name: ems-app
35:           # TODO (Lab): same image you used in pod-solo.yaml.
36:           image: vamandeshmukh/acme-ems-api:1.0
37: 
38:           # TODO (Lab): same port as before.
39:           ports:
40:             - containerPort: 8080
```
- **Line 13** — `apps/v1` — Deployment lives in the `apps` API group
  (unlike the bare `v1` Pod above).
- **Line 22** — `replicas: 4` — the trainee chose 4 rather than the
  suggested 2; higher replica count means deleting any single Pod is even
  less visible to a client hitting the Service, since 3 others keep serving
  traffic while the ReplicaSet controller recreates the fourth.
- **Lines 24–26** — `spec.selector.matchLabels.app: ems-app` **must** match
  `spec.template.metadata.labels.app` on line 31 — this is the live wiring
  between the Deployment and the Pods it manages; if they diverge,
  `kubectl apply` fails validation.
- **Line 28** — `template` is the Pod spec the Deployment stamps out for
  each replica — identical in shape to the standalone `pod-solo.yaml`
  above, just nested one level deeper.
- **Line 36** — same image reference as `pod-solo.yaml`, confirming this
  Deployment supersedes rather than replaces the bare-Pod exercise.

### `k8s/app-service.yaml` (Exercise 4)

```yaml
1:  # ============================================================
2:  # App Service — STARTER (Exercise 4)
3:  # ============================================================
4:  # Pods are disposable and get new IPs every time they're recreated —
5:  # that's exactly what you saw in Exercise 3's self-healing demo. A
6:  # Service gives you one stable name/IP that automatically routes to
7:  # whichever Pods currently match its selector, no matter how many
8:  # times they get replaced.
9:  #
10: # Apply with: kubectl apply -f k8s/app-service.yaml
11: # Access with: minikube service ems-app --url
12: # ============================================================
13: 
14: apiVersion: v1
15: kind: Service
16: metadata:
17:   name: ems-app
18: spec:
19:   # TODO (Lab): this needs to be reachable from OUTSIDE the cluster
20:   # (your laptop's browser/curl). Which Service type does that?
21:   # (ClusterIP is internal-only — not what you want here.)
22:   type: ????
23: 
24:   # This MUST match the labels on the Pods from app-deployment.yaml —
25:   # that's how the Service knows which Pods to send traffic to.
26:   selector:
27:     app: ems-app
28: 
29:   ports:
30:     - port: 8080
31:       # TODO (Lab): which port are the Pods actually listening on?
32:       # (Same value you used for containerPort earlier.)
33:       targetPort: ????
```
- **Line 22** — this file was left **unfinished**: `type: ????` was never
  resolved. Per the file's own hint and Part 3 §6, the correct value for
  "reachable from outside the cluster during local development" is
  `NodePort` (or `LoadBalancer` on a real cloud cluster) — `ClusterIP`
  (the default if `type` were simply omitted) would **not** work here.
- **Line 26–27** — `selector.app: ems-app` must match the Pod template
  label from `app-deployment.yaml` line 31 — this is the live routing link;
  get it wrong and the Service has zero matching endpoints.
- **Line 33** — also left unresolved (`targetPort: ????`); the correct
  value is `8080`, matching `containerPort` in the Deployment.
- **Net effect:** as checked into this project, `app-service.yaml` is
  **not directly applyable** — `kubectl apply -f` would fail YAML/schema
  validation on the literal `????` tokens. This is a real, notable gap
  between "what the lab exercise asks for" and "what's actually saved on
  disk" worth flagging on an assessment.

### `k8s/db-secret.yaml`, `db-pvc.yaml`, `db-deployment.yaml`, `db-service.yaml` (Exercise 5)

All four database manifests are likewise left as **unfinished starters**
with `????` placeholders still in place:

```yaml
# db-secret.yaml
15: apiVersion: v1
16: kind: Secret
17: metadata:
18:   name: ems-db-secret
19: type: Opaque
20: stringData:
21:   POSTGRES_DB: ????
22:   POSTGRES_USER: ????
23:   POSTGRES_PASSWORD: ????
```
- **Line 19** — `type: Opaque` is the generic Secret type for arbitrary
  key/value data (as opposed to `kubernetes.io/tls` or
  `kubernetes.io/dockerconfigjson`).
- **Line 20** — `stringData` (not `data`) is used deliberately — Kubernetes
  accepts **plain-text** values under `stringData` and base64-encodes them
  automatically on write, sparing the trainee from hand-encoding, unlike
  the `data:` field shown in the courseware (Part 3 §7) which requires
  pre-encoded base64 strings.
- **Lines 21–23** — never filled in; per the file's comment these should
  mirror `docker/db/.env` values (e.g. `emsdb` / `ems_user` / a real
  password).

```yaml
# db-pvc.yaml
12: apiVersion: v1
13: kind: PersistentVolumeClaim
14: metadata:
15:   name: ems-db-pvc
16: spec:
17:   accessModes:
18:     - ????
19: 
20:   resources:
21:     requests:
22:       storage: ????
```
- **Line 18** — should be `ReadWriteOnce` per the file's own hint: a single
  Postgres Pod only ever needs to mount its own volume from one node at a
  time.
- **Line 22** — should be a size like `1Gi` per the hint — left blank.

```yaml
# db-deployment.yaml
19: apiVersion: apps/v1
20: kind: Deployment
21: metadata:
22:   name: ems-db
23:   labels:
24:     app: ems-db
25: spec:
26:   replicas: 1
27:   selector:
28:     matchLabels:
29:       app: ems-db
30:   template:
31:     metadata:
32:       labels:
33:         app: ems-db
34:     spec:
35:       containers:
36:         - name: ems-db
37:           image: postgres:16-alpine
38:           ports:
39:             - containerPort: ????
40:           envFrom:
41:             - secretRef:
42:                 name: ????
43:           volumeMounts:
44:             - name: db-storage
45:               mountPath: ????
46:       volumes:
47:         - name: db-storage
48:           persistentVolumeClaim:
49:             claimName: ????
```
- **Line 26** — `replicas: 1` is **intentionally hardcoded**, not a
  TODO — the file's header comment explains why: a plain Postgres container
  has no built-in clustering, so 2+ replicas would mean two independent
  Postgres instances writing to the *same* PVC and corrupting data. Real
  multi-replica databases need a StatefulSet plus database-level
  replication, explicitly out of scope.
- **Line 39** — should be `5432` (Postgres's default port) — left blank.
- **Lines 40–42** — `envFrom.secretRef.name` should reference `ems-db-
  secret` from `db-secret.yaml` — this is how the three `POSTGRES_*`
  keys get injected as environment variables without duplicating their
  values in the Deployment spec. Left blank.
- **Line 45** — should be `/var/lib/postgresql/data` — the official
  Postgres image's data directory — left blank.
- **Line 49** — should reference `ems-db-pvc` from `db-pvc.yaml` — left
  blank.

```yaml
# db-service.yaml
14: apiVersion: v1
15: kind: Service
16: metadata:
17:   name: ems-db
18: spec:
19:   type: ClusterIP
20:   selector:
21:     app: ems-db
22:   ports:
23:     - port: 5432
24:       targetPort: ????
```
- **Line 19** — `type: ClusterIP` is the **one field already filled in
  correctly and not a TODO** here — the comment explains why: unlike
  `app-service.yaml`, the database must stay internal-only, never reachable
  from outside the cluster, so `ClusterIP` (the default, internal-only
  type) is deliberately hardcoded rather than left as a blank.
- **Line 21** — `selector.app: ems-db` routes to Pods from
  `db-deployment.yaml`'s template labels.
- **Line 24** — `targetPort: ????` should be `5432` to match the (also
  unfilled) `containerPort` in the Deployment — left blank.

**Takeaway for the exam:** the `acme-ems-docker/k8s/` folder as it sits on
disk is a **mixed state** — `pod-solo.yaml` and `app-deployment.yaml` are
fully completed and would `kubectl apply` cleanly; `app-service.yaml` and
all four `db-*.yaml` files still contain literal `????` placeholders and
would fail to apply as-is. Know both the *intended* correct values (given
above) and the fact that this particular checked-in copy is unfinished —
useful if asked to debug "why won't this manifest apply."


## 7.4 Ansible — `Code/Ansible/` (playbook-driven deploy with its own Jenkinsfile)

This project is a minimal Express app (`acme-ansible-demo`) whose deployment
is delegated to an Ansible playbook that itself shells out to `kubectl`
against a **local** Kubernetes cluster (Docker Desktop's built-in cluster)
— i.e., Ansible here is used as an orchestration/idempotency wrapper around
`kubectl`, not for provisioning VMs.

### `ansible/inventory.ini`

```ini
1:  [local]
2:  localhost ansible_connection=local
```
- **Line 1** — a single group, `[local]`.
- **Line 2** — the only host is `localhost`, with
  `ansible_connection=local`, which tells Ansible to run tasks as **local
  shell commands** rather than opening an SSH connection to itself — the
  correct choice since the "target" here is the same Windows/WSL machine
  Jenkins and `kubectl` already run on, not a remote server.

### `ansible/deploy-playbook.yml`

```yaml
1:  ---
2:  # Deploys acme-ansible-demo to Kubernetes.
3:  # Fully self-sufficient: creates the Deployment/Service on first run,
4:  # and updates them on every subsequent run. No manual `kubectl apply`
5:  # step needed -- push code, Jenkins + this playbook handle the rest.
6:  #
7:  # Run manually for practice with:
8:  #   ansible-playbook -i inventory.ini deploy-playbook.yml --extra-vars "image_tag=latest"
9:  
10: - name: Deploy acme-ansible-demo to Kubernetes
11:   hosts: local
12:   gather_facts: false
13: 
14:   vars:
15:     image_name: "vamandeshmukh/acme-ansible-demo"
16:     deployment_name: "acme-ansible-demo"
17:     container_name: "acme-ansible-demo"
18:     image_tag: "latest"
19: 
20:   tasks:
21: 
22:     - name: Ensure the Deployment and Service exist (creates on first run, no-op after)
23:       ansible.builtin.command:
24:         cmd: "kubectl apply -f {{ playbook_dir }}/../k8s/"
25:       register: apply_result
26:       changed_when: "'unchanged' not in apply_result.stdout"
27: 
28:     - name: Ensure Kubernetes uses the pushed image (registry pull allowed)
29:       ansible.builtin.command:
30:         cmd: >
31:           kubectl patch deployment {{ deployment_name }}
32:           -p "{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"{{ container_name }}\",\"imagePullPolicy\":\"IfNotPresent\"}]}}}}"
33:       register: patch_result
34:       changed_when: "'no change' not in patch_result.stdout"
35: 
36:     - name: Point the deployment at the new image tag
37:       ansible.builtin.command:
38:         cmd: "kubectl set image deployment/{{ deployment_name }} {{ container_name }}={{ image_name }}:{{ image_tag }}"
39:       register: set_image_result
40:       changed_when: "'image updated' in set_image_result.stdout"
41: 
42:     - name: Wait for the rollout to finish
43:       ansible.builtin.command:
44:         cmd: "kubectl rollout status deployment/{{ deployment_name }} --timeout=90s"
45:       register: rollout_result
46:       changed_when: false
47: 
48:     - name: Show rollout result
49:       ansible.builtin.debug:
50:         msg: "{{ rollout_result.stdout }}"
```
- **Line 11** — `hosts: local` targets the `[local]` group from
  `inventory.ini`.
- **Line 12** — `gather_facts: false` skips Ansible's normal Setup module
  fact-gathering step (OS info, network interfaces, etc.) — unnecessary
  overhead here since every task is just a `kubectl` invocation, not
  anything that depends on the target's OS facts.
- **Lines 14–18** — play-level `vars` parameterize the image name,
  Deployment/container name, and image tag; `image_tag` defaults to
  `"latest"` but is overridden by `--extra-vars "image_tag=..."` (per the
  header comment on line 8, and per the Jenkinsfile which passes the
  build-specific tag — see below).
- **Lines 22–26** — task 1 runs `kubectl apply -f <k8s-dir>/` to
  create-or-update the Deployment and Service from `k8s/deployment.yaml`
  and `k8s/service.yaml`. `{{ playbook_dir }}/../k8s/` resolves relative to
  where the playbook file lives, so it works regardless of the caller's
  current working directory — important since the Jenkinsfile calls it via
  `wsl` from a different working directory than a human running it
  manually. `register: apply_result` captures stdout for the next line;
  `changed_when: "'unchanged' not in apply_result.stdout"` is a **manual
  idempotency signal** — `kubectl apply` itself always exits 0 whether or
  not anything changed, so this task inspects the *text* of `kubectl`'s
  own output (which says `"... unchanged"` when nothing changed) to decide
  whether Ansible should report `changed` or `ok`. This is the
  `command`/`shell`-module idempotency gap called out in the Ansible lab's
  FAQ — a task using `command` must be told explicitly how to detect "did
  anything actually change," unlike purpose-built modules such as `apt` or
  `service`.
- **Lines 28–34** — task 2 patches `imagePullPolicy` to `IfNotPresent` via
  a raw `kubectl patch` JSON merge patch, ensuring the cluster is willing to
  pull a registry image rather than assume it already has a locally-built
  one cached. `changed_when` again parses `kubectl`'s own "no change"
  message from stdout.
- **Lines 36–40** — task 3, the actual deployment trigger: `kubectl set
  image deployment/<name> <container>=<image>:<tag>` — this is the same
  primitive the courseware's Kubernetes-integration Jenkinsfile
  (Part 5 §8) calls directly; here it's wrapped in Ansible instead.
  `changed_when` checks for `kubectl`'s `"image updated"` confirmation
  text.
- **Lines 42–46** — task 4 blocks until the rollout finishes or times out
  after 90 seconds (`kubectl rollout status ... --timeout=90s`), mirroring
  the courseware's `kubectl rollout status ... --timeout=120s` pattern.
  `changed_when: false` marks this task as never "changing" anything itself
  — it's purely an observability/wait step, the same pattern the
  courseware's `roles/nodejs/tasks/main.yaml` used for `command: node
  --version`.
- **Lines 48–50** — `ansible.builtin.debug` prints the captured rollout
  output for visibility in the Jenkins console log — same pattern as the
  courseware's `debug: msg: "Node.js version: ..."`.

### `app/Dockerfile`

```dockerfile
1:  FROM node:20-alpine
2:  
3:  WORKDIR /app
4:  
5:  COPY package*.json ./
6:  RUN npm install --production
7:  
8:  COPY server.js ./
9:  
10: EXPOSE 3000
11: CMD ["node", "server.js"]
```
- **Line 5–6** — correctly copies only the package manifests first, then
  `npm install --production` (skips `devDependencies` like `jest`/
  `supertest`), so this layer is cached across rebuilds unless
  `package.json`/`package-lock.json` change — the exact layer-caching
  discipline taught in Part 2 §5.
- **Line 8** — copies only `server.js` (not the whole directory), keeping
  the image minimal — test files never enter the image at all.
- **Line 11** — `CMD ["node", "server.js"]` — plain `CMD`, no
  `ENTRYPOINT`, since this is a simple demo with no need to lock the
  startup command against override.

### `app/server.js`

```javascript
1:  import express from 'express';
2:  import os from 'os';
3:  import { fileURLToPath } from 'url';
4:  
5:  const app = express();
6:  const PORT = process.env.PORT || 3000;
7:  
8:  const MESSAGE = 'Hello from the CI/CD Demo!';
9:  
10: app.get('/', (req, res) => {
11:   res.json({
12:     message: MESSAGE,
13:     version: process.env.APP_VERSION || '1.0.0',
14:     hostname: os.hostname()
15:   });
16: });
17: 
18: app.get('/health', (req, res) => {
19:   res.json({ status: 'UP' });
20: });
21: 
22: if (process.argv[1] === fileURLToPath(import.meta.url)) {
23:   app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
24: }
25: 
26: export default app;
```
- **Line 6** — `PORT` is read from the environment first, falling back to
  `3000` — matches `k8s/deployment.yaml`'s `containerPort: 3000` and the
  Dockerfile's `EXPOSE 3000`.
- **Lines 10–16** — the root route returns JSON including `os.hostname()`
  — inside a Pod this resolves to the **Pod's name**, which is exactly how
  you can visually prove load-balancing/multiple replicas are working (each
  `curl` may return a different hostname across the Deployment's replicas).
- **Line 13** — `process.env.APP_VERSION` is read at runtime and defaults
  to `'1.0.0'` if unset — `k8s/deployment.yaml` sets `APP_VERSION: "1.0.0"`
  explicitly via an `env:` entry, so this always resolves the same way in
  the cluster, but the fallback still lets the app run standalone without
  it.
- **Lines 18–20** — the `/health` endpoint is the **exact URL** the
  Deployment's `readinessProbe.httpGet.path` checks (see below), and the
  same URL the Jenkinsfile's final `Verify` stage curls.
- **Lines 22–24** — the "only listen if this file is the entry point"
  guard (comparing `process.argv[1]` against this module's own resolved
  file URL) is what lets `app/test/basic.test.js` `import app from
  '../server.js'` and drive it with `supertest` **without** the import
  itself triggering a real `app.listen()` — a clean pattern for making an
  Express app both a real server and a directly-testable module.
- **Line 26** — `export default app` is what makes line 22's guard
  meaningful — the module exports the Express app instance itself for
  tests to wrap.

### `app/test/basic.test.js`

```javascript
1:  import request from 'supertest';
2:  import app from '../server.js';
3:  
4:  describe('Simple CI/CD Demo App', () => {
5:  
6:    it('GET / returns a welcome message', async () => {
7:      const res = await request(app).get('/');
8:      expect(res.body.message).toBeDefined();
9:    });
10: 
11:   it('GET / returns status 200', async () => {
12:     const res = await request(app).get('/');
13:     expect(res.statusCode).toBe(200);
14:   });
15: 
16:   it('GET / does not return status 404', async () => {
17:     const res = await request(app).get('/');
18:     expect(res.statusCode).not.toBe(404);
19:   });
20: 
21:   it('GET /health returns UP status', async () => {
22:     const res = await request(app).get('/health');
23:     expect(res.body.status).toBe('UP');
24:   });
25: 
26:   it('GET /health returns status 200', async () => {
27:     const res = await request(app).get('/health');
28:     expect(res.statusCode).toBe(200);
29:   });
30: 
31:   it('GET /health does not return status 404', async () => {
32:     const res = await request(app).get('/health');
33:     expect(res.statusCode).not.toBe(404);
34:   });
35: 
36: });
```
- **Line 1–2** — `supertest` drives HTTP assertions directly against the
  exported Express `app` object **in-process**, with no real socket/port
  bound — this is exactly what line 22–24 of `server.js` makes possible.
- **Lines 6–19** — three assertions against `/`: message defined, status
  200, and explicitly *not* 404 (a slightly redundant but common defensive
  pattern — proving the route actually matched rather than falling through
  to Express's default 404 handler).
- **Lines 21–34** — the same three-assertion pattern repeated against
  `/health`, which is the endpoint the Jenkinsfile's CI `npm test` stage
  and the Kubernetes `readinessProbe` both depend on being correct.
- These are exactly the tests the Jenkinsfile's `Install & Test` stage
  runs via `npm test` (Part 5 §4's "fail fast" principle in action — this
  stage runs before any Docker build).

### `k8s/deployment.yaml`

```yaml
1:  apiVersion: apps/v1
2:  kind: Deployment
3:  metadata:
4:    name: acme-ansible-demo
5:    labels:
6:      app: acme-ansible-demo
7:  spec:
8:    replicas: 2
9:    selector:
10:     matchLabels:
11:       app: acme-ansible-demo
12:   template:
13:     metadata:
14:       labels:
15:         app: acme-ansible-demo
16:     spec:
17:       containers:
18:         - name: acme-ansible-demo
19:           image: vamandeshmukh/acme-ansible-demo:latest
20:           imagePullPolicy: IfNotPresent
21:           ports:
22:             - containerPort: 3000
23:           env:
24:             - name: APP_VERSION
25:               value: "1.0.0"
26:           readinessProbe:
27:             httpGet:
28:               path: /health
29:               port: 3000
30:             initialDelaySeconds: 3
31:             periodSeconds: 5
```
- **Line 8** — `replicas: 2` — two Pods, enabling both rolling updates and
  the "different hostname per request" demo from `server.js`.
- **Lines 9–11 / 12–15** — the selector/template label pairing is
  identical in shape to the `ems-app` Deployment in Part 7.3 — this is the
  same required-match pattern every Deployment needs.
- **Line 19** — the image tag here is hardcoded to `:latest` in the
  checked-in manifest, but is **overridden live** by the Ansible
  playbook's task 3 (`kubectl set image ... {{ image_tag }}`), which is
  passed the Jenkins build number as `image_tag` — so the file on disk is
  really just the "first apply" bootstrap state; the tag that actually ends
  up running is whatever the pipeline last set.
- **Line 20** — `imagePullPolicy: IfNotPresent` — checked into the
  manifest directly this time (unlike `acme-ems-docker`, where the
  equivalent setting is done as a live `kubectl patch` by the playbook) —
  tells the kubelet to reuse a locally cached image with a matching tag
  rather than always re-pulling.
- **Lines 23–25** — `APP_VERSION` is injected as a literal env var,
  matching what `server.js` line 13 reads.
- **Lines 26–31** — `readinessProbe` hits `/health` on port 3000, waits 3
  seconds before the first check (`initialDelaySeconds`), then re-checks
  every 5 seconds (`periodSeconds`) — a Pod failing this probe is taken out
  of Service rotation without being killed (that's what `livenessProbe`
  would do instead, which this manifest doesn't define).

### `k8s/service.yaml`

```yaml
1:  apiVersion: v1
2:  kind: Service
3:  metadata:
4:    name: acme-ansible-demo-svc
5:  spec:
6:    type: NodePort
7:    selector:
8:      app: acme-ansible-demo
9:    ports:
10:     - port: 3000
11:       targetPort: 3000
12:       nodePort: 30081
```
- **Line 6** — `NodePort`, correct for reaching this Service from outside
  the cluster on Docker Desktop's local Kubernetes without a cloud load
  balancer.
- **Line 12** — `nodePort: 30081` is a fixed, explicit port in the
  30000–32767 valid range, rather than letting Kubernetes assign one
  randomly — this is what lets the Jenkinsfile's `Verify` stage curl a
  known, stable URL (`http://localhost:30081/health`) every single build.

### `Jenkinsfile`

```groovy
1:  // Ansible-driven CI/CD demo pipeline
2:  // Flow: Checkout -> Test -> Docker Build -> Docker Push (DockerHub) -> Deploy (Ansible -> Kubernetes) -> Verify
3:  //
4:  // One-time setup before running:
5:  //   1. Jenkins > Manage Jenkins > Credentials > add "Username with password"
6:  //      with ID "DOCKERHUB_CREDENTIALS" (your DockerHub username + access token)
7:  //   2. Update IMAGE_NAME below to <your-dockerhub-username>/acme-ansible-demo
8:  //   3. Run `kubectl apply -f k8s/` once manually so the Deployment/Service exist
9:  //      before this pipeline tries to `kubectl set image` on it
10: //   4. Jenkins agent (native install) needs docker, kubectl, and node on PATH,
11: //      and its kubectl context must already point at docker-desktop
12: //   5. Install WSL2 (if not already present from Docker Desktop) and, inside
13: //      the WSL distro: `sudo apt update && sudo apt install -y ansible`.
14: //      Verify `kubectl get deployments` works from inside WSL too -- Docker
15: //      Desktop shares its docker-desktop context with WSL automatically.
16: 
17: pipeline {
18:     agent any
19: 
20:     environment {
21:         IMAGE_NAME = "vamandeshmukh/acme-ansible-demo"
22:         IMAGE_TAG  = "${env.BUILD_NUMBER}"
23:     }
24: 
25:     stages {
26: 
27:         stage('Checkout') {
28:             steps {
29:                 git branch: 'main', url: 'https://github.com/dyesmuk/acme-ansible-demo-4-jun-2026.git'
30:             }
31:         }
32: 
33:         stage('Install & Test') {
34:             steps {
35:                 dir('app') {
36:                     bat 'npm install'
37:                     bat 'npm test'
38:                 }
39:             }
40:         }
41: 
42:         stage('Docker Build') {
43:             steps {
44:                 dir('app') {
45:                     bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
46:                 }
47:             }
48:         }
49: 
50:         stage('Docker Push') {
51:             steps {
52:                 withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_CREDENTIALS', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW')]) {
53:                     bat '''
54:                     @echo off
55:                     powershell -Command "$env:DOCKERHUB_CREDENTIALS_PSW | docker login -u $env:DOCKERHUB_CREDENTIALS_USR --password-stdin"
56:                     '''
57:                     bat "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
58:                     bat "docker push ${IMAGE_NAME}:latest"
59:                 }
60:             }
61:         }
62: 
63:         stage('Deploy to Kubernetes (Ansible)') {
64:             steps {
65:                 dir('ansible') {
66:                     bat "wsl ansible-playbook -i inventory.ini deploy-playbook.yml --extra-vars \"image_tag=${IMAGE_TAG}\""
67:                 }
68:             }
69:         }
70: 
71:         stage('Verify') {
72:             steps {
73:                 bat "kubectl get pods -l app=acme-ansible-demo"
74:                 bat "curl -s http://localhost:30081/health || true"
75:             }
76:         }
77:     }
78: 
79:     post {
80:         success {
81:             echo "Pipeline succeeded -- ${IMAGE_NAME}:${IMAGE_TAG} is live on Kubernetes"
82:         }
83:         failure {
84:             echo "Pipeline failed -- check the stage logs above"
85:         }
86:     }
87: }
```
- **Lines 4–15** — this pipeline's header comments are effectively its own
  runbook: it documents the exact Jenkins credential ID it expects
  (`DOCKERHUB_CREDENTIALS`), the manual `kubectl apply -f k8s/` bootstrap
  that must happen once before the pipeline can `set image` on a
  Deployment that doesn't exist yet, and — the detail unique to this
  project — the requirement that **Ansible runs inside WSL2**, since
  Ansible has no native Windows control node, while `kubectl` still runs
  natively on Windows and shares Docker Desktop's `docker-desktop`
  context with WSL.
- **Line 18** — `agent any` — Jenkins is running **natively** on the
  training machine (not in a Docker container itself), consistent with the
  courseware's `06-ems-devops-series-overview.md` design note that this
  lab's CI/CD stage runs Jenkins natively "to avoid cross-platform config
  headaches" once the shared-Jenkins-instance idea collapsed to
  individual/local.
- **Lines 20–23** — `IMAGE_TAG = "${env.BUILD_NUMBER}"` — every build gets
  a unique, traceable tag (Jenkins' auto-incrementing build number),
  exactly the pattern from Part 5 §8's Kubernetes-integration example.
- **Line 29** — `git branch: 'main', url: '...'` — declarative-syntax
  checkout step, identical shape to every courseware Jenkinsfile example.
- **Lines 33–40** — `dir('app') { bat 'npm install'; bat 'npm test' }` —
  `dir()` scopes the working directory to `app/` for these steps (so
  relative paths like `package.json` resolve correctly); `bat` (not `sh`)
  is used throughout because this Jenkins agent is a **native Windows**
  install, not Linux/WSL — the one exception is line 66, which explicitly
  shells out **into** WSL via `bat "wsl ansible-playbook ..."` since
  Ansible itself has no Windows-native binary.
- **Lines 42–48** — builds two tags in one `docker build` invocation
  (`-t ...${IMAGE_TAG} -t ...latest`), avoiding a second separate `docker
  tag` command.
- **Lines 50–61** — `withCredentials` binds the stored Jenkins credential
  to two env vars (`_USR`/`_PSW` suffixes are Jenkins' convention for
  `usernamePassword` bindings), then pipes the password into `docker login
  --password-stdin` via PowerShell (never echoing the password to a plain
  log line) before pushing both tags and never calling `docker logout`
  explicitly (unlike the courseware's Part 5 §8 example, which does call
  `docker logout` — a minor hygiene gap in this project's Jenkinsfile
  worth noting).
- **Lines 63–69** — the stage that actually differentiates this project
  from the plain `Code/Jenkins` one below: instead of calling `kubectl`
  directly, it calls the Ansible playbook (`dir('ansible')` + `bat "wsl
  ansible-playbook -i inventory.ini deploy-playbook.yml --extra-vars
  \"image_tag=${IMAGE_TAG}\""`), passing the build's own `IMAGE_TAG`
  straight into the playbook's `image_tag` variable — this is precisely
  the "Jenkins passes `--extra-vars` to Ansible" pattern from Part 5 §7.
- **Lines 71–75** — `Verify` stage: lists Pods matching the app label,
  then curls the fixed NodePort's `/health` endpoint, with `|| true` so a
  transient curl failure doesn't fail the whole pipeline (a soft check
  rather than a hard gate, unlike the courseware's Part 5 §8 smoke test
  which uses `curl -f ... || exit 1` to hard-fail).
- **Lines 79–86** — no automatic rollback on failure here (unlike the
  fuller courseware Kubernetes pipeline's `post.failure` block that runs
  `kubectl rollout undo`) — this project's `post` block only echoes
  success/failure messages.


## 7.5 Jenkins — `Code/Jenkins/` (straight kubectl deploy, no Ansible)

Same shape as the Ansible project's app (an Express server), same
Dockerfile pattern, but here the Jenkinsfile talks to Kubernetes **directly
via `kubectl`** rather than through an Ansible playbook — the cleanest
side-by-side comparison of "CI/CD deploying via raw `kubectl`" vs.
"CI/CD deploying via an Ansible wrapper around `kubectl`."

### `app/server.js` (near-identical to the Ansible app, one line different)

```javascript
1:  import express from 'express';
2:  import os from 'os';
3:  import { fileURLToPath } from 'url';
4:  
5:  const app = express();
6:  const PORT = process.env.PORT || 3000;
7:  
8:  const MESSAGE = 'Hello from the CI/CD Demo!';
9:  
10: app.get('/', (req, res) => {
11:   console.log(req);
12:   res.json({
13:     message: MESSAGE,
14:     version: process.env.APP_VERSION || '1.0.0',
15:     hostname: os.hostname()
16:   });
17: });
18: 
19: app.get('/health', (req, res) => {
20:   res.json({ status: 'UP' });
21: });
22: 
23: if (process.argv[1] === fileURLToPath(import.meta.url)) {
24:   app.listen(PORT, () => {
25:     console.log(`Server running on port ${PORT}`);
26:   });
27: }
28: 
29: export default app;
```
- **Line 11** — the one real difference from the Ansible project's
  `server.js`: `console.log(req)` logs the **entire raw request object**
  on every hit to `/`. This is debug-grade logging left in (dumping a huge
  circular Node.js request object to stdout on every request is noisy and
  not something you'd want in a real production log stream) — worth
  flagging as a "what would you fix in code review" item if this came up
  on an assessment.
- Everything else (`app.js`'s Dockerfile, `package.json`,
  `test/basic.test.js`) is byte-for-byte the same pattern as the Ansible
  project's app — same `/health` endpoint, same test suite shape, same
  `COPY package*.json ./` → `RUN npm install --production` → `COPY
  server.js ./` Dockerfile.

### `k8s/deployment.yaml` and `k8s/service.yaml`

```yaml
# k8s/deployment.yaml
1:  apiVersion: apps/v1
2:  kind: Deployment
3:  metadata:
4:    name: acme-cicd-demo
5:    labels:
6:      app: acme-cicd-demo
7:  spec:
8:    replicas: 2
9:    selector:
10:     matchLabels:
11:       app: acme-cicd-demo
12:   template:
13:     metadata:
14:       labels:
15:         app: acme-cicd-demo
16:     spec:
17:       containers:
18:         - name: acme-cicd-demo
19:           image: vamandeshmukh/acme-cicd-demo:latest
20:           ports:
21:             - containerPort: 3000
22:           env:
23:             - name: APP_VERSION
24:               value: "1.0.0"
25:           readinessProbe:
26:             httpGet:
27:               path: /health
28:               port: 3000
29:             initialDelaySeconds: 3
30:             periodSeconds: 5
```
- Structurally identical to the Ansible project's `k8s/deployment.yaml`
  (same replicas, probe timing, env var), with one deliberate omission:
  **no `imagePullPolicy` field at all** here. Kubernetes' default
  `imagePullPolicy` behavior is `IfNotPresent` for any tag other than
  `:latest`, but `Always` when the tag **is** `:latest` (line 19 uses
  `:latest`) — meaning the kubelet will, by default, always attempt to
  re-pull this image on every Pod (re)start. The Ansible project handles
  this explicitly (via the playbook's live `kubectl patch` to force
  `IfNotPresent`); this plain-Jenkins project does not, and instead relies
  entirely on the Jenkinsfile's own `kubectl set image` (below) pointing at
  a build-numbered tag rather than `:latest` at deploy time.

```yaml
# k8s/service.yaml
1:  apiVersion: v1
2:  kind: Service
3:  metadata:
4:    name: acme-cicd-demo-svc
5:  spec:
6:    type: NodePort
7:    selector:
8:      app: acme-cicd-demo
9:    ports:
10:     - port: 3000
11:       targetPort: 3000
12:       nodePort: 30080
```
- Same `NodePort` pattern as the Ansible project, on a **different** fixed
  port (`30080` here vs. `30081` there) — the two demo projects were
  clearly designed to be able to run side-by-side on the same Docker
  Desktop cluster without port collisions.

### `Jenkinsfile`

```groovy
1:  // Simple Node.js CI/CD demo pipeline
2:  // Flow: Checkout -> Test -> Docker Build -> Docker Push (DockerHub) -> Deploy (Kubernetes/Minikube) -> Verify
3:  //
4:  // One-time setup before running:
5:  //   1. Jenkins > Manage Jenkins > Credentials > add "Username with password"
6:  //      with ID "DOCKERHUB_CREDENTIALS" (your DockerHub username + access token)
7:  //   2. Update IMAGE_NAME below to <your-dockerhub-username>/acme-cicd-demo
8:  //   3. Run `kubectl apply -f k8s/` once manually so the Deployment/Service exist
9:  //      before this pipeline tries to `kubectl set image` on it
10: //   4. Jenkins agent (native install) needs docker, kubectl, and node on PATH,
11: //      and its kubectl context must already point at docker-desktop
12: 
13: pipeline {
14:     agent any
15: 
16:     environment {
17:         IMAGE_NAME = "vamandeshmukh/acme-cicd-demo"
18:         IMAGE_TAG  = "${env.BUILD_NUMBER}"
19:     }
20: 
21:     stages {
22: 
23:         stage('Checkout') {
24:             steps {
25:                 git branch: 'main', url: 'https://github.com/dyesmuk/acme-cicd-demo-4-jun-2026.git'
26:             }
27:         }
28: 
29:         stage('Install & Test') {
30:             steps {
31:                 dir('app') {
32:                     bat 'npm install'
33:                     bat 'npm test'
34:                 }
35:             }
36:         }
37: 
38:         stage('Docker Build') {
39:             steps {
40:                 dir('app') {
41:                     bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
42:                 }
43:             }
44:         }
45: 
46:         stage('Docker Push') {
47:             steps {
48:                 withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_CREDENTIALS', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW')]) {
49:                     bat '''
50:                     @echo off
51:                     powershell -Command "$env:DOCKERHUB_CREDENTIALS_PSW | docker login -u $env:DOCKERHUB_CREDENTIALS_USR --password-stdin"
52:                     '''
53:                     bat "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
54:                     bat "docker push ${IMAGE_NAME}:latest"
55:                 }
56:             }
57:         }
58: 
59:         stage('Deploy to Kubernetes') {
60:             steps {
61:                 bat "kubectl patch deployment acme-cicd-demo -p \"{\\\"spec\\\":{\\\"template\\\":{\\\"spec\\\":{\\\"containers\\\":[{\\\"name\\\":\\\"acme-cicd-demo\\\",\\\"imagePullPolicy\\\":\\\"IfNotPresent\\\"}]}}}}\""
62:                 bat "kubectl set image deployment/acme-cicd-demo acme-cicd-demo=${IMAGE_NAME}:${IMAGE_TAG} --record"
63:                 bat "kubectl rollout status deployment/acme-cicd-demo --timeout=90s"
64:             }
65:         }
66: 
67:         stage('Verify') {
68:             steps {
69:                 bat "kubectl get pods -l app=acme-cicd-demo"
70:                 bat "curl -s http://localhost:30080/health || true"
71:             }
72:         }
73:     }
74: 
75:     post {
76:         success {
77:             echo "Pipeline succeeded -- ${IMAGE_NAME}:${IMAGE_TAG} is live on Kubernetes"
78:         }
79:         failure {
80:             echo "Pipeline failed -- check the stage logs above"
81:         }
82:     }
83: }
```
- **Lines 1–11, 13–58** — identical pattern and even identical prose to the
  Ansible project's Jenkinsfile through the Docker Push stage (same
  `dir('app')` scoping, same `withCredentials` login pattern, same
  double-tag build) — confirming these two demo repos are deliberately
  parallel implementations of the same pipeline shape, differing only at
  the deploy stage.
- **Line 61** — the `imagePullPolicy: IfNotPresent` patch that the Ansible
  project's playbook does as a **registered, idempotency-checked task**
  (task 2 in `deploy-playbook.yml`) is done here as a **raw inline
  `kubectl patch`** with manually hand-escaped JSON (`\"` and `\\\"`
  nesting for Groovy string + Windows `bat` shell + JSON, three layers of
  escaping) — a good illustration of exactly the kind of fragile-but-
  functional shell escaping that using Ansible (or a Kubernetes-native
  Jenkins plugin like `withKubeConfig`, per the courseware's Part 5 §8)
  is meant to abstract away.
- **Line 62** — `kubectl set image ... --record` — the `--record` flag
  (deprecated in modern `kubectl` but still present here) annotates the
  resulting rollout with the command that caused it, visible later in
  `kubectl rollout history`.
- **Line 63** — `kubectl rollout status ... --timeout=90s` — same wait
  pattern as the Ansible playbook's task 4, just invoked directly instead
  of through Ansible.
- **Lines 67–71** — same `Verify` shape as the Ansible project, on port
  `30080` instead of `30081`.
- **Lines 75–82** — same `post` block shape — success/failure echo only, no
  automatic rollback in either project (both diverge from the courseware's
  fuller `post.failure { kubectl rollout undo ... }` example).

### `demo-script.md` and `README.md`

Both are instructor-facing walkthroughs for live-demoing this exact
pipeline in a training session (confirmed by file names/context) — they
don't introduce new technical content beyond what's captured in the
Jenkinsfile and manifests above, so they aren't reproduced line by line
here; their value is entirely in the sequencing of *how* to demo the
pipeline, not in new configuration.


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
   <image>:latest .` builds the image from `app/Dockerfile`'s `COPY
   package*.json ./` → `npm install --production` → `COPY server.js ./`
   layer sequence, using Docker's layer cache so unchanged dependencies
   skip re-installation.
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

