package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.symbol.Name;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

public class Namespace<T> {

    private final Deque<Map<Name, T>> content;

    public Namespace() {
        this.content = new ArrayDeque<>(List.of(new HashMap<>()));
    }

    public void put(NameTree name, T value, BinaryOperator<T> merger) {
        put(name.name(), value, merger);
    }

    public void put(Name name, T value) {
        put(name, value, (_, _) -> value);
    }

    public void put(Name name, T value, BinaryOperator<T> merger) {
        T existing = get(name);
        T newValue = value;
        if (existing != null) {
            newValue = merger.apply(existing, newValue);
        }

        assert this.content.peek() != null;
        this.content.peek().put(name, newValue);
    }

    public @Nullable T get(NameTree name) {
        return get(name.name());
    }

    public @Nullable T get(Name name) {
        for (Map<Name, T> scope : this.content) {
            T value = scope.get(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public void openScope() {
        this.content.push(new HashMap<>());
    }

    public Map<Name, T> closeScope() {
        if (this.content.size() <= 1) {
            throw new IllegalStateException("Cannot close last scope");
        }

        return this.content.pop();
    }

    public Map<Name, T> currentScope() {
        Map<Name, T> fullScope = new HashMap<>();
        for (Map<Name, T> scope : this.content.reversed()) {
            fullScope.putAll(scope);
        }
        return fullScope;
    }
}
