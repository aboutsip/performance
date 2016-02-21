package com.aboutsip.performance;

import com.aboutsip.performance.api.sipp.Rate;
import com.aboutsip.performance.api.sipp.SIPpTO;
import com.aboutsip.performance.core.sipp.SIPp;
import com.aboutsip.performance.core.sipp.SIPpManager;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Defines the main REST interface for manipulating SIPp instances on the same physical
 * machine as we are running on.
 */
@Path("/sipp")
@Produces(MediaType.APPLICATION_JSON)
public class SIPpResource {

    private final SIPpManager sippManager;

    public SIPpResource(final SIPpManager manager) {
        this.sippManager = manager;
    }

    // ************************************************************************
    // **************** Working with SIPp scenarios ***************************
    // ************************************************************************

    @GET
    @Path("scenarios")
    public String sayHello(@QueryParam("name") String name) {
        return name;
    }

    // ************************************************************************
    // **************** Working with SIPp instances ***************************
    // ************************************************************************

    @GET
    @Path("instances")
    public List<SIPp> listSIPpInstances() {
        return sippManager.getAllInstances();
    }

    @POST
    @Path("instances")
    public Response createNewInstance(@FormParam("FriendlyName") @DefaultValue("Default") final String friendlyName,
                                      @FormParam("Scenario") @DefaultValue("uac") final String scenario,
                                      @FormParam("Port") @DefaultValue("-1") final int port) {
        final SIPp.Builder builder = sippManager.newInstance();
        final SIPp sipp = builder.withFriendlyName(friendlyName).build();
        return Response.status(Response.Status.CREATED).entity(sipp).build();
    }

    @GET
    @Path("instances/{instance}")
    public SIPp getSIPpInstance(@PathParam("instance") final UUID instance) {
        return sippManager.getInstance(instance).orElseThrow(NotFoundException::new);
    }

    @POST
    @Path("instances/{instance}")
    public SIPp updateSIPpInstance(@PathParam("instance") final UUID instance) {
        return sippManager.getInstance(instance).orElseThrow(NotFoundException::new);
    }

    /**
     * Return information about the rate, which includes the target rate as well
     * as how much it actually is able to push right now. E.g., your target rate
     * may be 1000 CPS but it may only be able to push 895. Also, if the instance
     * isn't running just yet then the current rate will be zero (duh)
     *
     * @param instance
     * @return
     */
    @GET
    @Path("instances/{instance}/rate")
    public Rate getSIPpRate(@PathParam("instance") final UUID instance) {
        final SIPp sipp = sippManager.getInstance(instance).orElseThrow(NotFoundException::new);
        return null;
    }

    /**
     * Start the instance.
     *
     * @param instance
     * @return
     */
    @PUT
    @Path("instances/{instance}/start")
    public SIPp startSIPp(@PathParam("instance") final UUID instance) throws ExecutionException, InterruptedException {
        final SIPp sipp = sippManager.getInstance(instance).orElseThrow(NotFoundException::new);
        return sipp.start().get();
    }

    /**
     * Increase the current rate with +10. This corresponds to the '*' command
     * when use SIPp.
     *
     * @param instance
     * @return
     */
    @POST
    @Path("instances/{instance}/rate/increase10")
    public Rate increaseSIPpRate(@PathParam("instance") final UUID instance) {
        final SIPp sipp = sippManager.getInstance(instance).orElseThrow(NotFoundException::new);
        return null;
    }

    /**
     * Decrease the current rate with -10. This corresponds to the '/' command
     * when use SIPp.
     *
     * @param instance
     * @return the {@link Rate}
     */
    @POST
    @Path("instances/{instance}/rate/decrease10")
    public Rate decreaseSIPpRate(@PathParam("instance") final UUID instance) {
        final SIPp sipp = sippManager.getInstance(instance).orElseThrow(NotFoundException::new);
        return null;
    }

    /**
     * Set the target rate.
     *
     * @param instance
     * @return the {@link Rate}
     */
    @POST
    @Path("instances/{instance}/rate")
    public Rate setSIPpRate(@PathParam("instance") final UUID instance,
                            @FormParam("rate") final Integer target) {
        final int targetRate = Optional.ofNullable(target).orElseThrow(() -> new BadRequestException("You must specify the target rate (form parameter 'rate')"));
        final SIPp sipp = sippManager.getInstance(instance).orElseThrow(NotFoundException::new);
        return null;
    }

}
