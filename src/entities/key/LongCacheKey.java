package entities.key;

public class LongCacheKey implements CacheKey<Long> {
    private final long key;

    public LongCacheKey(long key) {
        this.key = key;
    }

    @Override
    public Long rawKey() {
        return key;
    }

    @Override
    public long getHash() {
        return key;
    }

    @Override
    public String display() {
        return String.valueOf(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongCacheKey)) return false;
        return key == ((LongCacheKey) o).key;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(key);
    }
}
