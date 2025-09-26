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

        // Add endpoint to check semantic ba reducing maxsize in the QueryCommand instance
        app.post("/semanticCheck", this::checkSemantic);
        
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
            if(evaluator.semanticCheck && !evaluator.passed) {
              ctx.status(HttpStatus.BAD_REQUEST).result("Semantic check failed");
              evaluator.semanticCheck = false;
              evaluator.passed = true;
              return;
            }
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

    private void checkSemantic(Context ctx) {
      evaluator = Evaluator.getInstance();
      evaluator.semanticCheck = true;
      handleQuery(ctx);
    }
    
    private String extractQueryFromJson(String jsonBody) {
        if (jsonBody == null) return null;

        // Find the "query" key (simple, case-sensitive) and parse its JSON string value.
        int idx = 0;
        while (true) {
            int keyPos = jsonBody.indexOf("\"query\"", idx);
            if (keyPos == -1) return null;
            int colonPos = jsonBody.indexOf(':', keyPos + 7);
            if (colonPos == -1) return null;
            int i = colonPos + 1;
            // skip whitespace
            while (i < jsonBody.length() && Character.isWhitespace(jsonBody.charAt(i))) i++;
            if (i >= jsonBody.length()) return null;

            if (jsonBody.charAt(i) == '"') {
                // parse JSON string literal starting at i
                int endQuote = findStringEnd(jsonBody, i);
                if (endQuote == -1) return null;
                String raw = jsonBody.substring(i + 1, endQuote);
                return unescapeJsonString(raw);
            } else {
                // Not a string value: read until comma or closing brace
                int j = i;
                while (j < jsonBody.length() && jsonBody.charAt(j) != ',' && jsonBody.charAt(j) != '}') j++;
                String value = jsonBody.substring(i, j).trim();
                // strip surrounding quotes if present
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                    return unescapeJsonString(value);
                }
                return value;
            }
        }
    }

    // Find the matching closing quote for a JSON string starting at quoteIndex (which points to opening '"')
    private int findStringEnd(String s, int quoteIndex) {
        int i = quoteIndex + 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                // skip escaped char
                i++;
                if (i < s.length()) {
                    char esc = s.charAt(i);
                    if (esc == 'u') {
                        // unicode escape, skip 4 hex digits
                        i += 4;
                    }
                }
            } else if (c == '"') {
                return i;
            }
            i++;
        }
        return -1; // not found
    }

    // Unescape JSON string content (without surrounding quotes)
    private String unescapeJsonString(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case '/': sb.append('/'); i++; break;
                    case 'b': sb.append('\b'); i++; break;
                    case 'f': sb.append('\f'); i++; break;
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    case 'u':
                        // unicode escape \\uXXXX
                        if (i + 5 < s.length()) {
                            String hex = s.substring(i + 2, i + 6);
                            try {
                                int code = Integer.parseInt(hex, 16);
                                sb.append((char) code);
                                i += 5;
                            } catch (NumberFormatException ex) {
                                // invalid; append literally
                                sb.append('u');
                                i++;
                            }
                        } else {
                            // truncated unicode escape; append literally
                            sb.append('u');
                            i++;
                        }
                        break;
                    default:
                        // unknown escape, keep next char
                        sb.append(next);
                        i++;
                        break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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