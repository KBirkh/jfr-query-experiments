package me.bechberger.jfr.tool;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.CorsPluginConfig;
import me.bechberger.jfr.wrap.Evaluator;
import me.bechberger.jfr.wrap.Lexer;
import me.bechberger.jfr.wrap.Parser;
import me.bechberger.jfr.wrap.nodes.ProgramNode;

public class JFRAPI {
    private Evaluator evaluator;
    private String recordingFile;
    private Javalin app;
    private final int port;
    
    public JFRAPI(String file) {
        this(8080, file);
    }
    
    public JFRAPI(int port, String file) {
        this.port = port;
        evaluator = Evaluator.getInstance();
        recordingFile = file;
        setupServer();
    }
    
    private void setupServer() {
        app = Javalin.create(config -> {
            // Configure CORS
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });
        });
        
        // Add the query endpoint
        app.post("/query", this::handleQuery);
        app.get("/query", this::handleQuery);
        
        // Add a simple health check endpoint
        app.get("/health", ctx -> ctx.result("JFR API Server is running"));

        app.get("/showEvents", this::handleShowEvents);
    }
    
    public void start() {
        app.start(port);
        System.out.println("JFR API Server started on port " + port);
        System.out.println("POST endpoint available at: http://localhost:" + port + "/query");
        System.out.println("GET endpoint available at: http://localhost:" + port + "/showEvents");
        System.out.println("Health check available at: http://localhost:" + port + "/health");
    }
    
    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private void handleShowEvents(Context ctx) {
      try {
          QueryCommand queryCommand = new QueryCommand();
          PrintStream old = System.out;
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          PrintStream ps = new PrintStream(baos);
          System.setOut(ps);

          queryCommand.setView("SHOW EVENTS");
          queryCommand.setFile(recordingFile);
          queryCommand.setConfigOptions(new ConfigOptions());
          try {
              queryCommand.call();
              System.out.flush();
              System.setOut(old);
            } catch (Exception e) {
              System.out.flush();
              System.setOut(old);
              e.printStackTrace();
          }
          String output = baos.toString();
          String[] lines = output.split("\n");
          String[][] events = new String[lines.length][2];
          for(int i = 2; i < lines.length; i++) {
            String line = lines[i];
            line = line.trim();
            String[] parts = line.split("  ", 2);
            if(parts.length == 2) {
              events[i][0] = parts[0].trim();
              events[i][1] = parts[1].trim();
            } else if(parts.length == 1) {
              events[i][0] = parts[0].trim();
              events[i][1] = "";
            } else {
              events[i][0] = "";
              events[i][1] = "";
            }
          }
          String json = convertEventsToJson(events);
          ctx.status(HttpStatus.OK).contentType("application/json").result(json);

      } catch (Exception e) {
          e.printStackTrace();
          ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Internal server error: " + e.getMessage());
      }
    }

    private String convertEventsToJson(String[][] events) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"tables\": [\n");
        
        boolean first = true;
        
        // First, add all items from the first column (event[0])
        for (String[] event : events) {
            if (event[0] != null && !event[0].trim().isEmpty()) {
                if (!first) {
                    json.append(",\n");
                }
                json.append("    { \"name\": \"").append(escapeJson(event[0])).append("\" }");
                first = false;
            }
        }
        
        // Then, add all items from the second column (event[1])
        for (String[] event : events) {
            if (event[1] != null && !event[1].trim().isEmpty()) {
                if (!first) {
                    json.append(",\n");
                }
                json.append("    { \"name\": \"").append(escapeJson(event[1])).append("\" }");
                first = false;
            }
        }
        
        json.append("\n  ]\n}");
        return json.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    
    private void handleQuery(Context ctx) {
        
        try {
            String query = extractQuery(ctx);
            if (query == null || query.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).result("Query parameter is required");
                return;
            }
            
            // Process the query using the evaluator
            String result = processQuery(query);
            ctx.status(HttpStatus.OK).contentType("application/json").result(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Internal server error: " + e.getMessage());
        }
    }
    
    private String extractQuery(Context ctx) {
        // Check URL query parameter first
        String queryParam = ctx.queryParam("query");
        if (queryParam != null && !queryParam.trim().isEmpty()) {
            return queryParam;
        }
        
        // Check JSON body
        String contentType = ctx.contentType();
        if (contentType != null && contentType.contains("application/json")) {
            try {
                String body = ctx.body();
                return extractQueryFromJson(body);
            } catch (Exception e) {
                // Fall through to form data check
            }
        }
        
        // Check form data
        String formQuery = ctx.formParam("query");
        if (formQuery != null && !formQuery.trim().isEmpty()) {
            return formQuery;
        }
        
        return null;
    }
    
    private String extractQueryFromJson(String jsonBody) {
        // Simple JSON parsing for {"query": "value"} format
        if (jsonBody.trim().startsWith("{") && jsonBody.trim().endsWith("}")) {
          String content = jsonBody.trim().substring(1, jsonBody.trim().length() - 1);
          String[] keyValue = content.split(":", 2);
            if (keyValue.length == 2) {
              String key = keyValue[0].trim().replaceAll("\"", "");
              String value = keyValue[1].trim().replaceAll("\"", "");
              if ("query".equals(key)) {
                  return value;
              }
            }
        }
        return null;
    }
    
    private String processQuery(String query) {
        try {
            // This is where you would integrate with your existing query processing logic
            // For now, returning a placeholder response
            System.out.println("Processing query on file: " + recordingFile);
            evaluator = Evaluator.getInstance();
            evaluator.setFile(recordingFile);
            Lexer lexer = new Lexer(query);
            Parser parser = new Parser(lexer.tokenize(), query);
            ProgramNode tree = parser.parse();
            tree.eval();
            String json = evaluator.getJsonOutput();
            evaluator.destruct();
            return json;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process query: " + query, e);
        }
    }

    public void setFile(String file) {
      this.recordingFile = file;
    }
}