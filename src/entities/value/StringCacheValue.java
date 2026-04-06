package entities.value;

public class StringCacheValue implements CacheValue<String> {
    private final String value;

    public StringCacheValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
