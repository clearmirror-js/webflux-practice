package com.spring.reactorbase;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

@Slf4j
public class ColdAndHotSequence {

    public static void main(String[] args) throws InterruptedException {
        coldSequence();
        hotSequence();
        worldTimeColdSequence(worldTimeUri);
        worldTimeHotSequence(worldTimeUri);
    }

    private static void coldSequence() throws InterruptedException {
        System.out.println("Example7.coldSequence");
        Flux<String> coldFlux = Flux
            .fromIterable(Arrays.asList("KOREA", "JAPAN", "CHINESE"))
            .map(String::toLowerCase);

        coldFlux.subscribe(country -> log.info("# Subscriber1: {}", country));
        System.out.println("-------------------------");
        Thread.sleep(2000L);
        coldFlux.subscribe(country -> log.info("# Subscriber2: {}", country));
        System.out.println();
    }

    private static void hotSequence() throws InterruptedException {
        System.out.println("Example7.HotSequence");
        String[] singers = {"Singer A", "Singer B", "Singer C", "Singer D", "Singer E"};

        log.info("# Begin concert");
        Flux<String> hotFlux = Flux
            .fromArray(singers)
            // delayElements는 기본 스케줄러로 parallel를 사용한다.
            .delayElements(Duration.ofSeconds(1))
            // multicast original flux, 여러 subscriber가 하나의 원본 Flux를 공유한다.
            .share();

        hotFlux.subscribe(
            singer -> log.info("# Subscriber1 is watching {}'s song", singer)
        );

        Thread.sleep(2500L);

        hotFlux.subscribe(
            singer -> log.info("# Subscriber2 is watching {}'s song", singer)
        );

        Thread.sleep(3000L);
        System.out.println();
    }

    private static URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
        .host("worldtimeapi.org")
        .port(80)
        .path("/api/timezone/Asia/Seoul")
        .build()
        .encode()
        .toUri();

    private static void worldTimeColdSequence(URI worldTimeUri) throws InterruptedException {
        System.out.println("ColdAndHotSequence.worldTimeColdSequence");
        Mono<String> mono = WebClient.create()
            .get()
            .uri(worldTimeUri)
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> JsonPath.parse(response).read("$.datetime"));

        mono.subscribe(dateTime -> log.info("# dateTime1: {}", dateTime));
        Thread.sleep(2000L);
        mono.subscribe(dateTime -> log.info("# dateTime2: {}", dateTime));
        Thread.sleep(2000L);
        System.out.println();
    }

    private static void worldTimeHotSequence(URI worldTimeUri) throws InterruptedException {
        System.out.println("ColdAndHotSequence.worldTimeHotSequence");
        Mono<String> mono = WebClient.create()
            .get()
            .uri(worldTimeUri)
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> JsonPath.parse(response).<String>read("$.datetime"))
            .cache();

        mono.subscribe(dateTime -> log.info("# dateTime1: {}", dateTime));
        Thread.sleep(2000L);
        mono.subscribe(dateTime -> log.info("# dateTime2: {}", dateTime));
        Thread.sleep(2000L);
        System.out.println();
    }
}
