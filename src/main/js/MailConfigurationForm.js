//@flow
import React from "react";
import { DropDown, InputField } from "@scm-manager/ui-components";
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

type State = MailConfiguration;

class MailConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  isStateValid = () => {
    const { host, from, port, transportStrategy } = this.state;
    return !!host && !!from && port > 0 && transportStrategy !== "";
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

  renderPasswordInpuField = () => {
    const { t } = this.props;
    return (
      <InputField
        type="password"
        name="password"
        label={t("scm-mail-plugin.form.password")}
        onChange={this.configChangeHandler}
      />
    );
  };

  renderTransportStrategyDropDown = () => {
    const { t } = this.props;
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
          />
        </div>
      </div>
    );
  };

  render() {
    const fields = ["host", "port", "from", "username"].map(name => {
      return this.renderInputField(name);
    });

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
