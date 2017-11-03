import React from 'react'
import { string, number } from 'prop-types'

const vitalNameStyle = {
  fontSize: "20px",
	marginLeft: "5px",
	marginTop: "0px",
	marginBottom: "0px"
}

const vitalValueStyle = {
  fontSize: "50px",
	textAlign: "center",
	marginBottom: "0px"
}

const vitalUOMStyle = {
  textAlign: "right",
	fontSize: "20px",
	marginRight: "0px",
	marginTop: "-25px"
}

const vitalBoxStyle = {
  backgroundColor: "#152935",
	// border: "3px solid black",
	/* width: 220px; */
	height: "120px",
	fontFamily: "arial",
	color: "#FFF",
	margin: "5px"
}

class VitalText extends React.Component {

  static propTypes = {
    vitalName: string.isRequired,
    vitalValue: number.isRequired,
    vitalUOM : string.isRequired,
    label : string,
    format : string
  }

  render() {
    const  numeral = require('numeraljs')
    var label = this.props.label === "" ? this.props.vitalName : this.props.label
    return (
      <div style={vitalBoxStyle}>
        <div style={{...vitalNameStyle, ...this.props.style}}>{label}</div>
        <div style={{...vitalValueStyle, ...this.props.style}}>{numeral(this.props.vitalValue).format(this.props.format)}</div>
        <div style={{...vitalUOMStyle, ...this.props.style}}>{this.props.vitalUOM}</div>
      </div>
    )
  }
}

VitalText.defaultProps = {
  format : '0',
  label : ""
}

export default VitalText;
