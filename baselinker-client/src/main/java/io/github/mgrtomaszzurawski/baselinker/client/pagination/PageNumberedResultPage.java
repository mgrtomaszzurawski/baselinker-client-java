package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import java.util.List;
import java.util.Objects;
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
public final class PageNumberedResultPage<T> extends AbstractSingleUseResultPage<T> {

    private static final int FIRST_PAGE = 1;

    private final IntFunction<List<T>> fetchPage;

    public PageNumberedResultPage(IntFunction<List<T>> fetchPage) {
        this.fetchPage = Objects.requireNonNull(fetchPage, "fetchPage must not be null");
    }

    @Override
    protected Stream<T> streamItems() {
        return IntStream.iterate(FIRST_PAGE, page -> page + 1)
                .mapToObj(this::fetchNonNull)
                .takeWhile(batch -> !batch.isEmpty())
                .flatMap(List::stream);
    }

    private List<T> fetchNonNull(int page) {
        List<T> batch = fetchPage.apply(page);
        return Objects.requireNonNull(batch, () -> "fetchPage returned null for page " + page);
    }
}
