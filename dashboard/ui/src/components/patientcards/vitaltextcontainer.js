import React from 'react'
import { connect } from 'react-redux'
import {updatePatientVitalsData} from '../../actions/actions'
import { string } from 'prop-types'
import VitalText from './vitaltext'

const mapStateToProps = state => {
  return {
    vitalsData : state.vitalsData,
    updatedVitalNames : state.updatedVitalNames
  }
};

const mapDispatchToProps = dispatch => {
  return {
    updatePatientVitalsData : (patientId, vitalName, start, end) => {
        dispatch(updatePatientVitalsData(patientId, vitalName, start, end))
    }
  }
}

class VitalTextContainer extends React.Component {

  static propTypes = {
    patientId: string.isRequired,
    vitalName: string.isRequired,
    vitalUOM: string.isRequired
  }

  constructor(props) {
    super(props)

    this.state = {
      nextStartTimestamp : "-inf",
      currentVitalsValue : -1
    }
  }

  updateData() {
    console.log("[updateData] Updating data!")
    this.props.updatePatientVitalsData(this.props.patientId, [this.props.vitalName], this.state.nextStartTimestamp, "+inf");
    this.timerId = setTimeout(this.updateData.bind(this), 1000);
  }

  componentDidMount() {
    this.updateData();
  }

  componentWillUnmount() {
    clearTimeout(this.timerId);
    clearInterval(this.interval)
  }

  componentWillReceiveProps(nextProps) {
    if(nextProps.updatedVitalNames.indexOf(this.props.vitalName) !== -1) {
      var nextVitalsData = nextProps.vitalsData[this.props.vitalName]
      if(typeof nextVitalsData !== 'undefined' && nextVitalsData.length > 0) {
        var nextStartTimestamp = nextVitalsData[nextVitalsData.length-1].ts+1
        var vitalsValue = nextVitalsData[nextVitalsData.length-1].value
        this.setState({nextStartTimestamp : nextStartTimestamp, currentVitalsValue : vitalsValue});
      }
    }
  }

  render() {
    return (
      <VitalText
        vitalName={this.props.vitalName}
        vitalValue={this.state.currentVitalsValue}
        vitalUOM={this.props.vitalUOM}
        format={this.props.format}
        label={this.props.label}
        style={this.props.style} />
    )
  }
}

VitalTextContainer.defaultProps = {
  vitalsData : []
}

export default connect(mapStateToProps, mapDispatchToProps)(VitalTextContainer);
