//@flow
import React from "react";
import { translate } from "react-i18next";
import { Button, InputField } from "@scm-manager/ui-components";
import { apiClient } from "@scm-manager/ui-components";
import type { MailConfiguration } from "./MailConfiguration";

type Props = {
  configuration: MailConfiguration,
  t: string => string
};

type State = {
  showModal: boolean,
  failure: boolean,
  mail: string
};

class MailConfigurationTest extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...this.state,
      showModal: false,
      failure: false
    };
  }

  updateEmail = (value: string) => {
    this.setState({
      mail: value
    });
  };

  testConfiguration = () => {
    const { configuration } = this.props;
    const { mail } = this.state;
    const configLink = configuration._links.test.href + "?to=" + mail;

    return apiClient
      .post(configLink, configuration)
      .then(response => {
        this.setState({ showModal: true, failure: false });
      })
      .catch(err => {
        this.setState({ showModal: true, failure: true });
      });
  };

  closeModal = () => {
    this.setState({
      showModal: false
    });
  };

  renderModal = () => {
    const { t } = this.props;
    return (
      <div className="modal is-active">
        <div className="modal-background" />
        <div className="modal-card">
          <header className="modal-card-head">
            <p className="modal-card-title">
              {t("scm-mail-plugin.test.title")}
            </p>
            <button
              className="delete"
              aria-label="close"
              onClick={() => this.closeModal()}
            />
          </header>
          <section className="modal-card-body">
            {this.renderModalContent()}
          </section>
        </div>
      </div>
    );
  };

  renderModalContent = () => {
    const { t } = this.props;
    const { failure } = this.state;
    if (failure) {
      return <div className="content">{t("scm-mail-plugin.test.error")}</div>;
    }

    return <div className="content">{t("scm-mail-plugin.test.success")}</div>;
  };

  render() {
    const { t } = this.props;
    const { showModal } = this.state;

    if (showModal) {
      return this.renderModal();
    }

    return (
      <>
        <hr />
        <InputField
          label={t("scm-mail-plugin.test.title")}
          placeholder={t("scm-mail-plugin.test.input")}
          type="email"
          onChange={this.updateEmail}
        />
        <Button
          label={t("scm-mail-plugin.test.button")}
          action={this.testConfiguration}
        />
      </>
    );
  }
}

export default translate("plugins")(MailConfigurationTest);
