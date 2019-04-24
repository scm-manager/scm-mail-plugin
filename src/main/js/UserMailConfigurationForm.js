//@flow
import React from "react";
import {
  DropDown
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { UserMailConfiguration} from "./MailConfiguration";

type Props = {
  initialConfiguration: UserMailConfiguration,
  readOnly: boolean,
  onConfigurationChange: (UserMailConfiguration, boolean) => void,

  // context prop
  t: string => string
};

type State = UserMailConfiguration ;

class UserMailConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  configChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange({ ...this.state }, true)
    );
  };

  render() {
    const { readOnly, t } = this.props;
    return (
      <div className="field">
        <label className="label">
          {t("scm-mail-plugin.form.language")}
        </label>
        <div className="control">
          <DropDown
            options={[t("scm-mail-plugin.language.de"), t("scm-mail-plugin.language.en")]}
            optionValues={["de", "en"]}
            preselectedOption={this.state.language}
            optionSelected= {selection => {
              this.setState({ ...this.state, language: selection });
              this.configChangeHandler(selection, "language");
            }}
            disabled={readOnly}
          />
        </div>
      </div>
    );
  }
}

export default translate("plugins")(UserMailConfigurationForm);
