package io.wahid.publication;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Hello from servlet api-----------------------------------------------------------");
//        JwtContext ctx = (JwtContext) req.getAttribute("jwt.context");
//
//        req.getAttributeNames().asIterator().forEachRemaining(System.out::println);
//        if (ctx == null) {
//            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return;
//        }
//
//        // Example: role-based check
//        if (!ctx.roles.contains("admin")) {
//            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            resp.getWriter().write("Access denied");
//            return;
//        }

        resp.setStatus(HttpServletResponse.SC_OK);
//        resp.getWriter().write("Hello " + ctx.uid + " from tenant " + ctx.tenantId);
    }
}
