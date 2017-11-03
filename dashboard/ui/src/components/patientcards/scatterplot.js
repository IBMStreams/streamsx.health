import React from 'react'
import ReactDOM from 'react-dom'
import D3ScatterPlot from './d3_scatterplot'
import { number } from 'prop-types'


class Scatterplot extends React.Component {

  // static propTypes = {
  //   graphWidth : number.isRequired,
  //   minValue : number.isRequired,
  //   maxValue : number.isRequired
  // }

  constructor(props) {
      super(props);
      var chartProps = {
        width : props.chartWidth,
        height : props.chartHeight,
        margin : props.chartMargin
      }
      this.d3ScatterPlot = new D3ScatterPlot(chartProps);

      this.state = {
        eventChartCreated : false
      }
  }

  componentDidMount() {
    var el = ReactDOM.findDOMNode(this);
    var tempProps = {...this.props}
    console.log(tempProps);
    this.d3ScatterPlot.create(el, tempProps);
    this.setState({eventChartCreated: true})
  }

  componentDidUpdate() {
    var el = ReactDOM.findDOMNode(this);
    var tempProps = {...this.props}
    console.log(tempProps);

    if(this.state.eventChartCreated === false) {
      this.d3ScatterPlot.create(el, tempProps);
      this.setState({eventChartCreated: true})
    } else {
      this.d3ScatterPlot.update(el, tempProps);
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

Scatterplot.defaultProps = {
  yAxisLabel : "(s)",
  chartTitle : "",
  yAxisFormat : ",.1f"
}

export default Scatterplot;
