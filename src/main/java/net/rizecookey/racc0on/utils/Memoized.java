package net.rizecookey.racc0on.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class Memoized<T> implements Supplier<T> {
    private final Supplier<@NonNull T> supplier;
    private @Nullable T value;

    private Memoized(Supplier<@NonNull T> valueSupplier) {
        this.supplier = valueSupplier;
    }

    public static <T> Memoized<T> memoize(Supplier<@NonNull T> supplier) {
        return new Memoized<>(supplier);
    }

    @Override
    public @NonNull T get() {
        if (value != null) {
            return value;
        }

        return value = supplier.get();
    }

    public void clear() {
        this.value = null;
    }
}
