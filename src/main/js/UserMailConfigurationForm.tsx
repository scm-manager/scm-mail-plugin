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
import React from "react";
import { DropDown } from "@scm-manager/ui-components";
import { withTranslation, WithTranslation } from "react-i18next";
import { UserMailConfiguration } from "./MailConfiguration";

type Props = WithTranslation & {
  initialConfiguration: UserMailConfiguration;
  readOnly: boolean;
  onConfigurationChange: (p1: UserMailConfiguration, p2: boolean) => void;
};

type State = UserMailConfiguration;

class UserMailConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = props.initialConfiguration;
  }

  languageChangedHandler = (value: string) => {
    this.setState({ language: value }, () => this.props.onConfigurationChange(this.state, true));
  };

  render() {
    const { readOnly, t } = this.props;
    return (
      <div className="field">
        <label className="label">{t("scm-mail-plugin.form.language")}</label>
        <div className="control">
          <DropDown
            options={[t("scm-mail-plugin.language.de"), t("scm-mail-plugin.language.en")]}
            optionValues={["de", "en"]}
            preselectedOption={this.state.language}
            optionSelected={this.languageChangedHandler}
            disabled={readOnly}
          />
        </div>
      </div>
    );
  }
}

export default withTranslation("plugins")(UserMailConfigurationForm);
