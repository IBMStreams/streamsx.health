import React from 'react'
import ReactDOM from 'react-dom'
import { Charts, Resizable, ChartContainer, ChartRow, YAxis, LineChart, styler } from "react-timeseries-charts";
import { number } from 'prop-types'
import Colours from '../../colours'

class WaveformGraph extends React.Component {

  constructor(props) {
    super(props)
    this.colours = new Colours()

    this.colourMap = {}
  }

  static propTypes = {
    graphWidth : number.isRequired,
    minValue : number.isRequired,
    maxValue : number.isRequired
  }

  getLineCharts(allSeries, axisName) {
    var lineCharts = []
    for(var idx in allSeries) {
      lineCharts.push(
        <LineChart
          key={idx}
          axis={axisName}
          series={allSeries[idx]}
          style={styler([
            {key: "value", color: this.colourMap[idx]}
          ])}
        />)
    }

    return lineCharts
  }

  componentWillReceiveProps(nextProps) {
    for(var idx in nextProps.series) {
      if(!this.colourMap.hasOwnProperty(idx))
        this.colourMap[idx] = this.colours.getNextColour()
    }
  }

  render() {
    return (
      <div>
        <div className="h6" style={{transform: "translateX(50%)"}}>{this.props.chartTitle}</div>
        <Resizable>
          <ChartContainer
            showGrid={true}
            timeRange={this.props.timeRange}>
            <ChartRow height="200">
                <YAxis
                  id="axis1"
                  min={this.props.minValue}
                  max={this.props.maxValue}
                  width="60"
                  type="linear"
                  format={this.props.yAxisFormat}
                  label={this.props.yAxisLabel}/>
                <Charts>
                  {this.getLineCharts(this.props.series, "axis1")}
                    {/* <LineChart axis="axis1" series={this.props.series}/> */}
                </Charts>
            </ChartRow>
          </ChartContainer>
        </Resizable>
      </div>

    )
  }
}

WaveformGraph.defaultProps = {
  yAxisLabel : "(mv)",
  chartTitle : "",
  yAxisFormat : ",.1f"
}

export default WaveformGraph;
