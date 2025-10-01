# 🔎 Distributed Service Discovery System

## 📖 Overview

This project implements a **distributed service discovery system** inspired by tools like Consul, Zookeeper, and Eureka.

In modern microservice architectures, services are dynamic — they scale up, scale down, and move across hosts. Hard-coding IPs and ports is brittle and doesn’t survive failures. **Service Discovery** solves this by:

- Providing a **registry** where services can register themselves with metadata (IP, port, instance ID).
- Allowing clients to **discover** available instances at runtime.
- Propagating registry state across nodes via a **gossip protocol**, ensuring eventual consistency in a distributed deployment.

This system is built as infrastructure software: lightweight, fault-tolerant, and easy to scale out by simply adding more nodes.

---

## ✨ Features

- **Dynamic Registration / Deregistration**: Services can join or leave the cluster at runtime.
- **Service Discovery API**: Query by service name to fetch all active instances.
- **Heartbeat with TTL**: Instances periodically refresh their registration, automatically expiring dead ones.
- **Distributed Gossip Protocol**: No single point of failure — registry state spreads across nodes.
- **Pluggable Storage**: Backed by Redis for persistence and TTL handling.

---

## ⚙️ Setup

### 1. Clone & Build

```bash
git clone https://github.com/adnant1/service-discovery-system.git
cd service-discovery-system
./gradlew clean build
docker compose up --build
```

This launches:

- A Redis instance (`redis:7`)
- A single Service Discovery node (`servicediscovery`) listening on **gRPC port 50051**

---

### 2. Running Multiple Nodes

To scale out, add additional `servicediscovery` and `redis` entries to your `docker-compose.yml`:

```YAML
redis2:
  image: redis:7
  container_name: redis2
  ports:
    - "6379:6379"

servicediscovery2:
    build: .
    container_name: servicediscovery2
    ports:
      - "50052:50051"
    depends_on:
      - redis2
    environment:
      - NODE_HOST=servicediscovery2
      - SEEDS=servicediscovery:50051   # Always seed with the first node
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
```

- Each node must have a **unique container name + exposed port**
- `SEEDS` should point to at least one existing node (commonly the first)
- `NODE_HOST` is how the node identifies itself in gossip

Spin them up with:

```bash
docker compose up --build
```

---

## 📡 API Usage

All interactions are via **gRPC**. The `.proto` file is located in `src/main/proto/registry.proto`.

### Register a Service

```bash
grpcurl -plaintext \
  -proto ./src/main/proto/registry.proto \
  -d '{"serviceName":"payment","instanceId":"inst1","ip":"127.0.0.1","port":8080}' \
  localhost:50051 registry.RegistryService/Register
```

### Deregister a Service

```bash
grpcurl -plaintext \
  -proto ./src/main/proto/registry.proto \
  -d '{"serviceName":"payment","instanceId":"inst1"}' \
  localhost:50051 registry.RegistryService/Deregister
```

### Discover Instances

```bash
grpcurl -plaintext \
  -proto ./src/main/proto/registry.proto \
  -d '{"serviceName":"payment"}' \
  localhost:50051 registry.RegistryService/Discover
```

### Send Heartbeats

Clients should periodically refresh their TTL so instances don’t expire:

```bash
grpcurl -plaintext \
  -proto ./src/main/proto/registry.proto \
  -d '{"serviceName":"payment","instanceId":"inst1"}' \
  localhost:50051 registry.RegistryService/Heartbeat
```

---

## 🔧 How It Works Internally

- Services **register** with a node → persisted in Redis with TTL.
- Nodes periodically **dump** their registry state and **gossip** it to peers.
- Other nodes **merge** the received state into their own, ensuring eventual consistency.
- When TTL expires, instances are removed automatically.

---

## 👨‍💻 Author

**Adnan T.** — [@adnant1](https://github.com/adnant1)
