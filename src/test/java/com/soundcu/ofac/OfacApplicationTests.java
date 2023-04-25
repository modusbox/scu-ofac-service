package com.soundcu.ofac;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class OfacApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;
	@Test
	void testSendAnInvalidRequest()
	{
		ResponseEntity<String> wire = restTemplate.postForEntity("/verify/ofac", null, String.class);
		Assertions.assertEquals(HttpStatus.BAD_REQUEST,wire.getStatusCode());
		Assertions.assertNotNull(wire.getBody());
		Assertions.assertTrue(wire.getBody().contains("Required request header 'user_id' for method parameter type String is not present"));
	}
}
