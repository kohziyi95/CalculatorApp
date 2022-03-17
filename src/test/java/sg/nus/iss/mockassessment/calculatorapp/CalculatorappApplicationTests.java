package sg.nus.iss.mockassessment.calculatorapp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import sg.nus.iss.mockassessment.calculatorapp.controller.CalculatorController;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorappApplicationTests {
	
	@Autowired
	private MockMvc mvc;

	@Autowired
	private CalculatorController controller;

	@Test
	void contextLoads(){
		Assertions.assertNotNull(controller);
	}

	@Test
	void shouldReturnCorrectResult() throws Exception{
		Random rnd = new SecureRandom();
		int oper1 = rnd.nextInt(-50,50);
		int oper2 = rnd.nextInt(-50,50);

		JsonObject payload = Json.createObjectBuilder()
			.add("oper1", oper1)
			.add("oper2", oper2)
			.add("ops", "plus")
			.build();

		RequestBuilder req = MockMvcRequestBuilders.post("/calculate")
			.header("User-Agent","junit")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(payload.toString());

		MvcResult resp = mvc.perform(req).andReturn();
		MockHttpServletResponse httpResp = resp.getResponse();

		assertEquals(200,httpResp.getStatus());

		Optional<JsonObject> opt = string2Json(httpResp.getContentAsString());

		JsonObject obj = opt.get();
		for(String s: List.of("result", "timestamp", "userAgent"))
			assertFalse(obj.isNull(s));

		assertEquals(oper1 + oper2, obj.getInt("result"));
	}

	@Test
	void shouldReturnBadResult() throws Exception{
		Random rnd = new SecureRandom();
		int oper1 = rnd.nextInt(-50,50);
		int oper2 = rnd.nextInt(-50,50);

		JsonObject payload = Json.createObjectBuilder()
			.add("oper1", oper1)
			.add("oper2", oper2)
			.add("ops", "abc")
			.build();

		RequestBuilder req = MockMvcRequestBuilders.post("/calculate")
			.header("User-Agent","junit")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(payload.toString());

		MvcResult resp = mvc.perform(req).andReturn();
		MockHttpServletResponse httpResp = resp.getResponse();

		// Only if the controller throws an Exception
		// assertThrowsExactly(
		// 	NestedServletException.class,
		// 	() -> {
		// 		mvc.perform(req);
		// 	});


		assertTrue(httpResp.getStatus() >= 400);
	}



	public static Optional<JsonObject> string2Json(String s){
		JsonObject body = null;
        try (InputStream is = new ByteArrayInputStream(s.getBytes())){
            JsonReader reader = Json.createReader(is);
            return Optional.of(reader.readObject());
        } catch (Exception e) {
            System.err.printf("Error: $s\n", e.getMessage());
			return Optional.empty();
        }
	}
}
