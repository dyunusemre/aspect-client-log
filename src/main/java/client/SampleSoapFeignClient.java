package client;


import logging.Loggable;
import logging.enums.ServiceType;
import logging.enums.UserIdentifierType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "sample-soap", url = "https://sample-soap-url/")
public interface SampleSoapFeignClient {

	@Loggable(serviceType = ServiceType.SOAP, userIdentifier = UserIdentifierType.SECURITY_NUMBER, userId = "#personObject.securityNumber")
	@RequestMapping(method = RequestMethod.POST, value = "/sample-soap-operation", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
	Object sampleSoapClientOperation(@RequestBody PersonObject personObject);

}
