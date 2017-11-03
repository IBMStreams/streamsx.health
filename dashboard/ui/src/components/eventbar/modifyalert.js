import React from 'react'

class ModifyAlert extends React.Component {

  constructor(props) {
    super(props)

    this.state = {...props}
    this.handleInputChange = this.handleInputChange.bind(this)
  }

  componentWillReceiveProps(nextProps) {
    this.setState({alertRule : nextProps.alertRule})
  }

  handleInputChange(event) {
    const target = event.target
    const value = target.value
    const name = target.name

    var updatedRule = {...this.state.alertRule}
    updatedRule[name] = value;
    this.setState({alertRule : updatedRule})
  }

  render() {
    return (
      <div className="container">
        <div className="row">
          <div className="col-4">
            <div><span><pre>Alert Name:</pre></span></div>
          </div>
          <div className="col">
            <input type="text" name="ruleName" value={this.state.alertRule.ruleName} onChange={this.handleInputChange}></input>
          </div>
        </div>
        <div className="row">
          <div className="col-4">
            <span style={{textAlign : "center"}}><pre><code>if</code></pre></span>
          </div>
          <div className="col">
            <select name="vitalName" value={this.state.alertRule.vitalName} onChange={this.handleInputChange}>
              <option value="heart_rate">Heart Rate</option>
              <option value="pulse">Pulse</option>
              <option value="spo2">SpO2</option>
              <option value="abp_dias">ABP Diastolic</option>
              <option value="abp_sys">ABP Systolic</option>
              <option value="temperature">Temperature</option>
            </select>
          </div>
        </div>
        <div className="row">
          <div className="col-4">
            <span style={{textAlign : "center"}}><pre><code>is between</code></pre></span>
          </div>
          <div className="col">
            <input type="text" name="minInclusive" value={this.state.alertRule.minInclusive} onChange={this.handleInputChange}></input>
          </div>
        </div>
        <div className="row">
          <div className="col-4">
            <span style={{textAlign : "center"}}><pre><code>and</code></pre></span>
          </div>
          <div className="col">
            <input type="text" name="maxExclusive" value={this.state.alertRule.maxExclusive} onChange={this.handleInputChange}></input>
          </div>
        </div>
        <div className="row">
          <div className="col-4">
            <span style={{textAlign : "center"}}><pre><code>for</code></pre></span>
          </div>
          <div className="col">
            <input type="text" name="duration" placeholder="0" value={this.state.alertRule.duration} onChange={this.handleInputChange}></input> seconds
          </div>
        </div>
        <div className="row">
          <div className="col-12"  style={{textAlign : "right"}}>
            <button onClick={() => {this.props.onSave(this.props.alertRule, this.state.alertRule)}}>Save</button>
          </div>
        </div>
      </div>
    )
  }
}

ModifyAlert.defaultProps = {
  alertRule : {
    ruleName : "",
    vitalName : "",
    minInclusive : "",
    maxExclusive : "",
    duration : ""
  }
}

export default ModifyAlert;
