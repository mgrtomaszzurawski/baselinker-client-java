package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import java.util.List;
import java.util.stream.Stream;

/**
 * Lazy iterable over a BaseLinker paginated response.
 *
 * <p>Hides the underlying paging strategy (page-number or date cursor) from the caller.
 * A {@code ResultPage} is single-use: the terminal operation ({@link #stream()},
 * {@link #toList()}, or {@link #iterator()}) may be invoked at most once. Subsequent
 * calls throw {@link IllegalStateException} to avoid silently firing duplicate API calls.
 */
public interface ResultPage<T> extends Iterable<T> {

    /**
     * Returns a lazy {@link Stream} that fetches additional pages on demand.
     * Terminal operations on the stream propagate exceptions from the underlying fetcher.
     */
    Stream<T> stream();

    /**
     * Materializes all items by consuming every page. Equivalent to {@code stream().toList()}.
     */
    List<T> toList();
}
