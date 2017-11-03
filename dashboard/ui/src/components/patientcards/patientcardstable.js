import React from 'react';
import PatientCard from './patientcard'
import PatientCardSmall from './patientcardsmall'
import { fetchPatients, fetchPatientActiveAlerts, fetchAvailablePatientVitals, openPatientModal, fetchEnabledServicesForPatient } from '../../actions/actions'
import { connect } from 'react-redux'

const mapStateToProps = state => {
  return {
    patients : state.patients,
    patientInfo : state.patientInfo,
    activeAlerts : state.activeAlerts,
    patientCardSize : state.patientCardSize,
    patientCardsFilterTypes : state.patientCardsFilterTypes
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchPatients : () => {
      dispatch(fetchPatients());
    },
    fetchActiveAlerts : () => {
      dispatch(fetchPatientActiveAlerts())
    },
    openModal : (patientId) => {
      dispatch(openPatientModal(patientId))
      dispatch(fetchEnabledServicesForPatient(patientId))
      dispatch(fetchAvailablePatientVitals(patientId))
    }
  }
}

const patientContainerStyle = {
  paddingTop : "10px",
  width : "100%"
}

const UPDATE_PATIENTS_PERIOD = 5000
const UPDATE_ACTIVE_ALERTS_PERIOD = 2000

class PatientCardsTable extends React.Component {
  updatePatients() {
    this.props.fetchPatients();
    this.updatePatientsTimerId = setTimeout(this.updatePatients.bind(this), UPDATE_PATIENTS_PERIOD);
  }

  updateActiveAlerts() {
    this.props.fetchActiveAlerts();
    this.updateActiveAlertsTimerId = setTimeout(this.updateActiveAlerts.bind(this), UPDATE_ACTIVE_ALERTS_PERIOD);
  }

  componentWillMount() {
    this.updatePatients();
    this.updateActiveAlerts();
  }

  componentWillUnMount() {
    clearTimeout(this.updatePatientsTimerId);
    clearTimeout(this.updateActiveAlertsTimerId);
  }

  getPatientCards() {
    let cards = [];
    if(typeof this.props.patients === 'undefined')
      return cards;

    var sortedPatients = this.props.patients.slice().sort();
    for(var idx in sortedPatients) {
      var patientId = sortedPatients[idx]
      var source = this.props.patientInfo[patientId].readingSource;
      var activeAlerts = this.props.activeAlerts[patientId]

      // do not display the card if the "alertsOnly" filter is set
      if(this.props.patientCardsFilterTypes.indexOf("alertsOnly") !== -1 && Object.keys(activeAlerts).length === 0) {
        continue;
      }

      if(this.props.patientCardSize === "small") {
        cards.push(<PatientCardSmall key={patientId} activeAlerts={activeAlerts} patientId={patientId} source={source} onCardClick={this.props.openModal} />)
      } else if(this.props.patientCardSize === "large") {
        cards.push(<PatientCard key={patientId} activeAlerts={activeAlerts} patientId={patientId} source={source} onCardClick={this.props.openModal} />)
      }

    }

    return cards;
  }

  render() {
    return (
      <div className="container" style={patientContainerStyle}>
        <div className="row justify-content-start">
          {this.getPatientCards()}
        </div>
      </div>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(PatientCardsTable);
