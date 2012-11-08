/* *
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


Ext.ns("Sonia.mail");


Sonia.mail.GlobalConfigPanel = Ext.extend(Sonia.config.ConfigForm, {
  
  titleText: 'Mail Configuration',
  
  hostText: 'Host',
  portText: 'Port',
  usernameText: 'Username',
  passwordText: 'Password',
  fromText: 'From',
  transportStrategyText: 'Transport Strategy',
  subjectPrefixText: 'Subject Prefix',
  testConfigurationText: 'Test Configuration',
  
  hostHelpText: 'Hostname of the SMTP server.',
  portHelpText: 'Port of the SMTP server.',
  usernameHelpText: 'Username used to access the SMTP server.',
  passwordHelpText: 'Password used to access the SMTP server.',
  fromHelpText: 'From addresse for notifications.',
  transportStrategyHelpText: 'Transport strategy setting for the SMTP server.',
  subjectPrefixHelpText: 'Subjct prefix for notifications.',
  testConfigurationHelpText: 'Test SMTP Server Configuration',
  
  // dialog
  testMailConfigurationTitle: 'Test Mail Configuration',
  testMailConfigurationLable: 'Address:',
  
  initComponent: function(){
    var config = {
      title: this.titleText,
      items: [{
        xtype : 'textfield',
        name: 'host',
        allowBlank : false,
        fieldLabel: this.hostText,
        helpText: this.hostHelpText
      },{
        xtype : 'textfield',
        name: 'port',
        allowBlank : false,
        fieldLabel: this.portText,
        helpText: this.portHelpText
      },{
        xtype : 'textfield',
        name: 'username',
        allowBlank : true,
        fieldLabel: this.usernameText,
        helpText: this.usernameHelpText
      },{
        xtype : 'textfield',
        name: 'password',
        inputType: 'password',
        allowBlank : true,
        fieldLabel: this.passwordText,
        helpText: this.passwordHelpText
      },{
        xtype : 'textfield',
        name: 'from',
        vtype: 'email',
        allowBlank : false,
        fieldLabel: this.fromText,
        helpText: this.fromHelpText
      },{
        xtype : 'combo',
        name: 'transport-strategy',
        fieldLabel: this.transportStrategyText,
        helpText: this.transportStrategyHelpText,
        valueField: 'cs',
        displayField: 'cs',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        mode: 'local',
        store: new Ext.data.SimpleStore({
          fields: ['cs'],
          data: [
            ['SMTP_PLAIN'],
            ['SMTP_SSL'],
            ['SMTP_TLS']
          ]
        })
      },{
        xtype : 'textfield',
        name: 'subject-prefix',
        allowBlank : true,
        fieldLabel: this.subjectPrefixText,
        helpText: this.subjectPrefixHelpText
      },{
        xtype: 'button',
        text: this.testConfigurationText,
        fieldLabel: this.testConfigurationHelpText,
        scope: this,
        handler: function(){
          Ext.MessageBox.prompt(
            this.testMailConfigurationTitle,
            this.testMailConfigurationLable,
            this.sendTestMail,
            this
          );
        }
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.mail.GlobalConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  sendTestMail: function(btn, to){
    var values = this.getForm().getValues();
    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'plugins/mail/test?to=' + to,
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
      }
    });    
  },

  onSubmit: function(values){
    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'plugins/mail/config.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
      }
    });
  },

  onLoad: function(el){
    var tid = setTimeout( function(){
      el.mask(this.loadingText);
    }, 100);
    Ext.Ajax.request({
      url: restUrl + 'plugins/mail/config.json',
      method: 'GET',
      scope: this,
      disableCaching: true,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        clearTimeout(tid);
        el.unmask();
      },
      failure: function(){
        el.unmask();
        clearTimeout(tid);
        alert('failure');
      }
    });
  }

});

Ext.reg("mailGlobalConfigPanel", Sonia.mail.GlobalConfigPanel);

// register global config panel
registerGeneralConfigPanel({
  id: 'mailGlobalConfigPanel',
  xtype: 'mailGlobalConfigPanel'
});