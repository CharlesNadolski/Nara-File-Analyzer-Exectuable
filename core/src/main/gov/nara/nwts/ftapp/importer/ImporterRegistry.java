package gov.nara.nwts.ftapp.importer;

import gov.nara.nwts.ftapp.FTDriver;

import java.util.Vector;

/**
 * Activates the Importers that will be presented on the Import tab.
 * @author TBrady
 *
 */
public class ImporterRegistry extends Vector<Importer> {
	
	private static final long serialVersionUID = 1L;

	public ImporterRegistry(FTDriver dt) {
		add(new DelimitedFileImporter(dt));
		add(new Parser(dt));
	}
	

}
