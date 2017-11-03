import * as d3 from "d3";

class D3EventChart {

  constructor(props) {
    this.graphX = 0;
    this.graphY = 0;
    this.width = props.width;
    this.patientLaneHeight = props.laneHeight;
    this.height = 0;
    this.margin = props.margin;
    this.radius = 3;
    this.graph = undefined;
    this.showLegend = props.showLegend,
    this.legend = undefined;
    this.circleLayer = undefined;
    this.linesLayer = undefined;
    this.ruleNames = [];
    this.ruleColours = {};
    this.alertColours = [
      "1BAB39", // green
      "7D41AB", // purple
      "FF9700", // orange
      "3F1CE8", // blue
      "FFE000", // yellow
      "FF0216", // red
      "E84DB9", // pink
      "32DBE3" // cyan
    ];
  }

  update(chartElem, props) {
    this._draw(chartElem, props, true);
  }

  create(chartElem, props) {
    this._draw(chartElem, props, false);
  }

  _draw(chartElem, props, doUpdate) {
    var patientAlerts = this._convertToObjects(props.patientAlerts);
    var patients = this._getUniquePatients(patientAlerts);
    this.ruleNames = this._getUniqueRuleNames(patientAlerts);
    this.ruleColours = this._getGeneratedRuleColours();

    if(!this.showLegend) {
      var parentWidth = d3.select(chartElem).node().offsetWidth * (parseFloat(this.width)/100.0)
    } else {
      var parentWidth = d3.select(chartElem).select("#chart").node().offsetWidth * (parseFloat(this.width)/100.0)
    }


    // calculate the height of the chart based
    // on the # of patients
    this.height = (patients.length) * this.patientLaneHeight;

    // remove the existing graph before creating calculating the new one
    if(doUpdate && typeof(this.graph) !== 'undefined') {
      this.graph.remove();
    }

    // remove the existing graph before creating calculating the new one
    if(doUpdate && typeof(this.legend) !== 'undefined') {
      this.legend.remove();
    }

    this.xAxisScale = d3.scaleTime()
      .domain(this._getDateRangeFromData(patientAlerts))
      .range([this.margin.left, parentWidth - this.margin.right])
    this.yAxisScale = d3.scaleBand()
      .domain(patients)
      .range([0, (this.height - this.margin.bottom - this.margin.top)])
      .align(0.5);

    this.xAxis = d3.axisBottom()
      .scale(this.xAxisScale);
    this.yAxis = d3.axisLeft()
      .scale(this.yAxisScale)
      .tickValues(this.yAxisScale.domain());

    this.xAxisGridLines = d3.axisTop()
      .scale(this.xAxisScale)
      .tickSize(this.height - this.margin.top - this.margin.bottom)
      .tickFormat("");

    this.graph = d3.select(chartElem).select("#chart").append("svg")
      .attr("id", "graph")
      .attr("width", parentWidth)
      .attr("height", this.height)
      .attr("transform", "translate(" + this.graphX + "," + this.graphY + ")");

    // add the patient swim lanes
    this.graph
      .selectAll('.graphRect')
      .data(this.yAxisScale.domain())
      .enter()
      .append('rect')
      .attr('class', 'graphRect')
      .attr('width', parentWidth - this.margin.right - this.margin.left)
      .attr('height', this.yAxisScale.bandwidth() * (this.yAxisScale.padding() + 1))
      .attr('x', this.margin.left)
      .attr('y', (d) => { return this.margin.top + this.yAxisScale(d); })
      .style('fill', (d) => {
        // alternating color bands
        var i = this.yAxisScale.domain().indexOf(d);
        if (i % 2 === 0) {
          return "E0F1FB";
        } else {
          return "white";
        }
      });

    // add the X axis
    this.graph
      .append('g')
      .attr('id', 'xAxis')
      .attr('transform', 'translate(0, ' + (this.height - this.margin.bottom) + ')')
      .call(this.xAxis);

    // add the vertical grid lines
    this.graph
      .append('g')
      .attr('id', 'gridlines')
      .attr('transform', 'translate(0, ' + (this.height - this.margin.bottom) + ')')
      .call(this.xAxisGridLines)
      .style('opacity', '0.3')
      .style('color', 'lightgrey');

    // add the Y axis
    this.graph
      .append('g')
      .attr('transform', 'translate(' + this.margin.left + ', ' + this.margin.top + ')')
      .attr('id', 'yAxis')
      .call(this.yAxis)
      .style('font-size', 11);

    this._drawAlertsOnGraph(patientAlerts, patients);

    if(this.showLegend) {
      this._drawLegend(chartElem, patientAlerts, patients);
    }
  }

