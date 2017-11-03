import React from 'react'
import { fetchPatients, openPatientModal, fetchEnabledServicesForPatient } from '../../actions/actions'
import { connect } from 'react-redux'
import PatientCardsTable from './patientcardstable'

const mapStateToProps = state => {
  return {
    patients : state.patients,
    patientInfo : state.patientInfo
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchPatients : () => {
      dispatch(fetchPatients());
    },
    openModal : (patientId) => {
      dispatch(openPatientModal(patientId))
      dispatch(fetchEnabledServicesForPatient(patientId))
    }
  }
}

class PatientCardsContainer extends React.Component {
  updatePatients() {
    this.props.fetchPatients();
    this.timerId = setTimeout(this.updatePatients.bind(this), 5000);
  }

  componentWillMount() {
    this.updatePatients();
  }

  componentWillUnMount() {
    clearTimeout(this.timerId);
  }

  render() {
    return (
      <PatientCardsTable patients={this.props.patients} patientInfo={this.props.patientInfo} onCardClick={this.props.openModal} />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(PatientCardsContainer);
