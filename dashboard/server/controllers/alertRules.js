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

router.post('/add', function(req, res) {
  var oldAlertRule = req.body.oldAlertRule
  var newAlertRule = req.body.newAlertRule

  async.series([
    function(callback) {
      if(typeof oldAlertRule !== 'undefined') {
        // remove old rule
        client.removeAlertRule(oldAlertRule, (reply) => {
          callback(null, reply)
        })
      }
    },
    function(callback) {
      // add new rule
      client.addAlertRule(newAlertRule, (reply) => {
        callback(null, reply)
      })
    }
  ], function(err, result) {
    var response = {
      status : "Alert added"
    }
    res.json(response);
  })
})

module.exports = router
