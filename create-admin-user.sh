#!/bin/bash

echo "Creating admin user and default data..."

# Insert genres
sudo -u postgres psql -d noveldb << EOF
-- Insert genres
INSERT INTO genres (id, name) VALUES 
(1, 'Romance'),
(2, 'Fantasy'),
(3, 'Mystery'),
(4, 'Thriller'),
(5, 'Horror'),
(6, 'Sci-Fi'),
(7, 'Adventure'),
(8, 'Historical')
ON CONFLICT (id) DO NOTHING;

-- Insert admin user (password: admin123)
-- Password is BCrypt hash of "admin123"
INSERT INTO users (id, name, email, password, role, created_at) VALUES 
(1, 'Admin', 'admin@novelplatform.com', '\$2a\$10\$ddF3BAeyl8p9mjfMfMevd.hqG6qebTNb9TeCaIBn69jeJhdCOIXTa', 'ADMIN', NOW())
ON CONFLICT (id) DO NOTHING;

-- Verify
SELECT 'Admin user created:' as status, email, role FROM users WHERE email = 'admin@novelplatform.com';
SELECT 'Genres created:' as status, COUNT(*) as count FROM genres;
EOF

echo ""
echo "✅ Done! Admin user created."
echo ""
echo "Login credentials:"
echo "  Email: admin@novelplatform.com"
echo "  Password: admin123"
echo ""
echo "Now run: ./TEST_API.sh"
