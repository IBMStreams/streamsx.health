import * as d3 from "d3";

class D3ScatterPlot {

  constructor(props) {
    this.graphX = 0;
    this.graphY = 0;
    this.width = props.width;
    this.height = props.height;
    this.margin = props.margin;
    this.radius = 3;
    this.graph = undefined;
    this.main = undefined;
    this.showLegend = props.showLegend,
    this.legend = undefined;
    this.circleLayer = undefined;
    this.linesLayer = undefined;
  }

  update(chartElem, props) {
    this._drawData(props.data)
  }

  create(chartElem, props) {
    this._draw(chartElem, props, false);
  }

  _draw(chartElem, props, doUpdate) {
    var parentWidth = d3.select(chartElem).node().offsetWidth * (parseFloat(this.width)/100.0)

    // remove the existing graph before creating calculating the new one
    if(doUpdate && typeof(this.graph) !== 'undefined') {
      this.graph.remove()
    }

    var width = parentWidth - this.margin.left - this.margin.right
    var height = this.height - this.margin.top - this.margin.bottom

    this.xAxisScale = d3.scaleLinear().range([0, width]).domain([0, 1.5])
    this.xAxis = d3.axisBottom().scale(this.xAxisScale)

    this.yAxisScale = d3.scaleLinear().range([height, 0]).domain([0, 1.5])
    this.yAxis = d3.axisLeft().scale(this.yAxisScale);

    this.graph = d3.select(chartElem).select("#chart").append("svg")
      .attr("id", "graph")
      .attr("width", width + this.margin.left + this.margin.right)
      .attr("height", height + this.margin.top + this.margin.bottom)
      .attr("class", "chart")

    this.main = this.graph.append('g')
      .attr('transform', 'translate(' + this.margin.left + ',' + this.margin.top + ')')
      .attr('width', width)
      .attr('height', height)
      .attr('class', 'main')

    // add the X axis
    this.main
      .append('g')
      .attr('id', 'xAxis')
      .attr('transform', 'translate(0, ' + height + ')')
      .call(this.xAxis);

    // add the Y axis
    this.main
      .append('g')
      .attr('transform', 'translate(0,0)')
      .attr('id', 'yAxis')
      .call(this.yAxis)
      .style('font-size', 11);

    // draw diagonal linear
    this.linesLayer = this.main.append('g');
    this.linesLayer
      .append('line')
      .attr('class', 'hLine')
      .attr('x1', 0)
      .attr('x2', width - this.margin.right)
      .attr('y1', height)
      .attr('y2', this.margin.top)
      .style('stroke', 'black')

    this.circleLayer = this.main.append('g');

    this._drawData(props.data)
  }

  _drawData(dataPoints) {
    var self = this;

    if(typeof dataPoints === 'undefined' || dataPoints.length === 0)
      return;

    this.circleLayer
      .selectAll('circle')
      .data(dataPoints)
      .enter()
      .append('circle')
      .attr('class', 'circle')
      .attr('cx', (d) => {
        return self.xAxisScale(d[0])
      })
      .attr('cy', (d) => { return self.yAxisScale(d[1])})
      .attr('r', self.radius)
      .style('fill', (d) => { return "blue"});
  }
}

export default D3ScatterPlot;
