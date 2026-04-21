package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Shared base for {@link ResultPage} implementations that must be consumed at most once.
 * Enforces single-use semantics via an {@link AtomicBoolean} and routes every terminal
 * shortcut through {@link #stream()} so the guard is triggered uniformly.
 */
abstract class AbstractSingleUseResultPage<T> implements ResultPage<T> {

    static final String SINGLE_USE_ERROR =
            "ResultPage is single-use; stream/iterator/toList may be called only once";

    private final AtomicBoolean consumed = new AtomicBoolean(false);

    @Override
    public final Stream<T> stream() {
        markConsumed();
        return streamItems();
    }

    @Override
    public final List<T> toList() {
        return stream().toList();
    }

    @Override
    public final Iterator<T> iterator() {
        return stream().iterator();
    }

    /**
     * Provides the backing stream after the single-use guard has been cleared.
     * Implementations may assume this is called at most once per instance.
     */
    protected abstract Stream<T> streamItems();

    private void markConsumed() {
        if (!consumed.compareAndSet(false, true)) {
            throw new IllegalStateException(SINGLE_USE_ERROR);
        }
    }
}
