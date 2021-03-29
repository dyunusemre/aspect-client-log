package logging.service;

import logging.CreateLogDTO;
import logging.entity.HttpLog;
import logging.repository.HttpLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {
	private final HttpLogRepository httpLogRepository;

	@Async
	public void createLog(CreateLogDTO dto) {
		HttpLog log = HttpLog.builder()
				.userId(dto.getId())
				.userIdentifierType(dto.getUserIdentifierType())
				.serviceType(dto.getServiceType())
				.httpMethod(dto.getHttpMethod())
				.httpStatus(dto.getHttpStatus())
				.path(dto.getPath())
				.requestBody(dto.getRequest())
				.responseBody(dto.getResponse())
				.build();
		httpLogRepository.save(log);
	}
}
