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

    private static final long INITIAL_CURSOR_EPOCH_SECONDS = 1_700_000_000L;
    private static final Instant INITIAL_CURSOR = Instant.ofEpochSecond(INITIAL_CURSOR_EPOCH_SECONDS);
    private static final long CURSOR_INCREMENT_SECONDS = 1L;
    private static final long FIRST_BATCH_OFFSET_SECONDS = 10L;
    private static final long SECOND_BATCH_OFFSET_SECONDS = 20L;
    private static final long FIVE_SECONDS_BEFORE = 5L;
    private static final long TEN_SECONDS_BEFORE = 10L;
    private static final int LIMIT_COUNT = 5;
    private static final int BATCH_SIZE = 100;
    private static final int EXPECTED_SINGLE_FETCH = 1;
    private static final int ZERO_FETCH_CALLS = 0;
    private static final String NULL_BATCH_MESSAGE_PREFIX = "fetchBatch returned null for cursor";

    private static final ToLongFunction<OrderRef> EXTRACT_DATE = OrderRef::dateConfirmed;

    private record OrderRef(long id, long dateConfirmed) {
    }

    @Test
    void stream_whenFirstBatchIsEmpty_returnsNoItems() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> List.of(), EXTRACT_DATE, INITIAL_CURSOR);

        assertEquals(List.of(), page.toList());
    }

    @Test
    void stream_whenSingleBatchFollowedByEmpty_emitsAllFirstBatchItems() {
        List<OrderRef> batch = List.of(
                new OrderRef(1, INITIAL_CURSOR_EPOCH_SECONDS),
                new OrderRef(2, INITIAL_CURSOR_EPOCH_SECONDS + FIRST_BATCH_OFFSET_SECONDS)
        );
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> cursor.equals(INITIAL_CURSOR) ? batch : List.of(),
                EXTRACT_DATE, INITIAL_CURSOR);

        assertEquals(batch, page.toList());
    }

    @Test
    void stream_whenMultipleBatches_advancesCursorByMaxDatePlusOne() {
        List<Instant> requestedCursors = new ArrayList<>();
        long batch1MaxDate = INITIAL_CURSOR_EPOCH_SECONDS + FIRST_BATCH_OFFSET_SECONDS;
        long batch2MaxDate = batch1MaxDate + SECOND_BATCH_OFFSET_SECONDS;

        Function<Instant, List<OrderRef>> fetcher = cursor -> {
            requestedCursors.add(cursor);
            if (cursor.equals(INITIAL_CURSOR)) {
                return List.of(
                        new OrderRef(1, batch1MaxDate - FIVE_SECONDS_BEFORE),
                        new OrderRef(2, batch1MaxDate)
                );
            }
            if (cursor.getEpochSecond() == batch1MaxDate + CURSOR_INCREMENT_SECONDS) {
                return List.of(new OrderRef(3, batch2MaxDate));
            }
            return List.of();
        };
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                fetcher, EXTRACT_DATE, INITIAL_CURSOR);

        List<OrderRef> items = page.toList();

        List<Long> emittedIds = items.stream().map(OrderRef::id).toList();
        assertEquals(List.of(1L, 2L, 3L), emittedIds);
        assertEquals(INITIAL_CURSOR, requestedCursors.get(0));
        assertEquals(Instant.ofEpochSecond(batch1MaxDate + CURSOR_INCREMENT_SECONDS),
                requestedCursors.get(1));
        assertEquals(Instant.ofEpochSecond(batch2MaxDate + CURSOR_INCREMENT_SECONDS),
                requestedCursors.get(2));
    }

    @Test
    void stream_whenItemDateBelowBatchCursor_skipsItem() {
        long freshDate = INITIAL_CURSOR_EPOCH_SECONDS + FIRST_BATCH_OFFSET_SECONDS;
        long staleDate = INITIAL_CURSOR_EPOCH_SECONDS - FIVE_SECONDS_BEFORE;
        Function<Instant, List<OrderRef>> fetcher = cursor -> {
            if (cursor.equals(INITIAL_CURSOR)) {
                return List.of(
                        new OrderRef(1, staleDate),
                        new OrderRef(2, freshDate)
                );
            }
            return List.of();
        };
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                fetcher, EXTRACT_DATE, INITIAL_CURSOR);

        List<Long> emittedIds = page.stream().map(OrderRef::id).toList();

        assertEquals(List.of(2L), emittedIds);
    }

    @Test
    void stream_whenCursorFailsToAdvance_stopsIteration() {
        long datePriorToCursor = INITIAL_CURSOR_EPOCH_SECONDS - TEN_SECONDS_BEFORE;
        AtomicInteger callCount = new AtomicInteger();
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> {
                    callCount.incrementAndGet();
                    return List.of(new OrderRef(1, datePriorToCursor));
                },
                EXTRACT_DATE, INITIAL_CURSOR);

        List<OrderRef> items = page.toList();

        assertEquals(List.of(), items, "stale item should be filtered out");
        assertEquals(EXPECTED_SINGLE_FETCH, callCount.get(),
                "must not loop when cursor does not advance");
    }

    @Test
    void stream_whenFetcherReturnsNull_throwsNullPointerException() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> null, EXTRACT_DATE, INITIAL_CURSOR);

        NullPointerException exception = assertThrows(NullPointerException.class, page::toList);
        assertTrue(exception.getMessage().startsWith(NULL_BATCH_MESSAGE_PREFIX),
                "actual message: " + exception.getMessage());
    }

    @Test
    void stream_whenCalledTwice_throwsIllegalStateException() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> List.of(), EXTRACT_DATE, INITIAL_CURSOR);
        page.stream();

        assertThrows(IllegalStateException.class, page::stream);
    }

    @Test
    void iterator_whenCalledAfterStream_throwsIllegalStateException() {
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> List.of(), EXTRACT_DATE, INITIAL_CURSOR);
        page.stream();

        assertThrows(IllegalStateException.class, page::iterator);
    }

    @Test
    void stream_whenCalledWithoutTerminalOperation_doesNotInvokeFetcher() {
        AtomicInteger callCount = new AtomicInteger();
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                cursor -> {
                    callCount.incrementAndGet();
                    return List.of();
                },
                EXTRACT_DATE, INITIAL_CURSOR);

        page.stream();

        assertEquals(ZERO_FETCH_CALLS, callCount.get(),
                "fetcher should not be called before terminal op");
    }

    @Test
    void stream_whenLimitedBelowBatchSize_fetchesOnlyFirstBatch() {
        AtomicInteger callCount = new AtomicInteger();
        Function<Instant, List<OrderRef>> fetcher = cursor -> {
            callCount.incrementAndGet();
            long base = cursor.getEpochSecond();
            List<OrderRef> batch = new ArrayList<>();
            for (int index = 0; index < BATCH_SIZE; index++) {
                batch.add(new OrderRef(base * BATCH_SIZE + index, base + index));
            }
            return batch;
        };
        DateCursorResultPage<OrderRef> page = new DateCursorResultPage<>(
                fetcher, EXTRACT_DATE, INITIAL_CURSOR);

        List<OrderRef> first = page.stream().limit(LIMIT_COUNT).toList();

        assertEquals(LIMIT_COUNT, first.size());
        assertEquals(EXPECTED_SINGLE_FETCH, callCount.get(),
                "limit below batch size should consume only first batch");
    }

    @Test
    void constructor_whenFetchBatchIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new DateCursorResultPage<>(null, EXTRACT_DATE, INITIAL_CURSOR));
    }

    @Test
    void constructor_whenExtractDateConfirmedIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new DateCursorResultPage<>(cursor -> List.of(), null, INITIAL_CURSOR));
    }

    @Test
    void constructor_whenInitialCursorIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new DateCursorResultPage<>(cursor -> List.of(), EXTRACT_DATE, null));
    }
}
