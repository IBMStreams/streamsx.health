import React from 'react'
import { func, string } from 'prop-types'

ServiceButton.propTypes = {
  onClick : func.isRequired,
  name : string.isRequired
}

const buttonStyle = {
  marginRight: "10px"
}

function ServiceButton(props) {
  var clazz = "btn ";
  clazz += props.className;
  return (
    <button type="button" className={clazz} style={buttonStyle} onClick={props.onClick}>{props.name}</button>
  )
};



export default ServiceButton;
