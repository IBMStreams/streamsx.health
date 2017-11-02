var express = require('express')
var router = express.Router()
var async = require('async')
var client = require('../models/redis_backend')

router.get('/:patientId', function(req, res) {
  var patientId = req.params['patientId']
  var vitalName = req.query.vitalName
  var startTime = req.query.startTime || "-inf"
  var endTime = req.query.endTime || "+inf"

  if(!vitalName) {
    res.status(500).send("Missing 'vitalName' query parameter!")
    return
  }

  var vitalNames = [];
  if(typeof vitalName === "string") {
        vitalNames.push(vitalName);
  } else {
    Array.prototype.push.apply(vitalNames, vitalName)
  }

  client.getData(patientId, vitalNames, startTime, endTime, (data) => {
    res.json(data)
  });
})

router.get('/:patientId/data-types', function(req, res) {
  var patientId = req.param['patientId']
  client.getAvailableVitals(patientId, (availableVitals) => {
    res.json(availableVitals)
  })
})

module.exports = router
