#!/bin/bash

echo "Setting up PostgreSQL database for Novel Platform"
echo ""

# Method 1: Try with sudo
echo "Attempting to create database..."
sudo -u postgres psql << EOF
ALTER USER postgres WITH PASSWORD 'password';
CREATE DATABASE noveldb;
GRANT ALL PRIVILEGES ON DATABASE noveldb TO postgres;
\q
EOF

if [ $? -eq 0 ]; then
    echo "✅ Database created successfully!"
else
    echo "⚠️  If you see 'database already exists', that's fine!"
fi

echo ""
echo "Verifying database..."
sudo -u postgres psql -c "\l" | grep noveldb

echo ""
echo "Done! Now run: ./START_WITHOUT_DOCKER.sh"
