//import * as Bokeh from '../../../node_modules/bokehjs/build/js/bokeh.min'

class BokehWaveform {
  update(elem, props) {
    this._draw(elem, props, true);
  }

  create(elem, props) {
    this._draw(elem, props, false);
  }

  _draw(elem, props, doUpdate) {
    const b = require('bokehjs')
    console.log(typeof b)
    const plt = b.Bokeh.Plotting
    // Create plot
    const plot = plt.figure({
      tools: "pan,wheel_zoom,save,reset",
      title: "testing",
      plot_width: 400,
      plot_height: 400,
      background_fill_color: "#eeeeff",
    });

    // Add axis and grid
    // const xaxis = new Bokeh.DatetimeAxis({axis_line_color: null, axis_label: 'time'});
    // const yaxis = new Bokeh.LinearAxis({axis_line_color: null, axis_label: 'price'});
    // plot.add_layout(xaxis, "below");
    // plot.add_layout(yaxis, "left");
    // plot.add_layout(new Bokeh.Grid({ticker: xaxis.ticker, dimension: 0}));
    // plot.add_layout(new Bokeh.Grid({ticker: yaxis.ticker, dimension: 1}));

    plt.show(plot, elem)
  }
}

export default BokehWaveform
