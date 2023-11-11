package com.spring.reactorbase;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.boot.json.JsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;

public class Example6 {

    public static void main(String[] args) {
        helloReactor();
        monoEmpty();
        worldTime();
    }

    private static void monoEmpty() {
        System.out.println("Example6.monoEmpty");
        Mono.empty()
            .subscribe(
                none -> System.out.println("# emitted onNext signal"),
                error -> System.out.println("# onError Signal"),
                () -> System.out.println("# onComplete Signal")
            );
        System.out.println();
    }

    private static void helloReactor() {
        System.out.println("Example6.helloReactor");
        Mono.just("Hello Reactor")
            .map(String::toUpperCase)
            .subscribe(System.out::println);
        System.out.println();
    }

    private static void worldTime() {
        System.out.println("Example6.worldTime");
        URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
            .host("worldtimeapi.org")
            .port(80)
            .path("/api/timezone/Asia/Seoul")
            .build()
            .encode()
            .toUri();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Mono.just(
                restTemplate.exchange(
                    worldTimeUri,
                    HttpMethod.GET,
                    new HttpEntity<String>(httpHeaders),
                    String.class
                )
            )
            .map(response -> {
                    DocumentContext jsonContext = JsonPath.parse(response.getBody());
                    String dateTime = jsonContext.read("$.datetime");
                    return dateTime;
                }
            )
            .subscribe(
                data -> System.out.println("# emitted data: %s".formatted(data)),
                error -> {
                    System.out.println("error.getMessage() = " + error.getMessage());
                },
                () -> System.out.println("# onComplete signal")
            );

        System.out.println();
    }
}
