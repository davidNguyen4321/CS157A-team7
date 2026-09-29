-- AI generated relationships
INSERT INTO RECIPES_USE_APPLIANCES (RECIPE_ID, APPLIANCE_ID) VALUES
(1, 2), -- Pomodoro uses Stovetop
(2, 2), -- Chicken uses Stovetop
(2, 9), -- Chicken uses Rice Cooker
(3, 9), -- Rice uses Rice Cooker
(4, 2), -- Eggs use Stovetop
(5, 2), -- Tomato Soup uses Stovetop
(5, 4), -- Tomato Soup uses Blender
(5, 1), -- Tomato Soup uses Oven
(6, 2), -- Pasta Salad uses Stovetop
(7, 1), -- Bread uses Oven
(7, 6), -- Bread uses Stand Mixer
(8, 2), -- Onions use Stovetop
(9, 2), -- Fried Rice uses Stovetop
(9, 9), -- Fried Rice uses Rice Cooker
(10, 1), -- Cookies use Oven
(10, 6), -- Cookies use Stand Mixer
(12, 4); -- Smoothie (even malicious) uses blender