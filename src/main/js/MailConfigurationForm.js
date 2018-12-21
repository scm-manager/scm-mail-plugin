//@flow
import React from "react";
import { InputField } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import MailConfigurationTest from "./MailConfigurationTest";
import type { MailConfiguration } from "./MailConfiguration";

type Props = {
  initialConfiguration: MailConfiguration,
  readOnly: boolean,
  onConfigurationChange: (MailConfiguration, boolean) => void,

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
    const { host, from, port } = this.state;
    return !!host && !!from && port > 0;
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

  render() {
    const fields = [
      "host",
      "port",
      "from",
      "username",
      "subjectPrefix",
      "transportStrategy"
    ].map(name => {
      return this.renderInputField(name);
    });

    return (
      <>
        {fields}
        <MailConfigurationTest configuration={this.state} />
      </>
    );
  }
}

export default translate("plugins")(MailConfigurationForm);
