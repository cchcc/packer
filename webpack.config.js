const path = require('path');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

module.exports = {
    entry: {
        'entry': './src/js/packer.js'
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'packer.js',
        library: 'packer',
        libraryTarget: 'umd'
    },
    plugins: [
        new UglifyJSPlugin()
    ],
    externals: {
        lodash: {
            commonjs: 'lodash',
            commonjs2: 'lodash',
            amd: 'lodash',
            root: '_'
        }
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['env']
                    }
                }
            }
        ]
    }
};