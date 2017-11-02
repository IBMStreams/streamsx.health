var express = require('express')
var router = express.Router()
var async = require('async')
var client = require('../models/redis_backend')

router.get('/', function(req, res) {
  var patientId = req.query.patientId || undefined;

  async.series([
    function(callback) {
      client.getAlertsList((reply) => {
        var filteredAlertRules = [];

        if(typeof patientId !== 'undefined') {
          filteredAlertRules = reply.filter((rule) => {
            var jsonRule = JSON.parse(rule);
            return jsonRule.patientId === patientId;
          });
        } else {
          filteredAlertRules = reply;
        }

        var fixedAlertRules = []
        for(var idx in filteredAlertRules) {
          fixedAlertRules.push(JSON.parse(filteredAlertRules[idx]))
        }

        var data = {
          alertRules : fixedAlertRules
        }
        callback(null, data);
      })
    }
  ], function(err, result) {
    res.json(result[0])
  })
})

module.exports = router
