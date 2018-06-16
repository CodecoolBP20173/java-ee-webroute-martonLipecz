package com.codecool.webroute;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Router {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestedPath = exchange.getRequestURI().getPath();

            Class router = null;
            try {
                router = Class.forName("com.codecool.webroute.Router");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Method[] allMethods = router.getMethods();
            for (Method method : allMethods) {
                if(method.getAnnotation(WebRoute.class) != null){
                    WebRoute annotation = (WebRoute) method.getAnnotation(WebRoute.class);
                    if (annotation.path().equals(requestedPath)){
                        try {
                            method.invoke(null, exchange);
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }

    @WebRoute(path="/foo")
    static public void handleFoo(HttpExchange t) throws IOException{
        String response = "Foo";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @WebRoute(path="/bar")
    static public void handleBar(HttpExchange t) throws IOException{
        String response = "Bar";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
