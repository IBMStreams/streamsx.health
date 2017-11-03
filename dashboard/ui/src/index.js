import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route, Switch } from 'react-router-dom';
import { Provider } from 'react-redux'
import { createLogger } from 'redux-logger'
import thunk from 'redux-thunk'
import promise from 'redux-promise-middleware'

import { createStore, applyMiddleware } from 'redux'
import handleActions from './reducers/reducers'

import Home from './routes/Home/Home'
import Admin from './routes/Admin/Admin'
import Events from './routes/Events/Events'
import NotFound from './routes/NotFound/NotFound'

require('./styles/main.css')
require('./styles/bootstrap/bootstrap.css')
require('./styles/fontawesome/css/font-awesome.min.css')
require('./styles/toggle.css')

const logger = createLogger();
const middleware = applyMiddleware(promise(), thunk, logger);
const store = createStore(handleActions, middleware);

ReactDOM.render(
  <Provider store={store}>
    <BrowserRouter>
      <Switch>
        <Route exact path='/' component={Home} />
        <Route path='/admin' component={Admin} />
        <Route path='/events' component={Events} />
        <Route path='*' component={NotFound} />
      </Switch>
    </BrowserRouter>
  </Provider>,
  document.getElementById('root')
);
