const path = require('path');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

module.exports = {

  entry: './src/js/app.js',

  target: 'web',

  output: {
    path: path.resolve(__dirname, 'src/resources/js'),
    filename: 'jacamoweb.js'
  },

  optimization: {
    minimizer: [
      new UglifyJSPlugin({
        cache: true,
        parallel: true,
        uglifyOptions: {
          compress: true,
          ecma: 6,
          keep_fnames: true,
          keep_classnames: true,
        },
        sourceMap: true
      })
    ]
  },

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
