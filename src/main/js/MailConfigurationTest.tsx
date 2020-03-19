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
import { withTranslation, WithTranslation } from "react-i18next";
import styled from "styled-components";
import { Modal, Button, Notification, InputField, Level } from "@scm-manager/ui-components";
import { apiClient, validation as validator } from "@scm-manager/ui-components";
import { MailConfiguration } from "./MailConfiguration";

type Props = WithTranslation & {
  configuration: MailConfiguration;
};

type State = {
  showModal: boolean;
  failure?: Error;
  mail: string;
  loading: boolean;
  mailValidationError?: Error;
};

const StretchedLevel = styled(Level)`
  align-items: stretch;
  margin-bottom: 1rem !important; // same margin as field
`;

const FullWidthInputField = styled(InputField)`
  width: 100%;
  margin-right: 1.5rem;
`;

const AlignFlexEndDiv = styled.div`
  align-self: flex-end;
`;

class MailConfigurationTest extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...this.state,
      showModal: false,
      loading: false
    };
  }

  updateEmail = (value: string) => {
    this.setState({
      mailValidationError: !validator.isMailValid(value),
      mail: value
    });
  };

  testConfiguration = () => {
    const { configuration } = this.props;
    const { mail } = this.state;
    const configLink = configuration._links.test.href + "?to=" + mail;

    this.setState({
      loading: true
    });

    return apiClient
      .post(configLink, configuration)
      .then(response => {
        this.setState({
          showModal: true,
          failure: null,
          loading: false
        });
      })
      .catch(err => {
        this.setState({
          showModal: true,
          failure: err,
          loading: false
        });
      });
  };

  closeModal = () => {
    this.setState({
      showModal: false
    });
  };

  renderModal = () => {
    const { t } = this.props;

    const footer = (
      <div className="field is-grouped">
        <p className="control">
          <Button label={t("scm-mail-plugin.test.close")} action={() => this.closeModal()} />
        </p>
      </div>
    );

    return (
      <Modal
        title={t("scm-mail-plugin.test.title")}
        closeFunction={() => this.closeModal()}
        body={this.renderModalContent()}
        footer={footer}
        active={true}
      />
    );
  };

  renderModalContent = () => {
    const { t } = this.props;
    const { failure } = this.state;

    return (
      <Notification type={failure ? "danger" : "success"}>
        {failure ? t("scm-mail-plugin.test.error") : t("scm-mail-plugin.test.success")}
      </Notification>
    );
  };

  render() {
    const { t } = this.props;
    const { showModal, loading } = this.state;

    let modal = null;
    if (showModal) {
      modal = this.renderModal();
    }

    return (
      <>
        {modal}
        <hr />
        <StretchedLevel
          children={
            <FullWidthInputField
              label={t("scm-mail-plugin.test.title")}
              placeholder={t("scm-mail-plugin.test.input")}
              type="email"
              validationError={this.state.mailValidationError}
              onChange={this.updateEmail}
              errorMessage={t("scm-mail-plugin.mailValidationError")}
            />
          }
          right={
            <AlignFlexEndDiv className="field">
              <Button label={t("scm-mail-plugin.test.button")} action={this.testConfiguration} loading={loading} />
            </AlignFlexEndDiv>
          }
        />
      </>
    );
  }
}

export default withTranslation("plugins")(MailConfigurationTest);
