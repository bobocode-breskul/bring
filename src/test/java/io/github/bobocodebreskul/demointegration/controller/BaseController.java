package io.github.bobocodebreskul.demointegration.controller;

import io.github.bobocodebreskul.server.annotations.Delete;
import io.github.bobocodebreskul.server.annotations.Get;
import io.github.bobocodebreskul.server.annotations.Head;
import io.github.bobocodebreskul.server.annotations.Post;
import io.github.bobocodebreskul.server.annotations.Put;
import io.github.bobocodebreskul.server.annotations.RequestBody;
import io.github.bobocodebreskul.server.annotations.RequestMapping;
import io.github.bobocodebreskul.server.annotations.RestController;

@RestController
// TODO wrong behavior on empty if in method start with slash
@RequestMapping("/")
public class BaseController {

  @Get
  public String doGet() {
    return "BaseController Get Method";
  }

  @Post
  public String doPost() {
    return "BaseController Post Method";
  }

  @Put
  public String doPut() {
    return "BaseController Put Method";
  }

  @Head
  public String doHead() {
    return "BaseController Head Method";
  }

  @Delete
  public String doDelete() {
    return "BaseController Delete Method";
  }

  @Post("withRequestBody")
  public String doPostWithRequestBody(@RequestBody RequestDto dto) {
    return dto.getString() + dto.getInteger();
  }

  @Get("withRequestBody")
  public String doGetWithRequestBody(@RequestBody RequestDto dto) {
    return dto.getString() + dto.getInteger();
  }

  @Delete("withRequestBody")
  public String doDeleteWithRequestBody(@RequestBody RequestDto dto) {
    return dto.getString() + dto.getInteger();
  }

  @Head("withRequestBody")
  public String doHeadWithRequestBody(@RequestBody RequestDto dto) {
    return dto.getString() + dto.getInteger();
  }
}
