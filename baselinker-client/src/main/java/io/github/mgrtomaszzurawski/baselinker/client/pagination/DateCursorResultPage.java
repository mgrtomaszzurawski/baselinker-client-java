package io.github.mgrtomaszzurawski.baselinker.client.pagination;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link ResultPage} for BaseLinker {@code getOrders} and similar endpoints paginated by a
 * time cursor (an inclusive {@code date_confirmed_from} parameter).
 *
 * <p>The BaseLinker API recommends incrementing the cursor by 1 second after each batch to
 * avoid re-fetching the tail of the previous batch. Because that still risks emitting the
 * same item twice when consecutive batches overlap on the second boundary, this class
 * deduplicates by a caller-provided id extractor.
 *
 * <p>Stops when the fetcher returns an empty list or when the cursor fails to advance
 * forward (defensive, prevents infinite loop on malformed data).
 */
public final class DateCursorResultPage<T> implements ResultPage<T> {

    private static final String SINGLE_USE_ERROR =
            "ResultPage is single-use; stream/iterator/toList may be called only once";
    private static final long CURSOR_INCREMENT_SECONDS = 1L;

    private final Function<Instant, List<T>> fetchBatch;
    private final ToLongFunction<T> extractDateConfirmed;
    private final ToLongFunction<T> extractId;
    private final Instant initialCursor;
    private final AtomicBoolean consumed = new AtomicBoolean(false);

    public DateCursorResultPage(Function<Instant, List<T>> fetchBatch,
                                ToLongFunction<T> extractDateConfirmed,
                                ToLongFunction<T> extractId,
                                Instant initialCursor) {
        this.fetchBatch = Objects.requireNonNull(fetchBatch, "fetchBatch must not be null");
        this.extractDateConfirmed = Objects.requireNonNull(extractDateConfirmed,
                "extractDateConfirmed must not be null");
        this.extractId = Objects.requireNonNull(extractId, "extractId must not be null");
        this.initialCursor = Objects.requireNonNull(initialCursor, "initialCursor must not be null");
    }

    @Override
    public Stream<T> stream() {
        markConsumed();
        return StreamSupport.stream(new CursorSpliterator(), false);
    }

    @Override
    public List<T> toList() {
        return stream().toList();
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    private void markConsumed() {
        if (!consumed.compareAndSet(false, true)) {
            throw new IllegalStateException(SINGLE_USE_ERROR);
        }
    }

    private final class CursorSpliterator implements Spliterator<T> {

        private Instant cursor = initialCursor;
        private Iterator<T> currentBatch = Collections.emptyIterator();
        private final Set<Long> seenIds = new HashSet<>();
        private boolean exhausted = false;

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (true) {
                while (currentBatch.hasNext()) {
                    T item = currentBatch.next();
                    if (seenIds.add(extractId.applyAsLong(item))) {
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
            List<T> batch = fetchBatch.apply(cursor);
            if (batch == null) {
                throw new NullPointerException("fetchBatch returned null for cursor " + cursor);
            }
            if (batch.isEmpty()) {
                exhausted = true;
                return false;
            }
            Instant nextCursor = computeNextCursor(batch);
            if (!nextCursor.isAfter(cursor)) {
                exhausted = true;
            }
            cursor = nextCursor;
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
            return ORDERED | NONNULL;
        }
    }
}
