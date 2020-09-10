const path = require('path');
const config = require('./webpack.config');

module.exports = {
    ...config,
    watch: true,
    watchOptions: {
        aggregateTimeout: 300,
        poll: 1000,
        ignored: [/node_modules/]
    },
    resolve: {
        unsafeCache: true,
    },
    mode: 'development'
};