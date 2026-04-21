package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * {@link ResultPage} for BaseLinker endpoints paginated by 1-based page number
 * (e.g. {@code getInventoryProductsList}, {@code getInventoryProductsStock},
 * {@code getInventoryProductLogs}).
 *
 * <p>Iteration starts at page 1 and stops when the fetcher returns an empty list.
 * The page size (e.g. 1000 items) is an API-side detail and not needed by this class.
 */
public final class PageNumberedResultPage<T> implements ResultPage<T> {

    private static final int FIRST_PAGE = 1;
    private static final String SINGLE_USE_ERROR =
            "ResultPage is single-use; stream/iterator/toList may be called only once";

    private final IntFunction<List<T>> fetchPage;
    private final AtomicBoolean consumed = new AtomicBoolean(false);

    public PageNumberedResultPage(IntFunction<List<T>> fetchPage) {
        this.fetchPage = Objects.requireNonNull(fetchPage, "fetchPage must not be null");
    }

    @Override
    public Stream<T> stream() {
        markConsumed();
        return IntStream.iterate(FIRST_PAGE, page -> page + 1)
                .mapToObj(this::fetchNonNull)
                .takeWhile(batch -> !batch.isEmpty())
                .flatMap(List::stream);
    }

    @Override
    public List<T> toList() {
        return stream().toList();
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    private List<T> fetchNonNull(int page) {
        List<T> batch = fetchPage.apply(page);
        if (batch == null) {
            throw new NullPointerException("fetchPage returned null for page " + page);
        }
        return batch;
    }

    private void markConsumed() {
        if (!consumed.compareAndSet(false, true)) {
            throw new IllegalStateException(SINGLE_USE_ERROR);
        }
    }
}
