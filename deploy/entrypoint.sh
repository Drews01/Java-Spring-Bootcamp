#!/bin/bash

# Start SQL Server in the background
/opt/mssql/bin/sqlservr &
pid=$!

# Wait for 15 seconds to ensure SQL Server is up
echo "Waiting for SQL Server to start..."
sleep 15

# Run the initialization script
echo "Running initialization script..."
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -i /usr/src/app/init-db.sql

# Wait for the SQL Server process to finish
wait $pid
