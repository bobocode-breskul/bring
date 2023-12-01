package io.github.bobocodebreskul;

import io.github.bobocodebreskul.server.BringResponse;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;
import io.github.bobocodebreskul.server.enums.ResponseStatus;
import java.util.Map;

@RestController
@RequestMapping("/test-response")
public class ResponseController {
  @Get("/get")
  public BringResponse<String> testGet() {
    return BringResponse.ok("SUCCESS");
  }

  @Get("/get-vehicle")
  public BringResponse<Vehicle> testGetVehicle() {
    BringResponse<Vehicle> body = BringResponse.status(ResponseStatus.OK)
        .headers(Map.of("X-Content-Type", "bring/response"))
        .body(new Vehicle("Chevrolet", "Malibu", 2012, "LTS"));
    return body;
  }
}
