package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateCursorResultPageTest {

    private static final Instant INITIAL_CURSOR = Instant.ofEpochSecond(1_700_000_000L);
    private static final long CURSOR_INCREMENT_SECONDS = 1L;
    private static final int LIMIT_FIVE = 5;
    private static final long SAME_DATE = 1_700_000_100L;

    private static final ToLongFunction<OrderRef> EXTRACT_DATE = OrderRef::dateConfirmed;
    private static final ToLongFunction<OrderRef> EXTRACT_ID = OrderRef::id;

    private record OrderRef(long id, long dateConfirmed) {
    }

    @Test
    void emptyFirstBatch_streamIsEmpty() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> List.of(), EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        assertEquals(List.of(), page.toList());
    }

    @Test
    void singleBatchThenEmpty_returnsFirstBatch() {
        List<OrderRef> batch = List.of(
                new OrderRef(1, INITIAL_CURSOR.getEpochSecond()),
                new OrderRef(2, INITIAL_CURSOR.getEpochSecond() + 10)
        );
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> cursor.equals(INITIAL_CURSOR) ? batch : List.of(),
                EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        assertEquals(batch, page.toList());
    }

    @Test
    void multipleBatches_advanceCursorByMaxDatePlusOne() {
        List<Instant> requestedCursors = new ArrayList<>();
        long batch1MaxDate = INITIAL_CURSOR.getEpochSecond() + 10;
        long batch2MaxDate = batch1MaxDate + 20;

        Function<Instant, List<OrderRef>> fetcher = cursor -> {
            requestedCursors.add(cursor);
            if (cursor.equals(INITIAL_CURSOR)) {
                return List.of(new OrderRef(1, batch1MaxDate - 5), new OrderRef(2, batch1MaxDate));
            }
            if (cursor.getEpochSecond() == batch1MaxDate + CURSOR_INCREMENT_SECONDS) {
                return List.of(new OrderRef(3, batch2MaxDate));
            }
            return List.of();
        };
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                fetcher, EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        List<OrderRef> items = page.toList();

        assertEquals(3, items.size());
        assertEquals(INITIAL_CURSOR, requestedCursors.get(0));
        assertEquals(Instant.ofEpochSecond(batch1MaxDate + CURSOR_INCREMENT_SECONDS),
                requestedCursors.get(1));
        assertEquals(Instant.ofEpochSecond(batch2MaxDate + CURSOR_INCREMENT_SECONDS),
                requestedCursors.get(2));
    }

    @Test
    void dedupsItemsAppearingInConsecutiveBatches() {
        Function<Instant, List<OrderRef>> fetcher = cursor -> {
            if (cursor.equals(INITIAL_CURSOR)) {
                return List.of(new OrderRef(1, SAME_DATE), new OrderRef(2, SAME_DATE));
            }
            if (cursor.getEpochSecond() == SAME_DATE + CURSOR_INCREMENT_SECONDS) {
                return List.of(
                        new OrderRef(2, SAME_DATE),
                        new OrderRef(3, SAME_DATE + CURSOR_INCREMENT_SECONDS)
                );
            }
            return List.of();
        };
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                fetcher, EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        List<Long> emittedIds = page.stream().map(OrderRef::id).toList();

        assertEquals(List.of(1L, 2L, 3L), emittedIds);
    }

    @Test
    void cursorFailsToAdvance_stopsToAvoidInfiniteLoop() {
        long datePriorToCursor = INITIAL_CURSOR.getEpochSecond() - 10;
        AtomicInteger callCount = new AtomicInteger();
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> {
                    callCount.incrementAndGet();
                    return List.of(new OrderRef(1, datePriorToCursor));
                },
                EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        List<OrderRef> items = page.toList();

        assertEquals(1, items.size());
        assertEquals(1, callCount.get(), "must not loop when cursor does not advance");
    }

    @Test
    void fetcherReturnsNull_throwsNullPointerException() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> null, EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        NullPointerException exception = assertThrows(NullPointerException.class, page::toList);
        assertTrue(exception.getMessage().contains("cursor"));
    }

    @Test
    void streamCalledTwice_throwsIllegalStateException() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> List.of(), EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);
        page.stream();

        assertThrows(IllegalStateException.class, page::stream);
    }

    @Test
    void iteratorAfterStream_throwsIllegalStateException() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> List.of(), EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);
        page.stream();

        assertThrows(IllegalStateException.class, page::iterator);
    }

    @Test
    void streamIsLazyUntilTerminalOperation() {
        AtomicInteger callCount = new AtomicInteger();
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> {
                    callCount.incrementAndGet();
                    return List.of();
                },
                EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        page.stream();

        assertEquals(0, callCount.get(), "fetcher should not be called before terminal op");
    }

    @Test
    void limitStopsFetchingEarly() {
        AtomicInteger callCount = new AtomicInteger();
        int batchSize = 100;
        Function<Instant, List<OrderRef>> fetcher = cursor -> {
            callCount.incrementAndGet();
            long base = cursor.getEpochSecond();
            List<OrderRef> batch = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                batch.add(new OrderRef(base * batchSize + i, base + i));
            }
            return batch;
        };
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                fetcher, EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR);

        List<OrderRef> first = page.stream().limit(LIMIT_FIVE).toList();

        assertEquals(LIMIT_FIVE, first.size());
        assertEquals(1, callCount.get(), "limit of 5 should consume only first batch");
    }

    @Test
    void nullConstructorArgs_throwNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new DateCursorResultPage<>(null, EXTRACT_DATE, EXTRACT_ID, INITIAL_CURSOR));
        assertThrows(NullPointerException.class,
                () -> new DateCursorResultPage<>(cursor -> List.of(), null, EXTRACT_ID, INITIAL_CURSOR));
        assertThrows(NullPointerException.class,
                () -> new DateCursorResultPage<>(cursor -> List.of(), EXTRACT_DATE, null, INITIAL_CURSOR));
        assertThrows(NullPointerException.class,
                () -> new DateCursorResultPage<>(cursor -> List.of(), EXTRACT_DATE, EXTRACT_ID, null));
    }
}
