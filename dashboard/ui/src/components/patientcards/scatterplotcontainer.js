import React from 'react'
import { connect } from 'react-redux'
import { string, array, number } from 'prop-types'
import { updatePatientVitalsData } from '../../actions/actions'
import Scatterplot from './scatterplot'
import { TimeRange, TimeSeries } from 'pondjs'
import axios from 'axios'

class ScatterplotContainer extends React.Component {
  static propTypes = {
    patientId: string.isRequired,
    vitalNames: array.isRequired,
    graphWidth : number,
    chartTitle : string
  }

  constructor(props) {
    super(props)

    this.updateDataPeriod = 500

    // create the arrays for the vitals
    var dataBuffer = {}
    for(var idx in props.vitalNames) {
        dataBuffer[props.vitalNames[idx]] = []
    }

    this.state = {
      dataBuffer : dataBuffer,
      nextStartTimestamp : new Date().getTime(), //"-inf",
      lastEnqueuedTimestamp : -1
    }
  }

  updateDataBuffer(newData) {
    var dataBuffer = {...this.state.dataBuffer}
    for(var vitalName in newData) {
      var dataArr = newData[vitalName];
      if(typeof dataArr !== 'undefined' && dataArr.length > 0) {
        var buffer = dataBuffer[vitalName];
        var prevTS = buffer.length > 0 ? buffer[buffer.length-1].ts : -1
        for(var idx in dataArr) {
          var data = dataArr[idx]
          if(data.ts < prevTS) {
            throw "TimeException: prevTS=", prevTS, ", nextTS=", data.ts;
          } else {
            buffer.push(data)
            prevTS = data.ts
          }
        }
        dataBuffer[vitalName] = buffer
      }

      var earliestTimestamp = -1;
      for(var idx in dataBuffer) {
        if(dataBuffer[idx].length === 0)
          continue;

        var ts = dataBuffer[idx][dataBuffer[idx].length-1].ts
        if(earliestTimestamp === -1 || ts < earliestTimestamp) {
          earliestTimestamp = ts;
        }
      }

      var nextStartTimestamp = earliestTimestamp === -1 ? this.state.nextStartTimestamp : earliestTimestamp;
      this.setState({dataBuffer : dataBuffer, nextStartTimestamp : nextStartTimestamp})
    }
  }

  updateData() {
    var self = this;
    var patientId = this.props.patientId
    var start = this.state.nextStartTimestamp
    var end = "+inf"
    var queryStr = ""
    for(var idx in this.props.vitalNames) {
      queryStr += "&vitalName=" + this.props.vitalNames[idx]
    }

    var restURL = '/data?patientId=' + patientId + queryStr + '&start=' + start + '&end=' + end
    console.log("[updateData] ", restURL)
    axios.get(restURL)
         .then(response => {
           self.updateDataBuffer(response.data);
         })
         .then(() => {
           self.timerId = setTimeout(self.updateData.bind(self), self.updateDataPeriod);
         })
  }

  componentDidMount() {
    this.updateData();
  }

  componentWillUnmount() {
    clearTimeout(this.timerId);
  }

  render() {
    for(var idx in this.props.vitalNames) {
      var vitalName = this.props.vitalNames[idx];
      if(this.state.dataBuffer.hasOwnProperty(vitalName)) {
        // console.log("[scatterplot]: length=", this.state.dataBuffer[vitalName].length)
      }
    }

    var seriesToSend = {}
    for(var vitalName in this.state.dataBuffer) {
      var data = this.state.dataBuffer[vitalName].slice()
      if(typeof data === 'undefined' || data.length === 0) {
        console.warn("NO DATA FOR: ", vitalName)
        continue;
      }

      var dataPoints = []
      for(var i = 0; i < data.length; i++) {
        if(i+1 < data.length) {
          var rr = data[i].value/1e6
          var rr_plus1 = data[i+1].value/1e6
          dataPoints.push([rr, rr_plus1])
        }
      }

      seriesToSend[vitalName] = dataPoints;
    }

    // console.log("IN RENDER!!!")
    // console.log(seriesToSend)
    return (
      <div>
        <Scatterplot data={seriesToSend['rr_interval']} chartMargin={this.props.chartMargin} chartWidth={this.props.chartWidth} chartHeight={this.props.chartHeight} />
      </div>
    )
  }
}

ScatterplotContainer.defaultProps = {
  chartWidth : "100%",
  chartHeight : 300,
  chartMargin : {
    left: 40,
    right: 20,
    bottom: 30,
    top: 20
  }
}

export default ScatterplotContainer;
