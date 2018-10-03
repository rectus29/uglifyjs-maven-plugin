uglifyJavascript = function (code, options) {
	return UglifyJS.minify(
		code,
		options
	).code;
};
