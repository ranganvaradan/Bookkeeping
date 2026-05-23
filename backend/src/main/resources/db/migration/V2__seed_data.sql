-- V2: Seed data — demo tenant and admin user

INSERT INTO tenants (id, name, subdomain)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Demo CPA Firm', 'demo');

-- Password: Admin123!  (BCrypt hash, cost factor 10)
INSERT INTO users (id, tenant_id, email, password_hash, role)
VALUES (
    'b1ffbc99-9c0b-4ef8-bb6d-6bb9bd380a22',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin@demo.com',
    '$2b$10$oZdz03Z0gvHNmtgee4gqgO5tIJi7ieoQVy/UY/TKIE26PWiWzbQMO',
    'ADMIN'
);
