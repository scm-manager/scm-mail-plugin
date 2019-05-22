/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.mail.internal;


import com.google.inject.Inject;
import org.apache.shiro.SecurityUtils;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.user.User;
import sonia.scm.util.ValidationUtil;

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
@Path("v2/plugins/mail")
public class MailConfigurationResource {

  private MailService mailService;
  private MailContext context;
  private MailConfigurationMapper mapper;

  @Inject
  public MailConfigurationResource(MailService mailService, MailContext context, MailConfigurationMapper mapper) {
    this.mailService = mailService;
    this.context = context;
    this.mapper = mapper;
  }

  @POST
  @Path("test")
  @Consumes(MediaType.APPLICATION_JSON)
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
  public void storeConfiguration(@Context UriInfo uriInfo, MailConfigurationDto mailConfigurationDto) {
    ConfigurationPermissions.write("mail").check();
    synchronized (context) {
      context.store(mapper.using(uriInfo).map(mailConfigurationDto));
    }
  }

  @PUT
  @Path("config")
  @Consumes(MediaType.APPLICATION_JSON)
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
  public MailConfigurationDto getConfiguration(@Context UriInfo uriInfo) {
    ConfigurationPermissions.read("mail").check();

    return mapper.using(uriInfo).map(context.getConfiguration());
  }

  @GET
  @Path("user-config")
  @Produces(MediaType.APPLICATION_JSON)
  public UserMailConfigurationDto getUserConfiguration(@Context UriInfo uriInfo) {
    User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    return mapper.using(uriInfo).map(context
      .getUserConfiguration(currentUser.getId())
      .orElse(new UserMailConfiguration()));
  }

  @PUT
  @Path("user-config")
  @Consumes(MediaType.APPLICATION_JSON)
  public void storeUserConfiguration(@Context UriInfo uriInfo, UserMailConfigurationDto userMailConfigurationDto) {
    User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    synchronized (context) {
      context.store(currentUser.getId(), mapper.using(uriInfo).map(userMailConfigurationDto));
    }
  }
}
