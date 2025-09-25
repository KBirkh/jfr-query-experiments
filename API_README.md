# JFR API Documentation

## HTTP Server Setup with Javalin

The `JFRAPI` class provides a Javalin-based HTTP server with endpoints for processing JFR queries.

### Starting the Server

```java
// Create server on default port 8080
JFRAPI api = new JFRAPI();
api.start();

// Or specify a custom port
JFRAPI api = new JFRAPI(9090);
api.start();
```

### API Endpoints

#### GET /health

Health check endpoint that returns a simple status message.

#### POST /query

Processes a JFR query and returns the results.

**Supported Input Formats:**

1. **URL Parameters:**
   ```bash
   POST http://localhost:8080/query?query=SELECT%20*%20FROM%20jdk.CPULoad
   ```

2. **JSON Body:**
   ```bash
   POST http://localhost:8080/query
   Content-Type: application/json
   
   {"query": "SELECT * FROM jdk.CPULoad"}
   ```

3. **Form Data:**
   ```bash
   POST http://localhost:8080/query
   Content-Type: application/x-www-form-urlencoded
   
   query=SELECT%20*%20FROM%20jdk.CPULoad
   ```

**Response Format:**
- Status 200: Query processed successfully
- Status 400: Missing or empty query parameter
- Status 405: Method not allowed (non-POST requests)
- Status 500: Internal server error

**CORS Support:**
Javalin provides built-in CORS support that is automatically configured:
- Handles OPTIONS preflight requests automatically
- Allows requests from any origin (anyHost rule configured)
- Supports all standard HTTP methods
- Includes proper headers for cross-origin requests
- No manual CORS configuration required

### Example Usage

```java
public class Example {
    public static void main(String[] args) {
        JFRAPI api = new JFRAPI(8080);
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(api::stop));
        
        api.start();
        
        // Server is now listening with Javalin
        // GET /health for health check
        // POST /query for JFR queries
        // Use Ctrl+C to stop the server
    }
}
```

### Testing

Run the provided test script to verify all endpoint formats:
```bash
./test_api.sh
```

This will test all three input formats and error conditions.
