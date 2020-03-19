const path = require('path');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = {

  entry: './src/js/app.js',

  target: 'web',

  output: {
    path: path.resolve(__dirname, 'src/resources/js'),
    filename: 'jacamoweb.js'
  },

  plugins: [new TerserPlugin()],

  module: {
    rules: [{
      test: /\.js$/,
      exclude: /(node_modules)/,
      use: {
        loader: 'babel-loader',
        options: {
          presets: ['@babel/preset-env']
        }
      }
    }]
  },

  mode: 'production'

};
