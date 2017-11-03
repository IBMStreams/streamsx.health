import React from 'react'
import ReactDOM from 'react-dom'
import BokehWaveformGraph from './bokehwaveformgraph'
import { connect } from 'react-redux'
import {updatePatientVitalsData} from '../../actions/actions'

const mapStateToProps = state => {
  return {
    vitalsData : state['vitalsData_' + state.vitalName]
  }
};

const mapDispatchToProps = dispatch => {
  return {
    updatePatientVitalsData : (patientId, vitalName, start, end) => {
        dispatch(updatePatientVitalsData(patientId, vitalName, start, end))
    }
  }
}

class BokehWaveformGraphContainer extends React.Component {
  render() {
    return (
      <BokehWaveformGraph />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(BokehWaveformGraphContainer);
