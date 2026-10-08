DELETE FROM shopping_carts WHERE id IN (SELECT id FROM users WHERE email = 'newuser@test.com');
DELETE FROM users_roles WHERE user_id IN (SELECT id FROM users WHERE email = 'newuser@test.com');
DELETE FROM users WHERE email = 'newuser@test.com';