package de.hoehne.preperation;

import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

public class TestSolrResultExtractor {

	@Test
	public void testCSV() throws Exception {
		try {
			Reader in = new FileReader("./src/test/resources/test_solr_result.csv");
			Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : records) {
				String relativeUri = record.get("relativeUri");
				String mmsArtNo = record.get("mmsArtNo");
				String variantNo = record.get("variantNo");
				String articleDescription = record.get("dyn_articleDescription");

				System.out.println(relativeUri);
				System.out.println(articleDescription + " - " + mmsArtNo + "/" + variantNo);

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}
}
