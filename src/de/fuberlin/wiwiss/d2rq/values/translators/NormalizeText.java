package de.fuberlin.wiwiss.d2rq.values.translators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fuberlin.wiwiss.d2rq.values.Translator;

/**
 * This class is conceived to normalize text values from a database table, which
 * may have issues. Example: "QUESTO E' UN TESTO." should be translated to
 * "Questo è un testo."
 * 
 * #) sostituzione a' con à, A' con Agrave, etc (sempre gravi?) #) tutto
 * lowercase #) maiuscole dopo il punto (con/senza spazi) #) prima lettera
 * maiuscola
 * 
 * VEDI: Normalizer.normalize(nText, Normalizer.Form.NFC);
 * nText.replaceAll("(.*?)\\.(\\s+)([a-z])(.*?)", "$1.$2"+ "$3".toUpperCase() +
 * "$4");
 * 
 * @author seralf
 * 
 * 
 * 
 *         TODO: RIFATTORIZZARE
 * 
 */
public class NormalizeText implements Translator {

	private static final Logger logger = LoggerFactory.getLogger(NormalizeText.class);

	// this map will contain the mappings for specific String values to replace
	private final Map<String, String> map = new HashMap<String, String>();

	// TODO: externalize this settings. Where?
	private boolean liveEditing = true;
	private boolean useMappings = true;
	private String mapping_file = "text_mappings.txt";
	private File mappingFile;

	public NormalizeText() {
		try {
			this.setMappingFile(mapping_file);
			loadMap();
		} catch (final IOException e) {
			logger.error("Problem reading the mapping file: " + mapping_file, e);
		}
	}

	private void loadMap() throws FileNotFoundException {
		map.clear();
		final Scanner sc = new Scanner(this.mappingFile);
		while (sc.hasNextLine()) {
			final String line = sc.nextLine();
			if (line != null && !line.trim().equals("")
					&& !line.startsWith("#")) {
				String[] pair = line.split("=");
				map.put(pair[0].trim(), pair[1].trim());
			}
		}
		sc.close();
	}

	/**
	 * this method is useful to set the path of the mapping file
	 * 
	 * @param mappingFile
	 * @throws IOException
	 */
	public void setMappingFile(final String mappingFile) throws IOException {
		this.mappingFile = new File(mappingFile).getCanonicalFile();
	}

	// TODO: split into multiple methods

	private static enum NORMALIZE {
		LOWERCASE, UPPERCASE, CAPITALIZATION
	}

	private String normalizeText(final String text, final NORMALIZE options) {
		switch (options) {
			case LOWERCASE: return text.toLowerCase();
			case UPPERCASE: return text.toLowerCase();
			case CAPITALIZATION: throw new RuntimeException("CAPITALIZATION method not yet implemented!");
			default: throw new RuntimeException("Problem with text capitalization");
		}
	}

	/**
	 * This method replace text values inside a String using the mapping file.
	 * @param originalValue
	 * @return
	 */
	private String replaceWithMapping(final String originalValue) {
		// TODO: avoid String replacement by using StringBuffer or similar methods
		String text = originalValue;
		if (this.liveEditing)
			try {
				this.loadMap();
			} catch (final FileNotFoundException e) {
				logger.error("Cannot find mapping file: " + mapping_file, e);
			}

		// TODO: avoid replacement of String using StringBuffer or similar approaches
		
			if (this.useMappings) {
				for (String k : this.map.keySet()) {
					text = text.replace(k, this.map.get(k));
				}
			}
		
		return text;
	}
	
	/**
	 * This method normalize the textual value to be inserted in RDF
	 */
	public String toRDFValue(final String dbValue) {

		if (dbValue != null && !dbValue.trim().equals("")) {
			// String text = dbValue.toLowerCase(); 
			String text = this.normalizeText(dbValue, NORMALIZE.LOWERCASE);
			text = replaceWithMapping(text);
			return text;
		}
		
		return dbValue;		
	}

	/**
	 * This method is currently DISABLED, and generated an exception if/when
	 * called.
	 */
	public String toDBValue(final String rdfValue) {
		// DISABLED!
		throw new RuntimeException("ERROR: the method NormalizeText.toDBValue() has not been implemented.");
	}

}
