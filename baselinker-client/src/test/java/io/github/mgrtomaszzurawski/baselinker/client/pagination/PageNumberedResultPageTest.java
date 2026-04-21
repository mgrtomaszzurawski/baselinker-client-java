package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageNumberedResultPageTest {

    private static final int FIRST_PAGE = 1;
    private static final int ITEMS_PER_PAGE = 3;
    private static final int LIMIT_COUNT = 5;
    private static final int EXPECTED_FETCH_CALLS_FOR_LIMIT = 2;
    private static final int ZERO_FETCH_CALLS = 0;
    private static final String NULL_BATCH_MESSAGE_PREFIX = "fetchPage returned null for page";

    @Test
    void stream_whenFirstPageIsEmpty_returnsNoItems() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());

        assertEquals(List.of(), page.toList());
    }

    @Test
    void stream_whenSinglePartialPage_returnsAllItems() {
        List<Integer> firstPage = List.of(1, 2);
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber ->
                pageNumber == FIRST_PAGE ? firstPage : List.of());

        assertEquals(firstPage, page.toList());
    }

    @Test
    void stream_whenMultiplePages_concatenatesAllItems() {
        List<List<Integer>> pages = List.of(
                List.of(1, 2, 3),
                List.of(4, 5, 6),
                List.of(7, 8)
        );
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageFromList(pages));

        List<Integer> expected = pages.stream().flatMap(List::stream).toList();
        assertEquals(expected, page.toList());
    }

    @Test
    void stream_whenConsumed_requestsPagesStartingAtOneAndIncrementing() {
        AtomicInteger callCount = new AtomicInteger();
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> {
            assertEquals(callCount.incrementAndGet(), pageNumber,
                    "pages must be requested in order starting at 1");
            return pageNumber <= ITEMS_PER_PAGE ? List.of(pageNumber) : List.of();
        });

        List<Integer> items = page.toList();

        List<Integer> expected = IntStream.rangeClosed(FIRST_PAGE, ITEMS_PER_PAGE).boxed().toList();
        assertEquals(expected, items);
    }

    @Test
    void stream_whenFetcherReturnsNull_throwsNullPointerException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> null);

        NullPointerException exception = assertThrows(NullPointerException.class, page::toList);
        assertTrue(exception.getMessage().startsWith(NULL_BATCH_MESSAGE_PREFIX),
                "actual message: " + exception.getMessage());
    }

    @Test
    void stream_whenCalledTwice_throwsIllegalStateException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());
        page.stream();

        assertThrows(IllegalStateException.class, page::stream);
    }

    @Test
    void iterator_whenCalledAfterStream_throwsIllegalStateException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());
        page.stream();

        assertThrows(IllegalStateException.class, page::iterator);
    }

    @Test
    void toList_whenCalledAfterStream_throwsIllegalStateException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());
        page.stream();

        assertThrows(IllegalStateException.class, page::toList);
    }

    @Test
    void stream_whenCalledWithoutTerminalOperation_doesNotInvokeFetcher() {
        AtomicInteger callCount = new AtomicInteger();
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> {
            callCount.incrementAndGet();
            return List.of();
        });

        page.stream();

        assertEquals(ZERO_FETCH_CALLS, callCount.get(),
                "fetcher should not be called before terminal op");
    }

    @Test
    void stream_whenLimitedBelowTotalItems_stopsFetchingEarly() {
        AtomicInteger callCount = new AtomicInteger();
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> {
            callCount.incrementAndGet();
            int start = (pageNumber - FIRST_PAGE) * ITEMS_PER_PAGE + 1;
            return IntStream.range(start, start + ITEMS_PER_PAGE).boxed().toList();
        });

        List<Integer> first = page.stream().limit(LIMIT_COUNT).toList();

        assertEquals(LIMIT_COUNT, first.size());
        assertEquals(EXPECTED_FETCH_CALLS_FOR_LIMIT, callCount.get(),
                "limit should stop after enough pages to cover it");
    }

    @Test
    void constructor_whenFetcherIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PageNumberedResultPage<>(null));
    }

    private static <T> IntFunction<List<T>> pageFromList(List<List<T>> pages) {
        return pageNumber -> {
            int index = pageNumber - FIRST_PAGE;
            return index < pages.size() ? pages.get(index) : List.of();
        };
    }
}
