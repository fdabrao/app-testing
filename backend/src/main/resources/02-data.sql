-- Sample Food Categories
INSERT INTO category (name, description, parent_category, active)
VALUES 
    ('Fresh Produce', 'Fresh fruits and vegetables', NULL, TRUE),
    ('Fruits', 'Fresh and seasonal fruits', 'Fresh Produce', TRUE),
    ('Vegetables', 'Fresh and seasonal vegetables', 'Fresh Produce', TRUE),
    ('Organic Produce', 'Certified organic fruits and vegetables', 'Fresh Produce', TRUE),
    ('Dairy & Eggs', 'Dairy products and eggs', NULL, TRUE),
    ('Milk', 'Fresh and plant-based milk products', 'Dairy & Eggs', TRUE),
    ('Cheese', 'Various types of cheese', 'Dairy & Eggs', TRUE),
    ('Bakery', 'Fresh baked goods', NULL, TRUE),
    ('Bread', 'Fresh bread and rolls', 'Bakery', TRUE),
    ('Pastries', 'Sweet pastries and desserts', 'Bakery', TRUE),
    ('Meat & Seafood', 'Fresh meat and seafood products', NULL, TRUE),
    ('Beef', 'Fresh beef cuts', 'Meat & Seafood', TRUE),
    ('Poultry', 'Chicken, turkey and other poultry', 'Meat & Seafood', TRUE),
    ('Fish', 'Fresh and frozen fish', 'Meat & Seafood', TRUE);

-- Get category IDs for reference (PostgreSQL specific)
DO $$
DECLARE
    fruits_id INTEGER;
    vegetables_id INTEGER;
    milk_id INTEGER;
    cheese_id INTEGER;
    bread_id INTEGER;
    pastries_id INTEGER;
    beef_id INTEGER;
    fish_id INTEGER;
BEGIN
    SELECT id INTO fruits_id FROM category WHERE name = 'Fruits';
    SELECT id INTO vegetables_id FROM category WHERE name = 'Vegetables';
    SELECT id INTO milk_id FROM category WHERE name = 'Milk';
    SELECT id INTO cheese_id FROM category WHERE name = 'Cheese';
    SELECT id INTO bread_id FROM category WHERE name = 'Bread';
    SELECT id INTO pastries_id FROM category WHERE name = 'Pastries';
    SELECT id INTO beef_id FROM category WHERE name = 'Beef';
    SELECT id INTO fish_id FROM category WHERE name = 'Fish';

    -- Insert products with appropriate category IDs
    INSERT INTO product (name, description, price, available, category_id)
    VALUES 
        ('Organic Apples', 'Fresh organic apples, bag of 8', 4.99, TRUE, fruits_id),
        ('Bananas', 'Bunch of ripe bananas', 1.99, TRUE, fruits_id),
        ('Carrots', 'Organic carrots, 1lb bag', 2.49, TRUE, vegetables_id),
        ('Broccoli', 'Fresh broccoli crown', 1.99, TRUE, vegetables_id),
        ('Whole Milk', 'Organic whole milk, 1 gallon', 3.99, TRUE, milk_id),
        ('Cheddar Cheese', 'Sharp cheddar cheese, 8oz block', 4.99, TRUE, cheese_id),
        ('Sourdough Bread', 'Freshly baked sourdough loaf', 3.99, TRUE, bread_id),
        ('Chocolate Croissant', 'Butter croissant with chocolate filling', 2.99, TRUE, pastries_id),
        ('Ribeye Steak', 'Prime ribeye steak, 12oz', 14.99, TRUE, beef_id),
        ('Wild Salmon', 'Wild-caught salmon fillet, 8oz', 12.99, TRUE, fish_id);
END $$; 