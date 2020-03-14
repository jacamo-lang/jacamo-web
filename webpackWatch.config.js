const config = require('./webpack.config');

module.exports = {
    ...config,
    watch: true,
    watchOptions: {
    aggregateTimeout: 300,
        poll: 1000
    }
};