package io.github.bobocodebreskul.server;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/")
public class DispatcherServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String servletPath = req.getServletPath();
    resp.setStatus(200);
    PrintWriter writer = resp.getWriter();
    writer.println(servletPath);
    writer.flush();
  }
}
