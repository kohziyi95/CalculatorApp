package sg.nus.iss.mockassessment.calculatorapp.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

@RestController
@RequestMapping(path="/calculate")  
public class CalculatorController {

    @PostMapping(consumes="application/json")
    public ResponseEntity<String> showResults(@RequestBody String payload, @RequestHeader("user-agent") String userAgent){
        JsonObject body;
        try (InputStream is = new ByteArrayInputStream(payload.getBytes())){
            JsonReader reader = Json.createReader(is);
            body = reader.readObject();
        } catch (Exception e) {
            body = Json.createObjectBuilder().add("error", e.getMessage()).build();
        }

        int value1 = body.getInt("oper1");
        int value2 = body.getInt("oper2");
        String operation = body.getString("ops");
        
        int result = 0;
        switch(operation) {
            case "plus":
                result = value1 + value2;
                break;
            case "minus":
                result = value1 - value2;
                break;
            case "multiply":
                result = value1 * value2;
                break;
            case "divide":
                try {
                    result = value1 / value2;
                } catch (ArithmeticException e) {
                    System.err.println("Error: Division by 0 not allowed");;
                }
                break;
            default:
                break;
            }

            String timestamp = Long.toString(System.currentTimeMillis());

            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            JsonObject response = objectBuilder
                .add("result", result)
                .add("timestamp", timestamp)
                .add("userAgent", userAgent)
                .build();
            
        return ResponseEntity.ok(response.toString());
    }
}
