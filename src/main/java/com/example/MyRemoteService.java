package com.example;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;

@RegisterRestClient(baseUri = "http://localhost:8080/api/")
public interface MyRemoteService {

    @GET
    @Path("/random-error")
    Uni<String> getRandomError();

    @GET
    @Path("/fruits/{name}")
    Uni<String> getFruitName(@PathParam("name") String name);

    @GET
    @Path("/fruits/price")
    Uni<BigDecimal> getPrice();
}
