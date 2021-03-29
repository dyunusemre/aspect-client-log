package logging.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import logging.enums.ServiceType;
import logging.enums.UserIdentifierType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "HTTP_LOG", indexes = {})
@SequenceGenerator(name = "SEQ", allocationSize = 1)
public class HttpLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ")
	private Long id;

	@Enumerated(EnumType.STRING)
	private UserIdentifierType userIdentifierType;

	private String userId;

	@Enumerated(EnumType.STRING)
	private ServiceType serviceType;

	private String httpMethod;

	private String httpStatus;

	private String path;

	private String requestBody;

	private String responseBody;

}
