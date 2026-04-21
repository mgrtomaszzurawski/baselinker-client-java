package io.github.mgrtomaszzurawski.baselinker.demo;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerApiException;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerException;

/**
 * Live test harness that exercises every v1 MVP method against the real BaseLinker API.
 *
 * <p>Required environment variable: {@code BASELINKER_TOKEN}.
 *
 * <p>By default runs READ-ONLY calls: order list/statuses, inventories, warehouses,
 * product list/stock/data/logs, document series. To also exercise write methods
 * (add order, update stock, GRN flow), set {@code BASELINKER_DEMO_WRITES=true}.
 * Write operations create real data in your BaseLinker account — use a throwaway
 * test catalog.
 *
 * <p>Run via: {@code mvn -pl demo-app exec:java -Dexec.mainClass=io.github.mgrtomaszzurawski.baselinker.demo.Demo}
 */
public final class Demo {

    private static final String ENV_TOKEN = "BASELINKER_TOKEN";
    private static final String ENV_WRITES = "BASELINKER_DEMO_WRITES";
    private static final String FLAG_TRUE = "true";

    private Demo() {
    }

    public static void main(String[] args) {
        String token = System.getenv(ENV_TOKEN);
        if (token == null || token.isBlank()) {
            System.err.println("FATAL: environment variable " + ENV_TOKEN + " is not set");
            System.exit(1);
        }
        boolean includeWrites = FLAG_TRUE.equalsIgnoreCase(System.getenv(ENV_WRITES));

        BaselinkerClient client = BaselinkerClient.builder()
                .apiToken(token)
                .build();

        DemoRunner runner = new DemoRunner(client, includeWrites);
        try {
            runner.run();
        } catch (BaselinkerApiException apiError) {
            System.err.println("BaseLinker API error: " + apiError.getErrorCode()
                    + " - " + apiError.getMessage());
            System.exit(2);
        } catch (BaselinkerException error) {
            System.err.println("BaseLinker transport error: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(3);
        }
    }
}
