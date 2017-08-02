package servlet;

import service.ParserReq;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;


/**
 * Created by USER on 20.07.2017.
 */
public class MainServlet extends HttpServlet{
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        PrintWriter w = resp.getWriter();
//        w.print("<h1>Hello Servlet</h1>");
//    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //System.out.println(req.getContentType());
        resp.getWriter().write(ParserReq.parse(req));

    }
}
