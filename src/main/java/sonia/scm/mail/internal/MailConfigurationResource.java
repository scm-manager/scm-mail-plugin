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

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;

import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;

//~--- JDK imports ------------------------------------------------------------

import javax.mail.Message.RecipientType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("v2/plugins/mail")
public class MailConfigurationResource {

  MailConfigurationMapper mapper;

  @Inject
  public MailConfigurationResource(MailService mailService, MailContext context, MailConfigurationMapper mapper) {
    this.mailService = mailService;
    this.context = context;
    this.mapper = mapper;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param configuration
   * @param to
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  @POST
  @Path("test")
  @Consumes(MediaType.APPLICATION_JSON)
  public void sendTestMessage(MailConfiguration configuration,
    @QueryParam("to") String to)
    throws MailException, MailSendBatchException
  {
    ConfigurationPermissions.write("mail").check();

    Email email = new Email();

    email.addRecipient(null, to, RecipientType.TO);
    email.setSubject("Test Message from SCM-Manager");
    email.setText("Test Message");
    mailService.send(configuration, email);
  }

  @POST
  @Path("config")
  @Consumes(MediaType.APPLICATION_JSON)
  public synchronized void storeConfiguration(@Context UriInfo uriInfo, MailConfigurationDto mailConfigurationDto) {
    ConfigurationPermissions.write("mail").check();
    context.store(mapper.using(uriInfo).map(mailConfigurationDto));
  }

  @PUT
  @Path("config")
  @Consumes(MediaType.APPLICATION_JSON)
  public synchronized void updateConfiguration(@Context UriInfo uriInfo, MailConfigurationDto mailConfigurationDto) {
    ConfigurationPermissions.write("mail").check();
    MailConfiguration mailConfiguration = mapper.using(uriInfo).map(mailConfigurationDto);
    context.store(mailConfiguration);
  }


  @GET
  @Path("config")
  @Produces(MediaType.APPLICATION_JSON)
  public MailConfigurationDto getConfiguration(@Context UriInfo uriInfo) {
    ConfigurationPermissions.read("mail").check();

    // TODO add _links.update if the user has write permissions
//    return context.getConfiguration();
    return mapper.using(uriInfo).map(context.getConfiguration());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private MailContext context;

  /** Field description */
  private MailService mailService;
}
