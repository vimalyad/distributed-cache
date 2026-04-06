# Distributed Cache — Low Level Design

A horizontally scalable, highly extensible, and event-driven distributed cache system implemented in Java.

This project is built strictly following **SOLID principles**, ensuring that every core behavior—such as data routing, capacity eviction, TTL expiration, and cache stampede prevention—is completely decoupled and pluggable via interfaces.

## 🚀 Key Features
* **Consistent Hashing (Routing):** Minimizes key remapping during cluster scale-out/scale-in.
* **Pluggable Eviction Policies:** Currently implements $O(1)$ LRU (Least Recently Used) but is open to LFU or others.
* **Decoupled TTL Management:** Exact event-driven expiration scheduling using background executor threads combined with lazy-evaluation.
* **Request Collapsing:** Prevents "Cache Stampedes" by collapsing concurrent database fetch requests for the same missing key into a single query.
* **Event-Driven Observers:** Core cache operations publish events (`ON_HIT`, `ON_EVICTION`, etc.) asynchronously to registered observers for metrics, logging, and database write-backs.
* **Prefetching Engine:** Anticipates sequential reads to load data before the application requests it.

---

## 🏗️ System Architecture & Class Diagram

The following UML Class Diagram visualizes the core components and their structural relationships.

### UML Legend:
* `<|..` **Realization (Inheritance):** A class implements an interface.
* `*--` **Composition:** A strong lifecycle dependency (e.g., a CacheNode owns its specific CacheStore).
* `o--` **Aggregation:** A weak lifecycle dependency (e.g., a Publisher holds a list of Observers, but doesn't own them).
* `-->` **Directed Association:** A class knows about and uses another class/interface.

```mermaid
classDiagram
    %% ==========================================
    %% DOMAIN MODELS (Keys, Values, Entries)
    %% ==========================================
    class CacheKey~K~ {
        <<interface>>
        +getHash() long
        +display() String
    }
    class StringCacheKey { }
    CacheKey <|.. StringCacheKey

    class CacheValue~V~ {
        <<interface>>
        +getValue() V
    }
    class StringCacheValue { }
    CacheValue <|.. StringCacheValue

    class CacheEntry~K,V~ {
        <<interface>>
        +key() CacheKey
        +value() CacheValue
        +expiredAt() long
    }
    class DefaultCacheEntry~K,V~ { }
    CacheEntry <|.. DefaultCacheEntry
    DefaultCacheEntry o-- CacheKey : aggregates
    DefaultCacheEntry o-- CacheValue : aggregates

    %% ==========================================
    %% CLIENT & ENTRY POINT
    %% ==========================================
    class CacheClient~K,V~ {
        <<interface>>
        +get(key) Optional
        +put(key, value, ttlSeconds)
    }
    class DistributedCacheClient~K,V~ { }
    CacheClient <|.. DistributedCacheClient

    %% Client Associations
    DistributedCacheClient --> DistributionPolicy : uses
    DistributedCacheClient --> RequestCollapser : uses
    DistributedCacheClient --> PrefetchPolicy : uses
    DistributedCacheClient --> PrefetchLoader : uses
    DistributedCacheClient --> OriginStore : uses
    DistributedCacheClient --> CacheEventPublisher : uses

    %% ==========================================
    %% ROUTING & REGISTRY
    %% ==========================================
    class DistributionPolicy~K~ {
        <<interface>>
        +route(key) CacheNode
        +addNode(node)
    }
    class ConsistentHashRouter~K~ { }
    DistributionPolicy <|.. ConsistentHashRouter
    ConsistentHashRouter --> CacheNode : routes to

    class NodeRegistry~K~ {
        <<interface>>
        +register(node)
    }
    class InMemoryNodeRegistry~K~ { }
    NodeRegistry <|.. InMemoryNodeRegistry
    InMemoryNodeRegistry o-- CacheNode : aggregates
    InMemoryNodeRegistry --> DistributionPolicy : notifies

    %% ==========================================
    %% CACHE NODE & STORAGE
    %% ==========================================
    class CacheNode~K,V~ {
        <<interface>>
        +nodeId() String
        +get(key) Optional
        +put(entry)
        +delete(key)
        +size() int
        +isHealthy() boolean
    }
    class LocalCacheNode~K,V~ { }
    CacheNode <|.. LocalCacheNode

    class CacheStore~K,V~ {
        <<interface>>
        +get(key) Optional
        +put(entry)
        +remove(key)
        +size() int
    }
    class LRUCacheStore~K,V~ { }
    CacheStore <|.. LRUCacheStore

    %% Node Composition (Strong Lifecycle Ownership)
    LocalCacheNode *-- CacheStore : owns
    LocalCacheNode *-- EvictionPolicy : owns
    LocalCacheNode *-- TTLManager : owns
    LocalCacheNode --> CacheEventPublisher : uses

    %% ==========================================
    %% POLICIES (Eviction, TTL, Collapsing, Prefetch)
    %% ==========================================
    class EvictionPolicy~K,V~ {
        <<interface>>
        +onAccess(entry)
        +onInsert(entry)
        +onRemove(key)
        +selectEvictionCandidate() CacheKey
    }
    class LRUEvictionPolicy~K,V~ { }
    EvictionPolicy <|.. LRUEvictionPolicy

    class TTLManager~K~ {
        <<interface>>
        +schedule(key, expiresAtMs)
        +cancel(key)
        +isExpired(key, nowMs) boolean
    }
    class ScheduledTTLManager~K~ { }
    TTLManager <|.. ScheduledTTLManager
    ScheduledTTLManager --> CacheNode : callbacks to delete

    class RequestCollapser~K,V~ {
        <<interface>>
        +getOrLoad(key, loader) CacheValue
    }
    class FutureBasedCollapser~K,V~ { }
    RequestCollapser <|.. FutureBasedCollapser

    class PrefetchPolicy~K,V~ {
        <<interface>>
        +keysToPreload(key, node) List
        +isEnabled() boolean
    }
    class NoPrefetch~K,V~ { }
    class SequentialPrefetcher~K,V~ { }
    PrefetchPolicy <|.. NoPrefetch
    PrefetchPolicy <|.. SequentialPrefetcher

    %% ==========================================
    %% EVENTS & OBSERVERS
    %% ==========================================
    class CacheEventType {
        <<enumeration>>
        ON_HIT, ON_MISS, ON_EVICTION, ON_EXPIRY, ON_PUT, ON_DELETE
    }

    class CacheEvent~K,V~ {
        <<interface>>
    }
    class DefaultCacheEvent~K,V~ { }
    CacheEvent <|.. DefaultCacheEvent
    DefaultCacheEvent --> CacheEventType : has

    class CacheEventPublisher~K,V~ {
        <<interface>>
        +register(observer)
        +publish(event)
    }
    class SynchronousEventPublisher~K,V~ { }
    CacheEventPublisher <|.. SynchronousEventPublisher

    class CacheObserver~K,V~ {
        <<interface>>
        +onEvent(event)
    }
    class CapacityAlertObserver~K,V~ { }
    class EvictionLogger~K,V~ { }
    class MetricsObserver~K,V~ { }
    class WriteBackObserver~K,V~ { }

    CacheObserver <|.. CapacityAlertObserver
    CacheObserver <|.. EvictionLogger
    CacheObserver <|.. MetricsObserver
    CacheObserver <|.. WriteBackObserver

    %% Publisher Aggregation (Weak Lifecycle)
    SynchronousEventPublisher o-- CacheObserver : notifies

    %% External System Integrations
    class AlertService { <<interface>> }
    class Logger { <<interface>> }
    class OriginStore~K,V~ { <<interface>> }

    CapacityAlertObserver --> AlertService : uses
    EvictionLogger --> Logger : uses
    WriteBackObserver --> OriginStore : uses

    %% ==========================================
    %% FACTORY / WIRING (Added)
    %% ==========================================
    class CacheSystemFactory {
        +buildDefault(...) CacheClient
    }
    CacheSystemFactory ..> DistributedCacheClient : creates
    CacheSystemFactory ..> LocalCacheNode : creates
```

---

## 🧩 Architectural Walkthrough

### 1. Application Layer (`DistributedCacheClient`)
The primary entry point. The client never stores data itself. Instead, it acts as an orchestrator. Upon receiving a `get(key)` request, it delegates to the `DistributionPolicy` to locate the correct physical node, checks the `PrefetchPolicy` to anticipate future reads, and uses the `RequestCollapser` if the data needs to be fetched from the database.

### 2. Node & Storage Layer (`LocalCacheNode`)
Represents an individual physical server or VM instance in the cache cluster. It enforces strict **Separation of Concerns**:
* `CacheStore`: Responsible solely for physically storing bytes in memory (e.g., `LRUCacheStore`).
* `EvictionPolicy`: Tracks memory pressure and decides *what* to remove when capacity is breached.
* `TTLManager`: Tracks time and decides *when* to remove items that have gone stale.

### 3. Asynchronous Operations (Observer Pattern)
To keep the core `LocalCacheNode` highly performant, secondary tasks are completely decoupled. When a cache miss or eviction happens, the node simply hands a `CacheEvent` to the `CacheEventPublisher`. Registered `CacheObserver` implementations (like `MetricsObserver` or `WriteBackObserver`) receive these events and process logging, metrics, and database syncs independently.

---

## ⚙️ Usage / Demo

To see the system in action, run the `Main.java` demo file. It utilizes the `CacheSystemFactory` to wire up the dependencies and run through 4 distinct scenarios:
1.  **Hit/Miss Cycle:** Fetching from the mock DB vs. memory.
2.  **Capacity Eviction:** Flooding the cache to trigger LRU eviction and database Write-Backs.
3.  **TTL Expiration:** Setting a short TTL and watching the `ScheduledTTLManager` destroy the entry.
4.  **Request Collapsing:** Simulating 5 concurrent threads triggering a Cache Stampede, demonstrating how the `FutureBasedCollapser` blocks and resolves them simultaneously with only *one* database query.
