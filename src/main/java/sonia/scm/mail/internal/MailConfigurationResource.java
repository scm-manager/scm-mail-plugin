/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.mail.internal;


import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.SecurityUtils;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.user.User;
import sonia.scm.util.ValidationUtil;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
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
    synchronized (context) {
      context.store(mapper.using(uriInfo).map(mailConfigurationDto));
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
    synchronized (context) {
      context.store(mailConfiguration);
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
