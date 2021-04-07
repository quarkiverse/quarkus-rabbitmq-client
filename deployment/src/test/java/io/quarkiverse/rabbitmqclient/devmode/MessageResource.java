package io.quarkiverse.rabbitmqclient.devmode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
@Path("/")
public class MessageResource {

    @Inject
    MessageService messageService;

    @Inject
    ManagedExecutor managedExecutor;

    @Path("/messages")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> messages() {
        return messageService.getMessages();
    }

    @Path("/send")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public void send(@QueryParam("msg") String msg) {
        managedExecutor.runAsync(() -> {
            try {
                messageService.send("HELLO: " + msg);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}
