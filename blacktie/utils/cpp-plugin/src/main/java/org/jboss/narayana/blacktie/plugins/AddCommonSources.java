package org.jboss.narayana.blacktie.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

public class AddCommonSources extends Task {

	private Reference mavenProjectRef;
	private String outputDir = "target";
	private String includes = ".*";

	public void execute() throws BuildException {
		MavenProject p = (MavenProject) mavenProjectRef.getProject()
				.getReference(mavenProjectRef.getRefId());
		processResources(p);
	}

	public void setMavenProject(Reference ref) {
		this.mavenProjectRef = ref;
	}

	public void setOutputDir(String outDir) {
		this.outputDir = outDir;
	}

	/**
	 * Pattern for matching which resources get included. See
	 * java.util.regex.Pattern for valid pattern syntax.
	 */
	public void setIncludes(String includes) {
		this.includes = includes;
	}

	private void processResources(MavenProject p) {
		List resources = p.getResources();

		if (resources != null) {
			for (Iterator i = resources.iterator(); i.hasNext();) {
				Resource resource = (Resource) i.next();
				String resourceRoot = resource.getDirectory();
				// System.out.println("adding source root: " + outputDir +
				// " from resource " + resource.toString());
				unzip(getClass().getResourceAsStream("/cxx.jar"), outputDir,
						includes);
				// p.addCompileSourceRoot(outputDir);
				p.addTestCompileSourceRoot(outputDir);
			}
		}
	}

	private static void unzip(InputStream from, String to, String pattern) {
		// System.out.println("from: " + from + " to: " + to + " pattern: " +
		// pattern);
		if (from == null || to == null)
			return;

		try {
			ZipInputStream zs = new ZipInputStream(from);
			ZipEntry ze;

			while ((ze = zs.getNextEntry()) != null) {
				String fname = to + '/' + ze.getName();
				// System.out.println(fname);
				boolean match = (pattern == null || ze.getName().matches(
						pattern));

				if (ze.isDirectory())
					new File(fname).mkdirs();
				else if (match)
					externalizeFile(fname, zs);
				else
					readFile(fname, zs);

				zs.closeEntry();
			}

			zs.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to unpack archive: "
					+ e.getMessage());
		}
	}

	private static void readFile(String fname, InputStream is)
			throws IOException {
		File f = new File(fname);
		byte[] buf = new byte[1024];
		int len;

		while ((len = is.read(buf)) > 0)
			;
	}

	private static File externalizeFile(String fname, InputStream is)
			throws IOException {
		File f = new File(fname);
		OutputStream out = new FileOutputStream(f);
		byte[] buf = new byte[1024];
		int len;

		while ((len = is.read(buf)) > 0)
			out.write(buf, 0, len);

		out.close();

		return f;
	}
}
