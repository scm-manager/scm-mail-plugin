/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.mail.internal;


import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.security.Authentications;
import sonia.scm.user.User;
import sonia.scm.util.ValidationUtil;
import sonia.scm.web.VndMediaType;

/**
 * @author Sebastian Sdorra
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Mail Plugin", description = "Mail plugin provided endpoints")
})
@Path("v2/plugins/mail")
public class MailConfigurationResource {

  private final MailService mailService;
  private final MailContext context;
  private final MailConfigurationMapper mapper;

  @Inject
  public MailConfigurationResource(MailService mailService, MailContext context, MailConfigurationMapper mapper) {
    this.mailService = mailService;
    this.context = context;
    this.mapper = mapper;
  }

  @POST
  @Path("test")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Test mail configuration", description = "Tests the mail configuration and send an email to the given recipient", tags = "Mail Plugin")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response sendTestMessage(@Context UriInfo uriInfo, MailConfigurationDto mailConfigurationDto,
                                  @QueryParam("to") String to)
    throws MailSendBatchException {
    ConfigurationPermissions.write("mail").check();

    MailConfiguration configuration = mapper.using(uriInfo).map(mailConfigurationDto);
    recoverOriginalPasswordIfNotChanged(configuration);
    if (configuration.isValid() && ValidationUtil.isMailAddressValid(to)) {

      mailService.emailTemplateBuilder()
        .withConfiguration(configuration)
        .toAddress(to)
        .withSubject("Test Message from SCM-Manager")
        .withTemplate("sonia/scm/mail/test.mustache", MailTemplateType.MARKDOWN_HTML)
        .andModel(configuration)
        .send();

      return Response.noContent().build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @POST
  @Path("config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Create mail configuration", description = "Creates new mail configuration", tags = "Mail Plugin")
  @ApiResponse(responseCode = "204", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void storeConfiguration(@Context UriInfo uriInfo, MailConfigurationDto mailConfigurationDto) {
    ConfigurationPermissions.write("mail").check();
    MailConfiguration newConfiguration = mapper.using(uriInfo).map(mailConfigurationDto);
    recoverOriginalPasswordIfNotChanged(newConfiguration);
    synchronized (context) {
      context.store(newConfiguration);
    }
  }

  @PUT
  @Path("config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Updates mail configuration", description = "Modifies the mail configuration", tags = "Mail Plugin")
  @ApiResponse(responseCode = "204", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void updateConfiguration(@Context UriInfo uriInfo, MailConfigurationDto mailConfigurationDto) {
    ConfigurationPermissions.write("mail").check();
    MailConfiguration mailConfiguration = mapper.using(uriInfo).map(mailConfigurationDto);
    recoverOriginalPasswordIfNotChanged(mailConfiguration);
    synchronized (context) {
      context.store(mailConfiguration);
    }
  }

  private void recoverOriginalPasswordIfNotChanged(MailConfiguration newConfiguration) {
    if (newConfiguration.getPassword() == null) {
      newConfiguration.setPassword(context.getConfiguration().getPassword());
    }
  }

  @GET
  @Path("config")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get mail configuration", description = "Returns the mail configuration", tags = "Mail Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = MailConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public MailConfigurationDto getConfiguration(@Context UriInfo uriInfo) {
    ConfigurationPermissions.read("mail").check();

    return mapper.using(uriInfo).map(context.getConfiguration());
  }

  @GET
  @Path("user-config")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get user mail configuration", description = "Returns the user-specific mail configuration", tags = "Mail Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = UserMailConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public UserMailConfigurationDto getUserConfiguration(@Context UriInfo uriInfo) {
    if (Authentications.isAuthenticatedSubjectAnonymous()) {
      throw new AuthorizationException("anonymous may not read the mail configuration");
    }

    User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    return mapper.using(uriInfo).map(context
      .getUserConfiguration(currentUser.getId())
      .orElse(new UserMailConfiguration()));
  }

  @PUT
  @Path("user-config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update user mail configuration", description = "Modifies the user-specific mail configuration", tags = "Mail Plugin")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void storeUserConfiguration(@Context UriInfo uriInfo, UserMailConfigurationDto userMailConfigurationDto) {
    if (Authentications.isAuthenticatedSubjectAnonymous()) {
      throw new AuthorizationException("anonymous may not change the mail configuration");
    }

    User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    synchronized (context) {
      context.store(currentUser.getId(), mapper.using(uriInfo).map(userMailConfigurationDto));
    }
  }

  @GET
  @Path("topics")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get available topics", description = "Returns the available topics that users can subscribe to", tags = "Mail Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = TopicCollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public TopicCollectionDto getTopics(@Context UriInfo uriInfo) {
    return mapper.using(uriInfo).map(context.availableTopics());
  }
}
