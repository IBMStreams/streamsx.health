var express = require('express')
var router = express.Router()

router.use('/patients', require('./patients'))
router.use('/services', require('./services'))
router.use('/alert-rules', require('./alertRules'))
router.use('/alerts', require('./alerts'))
router.use('/data', require('./data'))

module.exports = router
