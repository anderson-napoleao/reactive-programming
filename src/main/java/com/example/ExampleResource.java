package com.example;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Path("/api")
public class ExampleResource {

    @Inject
    @RestClient
    MyRemoteService myRemoteService;

    @GET
    @Path("/products")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Product>> products() {
        System.out.println(Thread.currentThread().getName() + "listing products");
        return Product.<Product>listAll()
                .onItem().transformToMulti(list -> Multi.createFrom().iterable(list))
                .map(this::capitalizeAllFirstLetterOfName).collect().asList();
    }

    private Product capitalizeAllFirstLetterOfName(Product p) {
        p.setName(capitalizeWords(p.getName()));
        p.setDescription(capitalizeWords(p.getDescription()));
        return p;
    }

    @GET
    @Path("/fruits/{name}/price")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Fruit> fruit(@PathParam("name") String name) {
        System.out.println(Thread.currentThread().getName() + "getting fruit " + name);
        var uni = myRemoteService.getFruitName(name)
                .onItem().transformToUni(s ->
                        myRemoteService.getPrice().onItem().transform(p -> new Fruit(s, p))
                );

        return uni;
    }

    @GET
    @Path("/fruits/{name}/price/async")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Fruit> fruitAsync(@PathParam("name") String name) {
        System.out.println(Thread.currentThread().getName() + "getting fruit async" + name);
        var uniName = myRemoteService.getFruitName(name);
        var uniPrice = myRemoteService.getPrice();

        var combined = Uni.combine().all().unis(uniName, uniPrice).asTuple()
                .onItem().transform(objects -> new Fruit(objects.getItem1(), objects.getItem2()));

        return combined;
    }

    @POST
    @Path("/products")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @WithTransaction
    public Uni<String> saveProducts(List<Product> products) {
        System.out.println(Thread.currentThread().getName() + "saving products");
        return Product.persist(products)
                .replaceWith("Products saved successfully")
                .onFailure().recoverWithItem(failure -> "failed to save products" + failure.getMessage());
    }

    @GET
    @Path("/random-error")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> randomError() {
        System.out.println(Thread.currentThread().getName() + "causing random error");
        if(ThreadLocalRandom.current().nextBoolean()){
           return Uni.createFrom().failure(new RuntimeException("Falha aleatória"));
        }
        return Uni.createFrom().item("Hello from Quarkus REST");
    }

    @GET
    @Path("/retry")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> retry() {
        System.out.println(Thread.currentThread().getName() + "recovering from random error");
        return myRemoteService.getRandomError()
                .onFailure()
                .retry()
                .withBackOff(Duration.ofMillis(100))
                .atMost(8).log();
    }

    @GET
    @Path("/fruits/{name}")
    public Uni<String> getFruitName(@PathParam("name") String name){
        return Uni.createFrom().item(name)
                .onItem().delayIt().by(Duration.ofMillis(10000));
    }

    @GET
    @Path("/fruits/price")
    public Uni<BigDecimal> getPrice(){
        double price = new Random().nextDouble() * 10;
        return Uni.createFrom().item(BigDecimal.valueOf(price).setScale(2, RoundingMode.CEILING))
                .onItem().delayIt().by(Duration.ofMillis(5000));
    }


    public static String capitalizeWords(String str) {
        System.out.println(Thread.currentThread().getName() + "capitalizing words");
        List<String> palavras = Arrays.asList(str.split(" ")); 
        return palavras.stream() .map(ExampleResource::capitalize) 
                .collect(Collectors.joining(" ")); 
    } 
    
    private static String capitalize(String palavra) {
        System.out.println(Thread.currentThread().getName() + "applying capitalization to " + palavra);
        if (palavra == null || palavra.isEmpty()) { 
            return palavra; 
        } 
        return palavra.substring(0, 1).toUpperCase() + palavra.substring(1).toLowerCase(); 
    }
}
