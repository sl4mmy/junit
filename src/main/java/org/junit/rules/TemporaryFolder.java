package org.junit.rules;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;

/**
 * The TemporaryFolder Rule allows creation of files and folders that are
 * guaranteed to be deleted when the test method finishes (whether it passes or
 * fails):
 * 
 * <pre>
 * public static class HasTempFolder {
 * 	&#064;Rule
 * 	public TemporaryFolder folder= new TemporaryFolder();
 * 
 * 	&#064;Test
 * 	public void testUsingTempFolder() throws IOException {
 * 		File createdFile= folder.newFile(&quot;myfile.txt&quot;);
 * 		File createdFolder= folder.newFolder(&quot;subfolder&quot;);
 * 		// ...
 * 	}
 * }
 * </pre>
 */
public class TemporaryFolder extends ExternalResource {
        private final File parentFolder;
	private File folder;

        public TemporaryFolder() {
                this(null);
        }

        public TemporaryFolder(File parentFolder) {
                this.parentFolder = parentFolder;
        }

	@Override
	protected void before() throws Throwable {
		create();
	}

	@Override
	protected void after() {
		delete();
	}

	// testing purposes only
	/**
	 * for testing purposes only. Do not use.
	 */
	public void create() throws IOException {
	        folder = createTemporaryFolderIn(parentFolder);
	}

	/**
	 * Returns a new fresh file with the given name under the temporary folder.
	 */
	public File newFile(String fileName) throws IOException {
		File file= new File(getRoot(), fileName);
		if (!file.createNewFile())
			throw new IllegalStateException(
					"a file with the name \'" + fileName + "\' already exists in the test folder");
		return file;
	}

	/**
	 * Returns a new fresh file with a random name under the temporary folder.
	 */
	public File newFile() throws IOException {
		return File.createTempFile("junit", null, getRoot());
	}

	/**
	 * Returns a new fresh folder with the given name under the temporary
	 * folder.
	 */
	public File newFolder(String... folderNames) {
		File file= getRoot();
		for (String folderName : folderNames) {
			file= new File(file, folderName);
			file.mkdir();
		}
		return file;
	}

	/**
	 * Returns a new fresh folder with a random name under the temporary folder.
	 */
	public File newFolder() throws IOException {
		return createTemporaryFolderIn(getRoot());
	}

	private File createTemporaryFolderIn(File parentFolder) throws IOException {
		File createdFolder= File.createTempFile("junit", "", parentFolder);
		createdFolder.delete();
		createdFolder.mkdir();
		return createdFolder;
	}

	/**
	 * @return the location of this temporary folder.
	 */
	public File getRoot() {
		if (folder == null) {
			throw new IllegalStateException(
					"the temporary folder has not yet been created");
		}
		return folder;
	}

	/**
	 * Delete all files and folders under the temporary folder. Usually not
	 * called directly, since it is automatically applied by the {@link Rule}
	 */
	public void delete() {
		if (folder != null)
			recursiveDelete(folder);
	}

	private void recursiveDelete(File file) {
		File[] files= file.listFiles();
		if (files != null)
			for (File each : files)
				recursiveDelete(each);
		file.delete();
	}
}
