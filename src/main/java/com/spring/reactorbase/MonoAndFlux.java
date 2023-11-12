package com.spring.reactorbase;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;

public class MonoAndFlux {

    public static void main(String[] args) {
        monoHelloReactor();
        monoEmpty();
        monoWorldTime();
        fluxJust();
        fluxFromArray();
        monoToFlux();
        fluxConcat();
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

    private static void monoHelloReactor() {
        System.out.println("Example6.helloReactor");
        Mono.just("Hello Reactor")
            .map(String::toUpperCase)
            .subscribe(System.out::println);
        System.out.println();
    }

    private static void monoWorldTime() {
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

        // non-blocking I/O 방식의 통신은 아니나, Mono를 활용하여 요청과 응답을 하나의 Operator 체인으로 처리할 수 있음
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

    private static void fluxJust() {
        System.out.println("Example6.fluxJust");
        Flux.just(6, 9, 13)
            .map(num -> num % 2)
            .subscribe(System.out::println);
        System.out.println();
    }

    private static void fluxFromArray() {
        System.out.println("Example6.fluxFromArray");
        Flux.fromArray(new Integer[]{3, 6, 7, 9})
            .filter(num -> num > 6)
            .map(num -> num * 2)
            .subscribe(System.out::println);
        System.out.println();
    }

    private static void monoToFlux() {
        System.out.println("Example6.monoToFlux");
        // 데이터 소스를 하나로 연결하여 새로운 flux publisher 생성
        Flux<String> flux = Mono.justOrEmpty("Steve")
            .concatWith(Mono.justOrEmpty("Jobs"));
        flux.subscribe(System.out::println);
        System.out.println();
    }

    private static void fluxConcat() {
        Flux
            // 여러 데이터 소스를 emit하는 flux를 생성
            .concat(
                Flux.just("Mercury", "Venus", "Earth"),
                Flux.just("Mars", "Jupiter", "Saturn"),
                Flux.just("Uranus", "Neptune", "Pluto")
            )
            // 여러 데이터 소스를 하나의 리스트로 병합
            // 결과로는 Mono가 생성
            .collectList()
            .subscribe(planets -> System.out.println("planets = " + planets));
    }
}
