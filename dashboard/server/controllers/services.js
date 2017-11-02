var express = require('express')
var router = express.Router()
var async = require('async')
var client = require('../models/redis_backend')
var services = require('../models/services')

router.get('/', function(req, res) {
  var serviceType = req.query.type || undefined;

  var svcs = {};
  if(typeof serviceType === 'undefined') {
    svcs = services.getAllServices()
  } else {
    svcs = services.getServicesByType(serviceType);
  }

  res.json(svcs);

  // var states = {};
  // for(var idx in svcs) {
  //   var svc = svcs[idx];
  //   states[svc.name] = serviceState[svc.name];
  // }
  //
  // res.send(JSON.stringify(states));
})

module.exports = router
