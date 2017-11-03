import React from 'react'
import EventChart from './eventchart'
import { connect } from 'react-redux'
import { string, array, number, object, bool } from 'prop-types'
import { updateAlerts } from '../../actions/actions'

const mapStateToProps = state => {
  return {
    patientAlerts : state.patientAlerts,
    patientEvents : state.patientEvents
  }
};

const mapDispatchToProps = dispatch => {
  return {
    updateAlerts : () => {
      var d = new Date();
      var currentTimeInEpochMilliseconds = Math.round(d.getTime());
      var startTime = currentTimeInEpochMilliseconds - 1500*1000; // -30 minutes
      dispatch(updateAlerts(startTime, currentTimeInEpochMilliseconds));
    }
  }
}

class EventChartContainer extends React.Component {

  static propTypes = {
    chartWidth : string,
    laneHeight : string,
    chartMargin : object,
    showLegend : bool,
    patientFilter : array
  }

  updateAlerts() {
    this.props.updateAlerts();
    this.timerId = setTimeout(this.updateAlerts.bind(this), 1000);
  }

  componentWillMount() {
    this.updateAlerts();
  }

  componentWillUnmount() {
    clearTimeout(this.timerId);
  }

  render() {
    return (
      <EventChart
        patientAlerts={this.props.patientAlerts}
        showLegend={this.props.showLegend}
        chartWidth={this.props.chartWidth}
        chartMargin={this.props.chartMargin}
        laneHeight={this.props.laneHeight}
        patientFilter={this.props.patientFilter}
        />
    )
  }
}

EventChartContainer.defaultProps = {
  chartWidth : "100%",
  laneHeight : "100",
  chartMargin : {
    left: 70,
    right: 70,
    bottom: 20,
    top: 20
  },
  showLegend : true,
  patientFilter : []
}

export default connect(mapStateToProps, mapDispatchToProps)(EventChartContainer);
