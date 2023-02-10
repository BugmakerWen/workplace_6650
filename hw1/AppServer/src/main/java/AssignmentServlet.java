import Model.Info;
import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "AssignmentServlet", value = "/AssignmentServlet")
public class AssignmentServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    PrintWriter writer = res.getWriter();

    //check if the URL is empty
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("Missing parameter!");
      writer.close();
      return;
    }

    // check if the URL is valid
    String[] urlParts = urlPath.split("/");

    //valid url
    if(validUrl(urlParts)) {
      if(urlParts[1].equals("left")){
        res.setStatus(HttpServletResponse.SC_CREATED);
        Info input = processRequest(req, res);
        writer.write(input.getComment());
      }else if(urlParts[1].equals("right")) {
        res.setStatus(HttpServletResponse.SC_CREATED);
        Info input = processRequest(req, res);
        writer.write(input.getComment());
      }else {
        // invalid request
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.write("Invalid request");
      }
    }else {
      // invalid url
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("Invalid request");
      writer.close();
      return;
    }

  }

  private boolean validUrl(String[] urlParts) {
    if(urlParts.length == 2 && urlParts[0].equals(""))
      return true;
    return false;
  }

  protected Info processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Gson gson = new Gson();
    Info reqBody = new Info();

    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }
      reqBody = (Info) gson.fromJson(sb.toString(), Info.class);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return reqBody;
  }

}
