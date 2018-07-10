uglifyJavascript = function (code) {

  return UglifyJS.parse(code, {
      // "compress" : true,
      // "mangle" :true
  }).print_to_string();

    // UglifyJS.minify('precious_file.js', {
    //     compress: true,
    //     mangle: {
    //         except: ['foozah']
    //     }
    // });

  // toplevel_ast.figure_out_scope();
  //   UglifyJS.compress(code)
  // var compressor = UglifyJS.Compressor();
  // var compressed_ast = toplevel_ast.transform(compressor);
  // compressed_ast.figure_out_scope();
  // compressed_ast.compute_char_frequency();
  // compressed_ast.mangle_names();
  //
  // return compressed_ast.print_to_string();
};
