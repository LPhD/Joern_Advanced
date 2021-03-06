package joern.plugins.importer;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileWalker.OrderedWalker;
import joern.api.JoernProject;
import joern.api.plugintypes.JoernProjectPlugin;

public class JoernImporter extends JoernProjectPlugin {

	private static final Logger logger = LoggerFactory.getLogger(JoernImporter.class);

	private boolean parsecode = true;
	private boolean importcsv = true;
	String projectPath;

	private JoernProject joernProject;

	@Override
	public void configure(JSONObject settings) {
		super.configure(settings);

		if (settings.has("noparsecode"))
			parsecode = false;
		if (settings.has("noimportcsv"))
			importcsv = false;
		
		if (settings.has("srcDir")) {		
			projectPath = settings.getString("srcDir");
		}
		else {
			System.out.println("No source found!");
		}
	}

	@Override
	public void execute() throws Exception {
		openProject();
		if (parsecode)
			parseSourceCode();
		if (importcsv)
			importCSVFilesIntoDatabase();		
	}

	private void openProject() {
		joernProject = (JoernProject) getProjectConnector().getWrapper();
	}


	private void parseSourceCode() {
		logger.warn("Parsing code");

		String parserOutputDirectory = joernProject.getParserOutputDirectory();
		//This path is not on the server
		String sourceCodeDirectory = projectPath;

		CParserWrapper parserWrapper = new CParserWrapper();
		parserWrapper.setMultiFileOutput(false);
		parserWrapper.initialize(parserOutputDirectory);
		parserWrapper.walkCodebase(new String[] { sourceCodeDirectory });

		logger.warn("Parsing complete");
	}

	private void importCSVFilesIntoDatabase() throws IOException {
		logger.warn("Importing graph");

		String parserOutputDirectory = joernProject.getParserOutputDirectory();

		OrderedWalker walker = new OrderedWalker();
		walker.setFilenameFilter("*nodes.csv");
		ImporterListener listener = new ImporterListener();
		listener.setProject(joernProject);

		walker.addListener(listener);
		walker.walk(new String[] { parserOutputDirectory });

		logger.warn("Import complete");
	}

}
