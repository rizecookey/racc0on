package net.rizecookey.racc0on.utils;

import org.jspecify.annotations.Nullable;

public class CastUtils {
    private CastUtils() {}

    public static <A, B extends A> @Nullable B safeGenericCast(A instance, Class<B> type) {
        return type.isInstance(instance) ? type.cast(instance) : null;
    }
}
