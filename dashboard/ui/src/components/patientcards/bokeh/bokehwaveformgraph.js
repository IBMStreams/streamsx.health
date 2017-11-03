import React from 'react'
import ReactDOM from 'react-dom'
import BokehWaveform from './bokeh_graph'

class BokehWaveformGraph extends React.Component {
  constructor(props) {
    super(props)

    this.bokehWaveform = new BokehWaveform()
  }

  componentDidMount() {
    var el = ReactDOM.findDOMNode(this);
    this.bokehWaveform.create(el, this.props);
  }

  render() {
    return (
      <div></div>
    )
  }
}

export default BokehWaveformGraph
