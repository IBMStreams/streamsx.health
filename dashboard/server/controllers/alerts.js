var express = require('express')
var router = express.Router()
var async = require('async')
var client = require('../models/redis_backend')

router.get('/', function(req, res) {
  var startTime = req.query.startTime || "-inf";
  var endTime = req.query.endTime || "+inf";

  async.waterfall([
    // retrieve the list of patients
    function(callback) {
      client.getPatients((reply) => {
        if(reply.length === 0) {
          reply.push("patient-1") // DEBUG!!!
        }
        callback(null, reply);
      })
    },
    // get the alerts for each patient
    function(patients, wfCallback) {
      async.map(patients,
        function(patient, callback) {
          client.getAlertsForPatient(patient, startTime, endTime, (reply) => {
            //console.log(patient, " -- reply: " , data);
            callback(null, reply);
          });
        },
        function(err, results) {
          if( err ) {
            console.log("Error retrieving alerts for patients: " + err)
          }

          var allAlerts = [];
          for(var idx in results) {
            allAlerts = allAlerts.concat(results[idx]);
          }

          wfCallback(null, allAlerts)
        })
    }],
    function(err, results) {
      //console.log("patient alerts: " + results);
      res.json(results);
    })
})

router.get('/active', function(req, res) {
  client.getActiveAlerts((activeAlerts) => {
    res.send(activeAlerts);
  })
})

module.exports = router
