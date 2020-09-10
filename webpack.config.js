const path = require('path');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = {

  entry: {
    jacamoweb: './src/js/app.js',
  },

  target: 'web',

  output: {
    path: path.resolve(__dirname, 'src/resources/js'),
    publicPath: 'js/',
    filename: '[name].js',
    chunkFilename: '[name].js',
  },

  plugins: [new TerserPlugin()],

  module: {
    rules: [{
      test: /\.js$/,
      include: path.resolve(__dirname, 'src/js'),
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
