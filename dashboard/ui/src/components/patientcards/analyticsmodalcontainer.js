import React from 'react';
import { connect } from 'react-redux'
import { string, func } from 'prop-types'
import { closePatientAnalyticsModal, fetchAvailablePatientVitals, updatePatientServices, fetchEnabledServicesForPatient, updateDisplayedVitals } from '../../actions/actions'
import PatientAnalyticsModal from './analyticsmodal'

const mapStateToProps = state => {
  return {
    analyticsModalPatientId: state.analyticsModalPatientId,
    patientServices: state.patientServices,
    fetchingPatientServices: state.fetchingPatientServices,
    availableVitals : state.availableVitals,
    displayedVitals : state.displayedVitals
  }
};

const mapDispatchToProps = dispatch => {
  return {
    closeModal : (patientId) => {
      dispatch(closePatientAnalyticsModal(patientId))
    },
    updatePatientServices : (patientId, changedServices) => {
      dispatch(updatePatientServices(patientId, changedServices, () => {
        dispatch(fetchEnabledServicesForPatient(patientId))
      }))
    },
    updateDisplayedVitals : (patientId, vitalNames) => {
      dispatch(updateDisplayedVitals(patientId, vitalNames))
    },
    fetchAvailablePatientVitals : (patientId) => {
      dispatch(fetchAvailablePatientVitals(patientId))
    }
  }
}

class PatientAnalyticsModalContainer extends React.Component {

  static propTypes = {
    patientId : string.isRequired,
    closeModal : func.isRequired
  }

  constructor(props) {
    super(props);

    this.saveModal = this.saveModal.bind(this);
  }

  componentDidMount() {
    this.props.fetchAvailablePatientVitals(this.props.patientId);
  }

  saveModal(patientId, changedServices, displayedVitals) {
    // dispatch action to enable/disable patient services
    this.props.updatePatientServices(patientId, changedServices)
    this.props.updateDisplayedVitals(patientId, displayedVitals)
    this.props.closeModal(patientId)
  }

  render() {
    var isModalOpen = (this.props.fetchingPatientServices === false
      && this.props.analyticsModalPatientId === this.props.patientId);
      var displayedVitals = (this.props.displayedVitals.hasOwnProperty(this.props.patientId) ? this.props.displayedVitals[this.props.patientId] : [])
      var availableVitals = (this.props.availableVitals.hasOwnProperty(this.props.patientId) ? this.props.availableVitals[this.props.patientId] : [])
    return (
      <PatientAnalyticsModal
        isModalOpen={isModalOpen}
        patientId={this.props.patientId}
        closeModal={this.props.closeModal}
        patientServices={this.props.patientServices}
        saveModal={this.saveModal}
        availableVitals={availableVitals}
        displayedVitals={displayedVitals}
      />
    )
  }
}



export default connect(mapStateToProps, mapDispatchToProps)(PatientAnalyticsModalContainer)
