package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.AdminPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IPluginMetadataDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;
import uk.ac.warwick.dcs.boss.model.testing.impl.TemporaryDirectory;

public class PerformEditPluginPage extends Page {

	public PerformEditPluginPage() throws PageLoadException {
		super("multi_edited", AccessLevel.ADMIN);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected GET request");
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		if (pageContext.hasUploadedFiles()) {
			InputStream in = null;
			String uploadedFilename = null;
			boolean unexpected = true;
			try {
				FileItemIterator fileIterator = pageContext.getUploadedFiles();

				// try to obtain the input stream (from uploaded file)
				while (fileIterator.hasNext()) {
					FileItemStream fileItemStream = fileIterator.next();
					if (fileItemStream.getFieldName().equals("file")) {
						in = fileItemStream.openStream();
						uploadedFilename = fileItemStream.getName();
						unexpected = false;
						break;
					}
				}
			} catch (FileUploadException e) {
				throw new ServletException("error while uploading plugin file",
						e);
			}

			// no file uploaded in request
			if (unexpected)
				throw new ServletException("Unexpected POST request");

			// put the uploaded file into a temporary folder for further
			// inspection and extraction
			File tempDir;

			InputStream is = new FileInputStream(new File(
					pageContext.getConfigurationFilePath()));
			Properties prop = new Properties();
			prop.load(is);
			tempDir = TemporaryDirectory.createTempDir("plugin",
					new File(prop.getProperty("testing.temp_dir")));

			OutputStream out = null;
			File pluginFile = new File(tempDir, uploadedFilename);
			try {
				out = new BufferedOutputStream(new FileOutputStream(pluginFile));
				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}
				out.flush();
			} finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
			JarFile jarFile = null;
			Attributes atts = null;

			try {
				// we have the plugin in a jar file
				jarFile = new JarFile(pluginFile);
				atts = jarFile.getManifest().getMainAttributes();

				// manifest file is required to supplied at least plugin's id,
				// name, and version
				if (!atts.containsKey(PLUGIN_ID)
						|| !atts.containsKey(PLUGIN_NAME)
						|| !atts.containsKey(PLUGIN_VERSION)) {
					throw new IOException();
				}
			} catch (IOException e) {
				throw new ServletException("Supplied file is not a BOSS plugin");
			}

			// valid plugin file (as far as MANIFEST file goes)
			String pluginId = atts.getValue(PLUGIN_ID);
			File pluginFolder = new File(pageContext.getWebInfFolderPath(),
			"plugins");

			// archiving the plugin file under WEB-INF/plugins/
			// since the pluginId is unique, there shouldn't be a collision
			// regarding name
			File pluginJarFile = new File(pluginFolder, pluginId + ".jar");
			FileUtils.copyFile(pluginFile, pluginJarFile);

			// make the plugin active by copy the jar file into WEB-INF/lib
			File webAppLibFolder = new File(pageContext.getWebInfFolderPath(),
			"lib");
			pluginJarFile = new File(webAppLibFolder, "plugin_" + pluginId
					+ ".jar");
			FileUtils.copyFile(pluginFile, pluginJarFile);

			// copy the dependencies of the plugin (residing under lib folder of
			// the plugin's jar file) if exists.
			List<String> libFileNames = new LinkedList<String>();
			Enumeration<JarEntry> enumeration = jarFile.entries();
			while (enumeration.hasMoreElements()) {
				JarEntry entry = enumeration.nextElement();
				String entryName = entry.getName();
				if (entryName.startsWith("lib/") && entryName.endsWith(".jar")) {
					String[] entryPathComps = entryName.split("/");
					String libFileName = entryPathComps[entryPathComps.length - 1];
					libFileNames.add(libFileName);
					File destLibFile = new File(webAppLibFolder, "plugin_"
							+ pluginId + "_" + libFileName);
					in = null;
					out = null;
					try {
						in = new BufferedInputStream(
								jarFile.getInputStream(entry));
						out = new BufferedOutputStream(new FileOutputStream(
								destLibFile));
						int c;
						while ((c = in.read()) != -1) {
							out.write(c);
						}
						out.flush();
					} finally {
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					}
				}
			}

			// create and persist PluginMetadata entity
			IDAOSession f;
			try {
				DAOFactory df = (DAOFactory) FactoryRegistrar
				.getFactory(DAOFactory.class);
				f = df.getInstance();
			} catch (FactoryException e) {
				throw new ServletException("dao init error", e);
			}
			
			try {
				f.beginTransaction();
				
				// create a PluginMetadata entity and set the corresponding values
				PluginMetadata pluginMetadata = new PluginMetadata();
				pluginMetadata.setPluginId(atts.getValue(PLUGIN_ID));
				pluginMetadata.setName(atts.getValue(PLUGIN_NAME));
				pluginMetadata.setVersion(atts.getValue(PLUGIN_VERSION));
				if (atts.containsKey(PLUGIN_AUTHOR))
					pluginMetadata.setAuthor(atts.getValue(PLUGIN_AUTHOR));
				if (atts.containsKey(PLUGIN_EMAIL))
					pluginMetadata.setEmail(atts.getValue(PLUGIN_EMAIL));
				if (atts.containsKey(PLUGIN_DESCRIPTION))
					pluginMetadata.setDescription(atts.getValue(PLUGIN_DESCRIPTION));
				
				// persist into database
				IPluginMetadataDAO pluginMetadataDao = f.getPluginMetadataDAOInstance();
				pluginMetadata.setId(pluginMetadataDao.createPersistentCopy(pluginMetadata));
				
				// set lib filenames if there're some
				if (!libFileNames.isEmpty())
					pluginMetadataDao.setLibJarFileNames(pluginMetadata.getId(), libFileNames.toArray(new String[0]));
				
				// done
				f.endTransaction();
			} catch (DAOException e) {
				f.abortTransaction();
				throw new ServletException("dao exception");
			}
			FileUtils.deleteDirectory(tempDir);
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("success", true);
			templateContext.put("nextPage", pageContext.getPageUrl(AdminPageFactory.SITE_NAME, AdminPageFactory.PLUGINS_PAGE));
			templateContext.put("nextPageParamName", "dummy");
			templateContext.put("nextPageParamValue", "nothing");
			pageContext.renderTemplate(template, templateContext);
		} else {
			String doString = pageContext.getParameter("do");
			if (doString != null && doString.equals("Delete")) {

			} else {
				throw new ServletException("Unexpected POST request");
			}
		}
	}

	public static final Attributes.Name PLUGIN_ID = new Attributes.Name(
	"BOSS-Plugin-Id");
	public static final Attributes.Name PLUGIN_NAME = new Attributes.Name(
	"BOSS-Plugin-Name");
	public static final Attributes.Name PLUGIN_AUTHOR = new Attributes.Name(
	"BOSS-Plugin-Author");
	public static final Attributes.Name PLUGIN_EMAIL = new Attributes.Name(
	"BOSS-Plugin-Email");
	public static final Attributes.Name PLUGIN_VERSION = new Attributes.Name(
	"BOSS-Plugin-Version");
	public static final Attributes.Name PLUGIN_DESCRIPTION = new Attributes.Name(
	"BOSS-Plugin-Description");
}
