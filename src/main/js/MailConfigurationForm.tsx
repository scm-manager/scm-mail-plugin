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

import React from "react";
import { Checkbox, DropDown, InputField, validation as validator } from "@scm-manager/ui-components";
import { withTranslation, WithTranslation } from "react-i18next";
import MailConfigurationTest from "./MailConfigurationTest";
import { MailConfiguration } from "./MailConfiguration";

type Props = WithTranslation & {
  initialConfiguration: MailConfiguration;
  readOnly: boolean;
  onConfigurationChange: (p1: MailConfiguration, p2: boolean) => void;
  transportStrategy: string;
};

type State = MailConfiguration & {
  fromFieldChanged: boolean;
};

class MailConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration,
      fromFieldChanged: false
    };
  }

  isStateValid = () => {
    const { host, from, port, transportStrategy } = this.state;
    return !!host && !!from && port > 0 && transportStrategy !== "" && validator.isMailValid(this.state["from"]);
  };

  configChangeHandler = (value: string | boolean, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          this.isStateValid()
        )
    );
  };

  renderInputField = (name: string) => {
    const { t } = this.props;
    const readOnly = false;
    return (
      <div className="column is-half">
        <InputField
          name={name}
          label={t("scm-mail-plugin.form." + name)}
          disabled={readOnly}
          value={this.state[name]}
          onChange={this.configChangeHandler}
        />
      </div>
    );
  };

  fromFieldInvalid = () => {
    if (!this.state.fromFieldChanged && !this.state["from"]) {
      return false;
    } else {
      return !validator.isMailValid(this.state["from"]);
    }
  };

  renderFromField = () => {
    const { t } = this.props;
    const readOnly = false;
    return (
      <div className="column is-half">
        <InputField
          name="from"
          label={t("scm-mail-plugin.form.from")}
          disabled={readOnly}
          value={this.state["from"]}
          onChange={(value: string, name: string) => {
            this.setState(prevState => ({
              ...prevState,
              fromFieldChanged: true
            }));
            this.configChangeHandler(value, name);
          }}
          validationError={this.fromFieldInvalid()}
          errorMessage={t("scm-mail-plugin.mailValidationError")}
        />
      </div>
    );
  };

  renderPasswordInpuField = () => {
    const { readOnly, t } = this.props;
    return (
      <div className="column is-half">
        <InputField
          type="password"
          name="password"
          label={t("scm-mail-plugin.form.password")}
          onChange={this.configChangeHandler}
          disabled={readOnly}
        />
      </div>
    );
  };

  renderTransportStrategyDropDown = () => {
    const { readOnly, t } = this.props;
    return (
      <div className="column is-half">
        <div className="field">
          <label className="label">{t("scm-mail-plugin.form.transportStrategy")}</label>
          <div className="control">
            <DropDown
              options={["SMTP", "SMTP_TLS", "SMTPS"]}
              optionSelected={this.handleDropDownChange}
              preselectedOption={this.state.transportStrategy}
              disabled={readOnly}
            />
          </div>
        </div>
      </div>
    );
  };

  renderLanguageDropDown = () => {
    const { readOnly, t } = this.props;
    return (
      <div className="column is-half">
        <div className="field">
          <label className="label">{t("scm-mail-plugin.form.language")}</label>
          <div className="control">
            <DropDown
              options={[t("scm-mail-plugin.language.de"), t("scm-mail-plugin.language.en")]}
              optionValues={["de", "en"]}
              preselectedOption={this.state.language}
              optionSelected={selection => {
                this.setState(prevState => ({
                  ...prevState,
                  language: selection
                }));
                this.configChangeHandler(selection, "language");
              }}
              disabled={readOnly}
            />
          </div>
        </div>
      </div>
    );
  };

  renderCheckboxForDefaultSender = () => {
    const { t } = this.props;
    return (
      <div className="column">
        <div className="field">
          <label className="label"> {t("scm-mail-plugin.form.defaultSender")} </label>
          <div className="control">
            <Checkbox
              label={t("scm-mail-plugin.form.defaultSenderCheck")}
              name="fromAddressAsSender"
              value={this.state["fromAddressAsSender"]}
              checked={this.state["fromAddressAsSender"]}
              onChange={(value: boolean, name: string) => {
                this.configChangeHandler(value, name);
              }}
            ></Checkbox>
          </div>
        </div>
      </div>
    );
  };

  render() {
    const fields = ["host", "port"].map(name => {
      return this.renderInputField(name);
    });

    fields.push(this.renderFromField());
    fields.push(this.renderInputField("subjectPrefix"));
    fields.push(this.renderInputField("username"));
    fields.push(this.renderPasswordInpuField());
    fields.push(this.renderLanguageDropDown());
    fields.push(this.renderTransportStrategyDropDown());
    fields.push(this.renderCheckboxForDefaultSender());

    return (
      <>
        <div className="columns is-multiline">{fields}</div>
        <MailConfigurationTest configuration={this.state} />
      </>
    );
  }

  handleDropDownChange = (selection: string) => {
    this.setState(prevState => ({
      ...prevState,
      transportStrategy: selection
    }));
    this.configChangeHandler(selection, "transportStrategy");
  };
}

export default withTranslation("plugins")(MailConfigurationForm);
