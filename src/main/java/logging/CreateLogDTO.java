package logging;


import logging.enums.ServiceType;
import logging.enums.UserIdentifierType;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateLogDTO {

	private UserIdentifierType userIdentifierType;
	private ServiceType serviceType;
	private String id;
	private String httpMethod;
	private String httpStatus;
	private String path;
	private String request;
	private String response;

}
