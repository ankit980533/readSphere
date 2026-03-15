-- Insert default genres
INSERT INTO genres (id, name) VALUES 
(1, 'Romance'),
(2, 'Fantasy'),
(3, 'Mystery'),
(4, 'Thriller'),
(5, 'Horror'),
(6, 'Sci-Fi'),
(7, 'Adventure'),
(8, 'Historical')
ON CONFLICT DO NOTHING;

-- Insert admin user (password: admin123)
INSERT INTO users (id, name, email, password, role, created_at) VALUES 
(1, 'Admin', 'admin@novelplatform.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW())
ON CONFLICT DO NOTHING;
