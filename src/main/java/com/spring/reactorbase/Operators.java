package com.spring.reactorbase;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class Operators {

    public static void main(String[] args) throws InterruptedException {
        justOrEmpty();
        fromIterable();
        fromStream();
        range();
        defer();
        defer2();
    }

    private static void justOrEmpty() {
        System.out.println("Operators.justOrEmpty");
        Mono
            // just operator와 달리 null을 전달해도 NPE가 발생하지 않는다.
            .justOrEmpty(null)
            .subscribe(data -> log.info("# onNext data: {}", data),
                error -> log.info("# onError"),
                () -> log.info("# complete"));
        System.out.println();
    }

    private static void fromIterable() {
        System.out.println("Operators.fromIterable");
        Flux
            .fromIterable(SampleData.coins)
            .subscribe(coin -> log.info("coin Name: {}, now price: {}", coin.getName(), coin.getPrice()));
        System.out.println();
    }

    public static class SampleData {
        public static List<Coin> coins = List.of(
            new Coin("BTC", 52000000),
            new Coin("ETH", 172000),
            new Coin("XRP", 533),
            new Coin("ICX", 4080),
            new Coin("BCH", 558000)
        );

        @Data
        public static class Coin {
            private String name;
            private int price;

            public Coin(String name, int price) {
                this.name = name;
                this.price = price;
            }
        }
    }

    private static void fromStream() {
        System.out.println("Operators.fromStream");
        Flux
            .fromStream(() -> SampleData.coins.stream())
            .filter(coin -> coin.getName().equals("BTC") || coin.getName().equals("ETH"))
            .subscribe(data -> log.info("{}", data));
        System.out.println();
    }

    //특정 횟수만큼 작업을 처리하고자 할 경우에 주로 사용한다.
    private static void range() {
        System.out.println("Operators.range");
        Flux
            .range(5, 10)
            .subscribe(data -> log.info("# onNext: {}", data));
        System.out.println();
    }

    /**
     * operator를 선언한 시점이 아니라, 구독하는 시점에 데이터를 emit한다.
     */
    private static void defer() throws InterruptedException {
        System.out.println("Operators.defer");

        log.info("# start: {}", LocalDateTime.now());
        Mono<LocalDateTime> justMono = Mono.just(LocalDateTime.now()); // Hot publisher
        Mono<LocalDateTime> deferMono = Mono.defer(() -> Mono.just(LocalDateTime.now())); // delay emitting

        Thread.sleep(2000);

        justMono.subscribe(data -> log.info("# onNext just1: {}", data)); // data replay
        deferMono.subscribe(data -> log.info("# onNext defer1: {}", data));

        Thread.sleep(2000L);

        justMono.subscribe(data -> log.info("# onNext just2: {}", data));
        deferMono.subscribe(data -> log.info("# onNext defer2: {}", data));

        System.out.println();
    }

    private static void defer2() throws InterruptedException {
        System.out.println("Operators.defer2");

        Supplier<Mono<String>> sayDefault = () -> {
            log.info("# Say Hi");
            return Mono.just("Hi");
        };

        log.info("# start: {}", LocalDateTime.now());
        Mono
//            .justOrEmpty(null)
            .just("Hello")
//            .delayElement(Duration.ofSeconds(3))
//            .switchIfEmpty(sayDefault.get())

            //defer로 래핑함으로써 메서드의 불필요한 호출을 방지할 수 있다.
            .switchIfEmpty(Mono.defer(() -> sayDefault.get()))
            .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(3500);

        System.out.println();
    }
}
