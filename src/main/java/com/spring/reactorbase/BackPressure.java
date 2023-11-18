package com.spring.reactorbase;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class BackPressure {

    public static void main(String[] args) throws InterruptedException{
//        setRequestSize();
//        strategy_ERROR();
//        strategy_DROP();
//        strategy_LATEST();
//        strategy_BUFFER_DROP_LATEST();
        strategy_BUFFER_DROP_OLDEST();
    }

    private static void setRequestSize() {
        System.out.println("BackPressure.setRequestSize");
        Flux.range(1, 5)
            .doOnRequest(data -> log.info("# doOnRequest: {}", data))
            .subscribe(new BaseSubscriber<Integer>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    request(1);
                }

                @SneakyThrows
                @Override
                protected void hookOnNext(Integer value) {
                    Thread.sleep(2000L);
                    log.info("# hookOnNext: {}", value);
                    request(1);
                }
            });
        System.out.println();
    }

    /**
     * ERROR 전략은 IllegalStateException을 발생시키고 sequence를 종료한다.
     */
    private static void strategy_ERROR() throws InterruptedException{
        System.out.println("BackPressure.strategyERROR");
        // 0.001s 단위로 데이터를 emit
        Flux.interval(Duration.ofMillis(1L))
            .onBackpressureError()
            .doOnNext(data -> log.info("# doOnNext: {}", data))
            .publishOn(Schedulers.parallel())
            // subscriber의 처리속도를 0.005s 설정
            .subscribe(data -> {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {}
                    log.info("# onNext: {}", data);
                },
                error -> log.error("# onError"));

        Thread.sleep(2000L);
        System.out.println();
    }

    /**
     * DROP 전략은 버퍼 밖에서 대기중인 데이터 중 가장 먼저 emit된 데이터를 drop한다.
     */
    private static void strategy_DROP() throws InterruptedException {
        System.out.println("BackPressure.strategyDROP");
        Flux.interval(Duration.ofMillis(1L))
            .onBackpressureDrop(dropped -> log.info("# dropped: {}", dropped))
            .publishOn(Schedulers.parallel())
            .subscribe(data -> {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {
                    }
                    log.info("# onNext: {}", data);
                },
                error -> log.error("# onError", error));

        Thread.sleep(2000L);
        System.out.println();
    }

    /**
     * LATEST 전략은 버퍼 밖에서 대기중인 데이터 중 가장 나중에 emit된 데이터만 남겨두고 나머지를 drop한다.
     */
    private static void strategy_LATEST() throws InterruptedException {
        System.out.println("BackPressure.strategyLATEST");
        Flux.interval(Duration.ofMillis(1L))
            .onBackpressureLatest()
            .publishOn(Schedulers.parallel())
            .subscribe(data -> {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {
                    }
                    log.info("# onNext: {}", data);
                },
                error -> log.error("# onError", error));
        Thread.sleep(2000L);
        System.out.println();
    }

    /**
     * BUFFER_DROP_LATEST 전략은 버퍼 안에서 가장 나중에 채워진 데이터를 drop한다.
     */
    private static void strategy_BUFFER_DROP_LATEST() throws InterruptedException {
        System.out.println("BackPressure.strategy_BUFFER_DROP_LATEST");
        Flux.interval(Duration.ofMillis(300L))
            .doOnNext(data -> log.info("# emitted by original Flux: {}", data))
            // 버퍼의 최대 용량을 2로 설정한다.
            .onBackpressureBuffer(2,
                // 버퍼 오버플로우가 발생했을 때, 후처리를 진행한다.
                dropped -> log.info("** Overflow and Dropped: {} **", dropped),
                // 전략 지정
                BufferOverflowStrategy.DROP_LATEST)
            .doOnNext(data -> log.info("[ # emitted by Buffer: {} ]", data))
            .publishOn(Schedulers.parallel(), false, 1)
            .subscribe(data -> {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                    }
                    log.info("# onNext: {}", data);
                },
                error -> log.error("# onError", error));
        Thread.sleep(3000L);
        System.out.println();;
    }

    /**
     * BUFFER_DROP_OLDEST 전략은 버퍼 안에서 가장 처음에 채워진 데이터를 drop한다.
     */
    private static void strategy_BUFFER_DROP_OLDEST() throws InterruptedException {
        System.out.println("BackPressure.strategy_BUFFER_DROP_OLDEST");
        Flux.interval(Duration.ofMillis(300L))
            .doOnNext(data -> log.info("# emitted by original Flux: {}", data))
            // 버퍼의 최대 용량을 2로 설정한다.
            .onBackpressureBuffer(2,
                // 버퍼 오버플로우가 발생했을 때, 후처리를 진행한다.
                dropped -> log.info("** Overflow and Dropped: {} **", dropped),
                // 전략 지정
                BufferOverflowStrategy.DROP_OLDEST)
            .doOnNext(data -> log.info("[ # emitted by Buffer: {} ]", data))
            .publishOn(Schedulers.parallel(), false, 1)
            .subscribe(data -> {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                    }
                    log.info("# onNext: {}", data);
                },
                error -> log.error("# onError", error));
        Thread.sleep(2500);
        System.out.println();;
    }
}
