package com.apm.stash;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StashFPwrapper {

	static Logger logger = Logger.getLogger(StashFPwrapper.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ParseException {

		// ******Configure log4J*****
		final Logger logger = Logger.getLogger(StashFPwrapper.class);
		{
			String log4jConfigFile = System.getProperty("user.dir") + File.separator + "stash.properties";
			PropertyConfigurator.configure(log4jConfigFile);
		}
		// Executer kicks of off run method @ set interval
		Runnable servers = new Runnable() {

			public void run() {

				String MetricRootLocation = (GetPropertiesFile.getPropertyValue("MetricLocation"));

				logger.debug("MetricLocation  = " + MetricRootLocation);

				// *****Create Metrics******
				// Array of actual Metrics WITHOUT the metrics KEY in front
				JSONArray metricArray = new JSONArray();

				// ******Get main WebService*******
				// Using Properties File to pass in 'callURL' parameters
				JSONObject mainWebServiceJSON = null;
				try {
					mainWebServiceJSON = (JSONObject) new JSONParser()
							.parse(WebServiceHandler.callURL(GetPropertiesFile.getPropertyValue("StashURL"),
									(GetPropertiesFile.getPropertyValue("StashUserName")),
									(GetPropertiesFile.getPropertyValue("StashPassword"))));
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// add SIZE to Metrics Using CreateMetric method
				// metricArray is what is used to build Introscope metrics
				metricArray.add(createMetric("LongCounter", MetricRootLocation + ":Number of Repos",
						mainWebServiceJSON.remove("size")));
				metricArray.add(
						createMetric("LongCounter", MetricRootLocation + ":Limit", mainWebServiceJSON.remove("limit")));

				// *****Grab Project & Repository info******

				JSONArray values = (JSONArray) mainWebServiceJSON.get("values");

				// ******For loop over the Number of Values*******
				// .size = Array size
				for (int i = 0; i < values.size(); i++) {

					JSONObject REPO = (JSONObject) values.get(i);
					String metricLocation = MetricRootLocation + "|" + ((JSONObject) REPO.get("project")).get("name")
							+ "|" + REPO.get("name");
							// Casting to Array

					// ********Get URL for each repository******
					String RepoURL = (String) ((JSONObject) ((JSONArray) ((JSONObject) REPO.get("links")).get("self"))
							.get(0)).get("href");

					// Get the PROJECT & REPO name
					String project = (String) ((JSONObject) REPO.get("project")).get("key");
					String repository = (String) REPO.get("name");
					// Update URL to Pull-Request URL
					// Split URL based on the "/"
					RepoURL = "http://" + RepoURL.split("/")[2] + "/rest/api/1.0/projects/" + project + "/repos/"
							+ repository + "/pull-requests";

					// *****Make Pull-Request WebService call*****
					// Call WebService, but give it new REPO URL
					JSONObject RepoJSON = null;
					try {
						RepoJSON = (JSONObject) new JSONParser().parse(WebServiceHandler.callURL(RepoURL,
								(GetPropertiesFile.getPropertyValue("StashUserName")),
								(GetPropertiesFile.getPropertyValue("StashPassword"))));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					logger.debug("Pull Request URLs  = " + RepoURL);

					long size = ((Long) RepoJSON.get("size")).intValue();

					// *** If RepoJSON Array does NOT have any pull request info
					// then skip that repository
					// if size not zero do this
					if (size != 0) {
						metricArray.add(createMetric("LongCounter", metricLocation + ":Pull Requests - Total",
								RepoJSON.remove("size")));
						metricArray.add(createMetric("StringEvent", metricLocation + ":Is Last Page",
								RepoJSON.remove("isLastPage")));
						metricArray
								.add(createMetric("LongCounter", metricLocation + ":Limit", RepoJSON.remove("limit")));

						JSONArray PullState = (JSONArray) RepoJSON.get("values");

						// ***Potential state a Pull Request can be in***
						int opens = 0;
						int merges = 0;
						int declines = 0;

						// ****Iterate over size of the PullState Array
						for (int m = 0; m < PullState.size(); m++) {

							JSONObject RepoPullState = (JSONObject) PullState.get(m);
							// Find Pull Request state by getting the
							// "state" key
							String PullStatus = (String) RepoPullState.get("state");

							if (PullStatus.matches("OPEN")) {
								opens++;
							}

							else if (PullStatus.matches("MERGE")) {
								merges++;
							}

							else if (PullStatus.matches("DECLINE")) {
								declines++;
							}

							else {
								logger.debug("State of Pull Request NOT Found");
							}
						}
						// Create metrics for Number of each Pull Request types
						metricArray.add(createMetric("LongCounter", metricLocation + ":Pull Requests - Open", opens));
						metricArray
								.add(createMetric("LongCounter", metricLocation + ":Pull Requests - Merged", merges));
						metricArray.add(
								createMetric("LongCounter", metricLocation + ":Pull Requests - Declined", declines));
						metricArray.add(createMetric("StringEvent", MetricRootLocation + ":PluginSuccess", ("YES")));
					}
				}

				JSONObject metricsToEPAgent = new JSONObject();
				// pre-pends "metrics" to the 'metricArray as this is required
				// by EPAgent API
				metricsToEPAgent.put("metrics", metricArray);

				// Using Properties File to pass in 'sendMetric' parameters
				WebServiceHandler.sendMetric(metricsToEPAgent.toString(),
						(GetPropertiesFile.getPropertyValue("EPAgentHost")),
						Integer.valueOf(GetPropertiesFile.getPropertyValue("EPAgentPort")));
			}

			public JSONObject createMetric(String type, String name, Object value) {

				JSONObject metric = new JSONObject();
				metric.put("type", type);
				metric.put("name", name);
				metric.put("value", value);
				return metric;

			}
		};

		// Executer to trigger runnable thread. Delay time is defined in the
		// stash.properties
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		executor.scheduleAtFixedRate(servers, 0, Integer.parseInt(GetPropertiesFile.getPropertyValue("delaytime")), TimeUnit.MINUTES);
	}
}