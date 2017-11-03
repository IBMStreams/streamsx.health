import React from 'react';
import { connect } from 'react-redux'
import { string } from 'prop-types'
import PatientModal from './patientmodal'
import { closePatientModal, openPatientAnalyticsModal, fetchEnabledServicesForPatient } from '../../actions/actions'

const mapStateToProps = state => {
  return {
    modalPatientId: state.modalPatientId,
    patientServices: state.patientServices,
    displayedVitals : state.displayedVitals,
    activeAlerts : state.activeAlerts,
    modalDisplayType : state.modalDisplayType
  }
};

const mapDispatchToProps = dispatch => {
  return {
    closeModal : (patientId) => {
      dispatch(closePatientModal(patientId))
    },
    openAnalyticsModal : (patientId) => {
      dispatch(openPatientAnalyticsModal(patientId))
    },
    fetchEnabledServicesForPatient: (patientId) => {
      dispatch(fetchEnabledServicesForPatient(patientId));
    }
  }
}

class PatientModalContainer extends React.Component {
  static propTypes = {
    patientId: string
  };

  render() {
    var isModalOpen = this.props.modalPatientId === this.props.patientId;
    var displayedVitals = this.props.displayedVitals[this.props.modalPatientId]
    return (
      <PatientModal
        isModalOpen={isModalOpen}
        openAnalyticsModal={this.props.openAnalyticsModal}
        patientId={this.props.patientId}
        closeModal={this.props.closeModal}
        patientServices={this.props.patientServices}
        displayedVitals={displayedVitals}
        activeAlerts={this.props.activeAlerts}
        modalDisplayType={this.props.modalDisplayType}
      />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(PatientModalContainer);
