package com.spring.reactorbase;

import reactor.core.publisher.Flux;

public class Example5 {

    public static void main(String[] args) {
        Flux.just("Hello", "Reactor")
            .map(data -> data.toLowerCase())
            .subscribe(System.out::println);
    }
}
