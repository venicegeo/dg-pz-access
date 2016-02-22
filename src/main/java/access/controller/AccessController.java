/**
 * Copyright 2016, RadiantBlue Technologies, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package access.controller;

import java.util.List;

import model.data.DataResource;
import model.response.DataResourceResponse;
import model.response.ErrorResponse;
import model.response.PiazzaResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import util.PiazzaLogger;
import access.database.MongoAccessor;

/**
 * Allows for synchronous fetching of Resource Data from the Mongo Resource
 * collection.
 * 
 * The collection is bound to the DataResource model.
 * 
 * This controller is similar to the functionality of the JobManager REST
 * Controller, in that this component primarily listens for messages via Kafka,
 * however, for instances where the user needs a direct read out of the database
 * - this should be a synchronous response that does not involve Kafka. For such
 * requests, this REST controller exists.
 * 
 * @author Patrick.Doody
 * 
 */
@RestController
public class AccessController {
	@Autowired
	private PiazzaLogger logger;
	@Autowired
	private MongoAccessor accessor;
	private static final String DEFAULT_PAGE_SIZE = "10";
	private static final String DEFAULT_PAGE = "0";

	/**
	 * Returns the Data resource object from the Resources collection.
	 * 
	 * @param dataId
	 *            ID of the Resource
	 * @return The resource matching the specified ID
	 */
	@RequestMapping(value = "/data/{dataId}", method = RequestMethod.GET)
	public PiazzaResponse getData(@PathVariable(value = "dataId") String dataId) {
		try {
			if (dataId.isEmpty()) {
				throw new Exception("No Data ID specified.");
			}
			// Query for the Data ID
			DataResource data = accessor.getData(dataId);
			if (data == null) {
				logger.log(String.format("Data not found for requested ID %s", dataId), PiazzaLogger.WARNING);
				return new ErrorResponse(null, String.format("Data not found: %s", dataId), "Access");
			}

			// Return the Data Resource item
			logger.log(String.format("Returning Data Metadata for %s", dataId), PiazzaLogger.INFO);
			return new DataResourceResponse(data);
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.log(String.format("Error fetching Data %s: %s", dataId, exception.getMessage()), PiazzaLogger.ERROR);
			return new ErrorResponse(null, "Error fetching Data: " + exception.getMessage(), "Access");
		}
	}

	/**
	 * Returns all Data held by the Piazza Ingest/Access components. This
	 * corresponds with the items in the Mongo db.Resources collection.
	 * 
	 * This is intended to be used by the Swiss-Army-Knife (SAK) administration
	 * application for reporting the status of this Job Manager component. It is
	 * not used in normal function of the Job Manager.
	 * 
	 * @return The list of all data held by the system.
	 */
	@RequestMapping(value = "/data", method = RequestMethod.GET)
	public List<DataResource> getAllData(
			@RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE) String page,
			@RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_PAGE_SIZE) String pageSize) {
		return accessor.getDataResourceCollection().find().skip(Integer.parseInt(page) * Integer.parseInt(pageSize))
				.limit(Integer.parseInt(pageSize)).toArray();
	}

	/**
	 * Returns the Number of Data Resources in the piazza system.
	 * 
	 * This is intended to be used by the Swiss-Army-Knife (SAK) administration
	 * application for reporting the status of this Job Manager component. It is
	 * not used in normal function of the Job Manager.
	 * 
	 * @return Number of Data items in the system.
	 */
	@RequestMapping(value = "/data/count", method = RequestMethod.GET)
	public long getDataCount() {
		return accessor.getDataResourceCollection().count();
	}

	/**
	 * Drops the Mongo collections. This is for internal development use only.
	 * We should probably remove this in the future. Don't use this.
	 */
	@RequestMapping(value = "/drop")
	public String dropAllTables(@RequestParam(value = "serious", required = false) Boolean serious) {
		if ((serious != null) && (serious.booleanValue())) {
			accessor.getDataResourceCollection().drop();
			accessor.getLeaseCollection().drop();
			accessor.getDeploymentCollection().drop();
			return "Collections dropped.";
		} else {
			return "You're not serious.";
		}
	}
}
