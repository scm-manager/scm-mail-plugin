//@flow
import React from "react";
import { InputField } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Configuration = {
  host: string
};

type Props = {
  initialConfiguration: Configuration,
  readOnly: boolean,
  onConfigurationChange: (Configuration, boolean) => void,

  // context prop
  t: (string) => string
}

type State = Configuration;

class MailConfigurationForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  configChangeHandler = (value: string, name: string) => {
    this.setState({
      [name]: value
    }, () => this.props.onConfigurationChange({...this.state}, true));
  };

  renderInputField = (name: string) => {
    const { t } = this.props;
    const readOnly = false;
    return <InputField name={name}
                      label={ t("scm-mail-plugin.form." + name) }
                      disabled={readOnly}
                      value={this.state[name]}
                      onChange={this.configChangeHandler}
           />;
  };

  render() {
    const fields = [ "host", "port", "from", "username", "subject-prefix", "transport-strategy" ].map((name) => {
      return this.renderInputField(name);
    });

    return (
      <>
        { fields }
      </>
    );
  }

}

export default translate("plugins")(MailConfigurationForm);
