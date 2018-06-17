package com.codecool.webroute;

import java.io.*;
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
            MethodType requestMethod = MethodType.identify(exchange.getRequestMethod());

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
                    if (annotation.path().equals(requestedPath) && annotation.method().equals(requestMethod)){
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
        String response = "<html><form target='/bar' method='post'>" +
                "<input name='message1' type='text' value='TADAMM'><br>" +
                "<input name='message2' type='text' value='VOILA'><br>" +
                "<input type='submit' value='SEND'>" +
                "</form></html>";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @WebRoute(path="/bar", method = MethodType.POST)
    static public void handleBarPOST(HttpExchange t) throws IOException{
        StringBuilder response = new StringBuilder();
        new BufferedReader(new InputStreamReader(t.getRequestBody()))
                .lines()
                .forEach( (String s) -> response.append(s + "\n") );
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

}
