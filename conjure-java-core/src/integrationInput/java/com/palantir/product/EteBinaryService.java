package com.palantir.product;

import com.palantir.conjure.java.lib.internal.ClientEndpoint;
import com.palantir.tokens.auth.AuthHeader;
import java.io.InputStream;
import java.util.Optional;
import javax.annotation.processing.Generated;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
@Generated("com.palantir.conjure.java.services.JerseyServiceGenerator")
public interface EteBinaryService {
    @POST
    @Path("binary")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @ClientEndpoint(method = "POST", path = "/binary")
    StreamingOutput postBinary(@HeaderParam("Authorization") @NotNull AuthHeader authHeader, @NotNull InputStream body);

    @POST
    @Path("binary/throws")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @ClientEndpoint(method = "POST", path = "/binary/throws")
    StreamingOutput postBinaryThrows(
            @HeaderParam("Authorization") @NotNull AuthHeader authHeader,
            @QueryParam("bytesToRead") int bytesToRead,
            @NotNull InputStream body);

    @GET
    @Path("binary/optional/present")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ClientEndpoint(method = "GET", path = "/binary/optional/present")
    Optional<StreamingOutput> getOptionalBinaryPresent(@HeaderParam("Authorization") @NotNull AuthHeader authHeader);

    @GET
    @Path("binary/optional/empty")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ClientEndpoint(method = "GET", path = "/binary/optional/empty")
    Optional<StreamingOutput> getOptionalBinaryEmpty(@HeaderParam("Authorization") @NotNull AuthHeader authHeader);

    /**
     * Throws an exception after partially writing a binary response.
     */
    @GET
    @Path("binary/failure")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ClientEndpoint(method = "GET", path = "/binary/failure")
    StreamingOutput getBinaryFailure(
            @HeaderParam("Authorization") @NotNull AuthHeader authHeader, @QueryParam("numBytes") int numBytes);

    @GET
    @Path("binary/aliased")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ClientEndpoint(method = "GET", path = "/binary/aliased")
    Optional<StreamingOutput> getAliased(@HeaderParam("Authorization") @NotNull AuthHeader authHeader);
}
