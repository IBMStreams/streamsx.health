import React from 'react';
import Modal from 'react-modal'
import Toggle from 'react-toggle'
import { string, array, bool, func, object } from 'prop-types'

const modalStyle = {
  content : {
    width: "40%",
    left: "30%",
    top: "30%",
    right: "auto"
  }
};

const modalWrapper = {
  minHeight: "100%",
  height: "auto !important",
  marginBottom: "-30px"
}

const modalBodyStyle = {
  height: "100%"
}

const modalFooterStyle = {
  height: "30px",
  textAlign: "right"
}

class ServiceToggle extends React.Component {
  static propTypes = {
    isEnabled: bool.isRequired,
    serviceName: string.isRequired,
    onServiceToggle: func.isRequired
  }

  render() {
    return (
      <div className="row">
        <div className="col-2">
          <Toggle defaultChecked={this.props.isEnabled} onChange={(event) => this.props.onServiceToggle(this.props.serviceName, event.target.checked)} />
        </div>
        <div className="col-10">
          <label>{this.props.serviceName}</label>
        </div>
      </div>
    )
  }
}

class PatientAnalyticsModal extends React.Component {

  static propTypes = {
    isModalOpen: bool.isRequired,
    patientId: string.isRequired,
    closeModal : func.isRequired,
    patientServices: object.isRequired,
    saveModal: func.isRequired,
    availableVitals : array
  }

  constructor(props) {
    super(props)

    this.closeModal = this.closeModal.bind(this);
    this.onServiceToggle = this.onServiceToggle.bind(this);
    this.saveModal = this.saveModal.bind(this);
    this.onDataToggle = this.onDataToggle.bind(this);

    this.toggledServices = {};
    this.displayedVitals = (typeof props.displayedVitals !== 'undefined' ? props.displayedVitals.slice() : [])
  }

  saveModal() {
    this.props.saveModal(this.props.patientId, this.toggledServices, this.displayedVitals);
  }

  closeModal() {
    this.props.closeModal(this.props.patientId);
  }

  onServiceToggle(serviceName, isEnabled) {
    this.toggledServices[serviceName] = {
      isEnabledForPatient : isEnabled
    }
    console.log("toggledServices: ", this.toggledServices);
  }

  onDataToggle(vitalName, isEnabled) {
    var idx = this.displayedVitals.indexOf(vitalName)
    if(idx === -1) {
      this.displayedVitals.push(vitalName)
    } else {
        this.displayedVitals.splice(idx, 1);
    }
  }

  getToggles() {
    var svcs = this.props.patientServices;
    var arr = [];
    for(var svcName in svcs) {
      var service = svcs[svcName];
      arr.push(<ServiceToggle serviceName={svcName} isEnabled={service.isEnabledForPatient} onServiceToggle={this.onServiceToggle} key={svcName} />)
    }

    return arr;
  }

  getDataToggles() {
    var vitals = this.props.availableVitals;
    var arr = []
    for(var idx in vitals) {
      var vitalName = vitals[idx]
      var isEnabled = this.displayedVitals.indexOf(vitalName) !== -1
      arr.push(<ServiceToggle serviceName={vitalName} isEnabled={isEnabled} onServiceToggle={this.onDataToggle} key={vitalName} />)
    }

    return arr;
  }

  render() {
    return (
      <Modal
        isOpen={this.props.isModalOpen}
        onAfterOpen={this.afterOpenModal}
        onRequestClose={this.closeModal}
        style={modalStyle}
        contentLabel="Configure Analytics"
      >
        <div style={modalWrapper}>
          <div id="modalHeader">
            <button className="close" onClick={this.closeModal}><span>&times;</span></button>
            <h2>Configure Analytics - {this.props.patientId}</h2>
            <hr />
          </div>
          <div id="modalBody" style={modalBodyStyle}>
            <div className="container">
              {this.getToggles()}
              <br />
              {this.getDataToggles()}
            </div>
          </div>
        </div>
        <div id="modalFooter" style={modalFooterStyle}>
          <button className="save" onClick={this.saveModal}><span>Save</span></button>
        </div>
      </Modal>
    )
  }
}

PatientAnalyticsModal.defaultProps = {
  availableVitals : [],
  displayedVitals : []
}

export default PatientAnalyticsModal;
