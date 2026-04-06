import client.CacheClient;
import entities.key.CacheKey;
import entities.key.StringCacheKey;
import entities.value.CacheValue;
import entities.value.StringCacheValue;
import factory.CacheSystemFactory;
import services.alert.AlertService;
import store.OriginStore;
import utils.Logger;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================");
        System.out.println("🚀 DISTRIBUTED CACHE LLD - LIVE DEMO 🚀");
        System.out.println("=========================================\n");

        // 1. Setup Mock Dependencies
        Logger consoleLogger = new ConsoleLogger();
        AlertService consoleAlerts = new ConsoleAlertService();
        MockDatabase mockDb = new MockDatabase();

        // 2. Initialize the Cache System

        // Configuration: 3 Nodes, ONLY 2 items per node (to force eviction quickly), 10 virtual nodes
        CacheClient<String, String> cache = CacheSystemFactory.buildDefault(
                3, 2, 10, 60, mockDb, consoleLogger, consoleAlerts
        );

        System.out.println("[SYSTEM] Cache Cluster Initialized with 3 Nodes. Capacity: 2 items per node.");
        Thread.sleep(1000);

        // ---------------------------------------------------------
        // SCENARIO 1: Basic Hit, Miss, and Database Write-Back
        // ---------------------------------------------------------
        System.out.println("\n--- SCENARIO 1: Cache Miss & Cache Hit ---");
        CacheKey<String> key1 = new StringCacheKey("user:1");

        System.out.println("Application: Fetching 'user:1'...");
        Optional<CacheValue<String>> result1 = cache.get(key1); // Miss -> DB Load
        System.out.println("Application: Received -> " + (result1.isPresent() ? result1.get().getValue() : "null"));

        System.out.println("\nApplication: Fetching 'user:1' again...");
        Optional<CacheValue<String>> result2 = cache.get(key1); // Hit -> Served from Cache
        System.out.println("Application: Received -> " + (result2.isPresent() ? result2.get().getValue() : "null"));
        Thread.sleep(1500);

        // ---------------------------------------------------------
        // SCENARIO 2: Forcing LRU Eviction
        // ---------------------------------------------------------
        System.out.println("\n--- SCENARIO 2: Forcing LRU Eviction ---");
        System.out.println("Application: Flooding cache with new keys to exceed capacity of 2...");

        // Since capacity is 2 per node, putting 10 items will trigger evictions across all 3 nodes
        for (int i = 10; i < 20; i++) {
            CacheKey<String> floodKey = new StringCacheKey("item:" + i);
            CacheValue<String> floodValue = new StringCacheValue("Data-" + i);
            cache.put(floodKey, floodValue, 0); // 0 = no TTL
            Thread.sleep(100); // Slight delay to watch logs appear sequentially
        }
        Thread.sleep(1500);

        // ---------------------------------------------------------
        // SCENARIO 3: TTL Expiration
        // ---------------------------------------------------------
        System.out.println("\n--- SCENARIO 3: TTL Expiration ---");
        CacheKey<String> tempKey = new StringCacheKey("otp:999");
        CacheValue<String> tempValue = new StringCacheValue("SecretCode");

        System.out.println("Application: Saving OTP with a strict 2-second TTL...");
        cache.put(tempKey, tempValue, 2); // 2 seconds TTL

        System.out.println("Application: Fetching OTP immediately -> " + cache.get(tempKey).get().getValue());

        System.out.println("Application: Waiting 3 seconds for expiration...");
        Thread.sleep(3000); // Wait for scheduled thread to kill it

        System.out.println("Application: Fetching OTP after 3 seconds...");
        Optional<CacheValue<String>> expiredResult = cache.get(tempKey);
        System.out.println("Application: Received -> " + (expiredResult.isPresent() ? expiredResult.get().getValue() : "MISS (Data Expired)"));
        Thread.sleep(1500);

        // ---------------------------------------------------------
        // SCENARIO 4: Request Collapsing (Cache Stampede Prevention)
        // ---------------------------------------------------------
        System.out.println("\n--- SCENARIO 4: Concurrent Cache Stampede (Request Collapsing) ---");
        CacheKey<String> heavyKey = new StringCacheKey("heavy_report:2026");

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1); // To make all threads fire at the exact same millisecond

        System.out.println("Application: 5 concurrent threads asking for the exact same missing data...");
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    latch.await(); // Wait for the starting gun
                    long start = System.currentTimeMillis();
                    Optional<CacheValue<String>> res = cache.get(heavyKey);
                    long duration = System.currentTimeMillis() - start;
                    System.out.println("  Thread " + threadId + " received data in " + duration + "ms -> " + res.get().getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown(); // Fire! All 5 threads hit cache.get() simultaneously
        Thread.sleep(3000); // Wait for threads to finish
        executor.shutdown();

        System.out.println("\n=========================================");
        System.out.println("✅ DEMO COMPLETE ✅");
        System.out.println("=========================================");
        System.exit(0); // Clean exit to kill background TTL scheduler threads
    }

    // =========================================================================
    // MOCK DEPENDENCIES FOR DEMO (Place these inside the Main class file)
    // =========================================================================

    static class MockDatabase implements OriginStore<String, String> {
        @Override
        public CacheValue<String> load(CacheKey<String> key) {
            System.out.println("   [DATABASE] Expensive Query executing for key: " + key.display() + " (Taking 1.5 seconds...)");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            return new StringCacheValue("DB_DATA_FOR_" + key.display());
        }

        @Override
        public void persist(CacheKey<String> key, CacheValue<String> value) {
            System.out.println("   [DATABASE] Write-Back executed! Persisting evicted key to DB: " + key.display());
        }
    }

    static class ConsoleLogger implements Logger {
        @Override
        public void info(String message) {
            System.out.println("   [LOGGER-INFO] " + message);
        }

        @Override
        public void warn(String message) {
            System.out.println("   [LOGGER-WARN] " + message);
        }
    }

    static class ConsoleAlertService implements AlertService {
        @Override
        public void trigger(String message) {
            System.err.println("   🚨 [ALERT PIPELINE] " + message + " 🚨");
        }
    }
}