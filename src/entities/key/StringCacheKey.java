package entities.key;

public class StringCacheKey implements CacheKey<String> {

    private final String key;

    public StringCacheKey(String key) {
        this.key = key;
    }

    @Override
    public String rawKey() {
        return key;
    }

    @Override
    public long getHash() {
        // FNV-1a hash simulation
        // stability across JVMs
        long hash = 0xcbf29ce484222325L;
        for (byte b : key.getBytes()) {
            hash ^= b;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    @Override
    public String display() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringCacheKey)) return false;
        return key.equals(((StringCacheKey) o).key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
