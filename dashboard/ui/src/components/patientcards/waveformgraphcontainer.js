import React from 'react';
import { connect } from 'react-redux'
import WaveformGraph from './waveformgraph.js'
import {updatePatientVitalsData} from '../../actions/actions'
import { TimeSeries, TimeRange } from "pondjs";
import {  string, number, array } from 'prop-types'
import axios from 'axios'

/* Retrieves the earliest timestamp from the data object...
 * expects the data parameter to be an object with vitalNames
 * as the key and an array as the value
 */
const _getEarliestTimestamp = (data) => {
  var earliestTimestamp = -1;
  for(var idx in data) {
    for(var dataIdx in data[idx]) {
      var ts = data[idx][dataIdx].ts
      if(earliestTimestamp === -1 || ts < earliestTimestamp) {
        earliestTimestamp = ts;
      }
    }
  }

  return earliestTimestamp;
}

/* Retrieves the earliest timestamp from the data object...
 * expects the data parameter to be an object with vitalNames
 * as the key and an array as the value
 */
const _getLatestTimestamp = (data) => {
  var latestTimestamp = -1;
  for(var idx in data) {
    for(var dataIdx in data[idx]) {
      var ts = data[idx][dataIdx].ts
      if(latestTimestamp === -1 || ts > latestTimestamp) {
        latestTimestamp = ts;
      }
    }
  }

  return latestTimestamp;
};

class WaveformGraphContainer extends React.Component {

  static propTypes = {
    patientId: string.isRequired,
    vitalNames: array.isRequired,
    graphWidth : number,
    chartTitle : string
  }

  constructor(props) {
    super(props)

    //const BUFFER_SIZE = 500
    this.dataLength = this.props.sampleRate * this.props.secondsToDisplay
    this.tickPeriod = 40
    this.updateDataPeriod = 500

    // create the arrays for the vitals
    var dataBuffer = {}
    var dataToSend = {}
    for(var idx in props.vitalNames) {
        dataBuffer[props.vitalNames[idx]] = []
        dataToSend[props.vitalNames[idx]] = []
    }

    this.state = {
      dataBuffer : dataBuffer,
      dataToSend : dataToSend,
      nextStartTimestamp : "-inf",
      lastEnqueuedTimestamp : -1,
      minValue : Number.MAX_SAFE_INTEGER,
      maxValue : Number.MIN_SAFE_INTEGER
    }

    this.tick = this.tick.bind(this)
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

    var restURL = '/data/' + patientId + '?startTime=' + start + '&endTime=' + end + queryStr
    console.log("[updateData] ", restURL)
    axios.get(restURL)
         .then(response => {
           if(start === '-inf') {
             // this is the first run, so only retrieve the last timestamp
             // so the graph has somewhere to start
             // this prevents the graph from sitting idle in the case that the data is lagged
             var ts = -1
             for(var vitalName in response.data) {
               var dataArr = response.data[vitalName];
               if(typeof dataArr !== 'undefined' && dataArr.length > 0) {
                 var lastTS = dataArr[dataArr.length-1].ts
                 if(ts === -1 || ts > lastTS) {
                   ts = lastTS;
                 }
               }
             }
             this.setState({nextStartTimestamp : ts+1})
           } else {
             self.updateDataBuffer(response.data);
           }
         })
         .then(() => {
           self.timerId = setTimeout(self.updateData.bind(self), self.updateDataPeriod);
         })
  }

  /* Ticks the graph along by retrieving the data from the buffer */
  tick() {
    var dataToSend = {...this.state.dataToSend}
    var dataBuffer = {...this.state.dataBuffer}
    var minValue = this.state.minValue
    var maxValue = this.state.maxValue

    for(var idx in this.props.vitalNames) {
      var vitalName = this.props.vitalNames[idx];

      if(dataBuffer[vitalName].length === 0)
        continue;

      // add the next data point to display
      var nextValue = dataBuffer[vitalName].shift()
      dataToSend[vitalName].push(nextValue)

      if(dataToSend[vitalName].length > this.dataLength)
        dataToSend[vitalName].splice(0, dataToSend[vitalName].length - this.dataLength);

      for(var dataIdx in dataToSend[vitalName]) {
        var v = dataToSend[vitalName][dataIdx].value
        if(v < minValue) {
          minValue = v
        }

        if(v > maxValue) {
          maxValue = v
        }
      }
    }

    var lastEnqueuedTimestamp = _getLatestTimestamp(dataToSend, this.state.lastEnqueuedTimestamp)
    this.setState({dataBuffer : dataBuffer, dataToSend : dataToSend, lastEnqueuedTimestamp : lastEnqueuedTimestamp, minValue, maxValue})
  }

  componentDidMount() {
    this.updateData();
    this.interval = setInterval(this.tick, this.tickPeriod);
  }

  componentWillUnmount() {
    clearTimeout(this.timerId);
    clearInterval(this.interval)
  }

  render() {
    var seriesToSend = {}

    for(var vitalName in this.state.dataToSend) {
      var data = this.state.dataToSend[vitalName].slice()
      if(typeof data === 'undefined' || data.length === 0) {
        console.warn("NO DATA FOR: " + vitalName)
        continue;
      }

      var dataPoints = []
      for(var dataIdx in data) {
        dataPoints.push([data[dataIdx].ts, data[dataIdx].value])
      }

      var d = {
        name: "data",
        columns: ["time","value"],
        points: dataPoints
      }

      seriesToSend[vitalName] = new TimeSeries(d);
    }
    var timeRange = new TimeRange(_getEarliestTimestamp(this.state.dataToSend), _getLatestTimestamp(this.state.dataToSend))



    return (
      <WaveformGraph chartTitle={this.props.chartTitle} yAxisFormat={this.props.yAxisFormat} series={seriesToSend} timeRange={timeRange} graphWidth={this.props.graphWidth} minValue={this.state.minValue} maxValue={this.state.maxValue} />
    )
  }
}

WaveformGraphContainer.defaultProps = {
  graphWidth : 800,
  chartTitle : "",
  minValue : -3,
  maxValue : 3,
  sampleRate : 100,
  secondsToDisplay : 5
}

export default WaveformGraphContainer;
