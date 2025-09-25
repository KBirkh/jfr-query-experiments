#!/bin/bash

echo "JFR API Server with Javalin - Quick Demo"
echo "========================================"

# Build the project
echo "Building project..."
mvn compile -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "Key improvements with Javalin:"
    echo "- Cleaner, more concise code"
    echo "- Built-in CORS support"
    echo "- Better error handling"
    echo "- Automatic JSON parsing"
    echo "- Health check endpoint"
    echo ""
    echo "To run the server:"
    echo "  mvn exec:java -Dexec.mainClass=\"me.bechberger.jfr.tool.APIExample\""
    echo ""
    echo "To test the endpoints:"
    echo "  ./test_api.sh"
else
    echo "❌ Build failed!"
    exit 1
fi
