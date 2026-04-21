package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link ResultPage} for BaseLinker {@code getOrders} and similar endpoints paginated by a
 * time cursor (an inclusive {@code date_confirmed_from} parameter, Unix seconds).
 *
 * <p>The BaseLinker API recommends incrementing the cursor by 1 second after each batch
 * to avoid re-fetching the tail of the previous batch. This class applies that rule and
 * additionally filters out any item whose {@code date_confirmed} is below the cursor sent
 * for its batch — a safety net for servers that occasionally return stale items. See
 * {@code ADR/ADR-002-date-cursor-dedup-strategy.md} for the rationale.
 *
 * <p>Stops when the fetcher returns an empty list or when the cursor fails to advance
 * forward (defensive guard against malformed data).
 */
public final class DateCursorResultPage<T> extends AbstractSingleUseResultPage<T> {

    private static final long CURSOR_INCREMENT_SECONDS = 1L;

    private final Function<Instant, List<T>> fetchBatch;
    private final ToLongFunction<T> extractDateConfirmed;
    private final Instant initialCursor;

    public DateCursorResultPage(Function<Instant, List<T>> fetchBatch,
                                ToLongFunction<T> extractDateConfirmed,
                                Instant initialCursor) {
        this.fetchBatch = Objects.requireNonNull(fetchBatch, "fetchBatch must not be null");
        this.extractDateConfirmed = Objects.requireNonNull(extractDateConfirmed,
                "extractDateConfirmed must not be null");
        this.initialCursor = Objects.requireNonNull(initialCursor, "initialCursor must not be null");
    }

    @Override
    protected Stream<T> streamItems() {
        return StreamSupport.stream(new CursorSpliterator(), false);
    }

    private final class CursorSpliterator implements Spliterator<T> {

        private Instant nextRequestCursor = initialCursor;
        private Instant currentBatchCursor = initialCursor;
        private Iterator<T> currentBatch = Collections.emptyIterator();
        private boolean exhausted = false;

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (true) {
                while (currentBatch.hasNext()) {
                    T item = currentBatch.next();
                    if (extractDateConfirmed.applyAsLong(item) >= currentBatchCursor.getEpochSecond()) {
                        action.accept(item);
                        return true;
                    }
                }
                if (exhausted) {
                    return false;
                }
                if (!fetchNextBatch()) {
                    return false;
                }
            }
        }

        private boolean fetchNextBatch() {
            Instant requestCursor = nextRequestCursor;
            List<T> batch = Objects.requireNonNull(fetchBatch.apply(requestCursor),
                    () -> "fetchBatch returned null for cursor " + requestCursor);
            if (batch.isEmpty()) {
                exhausted = true;
                return false;
            }
            Instant advanced = computeNextCursor(batch);
            if (!advanced.isAfter(requestCursor)) {
                exhausted = true;
            }
            currentBatchCursor = requestCursor;
            nextRequestCursor = advanced;
            currentBatch = batch.iterator();
            return true;
        }

        private Instant computeNextCursor(List<T> batch) {
            long maxDate = batch.stream()
                    .mapToLong(extractDateConfirmed)
                    .max()
                    .orElseThrow();
            return Instant.ofEpochSecond(maxDate + CURSOR_INCREMENT_SECONDS);
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return ORDERED | NONNULL | IMMUTABLE;
        }
    }
}
