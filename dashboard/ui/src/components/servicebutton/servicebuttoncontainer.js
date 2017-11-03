import React from 'react';
import ServiceButton from './servicebutton';

function ServiceButtonContainer(props) {
  return (
    <ServiceButton onClick={props.onClick} name={props.name} className={props.className}/>
  )
}

export default ServiceButtonContainer;
