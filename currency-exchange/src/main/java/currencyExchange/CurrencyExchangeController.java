package currencyExchange;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class CurrencyExchangeController {

    @Autowired //dependency injection
    private CurrencyExchangeRepository repo;

    @Autowired
    private Environment environment;
    
    //localhost:8000/currency-exchange/from/EUR/to/RSD - request example
    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    @RateLimiter(name = "default")
    public ResponseEntity<?> getExchange(@PathVariable String from, @PathVariable String to) {

        String port = environment.getProperty("local.server.port"); //  accessing value of server.port property from application.properties
        CurrencyExchange kurs = repo.findByFromAndToIgnoreCase(from, to); // find this in database, based on from and to values

        if (kurs != null) {
            kurs.setEnvironment(port);
            return ResponseEntity.ok(kurs);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Requested currency exchange could not be found");
        }
        
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex) {
        return ResponseEntity.status(503).body("Currency exchange service can only serve up to 2 requests every 45 seconds");
    }
}
