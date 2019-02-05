//@flow
import React from "react";
import {
  DropDown,
  InputField,
  validation as validator
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import MailConfigurationTest from "./MailConfigurationTest";
import type { MailConfiguration } from "./MailConfiguration";

type Props = {
  initialConfiguration: MailConfiguration,
  readOnly: boolean,
  onConfigurationChange: (MailConfiguration, boolean) => void,
  transportStrategy: string,

  // context prop
  t: string => string
};

type State = MailConfiguration & {
  fromFieldChanged: boolean
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
    return (
      !!host &&
      !!from &&
      port > 0 &&
      transportStrategy !== "" &&
      validator.isMailValid(this.state["from"])
    );
  };

  configChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange({ ...this.state }, this.isStateValid())
    );
  };

  renderInputField = (name: string) => {
    const { t } = this.props;
    const readOnly = false;
    return (
      <InputField
        name={name}
        label={t("scm-mail-plugin.form." + name)}
        disabled={readOnly}
        value={this.state[name]}
        onChange={this.configChangeHandler}
      />
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
      <InputField
        name="from"
        label={t("scm-mail-plugin.form.from")}
        disabled={readOnly}
        value={this.state["from"]}
        onChange={(value: string, name: string) => {
          this.setState({ ...this.state, fromFieldChanged: true });
          this.configChangeHandler(value, name);
        }}
        validationError={this.fromFieldInvalid()}
        errorMessage={t("scm-mail-plugin.mailValidationError")}
      />
    );
  };

  renderPasswordInpuField = () => {
    const { readOnly, t } = this.props;
    return (
      <InputField
        type="password"
        name="password"
        label={t("scm-mail-plugin.form.password")}
        onChange={this.configChangeHandler}
        disabled={readOnly}
      />
    );
  };

  renderTransportStrategyDropDown = () => {
    const { readOnly, t } = this.props;
    return (
      <div className="field">
        <label className="label">
          {t("scm-mail-plugin.form.transportStrategy")}
        </label>
        <div className="control">
          <DropDown
            options={["SMTP_PLAIN", "SMTP_TLS", "SMTP_SSL"]}
            optionSelected={this.handleDropDownChange}
            preselectedOption={this.state.transportStrategy}
            disabled={readOnly}
          />
        </div>
      </div>
    );
  };

  render() {
    const fields = ["host", "port"].map(name => {
      return this.renderInputField(name);
    });

    fields.push(this.renderFromField());
    fields.push(this.renderInputField("username"));
    fields.push(this.renderPasswordInpuField());
    fields.push(this.renderInputField("subjectPrefix"));
    fields.push(this.renderTransportStrategyDropDown());

    return (
      <>
        {fields}
        <MailConfigurationTest configuration={this.state} />
      </>
    );
  }

  handleDropDownChange = (selection: string) => {
    console.log("change");
    this.setState({ ...this.state, transportStrategy: selection });

    this.configChangeHandler(selection, "transportStrategy");
  };
}

export default translate("plugins")(MailConfigurationForm);
