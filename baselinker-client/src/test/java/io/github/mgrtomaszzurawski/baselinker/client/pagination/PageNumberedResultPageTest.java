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
    private static final int FULL_PAGE_SIZE = 3;
    private static final int LIMIT_FIVE = 5;

    @Test
    void emptyFirstPage_streamIsEmpty() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());

        assertEquals(List.of(), page.toList());
    }

    @Test
    void singlePartialPage_returnsAllItems() {
        List<Integer> firstPage = List.of(1, 2);
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber ->
                pageNumber == FIRST_PAGE ? firstPage : List.of());

        assertEquals(firstPage, page.toList());
    }

    @Test
    void multipleFullPages_concatenatesItems() {
        List<List<Integer>> pages = List.of(
                List.of(1, 2, 3),
                List.of(4, 5, 6),
                List.of(7, 8)
        );
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageFromList(pages));

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), page.toList());
    }

    @Test
    void pagesRequestedStartingAtOneAndIncrementing() {
        AtomicInteger callCount = new AtomicInteger();
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> {
            assertEquals(callCount.incrementAndGet(), pageNumber,
                    "pages must be requested in order starting at 1");
            return pageNumber <= FULL_PAGE_SIZE ? List.of(pageNumber) : List.of();
        });

        List<Integer> items = page.toList();

        assertEquals(List.of(1, 2, 3), items);
    }

    @Test
    void fetcherReturnsNull_throwsNullPointerException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> null);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> page.toList());
        assertTrue(exception.getMessage().contains("page"));
    }

    @Test
    void streamCalledTwice_throwsIllegalStateException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());
        page.stream();

        assertThrows(IllegalStateException.class, page::stream);
    }

    @Test
    void iteratorAfterStream_throwsIllegalStateException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());
        page.stream();

        assertThrows(IllegalStateException.class, page::iterator);
    }

    @Test
    void toListAfterStream_throwsIllegalStateException() {
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> List.of());
        page.stream();

        assertThrows(IllegalStateException.class, page::toList);
    }

    @Test
    void streamIsLazyUntilTerminalOperation() {
        AtomicInteger callCount = new AtomicInteger();
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> {
            callCount.incrementAndGet();
            return List.of();
        });

        page.stream();

        assertEquals(0, callCount.get(), "fetcher should not be called before terminal op");
    }

    @Test
    void limitStopsFetchingEarly() {
        AtomicInteger callCount = new AtomicInteger();
        PageNumberedResultPage<Integer> page = new PageNumberedResultPage<>(pageNumber -> {
            callCount.incrementAndGet();
            int start = (pageNumber - 1) * FULL_PAGE_SIZE + 1;
            return IntStream.rangeClosed(start, start + FULL_PAGE_SIZE - 1).boxed().toList();
        });

        List<Integer> first = page.stream().limit(LIMIT_FIVE).toList();

        assertEquals(LIMIT_FIVE, first.size());
        assertEquals(2, callCount.get(), "limit 5 with page size 3 needs 2 pages");
    }

    @Test
    void nullFetcher_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PageNumberedResultPage<>(null));
    }

    private static <T> IntFunction<List<T>> pageFromList(List<List<T>> pages) {
        return pageNumber -> {
            int index = pageNumber - FIRST_PAGE;
            return index < pages.size() ? pages.get(index) : List.of();
        };
    }
}
