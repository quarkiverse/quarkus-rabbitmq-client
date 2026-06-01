package io.quarkiverse.rabbitmq.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class RabbitMQResource {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQResource.class);

    @Inject
    RabbitMQSupport rabbitMQSupport;

    @POST
    @Path("/exchange")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createExchange(@FormParam("name") String name, @Context UriInfo uri) {
        try {
            rabbitMQSupport.declareExchange(name);
        } catch (Exception e) {
            log.error("Failed to create exchange: {}", name, e);
            return Response.serverError().build();
        }
        return Response.created(uri.getRequestUriBuilder().path(name).build()).build();
    }

    @POST
    @Path("/exchange/{name}/queue")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createQueue(@PathParam("name") String exchange, @FormParam("name") String name, @Context UriInfo uri) {
        try {
            rabbitMQSupport.declareQueue(name, exchange);
        } catch (Exception e) {
            log.error("Failed to create queue: {} on exchange: {}", name, exchange, e);
            return Response.serverError().build();
        }
        return Response.created(uri.getRequestUriBuilder().path(name).build()).build();
    }

    @POST
    @Path("/exchange/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendMessage(@PathParam("name") String name, @FormParam("message") String msg) {
        try {
            rabbitMQSupport.send(name, msg);
        } catch (Exception e) {
            log.error("Failed to create send to exchange: {}", name, e);
            return Response.serverError().build();
        }
        return Response.accepted().build();
    }

    @GET
    @Path("/queue/{queue}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessage(@PathParam("queue") String queue) {
        try {
            return Response.ok(rabbitMQSupport.getConsumedMessages(queue)).build();
        } catch (Exception e) {
            log.error("Failed to get received messages for queue: {}", queue, e);
            return Response.serverError().build();
        }
    }

}
