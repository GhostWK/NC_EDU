/**
 * Created by USER on 21.07.2017.
 */
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import servlet.MainServlet;

public class Main {

    public static void main(String[] args) throws Exception {
        MainServlet testPage = new MainServlet();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(testPage), "/mainServlet");

        Server server = new Server(8080);
        HandlerList handlers = new HandlerList( );
        handlers.setHandlers( new Handler[] { context } );
        server.setHandler(handlers);
        server.start();
        server.join();
    }

}