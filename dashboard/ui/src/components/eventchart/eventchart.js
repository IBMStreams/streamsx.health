import React from 'react'
import ReactDOM from 'react-dom'
import D3EventChart from './d3eventchart'
import { array, string, number, object, bool } from 'prop-types'
import { Button, Collapse } from 'react-bootstrap'

class EventChart extends React.Component {

  static propTypes = {
    patientAlerts : array,
    patientEvents : array,
    chartWidth : string,
    laneHeight : string,
    chartMargin : object,
    showLegend : bool
  }

  constructor(props) {
      super(props);
      var chartProps = {
        width : props.chartWidth,
        laneHeight : props.laneHeight,
        margin : props.chartMargin,
        showLegend : props.showLegend
      }
      this.d3EventChart = new D3EventChart(chartProps);

      this.state = {
        eventChartCreated : false,
        legendOpened : false
      }
  }

  filterAlerts() {
    if(this.props.patientFilter.length === 0) {
      // no filtering
      return this.props.patientAlerts;
    } else {
      return this.props.patientAlerts.filter((alert) => {
        var patientId = JSON.parse(alert).patientId
        return this.props.patientFilter.indexOf(patientId) !== -1
      });
    }
  }

  componentDidMount() {
    var el = ReactDOM.findDOMNode(this);
    var tempProps = {...this.props}
    tempProps.patientAlerts = this.filterAlerts()
    console.log(tempProps);
    if(tempProps.patientAlerts.length > 0) {
      this.d3EventChart.create(el, tempProps);
      this.setState({eventChartCreated: true})
    }
  }

  componentDidUpdate() {
    var el = ReactDOM.findDOMNode(this);
    var tempProps = {...this.props}
    tempProps.patientAlerts = this.filterAlerts();
    console.log(tempProps);

    if(this.state.eventChartCreated === false) {
      this.d3EventChart.create(el, tempProps);
      this.setState({eventChartCreated: true})
    } else {
      this.d3EventChart.update(el, tempProps);
    }
  }

  render() {
    return (
      <div>
        <div className="row">
          <div className="col-10 align-top" id="chart"></div>
          <div className="col-2" id="legend"></div>
        </div>

      </div>
    )
  }
}

EventChart.defaultProps = {
  patientAlerts : [],
  patientEvents : []
}

export default EventChart;
