package vn.minhtran.sm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
public class PersonController {
    private List<String> people = Arrays.asList("mike", "suzy");

    private final MeterRegistry registry;

    public PersonController(MeterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/api/people")
    @Timed(percentiles = { 0.5, 0.95, 0.999 }, histogram = true)
    @HystrixCommand(fallbackMethod = "fallbackPeople")
    public List<String> allPeople() {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return people;
    }

    @GetMapping("/api/peopleAsync")
    public CompletableFuture<Collection<String>> personNamesAsync() {
        return CompletableFuture
            .supplyAsync(() -> Collections.singletonList("jon"));
    }

    /**
     * Fallback for {@link PersonController#allPeople()}
     *
     * @return people
     */
    @SuppressWarnings("unused")
    public List<String> fallbackPeople() {
        return Arrays.asList("old mike", "fallback frank");
    }

    @GetMapping("/api/fail")
    public String fail() {
        throw new RuntimeException("boom");
    }

    @GetMapping("/api/stats")
    public Map<String, Number> stats() {
        return Optional.ofNullable(
            registry.find("http.server.requests").tags("uri", "/api/people")
                .timer())
            .map(t -> new HashMap<String, Number>() {
                {
                    put("count", t.count());
                    put("max", t.max(TimeUnit.MILLISECONDS));
                    put("mean", t.mean(TimeUnit.MILLISECONDS));
                    put(
                        "50.percentile",
                        t.percentile(0.5, TimeUnit.MILLISECONDS));
                    put(
                        "95.percentile",
                        t.percentile(0.95, TimeUnit.MILLISECONDS));
                }
            }).orElse(null);
    }
}