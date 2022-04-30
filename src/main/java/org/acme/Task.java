package org.acme;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

@ApplicationScoped
public class Task {
    @Inject
    WebClientResource webClient;

    @Scheduled(
        every = "5s",
        concurrentExecution = SKIP
    )
    public void runClient() {
        var urls = List.of(
            "https://docs.python.org/3/whatsnew/3.10.html",
            "https://docs.python.org/3/whatsnew/3.9.html",
            "https://docs.scala-lang.org/tour/higher-order-functions.html",
            "https://docs.scala-lang.org/tour/traits.html"
        );

        urls.forEach(
            url -> webClient.doGETRequest(url)
                .subscribe().with(resp -> {
                    if (resp != null)
                        Log.debugf("Received %s bytes...", resp.getBytes().length);
                    else
                        Log.debugf("UNI produced null...");
                })
        );
    }
}
