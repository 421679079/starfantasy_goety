package validation;

import java.util.*;
import org.spongepowered.asm.service.*;

public final class HeadlessProperties implements IGlobalPropertyService {
    private final Map<IPropertyKey, Object> values = new HashMap<>();
    private record Key(String name) implements IPropertyKey {}
    public IPropertyKey resolveKey(String name) { return new Key(name); }
    @SuppressWarnings("unchecked") public <T> T getProperty(IPropertyKey key) { return (T) values.get(key); }
    public void setProperty(IPropertyKey key, Object value) { values.put(key, value); }
    public <T> T getProperty(IPropertyKey key, T fallback) { T value = getProperty(key); return value == null ? fallback : value; }
    public String getPropertyString(IPropertyKey key, String fallback) { Object value = values.get(key); return value == null ? fallback : value.toString(); }
}
