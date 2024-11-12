package com.example;

import io.smallrye.mutiny.Multi;

import java.util.Random;

public class MutinyExample {

    public static void main(String[] args) {
        Multi.createFrom().items("Banana", "Laranja", "Manga")
                .onItem().transform(fruit -> {
                    double price = new Random().nextDouble() * 10;
                    return new Fruit(fruit, price);
                })
                .onItem().transform(MutinyExample::nameToUppercase)
                .onSubscription().invoke(() -> System.out.println("Subscribed!"))
                .onCompletion().invoke(() -> System.out.println("Multi completed!"))
                .subscribe().with(System.out::println);
    }

    private static Fruit nameToUppercase(Fruit fruit) {
        fruit.setName(fruit.getName().toUpperCase());
        return fruit;
    }

}
