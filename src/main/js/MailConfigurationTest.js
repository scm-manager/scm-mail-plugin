//@flow
import React from "react";
import { translate } from "react-i18next";
import { Button, InputField } from "@scm-manager/ui-components";
import { apiClient } from "@scm-manager/ui-components";

type Props = {
  t: string => string
};

type State = {
  showModal: boolean,
  failure: boolean
};

class MailConfigurationTest extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      showModal: false,
      failure: false
    };
  }

  fetchConfiguration = () => {
    const configLink =
      "http://localhost:8081/scm/api/v2/plugins/mail/test/?to=some@whe.re";
    //link + ".test ?to=";
    // TODO: Add config test link to API
    return apiClient
      .post(configLink)
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

  render() {
    const { t } = this.props;
    const { showModal, failure } = this.state;

    let modal = null;
    if (failure) {
      modal = <div className="content">{t("scm-mail-plugin.test.error")}</div>;
    } else {
      modal = (
        <div className="content">{t("scm-mail-plugin.test.success")}</div>
      );
    }

    if (showModal) {
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
            <section className="modal-card-body">{modal}</section>
          </div>
        </div>
      );
    }

    return (
      <>
        <InputField
          label={t("scm-mail-plugin.test.title")}
          placeholder={t("scm-mail-plugin.test.input")}
          type="email"
          onChange={this.handlePasswordChange}
        />
        <Button
          label={t("scm-mail-plugin.test.button")}
          action={this.fetchConfiguration}
        />
      </>
    );
  }
}

export default translate("plugins")(MailConfigurationTest);
