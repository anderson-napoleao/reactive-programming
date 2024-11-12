package com.example;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Fruit {
    private String name;
    private BigDecimal price;

    public Fruit(String name, double price) {
        this.name = name;
        this.price = BigDecimal.valueOf(price).setScale(2, RoundingMode.CEILING);
    }
}