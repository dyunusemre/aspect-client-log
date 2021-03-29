package client;

import logging.Loggable;
import logging.enums.UserIdentifierType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(value = "sample-rest", url = "https://sample-rest-url/")
public interface SampleRestClient {

	@Loggable(userIdentifier = UserIdentifierType.USER_ID, userId = "#userObject.userId")
	@RequestMapping(method = RequestMethod.GET, value = "/sample-rest-operation")
	Object sampleRestClientOperation(@RequestBody UserObject userObject);

}