  _drawLegend(chartElem, patientAlerts, patients) {
    var legendRectSize = 18;
    var legendSpacing = 4;
    var graphYOffset = 30
    var alertColours = this.alertColours;
    var legendWidth = d3.select(chartElem).select("#legend").node().offsetWidth

//    console.log("rule names: ", this.ruleNames)

    if(typeof this.ruleNames === 'undefined')
      return

//    console.log("height:" + (legendRectSize * 2 + legendSpacing) * this.ruleNames.length)

    this.legend = d3.select(chartElem).select("#legend").append("svg")
        .attr('id', 'legend')
        .attr("width", legendWidth)
        .attr("height", (legendRectSize * 2 + legendSpacing) * this.ruleNames.length)
        .attr("transform", "translate(" + this.graphX + "," + (this.graphY + graphYOffset) + ")");

    var alertRuleElements = this.legend.selectAll('.legend')
                    .data(this.ruleNames)
                    .enter()
                    .append('g')
                    .attr('class', '.legend')
                    .attr('id', (d, i) => { return 'legend_' + i})
                    .attr('transform', (d, i) => {
                      var x = legendSpacing * 2;
                      var y = i * legendRectSize * 2 + legendSpacing;
                      return 'translate(' + x + ',' + y + ')'
                    });

    alertRuleElements.append('rect')
      .attr('width', legendRectSize)
      .attr('height', legendRectSize)
      .style('fill', (d, i) => { return alertColours[i]; })
      .style('stroke', (d, i) => { return alertColours[i]; })
      .style('stroke-width', 1)
      .style('fill-opacity', 0.6);

    alertRuleElements.append('text')
      .attr('x', legendRectSize + legendSpacing)
      .attr('y', legendRectSize - legendSpacing)
      .text((d) => { return d; });

  }

  _drawAlertsOnGraph(patientAlerts, patients) {
    var self = this;
    // create new layer for the alert data points
    this.circleLayer = this.graph.append('g');
    this.circleLayer
      .selectAll('circle')
      .data(patientAlerts)
      .enter()
      .append('circle')
      .attr('class', 'circle')
      .attr('id', (d, i) => { return 'circle_' + i})
      .attr('cx', (d) => { return self.xAxisScale(new Date(d.epochSeconds))})
      .attr('cy', (d) => { return self._calculateYPositionWithinPatientBand(d)})
      .attr('r', this.radius)
      .style('fill', (d) => { return self.ruleColours[d.ruleName].fill})
      .on('mouseover', (d, i) => { self._dataPointMouseOver(d, i) })
      .on('mouseout', (d, i) => { self._dataPointMouseOut(d, i) });

      // create new layer for the alert lines
      this.linesLayer = this.graph.append('g');
      this.linesLayer
        .selectAll('hLine')
        .data(this._calculateAlertLines(patientAlerts, patients))
        .enter()
        .append('line')
        .attr('class', 'hLine')
        .attr('x1', (d) => { return self.xAxisScale(d.start)})
        .attr('x2', (d) => { return self.xAxisScale(d.end)})
        .attr('y1', (d) => { return self._calculateYPositionWithinPatientBand(d)})
        .attr('y2', (d) => { return self._calculateYPositionWithinPatientBand(d)})
        .style('stroke', (d) => { return self.ruleColours[d.ruleName].fill})
  }

