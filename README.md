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

With this setup, you only need **one service definition** in `docker-compose.yml`. Scaling is handled automatically by Docker Compose.

Bring up Redis + n discovery nodes with:

```bash
docker compose up --build --scale servicediscovery=n
```

Docker will start:

- `redis` → available on host port `6379`
- `servicediscovery_1` → host port `50051`
- `servicediscovery_2` → host port `50052`
- `servicediscovery_3` → host port `50053`

Each node gets a unique container hostname (`servicediscovery_1`, `_2`, `_3`) so gossip can distinguish them. The first node (`servicediscovery_1`) is used as the seed, and additional nodes will sync their state through gossip.

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
