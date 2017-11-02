var express = require('express')
var router = express.Router()
var async = require('async')
var client = require('../models/redis_backend')

router.get('/', function(req, res) {
  client.getPatientsInfo((reply) => {
    var patientMap = {}
    for(var patientId in reply) {
      patientMap[patientId] = JSON.parse(reply[patientId])
    }
    res.send(patientMap);
  })
})

module.exports = router
