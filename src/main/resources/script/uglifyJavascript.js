uglifyJavascript = function (code) {
  return UglifyJS.minify(
      code,
      {
          parse: {
              // parse options
          },
          compress: {
              ecma: 6
          },
          output: {
              ast: true,
              code: true
          },
          mangle: {
              eval: true,
              toplevel: true
          },
          ecma: 8
      }
    ).code;
};
