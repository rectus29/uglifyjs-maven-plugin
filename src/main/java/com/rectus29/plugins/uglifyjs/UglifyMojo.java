package com.rectus29.plugins.uglifyjs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * UglifyJS uglify
 *
 * @phase compile
 * @goal uglify
 */
public class UglifyMojo extends AbstractMojo {

	/**
	 * {@link org.apache.maven.shared.model.fileset.FileSet} containing JavaScript source files.
	 *
	 * @required
	 * @parameter expression="${sources}"
	 */
	protected FileSet sources;
	/**
	 * @parameter expression="${outputDirectory}"
	 */
	protected File outputDirectory;
	/**
	 * @parameter expression="${outputFile}"
	 */
	protected File outputFile;
	/**
	 * @parameter expression="${suffix}"
	 */
	protected String suffix = ".min";
	/**
	 * Skip UglifyJS execution.
	 *
	 * @parameter expression="${skip}" default-value="false"
	 */
	protected boolean skip = false;
	/**
	 * @parameter expression="${encoding}" default-value="UTF-8"
	 */
	private String encoding = "UTF-8";

	private boolean concatMode = false;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping");
			return;
		}

		if (outputDirectory == null && outputFile == null) {
			throw new MojoExecutionException("output is not specified.");
		}

		concatMode = (outputFile != null);

		try {
			int count = uglify(getSourceFiles());
			getLog().info("Uglified " + count + " file(s).");
			if(concatMode){
				getLog().info("Files uglified and merged into " + outputFile.getAbsolutePath());
			}else{
				getLog().info("Files uglified into " + outputDirectory.getAbsolutePath());
			}
		} catch (Exception e) {
			throw new MojoExecutionException("Failure to precompile handlebars templates.", e);
		}
	}

	protected int uglify(File[] jsFiles) throws Exception {
		int count = 0;
		for (File jsFile : jsFiles) {
			final String jsFilePath = jsFile.getPath();
			getLog().info("Uglifying " + jsFilePath);
			try {
				String output = new JavascriptContext("uglifyes-3.3.4.js", "uglifyJavascript.js").executeCmdOnFile("uglifyJavascript", jsFile);
				FileUtils.writeStringToFile(getOutputFile(jsFile), output, concatMode);
			} catch (Exception e) {
				getLog().error("Could not uglify " + jsFile.getPath() + ".", e);
				throw e;
			} finally {
				Context.exit();
			}
			count += 1;
		}
		return count;
	}

	protected final File getOutputFile(File inputFile) throws IOException {
		final String relativePath = getSourceDir().toURI().relativize(inputFile.getParentFile().toURI()).getPath();
		//if a file path is define sit the output toa concat file
		if (outputFile != null) {
			return outputFile;
		} else {
			final File output = new File(outputDirectory, relativePath);
			if (!output.exists()) {
				FileUtils.forceMkdir(output);
			}
			return new File(output, inputFile.getName().replace(".js", suffix + ".js"));
		}
	}

	/**
	 * Returns {@link File directory} containing JavaScript source {@link File files}.
	 *
	 * @return {@link File Directory} containing JavaScript source {@link File files}
	 */
	private File getSourceDir() {
		return new File(sources.getDirectory());
	}

	/**
	 * Returns JavaScript source {@link File files}.
	 *
	 * @return Array of JavaScript source {@link File files}
	 * @throws IOException
	 */
	private File[] getSourceFiles() throws IOException {
		final FileSetManager fileSetManager = new FileSetManager();
		final String[] includedFiles = fileSetManager.getIncludedFiles(sources);
		final File sourceDir = getSourceDir();
		final File[] sourceFiles = new File[includedFiles.length];
		for (int i = 0; i < includedFiles.length; i++) {
			sourceFiles[i] = new File(sourceDir, includedFiles[i]);
		}
		return sourceFiles;
	}

	class JavascriptContext {
		final Context cx = Context.enter();
		final ScriptableObject global = cx.initStandardObjects();

		JavascriptContext(String... scripts) throws IOException {
			ClassLoader cl = getClass().getClassLoader();
			for (String script : scripts) {
				InputStreamReader in = new InputStreamReader(cl.getResourceAsStream("script/" + script));
				cx.evaluateReader(global, in, script, 0, null);
				IOUtils.closeQuietly(in);
			}
		}

		String executeCmdOnFile(String cmd, File file) throws IOException {
			String data = FileUtils.readFileToString(file, "UTF-8");
			ScriptableObject.putProperty(global, "data", data);
			return cx.evaluateString(global, cmd + "(String(data));", "<cmd>", 1, null).toString();
		}
	}

}
