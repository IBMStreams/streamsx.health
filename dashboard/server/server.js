var express = require('express')
var app = express()
var redis = require('./models/redis_backend')
var st = require('./models/streamtool')
var fs = require('fs-extra')
var bodyParser = require('body-parser')

var properties = JSON.parse(fs.readFileSync('properties.json', 'utf8'))
redis.connect(properties['redis']['url'])
st.connect(properties['streams'])


app.use(bodyParser.urlencoded({ extended: false }));
app.use(require('./controllers'))

app.listen(4000, function() {
    console.log('Example app listening on port 4000');
})
