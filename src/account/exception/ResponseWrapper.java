package account.exception;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class ResponseWrapper extends HttpServletResponseWrapper {
  private CharArrayWriter charArrayWriter = new CharArrayWriter();

  public ResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public PrintWriter getWriter() {
    return new PrintWriter(charArrayWriter);
  }

  public String getResponseBody() {
    return charArrayWriter.toString();
  }
}
