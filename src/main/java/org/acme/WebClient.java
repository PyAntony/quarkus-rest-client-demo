package org.acme;

import io.smallrye.mutiny.Uni;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface WebClient {
    @GET
    @Path("/{endpoint}")
    @Consumes(MediaType.TEXT_PLAIN)
    Uni<String> getByEndpoint(@PathParam("endpoint") String endpoint);
}
