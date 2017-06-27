package de.hoehne.preperation;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONObject;

import de.mgi.mms.util.tuples.Triple;
import de.mgi.mms.util.tuples.Tuple;

public class ExtractTree {

	public static void main(String[] args) throws Exception {

		final long start = System.currentTimeMillis();
		final ProductHierarchy products = extract();

		queryArticles(products);

		toJSON(products);

		System.out.println(System.currentTimeMillis() - start);
		System.out.println(products.countArticls.get());

	}

	private static void toJSON(final ProductHierarchy products) {
		StringBuilder result = new StringBuilder("var articleData = [\n");

		products.getContainer().forEach((levelName_1, level_2) -> {
			result.append("    {\n");
			result.append("        text: '" + levelName_1 + "',\n");
			result.append("        nodes: [\n");

			level_2.forEach((levelName_2, level_3) -> {
				result.append("            {\n");
				result.append("                text: '" + levelName_2 + "',\n");
				result.append("                nodes: [\n");

				level_3.forEach((levelName_3, level_4) -> {
					result.append("                    {\n");
					result.append("                        text: '" + levelName_3 + "',\n");
					result.append("                        nodes: [\n");

					level_4.forEach((levelName_4, values) -> {
						result.append("                            {\n");
						result.append("                                text: '" + levelName_4 + "',\n");
						result.append("                                solr: true,\n");
						result.append("                                tags: ['" + (ProductHierarchy.maxNumberOfArticles > values.products.size() ? "" : ">") + values.products.size() + "'],\n");
						result.append("                                href: '"
								+ values.solrUrl.toString().replaceAll("wt\\=csv", "wt=json") + "',\n");
						result.append("                                nodes: [\n");

						values.products.stream().forEach(article -> {
							result.append("                                    {\n");
							result.append("                                        text: '" + article.getSecond()
									.replaceAll("'", " ").replaceAll("\"", " ").replaceAll("\\\\", " ") + "',\n");
							result.append(
									"                                        href: 'http://iappl4.mgi.de:11100/artcache/all/mcc/v1/articlecache/"
											+ article.getFirst() + "'\n");

							result.append("                                    },\n");
						});

						// closing level 4
						if (!values.products.isEmpty())
							result.deleteCharAt(result.length() - 2);
						result.append("                                ]\n");
						result.append("                            },\n");
					});

					// closing level 3
					result.deleteCharAt(result.length() - 2);
					result.append("                        ]\n");
					result.append("                    },\n");
				});

				// closing level 2
				result.deleteCharAt(result.length() - 2);
				result.append("                ]\n");
				result.append("            },\n");
			});

			// closing level 1
			result.deleteCharAt(result.length() - 2);
			result.append("        ]\n");
			result.append("    },\n");
		});

		result.deleteCharAt(result.length() - 2);
		result.append("];\n");

		System.out.println(result);
	}

	private static void queryArticles(final ProductHierarchy products) {
		products.getAllProducts().//
				stream().//
				// parallel().//
				forEach(node -> {

					final Client client = ClientBuilder.newClient();
					WebTarget target = null;
					try {
						final URL url = node.solrUrl;

						target = client.target(new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
								url.getPort(), url.getPath(), url.getQuery(), null));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					final String response = target.request().accept(MediaType.TEXT_PLAIN).get(String.class);

					Iterable<CSVRecord> records;
					try {
						records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(new StringReader(response));
						for (CSVRecord record : records) {
							String relativeUri = record.get("relativeUri");
							String mmsArtNo = record.get("mmsArtNo");
							String variantNo = record.get("variantNo");
							String articleDescription = record.get("dyn_articleDescription");

							products.countArticls.incrementAndGet();
							node.products.add(new Tuple<String, String>(relativeUri,
									articleDescription + " - " + mmsArtNo + "/" + variantNo));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				});
	}

	public static ProductHierarchy extract() throws Exception {

		return Files.lines(Paths.get("./src/main/resources/ProductHierarchy.csv"))//
				.collect(ProductHierarchy::new, ProductHierarchy::put, ProductHierarchy::putAll);

	}

	public static class ProductHierarchy {

		AtomicInteger countArticls = new AtomicInteger(0);
		static int maxNumberOfArticles = 20;

		public static class Products {
			Triple<Integer, Integer, Integer> filter = null;
			String solrString = "http://solr-artcache.was.metro.info:20100/solr-artcache/all/mcc/v1/solr-artcache_all_mcc_v3/select?q=dynLong_merchGroupMain:\"{1}\"+AND+dynLong_merchGroup:\"{2}\"+AND+dynLong_merchGroupSub:\"{3}\"+&fq=scope:+\"erp_ihds_tree\"+&fq=country:+\"fr\"+&fq=bundleNo:\"1\"+&rows="
					+ ProductHierarchy.maxNumberOfArticles + "&wt=csv&indent=\"true\"";
			URL solrUrl = null;
			Set<Tuple<String, String>> products = new HashSet<>();

			public Products(Triple<Integer, Integer, Integer> filter) {
				this.filter = filter;
				solrString = solrString.replaceAll("\\{1\\}", filter.getFirst().toString())
						.replaceAll("\\{2\\}", filter.getSecond().toString())
						.replaceAll("\\{3\\}", filter.getThird().toString());
				try {
					solrUrl = new URL(solrString);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public String toString() {
				return filter.toString() + " " + solrUrl + products;
			}
		}

		// level I -> level II -> level III -> level IV -> Values
		private Map<String, Map<String, Map<String, Map<String, Products>>>> container = new HashMap<>();

		private Set<Products> allProducts = new HashSet<>();

		/**
		 * e.g.: Entertainment & Communication ;Audio & Hi-Fi;HEADPHONES;ON-EAR
		 * HEADPHONES;668;040;010
		 * 
		 * or
		 * 
		 * Printing;Cartridge;;;679;020;015
		 * 
		 * @param word
		 */
		public void put(String line) {
			final String[] parts = line.split(";");
			String first = parts[0].trim();
			first = first.equals("") ? "-" : first;

			String second = parts[1].trim();
			second = second.equals("") ? "-" : second;

			String third = parts[2].trim();
			third = third.equals("") ? "-" : third;

			String fourth = parts[3].trim();
			fourth = fourth.equals("") ? "-" : fourth;

			Triple<Integer, Integer, Integer> values = new Triple<Integer, Integer, Integer>(
					Integer.parseInt(parts[4].trim()), Integer.parseInt(parts[5].trim()),
					Integer.parseInt(parts[6].trim()));

			Map<String, Products> level_4 = null;
			Map<String, Map<String, Products>> level_3 = null;
			Map<String, Map<String, Map<String, Products>>> level_2 = null;

			if (container.get(first) == null)
				container.put(first, new TreeMap<>());
			level_2 = container.get(first);

			if (level_2.get(second) == null)
				level_2.put(second, new TreeMap<>());
			level_3 = level_2.get(second);

			if (level_3.get(third) == null)
				level_3.put(third, new TreeMap<>());
			level_4 = level_3.get(third);

			final Products myProduct = new Products(values);
			level_4.put(fourth, myProduct);
			allProducts.add(myProduct);
		}

		public void putAll(ProductHierarchy map) {
			throw new UnsupportedOperationException();
		}

		public Map<String, Map<String, Map<String, Map<String, Products>>>> getContainer() {
			return container;
		}

		public Set<Products> getAllProducts() {
			return allProducts;
		}

	}

}