  _dataPointMouseOver(d, i) {
    // highlight the circle
      d3.select('#circle_' + i)
        .attr('r', this.radius * 1.5);

      this.graph
        .append('text')
        .attr('id', 't_' + i)
        .attr('x', () => { return this.xAxisScale(new Date(d.epochSeconds)) - d.notes.length/2 })
        .attr('y', () => { return this._calculateYPositionWithinPatientBand(d) - 5 })
        .text(() => { return d.notes });
  }

  _dataPointMouseOut(d, i) {
    d3.select('#circle_' + i)
      .attr('r', this.radius);

    this.graph
      .select('#t_' + i)
      .remove();
  }

  _calculateAlertLines(patientAlerts, patients) {
    var lines = [];

    for(var patientIdx in patients) {
      var patient = patients[patientIdx];
      for(var ruleIdx in this.ruleNames) {
        var ruleName = this.ruleNames[ruleIdx];
        var sortedByEpoch = patientAlerts
          .filter((d) => { return d.patientId === patient })
          .filter((d) => { return d.ruleName == ruleName })
          .sort((a, b) => { return a.epochSeconds - b.epochSeconds });

        function line(start=-1, end=-1, patientId='', ruleName='') {
          this.start = start;
          this.end = end;
          this.patientId = patientId;
          this.ruleName = ruleName;
        }

        var lastLine = new line();
        for(var alertIdx in sortedByEpoch) {
          var alert = sortedByEpoch[alertIdx];
          lastLine.ruleName = alert.ruleName;
          lastLine.patientId = patient;
          if(alert.type === 'ALERT_TRIGGERED') {
            lastLine.start = alert.epochSeconds;
          } else if(alert.type === 'ALERT_CANCELED') {
            if(lastLine.start === -1) {
              // need to set the start to the start of the xAxisScale
              lastLine.start = this.xAxisScale.domain()[0].getTime();
            }
            lastLine.end = alert.epochSeconds;

            lines.push(lastLine);
            lastLine = new line();
          }
        }

        // if lastLine has a start but no finish, draw line to end of chart
        if(lastLine.start > -1 && lastLine.end === -1) {
          lastLine.end = this.xAxisScale.domain()[1].getTime();
          lines.push(lastLine);
          lastLine = new line();
        }
      }
    }

    return lines;
  }

  _calculateYPositionWithinPatientBand(dataPoint) {
    var offsetFromTop = 5;
    var positionWithinBand = this.yAxisScale(dataPoint.patientId) + this.margin.top
      + offsetFromTop + this.yAxisScale.bandwidth()
      * this.ruleNames.indexOf(dataPoint.ruleName) / this.ruleNames.length;

      return positionWithinBand;
  }

  _getUniquePatients(patientAlerts) {
    var patients = [];
    for(var idx in patientAlerts) {
      var patientId = patientAlerts[idx].patientId;
      if(patients.indexOf(patientId) === -1) {
        patients.push(patientId);
      }
    }
    return patients;
  }

  _getGeneratedRuleColours() {
    var ruleColours = {};
    for(var idx in this.ruleNames) {
      var name = this.ruleNames[idx];
      ruleColours[name] = {
        "fill" : this.alertColours[idx % this.alertColours.length]
      }
    }

    return ruleColours;
  }

  _getUniqueRuleNames(patientAlerts) {
    var names = [];
    for(var idx in patientAlerts) {
      var ruleName = patientAlerts[idx].ruleName;
      if(names.indexOf(ruleName) === -1) {
        names.push(ruleName);
      }
    }
    names.sort();

    return names;
  }

  _getDateRangeFromData(patientAlerts) {
    // return d3.extent(patientAlerts, function(d) {
    //   return new Date(d.epochSeconds * 1000);
    // });

    var min = d3.min(patientAlerts, function(d) {
      return new Date(d.epochSeconds);
    });
    var max = new Date();
    return [min, max];
  }

  _convertToObjects(patientAlerts) {
    var convertedObjects = [];
    for(var idx in patientAlerts) {
      convertedObjects.push(JSON.parse(patientAlerts[idx]));
    }
    return convertedObjects;
  }

}

export default D3EventChart;
