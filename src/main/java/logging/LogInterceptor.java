package logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import logging.enums.ServiceType;
import logging.service.LogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class LogInterceptor {

	private final LogService logService;

	@Pointcut("@annotation(loggable)")
	public void loggableMethods(Loggable loggable) {
	}


	@Around(value = "loggableMethods(loggable)", argNames = "joinPoint,loggable")
	public Object auditBefore(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
		Object response = joinPoint.proceed();
		createLog(joinPoint, loggable, response, String.valueOf(HttpStatus.OK.value()), false);
		return response;
	}

	@AfterThrowing(pointcut = "loggableMethods(loggable)", throwing = "exception", argNames = "joinPoint,loggable,exception")
	public void logAfterThrowing(JoinPoint joinPoint, Loggable loggable, Throwable exception) throws Throwable {
		int status = exception instanceof FeignException ? ((FeignException) exception).status() : HttpStatus.NOT_FOUND.value();
		createLog(joinPoint, loggable, exception.toString(), String.valueOf(status), true);
	}

	private void createLog(JoinPoint joinPoint, Loggable loggable, Object response, String status, boolean isExceptionLog) throws Throwable {
		CreateLogDTO dto = CreateLogDTO.builder()
				.request(getRequestAsString(joinPoint, loggable.serviceType()))
				.response(getResponseAsString(response, loggable.serviceType(), isExceptionLog))
				.serviceType(loggable.serviceType())
				.userIdentifierType(loggable.userIdentifier())
				.httpMethod(getHttpMethodType(joinPoint))
				.id(getUserIdentifier(loggable, joinPoint))
				.path(getFullPath(joinPoint))
				.httpStatus(status)
				.build();
		logService.createLog(dto);
	}

	private String getUserIdentifier(Loggable loggable, JoinPoint joinPoint) {
		EvaluationContext context = new StandardEvaluationContext();
		Map<String, Object> paramMap = getRequestMap(joinPoint);
		for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
			context.setVariable(entry.getKey(), entry.getValue());
		}
		ExpressionParser expressionParser = new SpelExpressionParser();
		Expression expression = expressionParser.parseExpression(loggable.userId());
		return String.valueOf(expression.getValue(context));
	}

	private Method getMethod(JoinPoint joinPoint) {
		return ((MethodSignature) joinPoint.getSignature()).getMethod();
	}

	private String getRequestAsString(JoinPoint joinPoint, ServiceType serviceType) throws Throwable {
		ObjectMapper objectMapper = new ObjectMapper();
		if (ServiceType.SOAP.equals(serviceType) && joinPoint.getArgs().length > 0) {
			return stringToSoap(joinPoint.getArgs()[0]);
		}
		return objectMapper.writeValueAsString(getRequestMap(joinPoint));
	}

	private String stringToSoap(Object jaxbObject) throws JAXBException, ParserConfigurationException, IOException, SAXException, SOAPException {
		JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter out = new StringWriter();
		marshaller.marshal(jaxbObject, out);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		InputStream streamOfBody = new ByteArrayInputStream(out.toString().getBytes());
		Document doc = factory.newDocumentBuilder().parse(streamOfBody);
		SOAPMessage message = MessageFactory.newInstance().createMessage();
		message.getSOAPBody().addDocument(doc);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		message.writeTo(stream);
		return stream.toString();
	}

	private String getResponseAsString(Object result, ServiceType serviceType, boolean isExceptionLog) throws Throwable {
		ObjectMapper objectMapper = new ObjectMapper();
		if (ServiceType.SOAP.equals(serviceType) && result != null && !isExceptionLog) {
			return stringToSoap(result);
		}
		return objectMapper.writeValueAsString(result);
	}

	private Map<String, Object> getRequestMap(JoinPoint joinPoint) {
		Object[] objs = joinPoint.getArgs();
		String[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
		Map<String, Object> paramMap = new HashMap<>();
		for (int i = 0; i < objs.length; i++) {
			paramMap.put(argNames[i], objs[i]);
		}
		return paramMap;
	}

	private String getFullPath(JoinPoint joinPoint) {
		return getUrl(joinPoint) + getRequestPath(joinPoint);
	}

	private String getUrl(JoinPoint joinPoint) {
		return getMethod(joinPoint).getDeclaringClass().getAnnotation(FeignClient.class).url();
	}

	private String getHttpMethodType(JoinPoint joinPoint) {
		return getMethod(joinPoint).getAnnotation(RequestMapping.class).method()[0].name();
	}

	private String getRequestPath(JoinPoint joinPoint) {
		String[] value = getMethod(joinPoint).getAnnotation(RequestMapping.class).value();
		String path = value.length > 0 	? value[0] : "";
		for (Map.Entry<String, Object> entry : getRequestMap(joinPoint).entrySet()) {
			if (path.contains(entry.getKey())) {
				path = path.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue().toString());
			}
		}
		return path;
	}

}
