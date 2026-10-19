import java.sql.SQLException;

public class StarterPopulator {
	Starter starter;
	
	public StarterPopulator(Starter starter) {
		this.starter = starter;
		
	}
	
	public void populateTables() throws SQLException {
		populateEntitySets();
		populateRelationships();
	}
	
	public void populateEntitySets() throws SQLException {
		String[] codeblocks = {
				"""
				INSERT INTO USERS (USER_NAME, USER_IMAGE_PATH, USER_DESCRIPTION, USER_ACTIVE_TIMESTAMP, USER_REMOVE_TIMESTAMP, USER_STATUS, ROLE, TIMEZONE, PASSWORD_HASH, EMAIL) VALUES
				    (
				        'Jeremy Lin', 
				        '/images/users/jeremy_lin.jpg', 
				        'Home cook and comfort-food enthusiast who believes the best meals are simple, easy to make, and easy to eat. Always experimenting with quick, foolproof pantry staples.', 
				        '2024-01-01 11:01:01',
				        NULL,
				        'Active',
				        'Admin', 
				        'America/New_York', 
				        '$argon2id$v=19$m=65536,t=3,p=1$Z2VuZXJhdGVkc2FsdDI$9xK7m8Wb+5Bq7Zq7Q9Fw9k9Q7vX5k8Wb+5Bq7Zq7Q9E=', 
				        'linj436@gmail.com'
				    ),
				    (
				        'Mary Baker',
				        '/images/users/mary_baker.jpg',
				        'Passionate home baker with a soft spot for chewy cookies and show-stopping layer cakes. my goal is to make home baking approachable, fun, and irresistibly delicious. Grab a spatula and let''s bake something memorable together!',
				        '2024-02-02 12:12:12',
				        NULL,
				        'Active',
				        'Moderator',
				        'America/Chicago',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdHRlc3RzYWx0Mw$Kp3v5m8Wb+5Bq7Zq7Q9Fw9k9Q7vX5k8Wb+5Bq7Zq7Q8=',
				        'marybaker932!132@gmail.com'
				    ),
				    (
				        'Peter Parker',
				        '/images/users/peter_parker.jpg',
				        'Your friendly insect-devouring neighbor. I love cooking all types of insect based dishes. From ants, worms, cockroaches, locusts, you name it! I shall eat your pests away!',
				        '2024-03-03 13:03:03',
				        NULL,
				        'Active',
				        'Moderator',
				        'America/Los_Angeles',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdHRlc3RzYWx0NA$Wb+5Bq7Zq7Q9Fw9k9Q7vX5k8Wb+5Bq7Zq7Q9Fw9k9Q7v=',
				        'ppinsectdevourer328@gmail.com'
				    ),
				    (
				        'Valerie Vance',
				        '/images/users/valerie_vance.jpg',
				        'Hardcore environmentalist here. I hope everyone becomes addicted to my vegan dishes so we can all help reduce our carbon footprint together!',
				        '2024-04-04 14:14:14',
				        '2025-08-08 18:18:18',
				        'Deleted',
				        'User',
				        'America/New_York',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdHRlc3RzYWx0MQ$v/5k7m8Wb+5Bq7Zq7Q9Fw9k9Q7vX5k8Wb+5Bq7Zq7Q9',
				        'valeriev454@gmail.com'
				    ),
				    (
				        'Grill Boss', 
				        '/images/users/grill_boss.jpg', 
				        'Ready for some awesome roasts. Daddy here can grill, you down!? Haha sorry, please excuse daddy''s absolutely hilarious puns.',
				        '2024-05-05 15:05:05',
				        NULL,
				        'Active',
				        'User', 
				        'America/Denver', 
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdHRlc3RzYWx0NQ$8k9Q7vX5k8Wb+5Bq7Zq7Q9Fw9k9Q7vX5k8Wb+5Bq7Z8=', 
				        'grilldaddy2132@gmail.com'
				    ),
				    (
				        'Sue mei',
				        '/images/users/sue_mei.jpg',
				        'I am a predator and I like to eat things raw. What are you going to do? Sue me? Ha, good luck!',
				        '2025-06-06 16:09:16',
				        NULL,
				        'Active',
				        'User',
				        'Asia/Tokyo',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9qZkllajQzag$D76tBiqd93gScqfEnjSKcEA1nsv252ZuWfPic/Lbj34=',
				        'suemei8375@gmail.com'
				    ),
				    (
				        'Steph Curry',
				        '/images/users/steph_curry.jpg',
				        'Hey, if yall don''t know me, I am a really famous NBA player. Athletes gotta eat too ya know. My favorite food is curry, it is my namesake haha. Believe it or not, Indian food is my thing. Biryani, dosa, you name it!',
				        '2025-07-07 17:17:17',
				        NULL,
				        'Active',
				        'User',
				        'Europe/London',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9oZGR3WUQyMw$6lUiP/dsCzDKx8GtlvXLEV7V63hU5CN0iXkWpXxszFk=',
				        'justForRecipeGenieSC@gmail.com'
				    ),
				    (
				        '67_67',
				        NULL,
				        'Your dishes all fucking suck! If you take offense, that is your problem!',
				        '2026-09-10 09:09:09',
				        '2026-10-09 10:10:10',
				        'Banned',
				        'User',
				        'America/New_York',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9wYXNzd29yZDEyMw$NEuKhUIhvR6vk4LarqGZb7zUlvFY6YP4g1x+9QhMVbs=',
				        'dfg3785yib@gmail.com'
				    ),
				    (
				        'RealGordanRamsay',
				        '/images/users/realgordanramsay.jpg',
				        'I make fabulous dishes and feature in popular shows. Follow me for exquisite cuisines!',
				        '2026-08-09 08:08:08',
				        '2026-09-08 19:19:19',
				        'Banned',
				        'User',
				        'Europe/London',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9waWthY2h1MTIz$jgygfJieEA1C70cD3/zzgkMKv909VdzFcK3XkHmWrws=',
				        'realgordanramsay283@gmail.com'
				    ),
				    (
				        'Timothy32612',
				        NULL,
				        'I love cooking apps!',
				        '2026-11-11 06:06:06',
				        '2026-11-11 06:09:09',
				        'Banned',
				        'User',
				        'America/Los_Angeles',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9hdXN0aW5zdGFtcDEyMw$UoYEPZr6Al854O5789QrIgCvsUVrVsUdedqUQ5kMAy4=',
				        '1e89g4r63fydcwuo@gmail.com'
				    ),
				    (
				        'Timothy32613',
				        NULL,
				        'I love cooking apps!',
				        '2026-11-12 06:06:06',
				        '2026-11-12 06:08:08',
				        'Banned',
				        'User',
				        'America/Los_Angeles',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9hdXN0aW5zdGFtcDEyMw$UoYEPZr6Al854O5789QrIgCvsUVrVsUdedqUQ5kMAy4=',
				        'ewfegt8y@gmail.com'
				    ),
				    (
				        'Timothy32614',
				        NULL,
				        'I love cooking apps!',
				        '2026-11-13 06:06:06',
				        '2026-11-13 06:07:07',
				        'Banned',
				        'User',
				        'America/Los_Angeles',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9hdXN0aW5zdGFtcDEyMw$UoYEPZr6Al854O5789QrIgCvsUVrVsUdedqUQ5kMAy4=',
				        'h3efdsjefwieru@gmail.com'
				    );
				""",
				"""
				INSERT INTO APPLIANCES (APPLIANCE_NAME, APPLIANCE_IMAGE_PATH, APPLIANCE_DESCRIPTION) VALUES
					(
						'Rice Cooker', 
						'/images/appliances/rice_cooker.jpg', 
						'Add water to rice, grains, vegetables, etc., to automatically boil and steam them to perfection'
					),
					(
						'Oven', 
						'/images/appliances/oven.jpg', 
						'Bake food evenly from all sides at a specified temperature for a specified amount of time'
					),
					(
						'Toaster', 
						'/images/appliances/toaster.jpg', 
						'Heats and browns sliced bread'
					),
					(
						'Stovetop',
						'/images/appliances/stovetop.jpg',
						'Gas or electric range for boiling and frying'
					),
					(
						'Air Fryer', 
						'/images/appliances/air_fryer.jpg', 
						'Circulate super-heated air at high speeds, delivering crispy food using little to no oil'
					),
					(
						'Blender',
						'/images/appliances/blender.jpg',
						'Blend fruits, vegetables, ice, and more into smooth drinks and purées'
					),
					(
						'Microwave',
						'/images/appliances/microwave.jpg',
						'Uses radio waves to rapidly heat and cook food'
					),
					(
						'Food Processor', 
						'/images/appliances/food_processor.jpg', 
						'Used for chopping, slicing, and dicing'
					),
					(
						'Mixer', 
						'/images/appliances/mixer.jpg', 
						'Heavy duty mixer for dough and batter'
					),
					(
						'Grill', 
						'/images/appliances/grill.jpg', 
						'Uses burning charcoal to grill food over high, smoky heat'
					);
				""",
				"""
				INSERT INTO INGREDIENTS (INGREDIENT_NAME, INGREDIENT_IMAGE_PATH, INGREDIENT_DESCRIPTION, BASE_INGREDIENT_ID) VALUES
				    ('Rice', '/images/ingredients/rice.jpg', 'Food grain cultivated predominantly in flooded paddies.', NULL),
				    ('White Rice', '/images/ingredients/white_rice.jpg', 'Rice refined to remove outer fibrous layers, leaving only the starchy inside.', 1),
				    ('Flour', '/images/ingredients/flour.jpg', 'Powder made by grinding raw grains, used in baking and thickening.', NULL),
				    ('Bread', '/images/ingredients/bread.jpg', 'Baked staple food made from flour dough and water.', NULL),
				    ('Pasta', '/images/ingredients/pasta.jpg', 'Uncooked shaped dough made from flour and water.', NULL),
				    ('Rigatoni', '/images/ingredients/rigatoni.jpg', 'Tube-shaped pasta with ridges along its length.', 5),
				    ('Cabbage', '/images/ingredients/cabbage.jpg', 'Vegetables with leaves wrapped into a core.', NULL),
				    ('Taiwanese Cabbage', '/images/ingredients/taiwanese_cabbage.jpg', 'Extra large leaves, light green in color.', 7),
				    ('Yam', '/images/ingredients/yam.jpg', 'Starchy edible root vegetable.', NULL),
				    ('Nagaimo', '/images/ingredients/nagaimo.jpg', 'Chinese yam with a slimy texture when peeled, mild and digestive.', 9),
				    ('Ginger', '/images/ingredients/ginger.jpg', 'Pungent root used as a spice and ingredient in cooking.', NULL),
				    ('Spinach', '/images/ingredients/spinach.jpg', 'Nutritious green leafy vegetable rich in iron.', NULL),
				    ('Lettuce', '/images/ingredients/lettuce.jpg', 'Crisp leafy vegetable used primary in salads and sandwiches.', NULL),
				    ('Butterleaf Lettuce', '/images/ingredients/butterleaf_lettuce.jpg', 'A variety of lettuce with wide, soft, wavy leaves.', 13),
				    ('Romaine Lettuce', '/images/ingredients/romaine_lettuce.jpg', 'A variety of lettuce with long, sturdy, crisp leaves.', 13),
				    ('Tomato', '/images/ingredients/tomato.jpg', 'Juicy, savory red fruit cooked widely as a vegetable.', NULL),
				    ('Beefsteak Tomato', '/images/ingredients/beefsteak_tomato.jpg', 'Softball-sized, flattened tomato variety known for meaty flesh and minimal seed jelly.', 16),
				    ('Grape Tomato', '/images/ingredients/grape_tomato.jpg', 'Grape-sized, oblong tomato variety known for sweet flavor.', 16),
				    ('Onion', '/images/ingredients/onion.jpg', 'Pungent bulb vegetable used as an aromatic in cooking.', NULL),
				    ('Green Onion', '/images/ingredients/green_onion.jpg', 'Mild scallion/spring onion used as garnish and aromatic.', 19),
				    ('Garlic', '/images/ingredients/garlic.jpg', 'Pungent bulb used as a seasoning and aromatic.', NULL),
				    ('Garlic Powder', '/images/ingredients/garlic_powder.jpg', 'Dehydrated ground garlic used as a seasoning.', 21),
				    ('Tofu', '/images/ingredients/tofu.jpg', 'Bean curd made from condensed soy milk.', NULL),
				    ('Eggs', '/images/ingredients/eggs.jpg', 'Protein source laid by farm animals (e.g., chickens) commonly cooked or used in baking.', NULL),
				    ('Cheese', '/images/ingredients/cheese.jpg', 'Dairy product made from milk in a wide range of flavors, textures, and forms.', NULL),
				    ('American Cheese', '/images/ingredients/american_cheese.jpg', 'Processed cheese product with high meltability.', 25),
				    ('Silkworm Pupae', '/images/ingredients/silkworm_pupae.jpg', 'Edible insect delicacy commonly sold canned in Korea.', NULL),
				    ('Steak', '/images/ingredients/steak.jpg', 'High-quality cut of beef, typically grilled or pan-seared.', NULL),
				    ('Sashimi Salmon', '/images/ingredients/sashimi_salmon.jpg', 'Fresh, high-grade raw salmon suitable for raw consumption.', NULL),
				    ('Avocado', '/images/ingredients/avocado.jpg', 'Creamy, nutrient-rich green fruit high in healthy fats.', NULL),
				    ('Berries', '/images/ingredients/berries.jpg', 'Assorted fresh berries.', NULL),
				    ('Blueberries', '/images/ingredients/blueberries.jpg', 'Sweet, antioxidant-rich dark blue berries.', 31),
				    ('Banana', '/images/ingredients/banana.jpg', 'Sweet, tropical fruit providing texture and natural sweetness.', NULL),
				    ('Milk', '/images/ingredients/milk.jpg', 'Nutrient-rich liquid produced by mammals or plant alternatives.', NULL),
				    ('Coconut Milk', '/images/ingredients/coconut_milk.jpg', 'Rich, creamy liquid extracted from grated mature coconut meat.', 34),
				    ('Almond Milk', '/images/ingredients/almond_milk.jpg', 'Plant-based milk alternative made from ground almonds.', 34),
				    ('Vegetable Broth', '/images/ingredients/vegetable_broth.jpg', 'Savory liquid made by simmering vegetables in water.', NULL),		
				    ('Sesame', '/images/ingredients/sesame.jpg', 'Small nutty seeds used for flavor and garnish.', NULL), 
				    ('Sesame Oil', '/images/ingredients/sesame_oil.jpg', 'Aromatic oil derived from sesame seeds, rich in nutty flavor.', NULL),
				    ('Soy Sauce', '/images/ingredients/soy_sauce.jpg', 'Fermented savory condiment made from soybeans and wheat.', NULL),
				    ('Vinegar', '/images/ingredients/vinegar.jpg', 'Acidic liquid made from fermented liquids.', NULL),
				    ('Rice Vinegar', '/images/ingredients/rice_vinegar.jpg', 'Mild, slightly sweet vinegar made from fermented rice.', 41),
				    ('Puree', '/images/ingredients/puree.jpg', 'Smooth, creamy substance made of crushed fruits or vegetables.', NULL),
				    ('Tomato Puree', '/images/ingredients/tomato_puree.jpg', 'Blended, strained cooked tomatoes used as a thick base.', 43),
				    ('Butter', '/images/ingredients/butter.jpg', 'Rich dairy fat product used in baking and cooking.', NULL),
				    ('Sugar', '/images/ingredients/sugar.jpg', 'Sweet crystalline carbohydrate used in food and baking.', NULL),
				    ('White Sugar', '/images/ingredients/white_sugar.jpg', 'Granulated refined sugar.', 46),
				    ('Brown Sugar', '/images/ingredients/brown_sugar.jpg', 'Sugar containing molasses, adding moisture and deep flavor.', 46),
				    ('Vanilla Extract', '/images/ingredients/vanilla_extract.jpg', 'Flavoring solution made from vanilla beans.', NULL),
				    ('Chocolate', '/images/ingredients/chocolate.jpg', 'Food product made from roasted and ground cacao pods.', NULL),
				    ('Baking Soda', '/images/ingredients/baking_soda.jpg', 'Leavening agent used in baking.', NULL),
				    ('Pepper', '/images/ingredients/pepper.jpg', 'Pungent or spicy seasoning spice.', NULL),
				    ('Black Pepper', '/images/ingredients/black_pepper.jpg', 'Ground black peppercorns used for heat and aroma.', 52),
				    ('Gochugaru', '/images/ingredients/gochugaru.jpg', 'Coarsely ground Korean chili powder with smoky, sweet heat.', NULL),
				    ('Curry', '/images/ingredients/curry.jpg', 'Blend of aromatic spices including turmeric, coriander, and cumin.', NULL),
				    ('Chili', '/images/ingredients/chili.jpg', 'Crushed red pepper flakes adding spicy kick.', NULL),
				    ('Wasabi Paste', '/images/ingredients/wasabi_paste.jpg', 'Pungent green Japanese horseradish condiment.', NULL),
				    ('Seaweed', '/images/ingredients/seaweed.jpg', 'Edible marine algae used for umami flavor and garnish.', NULL),
				    ('Basil', '/images/ingredients/basil.jpg', 'Aromatic herb with sweet, herbal pepper notes.', NULL);
				""",
				"""
				INSERT INTO DISH_TYPES (DISH_TYPE_NAME, DISH_TYPE_DESCRIPTION, BASE_DISH_TYPE_ID) VALUES
				    ('Beverage', 'Drinks and liquid refreshments.', NULL),
				    ('Smoothie', 'A thick, creamy beverage made from blended fruit, vegetables, and liquids.', 1),
				    ('Soup', 'A primarily liquid food, served warm or hot, made by combining ingredients.', NULL),
				    ('Stew', 'A dish featuring a higher solid to liquid ratio.', 3),
				    ('Broth', 'A savory liquid food featuring a higher liquid to solid ratio.', 3),
				    ('Salad', 'A dish of mixed ingredients, typically including vegetables, served with a dressing.', NULL),
				    ('Handheld', 'Portable food items that can be easily eaten by hand.', NULL),
				    ('Sandwich', 'Food consisting of fillings placed on or between slices of bread.', 7),
				    ('Pasta & Noodles', 'Dishes featuring shaped or ribbon-cut dough cooked and coated in sauce.', NULL),
				    ('Rice', 'Dishes where rice is the foundational component or cooked into a dish.', NULL),
				    ('Baked Goods', 'Items baked in an oven, including cookies and pastries.', NULL),
				    ('Cookie', 'A small, flat, sweet baked good.', 11),
				    ('Meat', 'Dishes centered around primary cuts of meat like grilled steak.', NULL);
				""",
				"""
				INSERT INTO CUISINES (CUISINE_NAME, CUISINE_DESCRIPTION, BASE_CUISINE_ID) VALUES
				    ('Asian', 'Include China, India, Philippines, and other parts of Asia.', NULL),
				    ('East Asian', 'Include Japan, Korea, China, and other parts of East Asia.', 1),
				    ('South Asian', 'Include India, Pakistan, and other parts of South Asia.', 1),
				    ('Chinese', 'Featuring comforting staples like congee.', 2),
				    ('Korean', 'Known for bold spices and umami flavors.', 2),
				    ('Japanese', 'Focusing on fresh, high-grade components.', 2),
				    ('Indian', 'Defined by aromatic spice blends like curry.', 3),
				    ('Western', 'Include Europe, US, Canada, Australia, and more.', NULL),
				    ('American', 'Featuring baked goods, grilled meats, burgers, and more.', 8),
				    ('Mediterranean', 'Include Italy, Greece, and other countries bordering the Mediterranean.', 8),
				    ('Italian', 'Featuring pasta, tomatoes, olive oil, and more.', 10);
				""",
				"""
				INSERT INTO DIETARY_RESTRICTIONS (DIETARY_RESTRICTION_NAME, DIETARY_RESTRICTION_DESCRIPTION, BASE_DIETARY_RESTRICTION_ID) VALUES
				    ('Pescatarian', 'Diets that include plant-based foods, dairy, eggs, and fish/seafood, but exclude meat and poultry.', NULL),
				    ('Vegetarian', 'Diets that exclude meat, poultry, and seafood, but may include dairy and eggs.', 1),
				    ('Vegan', 'Strict plant-based diets that exclude all animal products including dairy, eggs, and honey.', 2),
				    ('Gluten-Free', 'Diets that exclude the protein gluten found in wheat, barley, rye, and triticale.', NULL),
				    ('Dairy-Free', 'Diets that avoid all milk-based dairy products and lactose.', NULL),
				    ('Nut-Free', 'Diets that exclude tree nuts and peanuts to prevent allergic reactions.', NULL),
				    ('Sodium-Free', 'Diets that completely eliminate added salt and high-sodium components.', NULL),
				    ('Sugar-Free', 'Diets that exclude all refined sugars, artificial sweeteners, and added syrups.', NULL),
				    ('Egg-Free', 'Diets that exclude eggs and any ingredients derived from eggs to accommodate allergies or personal choices.', NULL),
				    ('Low-Carb', 'Diets that restrict carbohydrate consumption in favor of proteins and fats.', NULL),
				    ('Paleo', 'A low-carb diet based on whole foods presumed to have been eaten by early humans, excluding grains, legumes, and dairy.', 10);
				""",
				"""
				INSERT INTO RECIPES (RECIPE_NAME, RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION, RECIPE_ACTIVE_TIMESTAMP, RECIPE_REMOVE_TIMESTAMP, RECIPE_STATUS, INSTRUCTIONS, TOTAL_MINUTES, COURSE, AI_GEN_DESCRIPTION, AI_GEN_APPLIANCES, AI_GEN_CUISINES, AI_GEN_DISH_TYPES, AI_GEN_TOTAL_MINUTES, AI_GEN_COURSE, AUTHOR_ID) VALUES
					( 
						'Comforting Vegan Congee for Stomach Relief',
						'/images/recipes/comforting_congee.jpg',
						'When your stomach needs a rest, this minimalist congee brings gentle, warming relief. It''s simple, nourishing, and wonderfully easy to digest.',
						'2024-01-01 21:21:21',
				        NULL,
				        'Active',
						'1. Add rice to inner pot and wash and drain twice with tap water. \n2. Add 5 cups of filtered water. \n3. Wash and cut cabbage into thin strips, add to pot. \n4. Peel and cut nagaimo into small chunks, add to pot. \n5. Peel and cut ginger into thin strips, add to pot. \n6. Dice tofu, add to pot. \n7. Add salt and stir. \n8. Place pot in cooker, select porridge setting, press start.',
						90, 'Main Course', FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, 1
					),
					(
						'Homemade Chocolate Chip Cookies',
						'/images/recipes/chocolate_chip_cookies.jpg',
						'Classic baked treats perfect for your friends and family!',
						'2024-02-02 22:22:22',
				        NULL,
				        'Active',
						'1. Whisk together flour, baking soda, and salt in a medium bowl. \n2. Beat butter in a larger bowl, then beat in white and brown sugar until creamy. \n3. Add eggs into larger bowl one at a time, beating well after each, then stir in vanilla extract. \n4. Gradually mix dry ingredients from medium bowl into wet mixture in larger bowl. \n5. Fold in chocolate chips. \n6. Scoop rounded tablespoons of dough onto baking sheet, chill in fridge for 30 minutes. \n7. Start preheating oven to 350°F (175°C) after chilling for 15 minutes. \n8. Bake for 10 minutes, then sit in pan outside for 5 minutes. \n9. Cool on wire rack for 15 minutes.',
						75, 'Dessert', TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, 2
					),
					(
						'Air-Fried Crispy Korean Beondegi',
						'/images/recipes/air_fried_insect.jpg',
						'A modern twist on a beloved South Korean street food staple. Silkworm pupae are air-fried to a satisfying crunch. Rich, nutty, and bursting with umami flavor—it''s street food made effortlessly modern',
						'2024-03-03 23:23:23',
				        NULL,
				        'Active',
						'1. Drain canned silkworm pupae. \n2. In a small bowl, evenly coat pupae with mixture of sesame oil, soy sauce, gochugaru, garlic powder, sugar, and salt. \n3. Preheat air fryer to 375°F (190°C) for 3 minutes. \n4. Spread the seasoned pupae in a single layer in the air fryer basket. \n5. Air fry for 9 minutes, shaking the basket every 3 minutes. \n6. Cool for a minute before serving. Enjoy!',
						15, 'Appetizer / Side', TRUE, FALSE, TRUE, FALSE, TRUE, FALSE, 3
					),
					(
						'Toasted Sandwich with Egg, Cheese, and Veggies!',
						'/images/recipes/sandwich.jpg',
						'Elevate your morning routine with this rich, fresh, and quick breakfast sandwich. Fluffy scrambled eggs, gooey melted American cheese, tender butterleaf lettuce, and juicy beefsteak tomatoes stacked high between golden crispy toast. The ultimate balanced breakfast sandwich!',
						'2024-03-15 15:15:15',
				        NULL,
				        'Active',
						'1. Toast 2 slices of bread until golden. \n2. Wash lettuce leaves, slice tomatoes, season with pinch of salt and black pepper. \n3. Whisk eggs with pinch of salt in small bowl. \n4. Medium-heat skillet with oil, pour in whisked eggs, gently push with spatula to form soft curds. \n5. Gather cooked eggs, turn off heat, lay cheese slice on top to melt. \n6. Lay 1 toast slice on plate, top with lettuce, cheesy scrambled eggs, and tomatoes, and close with remaining toast slice.',
						10, 'Main Course', TRUE, TRUE, FALSE, FALSE, TRUE, TRUE, 1
					),
					(
						'Deep Forest Smoothie',
						'/images/recipes/smoothie.jpg',
						'Fuel your morning with plant-based goodness. This thick green smoothie is as nourishing as it is delicious.',
						'2024-04-08 12:16:20',
				        NULL,
				        'Active',
						'1. Pour almond milk and avocado into bottom of blender pitcher. \n2. Layer in fresh spinach, berries and banana, ice. \n3. Start on low speed, gradually increasing to high until completely smooth and thick. \n4. Scoop into cup. Enjoy!',
						5, NULL, TRUE, FALSE, FALSE, TRUE, TRUE, FALSE, NULL
					),
					(
						'Microwave Tomato Soup', 
						'/images/recipes/tomato_soup.jpg', 
						'Super easy, simple, and quick to make. Quantities proportioned just right!', 
						'2024-09-09 18:27:36',
				        NULL,
				        'Active',
						'1. Mix tomato puree and vegetable broth (1:2 ratio) together in a bowl \n2. Lightly mix diced onion, chopped garlic, salt, pepper, and olive oil. \n3. Microwave 2 minutes per cup.', 
						NULL, 'Appetizer / Side', TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, NULL
					),
					(
						'Grilled Steak',
						'/images/recipes/grilled_steak.jpg',
						'Classic American Recipe. Tender juicy steak with that smokey flavor. Can''t live without it.',
						'2025-06-06 16:16:16',
				        NULL,
				        'Active',
				        '1. Pat strip of steak completely dry with paper towels. \n2. Rub all over with a tiny bit of oil and season generously with salt and black pepper. \n3. Preheat your grill until coals are glowing hot with medium-high heat. \n4. Place the steak on the hot grill. Grill for about 3 minutes per side for medium-rare. \n5. Transfer from grill to plate, let it rest for 5 minutes before slicing and serving.',
				        NULL, 'Main Course', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 5
				    ),
					(
						'Sashimi Salad',
						'/images/recipes/sashimi_salad.jpg',
						'For all my fellow predators out there, I shall introduce to you my Japanese Western fusion dish. Have you ever eaten salad and felt there was something missing. Eating vegetables by itself is such a pain. But don''t worry, with this dish, you won''t have to hate eating vegetables ever again.',
						'2025-06-16 16:16:16',
				        NULL,
				        'Active',
						'1. Wash and halve crisp romaine lettuce and grape tomatoes. Discard lettuce stem. \n2. In a small bowl, whisk soy sauce, rice vinegar, sesame oil, and wasabi paste until smooth. \n3. Toss the chopped romaine and grape tomatoes in a bowl and mix in half of dressing. \n4. Arrange thinly sliced sashimi-grade raw salmon on top of the greens. \n5. Drizzle remaining dressing over the fish and garnish with sesame seeds, green onion, and seaweed.',
						10, 'Main Course', FALSE, FALSE, TRUE, TRUE, TRUE, FALSE, 6
					),
					(
						'Curry Rigatoni',
						'/images/recipes/curry_rigatoni.jpg',
						'Fusion dish featuring ridged, hollow rigatoni tubes coated in a rich, velvety curry cream sauce. The inner chambers hold onto every drop of sauce for a flavorful bite.',
						'2025-07-17 17:17:17',
				        NULL,
				        'Active',
						'1. Boil 5 quarts of water with 1.5 tablespoons of salt, then add rigatoni and cook for 10 minutes, reserving 1/2 cup of starchy pasta water before draining. \n2. Heat oil in a deep skillet over medium heat and sauté diced onions and minced garlic until translucent. \n3. Stir in curry and chili flakes, toasting the spices for 30 seconds. \n4. Pour in coconut milk, simmering gently for 3 to 5 minutes until thickened. \n5. Add drained rigatoni and a splash of reserved pasta water, stirring until sauce fills the hollow tubes. \n6. Season with salt and pepper, garnish with fresh basil.',
						30, 'Main Course', TRUE, TRUE, FALSE, TRUE, TRUE, FALSE, 7
					),
					(
						'Classic American Breakfast Sandwich',
						'/images/recipes/sandwich.jpg',
						'If you got the skills, simple ingredients can transform into heavenly taste!',
						'2026-09-04 14:04:04',
						'2026-09-07 17:17:17',
				        'Banned',
						'1. Toast 2 slices of bread until golden. \n2. Wash lettuce leaves, slice tomatoes, season with pinch of salt and black pepper. \n3. Whisk eggs with pinch of salt in small bowl. \n4. Medium-heat skillet with oil, pour in whisked eggs, gently push with spatula to form soft curds. \n5. Gather cooked eggs, turn off heat, lay cheese slice on top to melt. \n6. Lay 1 toast slice on plate, top with lettuce, cheesy scrambled eggs, and tomatoes, and close with remaining toast slice.',
						10, 'Main Course', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 9
					),
					(
						'Next Level Fusion: Curry Pasta!!!',
						'/images/recipes/curry_rigatoni.jpg',
						'Combining Indian and Italian Cuisine for a super modern, extremely trendy, and most delicious dish!!!',
						'2026-09-05 09:05:09',
						'2026-09-08 19:19:19',
				        'Banned',
						'1. Boil 5 quarts of water with 1.5 tablespoons of salt, then add rigatoni and cook for 10 minutes, reserving 1/2 cup of starchy pasta water before draining. \n2. Heat oil in a deep skillet over medium heat and sauté diced onions and minced garlic until translucent. \n3. Stir in curry and chili flakes, toasting the spices for 30 seconds. \n4. Pour in coconut milk, simmering gently for 3 to 5 minutes until thickened. \n5. Add drained rigatoni and a splash of reserved pasta water, stirring until sauce fills the hollow tubes. \n6. Season with salt and pepper, garnish with fresh basil.',
						30, 'Main Course', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 9
					);
				""",
				"""
				INSERT INTO REVIEWS (REVIEW_NAME, REVIEW_IMAGE_PATH, REVIEW_DESCRIPTION, REVIEW_ACTIVE_TIMESTAMP, REVIEW_REMOVE_TIMESTAMP, REVIEW_STATUS, RATING, RECIPE_ID, REVIEWER_ID) VALUES
				    (
				        'Anyone who eats this is out of their mind',
				        NULL,
				        'Who in their right mind eats bugs?! Fucking crazy.',
				        '2026-09-15 11:00:00',
				        '2026-09-18 10:10:10',
				        'Banned',
				        1, 3, 8
				    ),
				    (
				        'GO TO HELL',
				        NULL,
				        'For your absolutely disgusting garbage of a dish, go kill your fucking self!',
				        '2026-10-01 14:00:00',
				        '2026-10-03 11:11:11',
				        'Banned',
				        1, 8, 8
				    ),
				    (
				        'Absolute Life Saver',
				        '/images/reviews/comforting_congee.jpg',
				        'When my stomach was acting up, this congee really brought me back to life. So easy and soothing!',
				        '2026-11-01 10:00:00', 
				        NULL, 
				        'Active', 
				        5, 1, 2
				    ),
				    (
				        'Crunchy and Unique',
				        '/images/reviews/air_fried_insect.jpg',
				        'Never thought I would eat insects, but air-frying them gives them a decent texture. Quite savory!',
				        '2026-11-03 16:45:00',
				        NULL,
				        'Active',
				        4, 3, 5
				    ),
				    (
				        'Not For Me',
				        '/images/reviews/air_fried_insect.jpg',
				        'I really wanted to give insect-based dishes a fair try, but the taste and texture just were not pleasant at all. Too earthy and fishy for my liking.',
				        '2026-11-05 14:25:00',
				        NULL,
				        'Active',
				        2, 3, 1
				    ),
				    (
				        'Fresh and Delicious',
				        '/images/reviews/sashimi_salad.jpg',
				        'As the grill boss, I usually love my meats charred, but this sashimi salad is surprisingly refreshing and top tier!',
				        '2025-11-06 12:00:00',
				        NULL,
				        'Active',
				        5, 8, 5
				    ),
				    (
				        'Raw Perfection',
				        '/images/reviews/air_fried_insect.jpg',
				        'As a predator who loves raw and wild food, these air-fried silkworm pupae hit the exact right spot. Delicious!',
				        '2024-11-07 15:00:00',
				        NULL,
				        'Active',
				        5, 3, 6
				    ),
				    (
				        'Meaty and Juicy',
				        '/images/reviews/grilled_steak.jpg',
				        'Nothing beats a good carnivorous meal. Cooked it right up and it satisfies my predator cravings completely.',
				        '2025-11-08 18:30:00',
				        NULL,
				        'Active',
				        5, 7, 6
				    ),
				    (
				        'Champion Breakfast',
				        '/images/reviews/sandwich.jpg',
				        'Athletes need a great start to the day. This loaded toast sandwich gives me all the energy I need before practice!',
				        '2025-11-09 09:00:00',
				        NULL,
				        'Active',
				        5, 4, 7
				    ),
				    (
				        'Solid Post-Workout Protein',
				        '/images/reviews/grilled_steak.jpg',
				        'Great source of protein after a hard workout session on the court. Simple, smokey, and hits the spot.',
				        '2025-11-10 20:00:00',
				        NULL,
				        'Active',
				        4, 7, 7
				    );	    
				""",
				"""
			    INSERT INTO REPORTS (REPORT_DESCRIPTION, REPORT_ACTIVE_TIMESTAMP, REPORT_STATUS, REASON, REJECTION_DESCRIPTION, SNAPSHOT_NAME, SNAPSHOT_IMAGE_PATH, SNAPSHOT_DESCRIPTION, SNAPSHOT_ACTIVE_TIMESTAMP, ACCUSED_ID, REPORTER_ID, MODERATOR_ID) VALUES
				    (
				        'Insects shouldnt be on the website because it is disgusting.',
				        '2024-04-05 11:00:00',
				        'Dismissed',
				        'Inappropriate Content',
				        'Personal preferences do not constitute a violation of platform guidelines.',
				        'Air-Fried Crispy Korean Beondegi',
				        '/images/recipes/air_fried_insects.jpg',
				        'A modern twist on a beloved South Korean street food staple. Silkworm pupae are air-fried to a satisfying crunch. Rich, nutty, and bursting with umami flavor—it is street food made effortlessly modern',
				        '2024-03-03 23:23:23',
				        3, 
				        4, 
				        2  
				    ),
				    (
				        'Sue''s profile shows that she is a pedophile.',
				        '2025-06-10 14:00:00',
				        'Approved',
				        'Inappropriate Content',
				        NULL,
				        'Sue mei',
				        '/images/users/sue_mei.jpg',
				        'I am a predator and I like to eat things raw. What are you going to do? Sue me? Ha, good luck!',
				        '2025-06-06 16:09:16',
				        6, 
				        4, 
				        3  
				    ),
				    (
				        'Worried about the diseases that might spread from eating raw fish.',
				        '2025-06-18 13:00:00',
				        'Dismissed',
				        'Health Concerns',
				        'Recipe specifies sashimi-grade fish handled safely under normal culinary standards.',
				        'Sashimi Salad',
				        '/images/recipes/sashimi_salad.jpg',
				        'For all my fellow predators out there, I shall introduce to you my Japanese Western fusion dish. Have you ever eaten salad and felt there was something missing. Eating vegetables by itself is such a pain. But don''t worry, with this dish, you won''t have to hate eating vegetables ever again.',
				        '2025-06-16 16:16:16',
				        6, 
				        4, 
				        3  
				    ),
				    (
				        'This 5-star review is totally misleading and feels like a fake promotional bot boosting the score.',
				        '2025-06-20 15:00:00',
				        'Dismissed',
				        'Scam',
				        'Investigation shows the reviewer has organic bookmark and platform activity. Rating is genuine.',
				        'Fresh and Delicious',
				        '/images/reviews/sashimi_salad.jpg',
				        'As the grill boss, I usually love my meats charred, but this sashimi salad is surprisingly refreshing and top tier!',
				        '2025-06-20 12:00:00',
				        5, 
				        4, 
				        2  
				    ),
				    (
				        'This fake copied my Classic American Breakfast Sandwich recipe.',
				        '2026-09-05 16:00:00',
				        'Approved',
				        'Copyright',
				        NULL,
				        'Classic American Breakfast Sandwich',
				        '/images/recipes/sandwich.jpg',
				        'If you got the skills, simple ingredients can transform into heavenly taste!',
				        '2026-09-04 14:04:04',
				        9, 
				        1, 
				        2  
				    ),
				    (
				        'I don''t even know who this guy is but he copied by Curry Rigatoni recipe.',
				        '2026-09-06 11:00:00',
				        'Approved',
				        'Copyright',
				        NULL,
				        'Next Level Fusion: Curry Pasta!!!',
				        '/images/recipes/curry_rigatoni.jpg',
				        'Combining Indian and Italian Cuisine for a super modern, extremely trendy, and most delicious dish!!!',
				        '2026-09-05 09:05:09',
				        9, 
				        7, 
				        3  
				    ),
				    (
				        'Abusive and hateful comments left on insect recipe.',
				        '2026-09-11 14:00:00',
				        'Approved',
				        'Harassment',
				        NULL,
				        'Anyone who eats this is out of their mind',
				        NULL,
				        'Who in their right mind eats bugs?! Fucking crazy.',
				        '2026-09-15 11:00:00',
				        8, 
				        3, 
				        2  
				    ),
				    (
				        'Malicious and hostile review text left on sashimi salad recipe.',
				        '2026-09-12 16:00:00',
				        'Approved',
				        'Inappropriate Content',
				        NULL,
				        'GO TO HELL',
				        NULL,
				        'For your absolutely disgusting garbage of a dish, go kill your fucking self!',
				        '2026-10-01 14:00:00',
				        8, 
				        6, 
				        3  
				    ),
				    (
				        'Toxic and abusive biography description.',
				        '2026-10-05 10:00:00',
				        'Approved',
				        'Harassment',
				        NULL,
				        '67_67',
				        NULL,
				        'Your dishes all fucking suck! If you take offense, that is your problem!',
				        '2026-09-10 09:09:09',
				        8, 
				        5, 
				        2  
				    ),
				    (
				        'Reporting low rating review because personal preference shouldn''t dictate a low score.',
				        '2026-11-11 10:00:00',
				        'Dismissed',
				        'Inappropriate Content',
				        'Reviewer provided constructive criticism regarding the taste and texture. Low ratings based on personal culinary experience are permitted.',
				        'Not For Me',
				        '/images/reviews/air_fried_insect.jpg',
				        'I really wanted to give insect-based dishes a fair try, but the taste and texture just were not pleasant at all. Too earthy and fishy for my liking.',
				        '2026-11-05 14:25:00',
				        1, 
				        3, 
				        2  
				    );
				""",
				"""
				INSERT INTO RECIPE_REPORTS (SNAPSHOT_RECIPE_ID, SNAPSHOT_APPLIANCES, SNAPSHOT_INGREDIENTS, SNAPSHOT_CUISINES, SNAPSHOT_DISH_TYPES, SNAPSHOT_INSTRUCTIONS, SNAPSHOT_TOTAL_MINUTES, SNAPSHOT_COURSE, SNAPSHOT_AI_GEN_DESCRIPTION, SNAPSHOT_AI_GEN_APPLIANCES, SNAPSHOT_AI_GEN_CUISINES, SNAPSHOT_AI_GEN_DISH_TYPES, SNAPSHOT_AI_GEN_TOTAL_MINUTES, SNAPSHOT_AI_GEN_COURSE, REPORT_ID) VALUES
				    (
				        3, 
				        'Air Fryer', 
				        'Silkworm Pupae, Sesame Oil, Soy Sauce, Gochugaru, Garlic Powder, Sugar, Salt', 
				        'Korean', 
				        'Meat', 
				        '1. Drain canned silkworm pupae. \n2. In a small bowl, evenly coat pupae with mixture of sesame oil, soy sauce, gochugaru, garlic powder, sugar, and salt. \n3. Preheat air fryer to 375°F (190°C) for 3 minutes. \n4. Spread the seasoned pupae in a single layer in the air fryer basket. \n5. Air fry for 9 minutes, shaking the basket every 3 minutes. \n6. Cool for a minute before serving. Enjoy!', 
				        15, 
				        'Appetizer / Side', 
				        TRUE, FALSE, TRUE, FALSE, TRUE, FALSE, 
				        1 
				    ),
				    (
				        8, 
				        'Food Processor', 
				        'Romaine Lettuce, Grape Tomato, Soy Sauce, Rice Vinegar, Sesame Oil, Wasabi Paste, Sashimi Salmon, Sesame, Green Onion, Seaweed', 
				        'Japanese, Western', 
				        'Salad, Meat', 
				        '1. Wash and halve crisp romaine lettuce and grape tomatoes. Discard lettuce stem. \n2. In a small bowl, whisk soy sauce, rice vinegar, sesame oil, and wasabi paste until smooth. \n3. Toss the chopped romaine and grape tomatoes in a bowl and mix in half of dressing. \n4. Arrange thinly sliced sashimi-grade raw salmon on top of the greens. \n5. Drizzle remaining dressing over the fish and garnish with sesame seeds, green onion, and seaweed.', 
				        10, 
				        'Main Course', 
				        FALSE, FALSE, TRUE, TRUE, TRUE, FALSE, 
				        3 
				    ),
				    (
				        10, 
				        'Toaster', 
				        'Bread, Lettuce, Tomato, Eggs, Cheese, Black Pepper', 
				        'Western', 
				        'Sandwich', 
				        '1. Toast 2 slices of bread until golden. \n2. Wash lettuce leaves, slice tomatoes, season with pinch of salt and black pepper. \n3. Whisk eggs with pinch of salt in small bowl. \n4. Medium-heat skillet with oil, pour in whisked eggs, gently push with spatula to form soft curds. \n5. Gather cooked eggs, turn off heat, lay cheese slice on top to melt. \n6. Lay 1 toast slice on plate, top with lettuce, cheesy scrambled eggs, and tomatoes, and close with remaining toast slice.', 
				        10, 
				        'Main Course', 
				        FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 
				        5
				    ),
				    (
				        11, 
				        'Stovetop', 
				        'Rigatoni, Onion, Garlic, Curry, Chili, Coconut Milk, Black Pepper, Basil', 
				        'Indian, Italian', 
				        'Pasta & Noodles', 
				        '1. Boil 5 quarts of water with 1.5 tablespoons of salt, then add rigatoni and cook for 10 minutes, reserving 1/2 cup of starchy pasta water before draining. \n2. Heat oil in a deep skillet over medium heat and sauté diced onions and minced garlic until translucent. \n3. Stir in curry and chili flakes, toasting the spices for 30 seconds. \n4. Pour in coconut milk, simmering gently for 3 to 5 minutes until thickened. \n5. Add drained rigatoni and a splash of reserved pasta water, stirring until sauce fills the hollow tubes. \n6. Season with salt and pepper, garnish with fresh basil.', 
				        30, 
				        'Main Course', 
				        FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 
				        6
				    );
				""",
				"""
				INSERT INTO REVIEW_REPORTS (SNAPSHOT_REVIEW_ID, SNAPSHOT_RATING, SNAPSHOT_RECIPE_ID, REPORT_ID) VALUES
				    (6, 5, 8, 4),  -- Valerie Vance reporting Grill Boss's 5-star review (Report ID 4)
				    (1, 1, 3, 7),  -- Peter Parker reporting 67_67's abusive review (Report ID 7)
				    (2, 1, 8, 8),  -- Sue mei reporting 67_67's hostile review (Report ID 8)
				    (5, 2, 3, 10); -- Peter Parker reporting Jeremy Lin's review (Report ID 10)
				""",
				"""
				INSERT INTO VIOLATIONS (VIOLATION_DESCRIPTION, VIOLATION_ACTIVE_TIMESTAMP, VIOLATION_STATUS, SEVERITY, SUSPENSION_STATUS, EXPIRE_TIMESTAMP, VIOLATOR_ID, REPORT_ID) VALUES
				    (
				        'Abuse of report feature by submitting frivolous reports based on personal taste preferences.',
				        '2024-04-08 14:00:00', -- Created 3 days after her April 5, 2024 report
				        'Expired',
				        'Minor',
				        'N/A',
				        '2024-05-08 14:00:00',
				        4, -- VIOLATOR_ID: Valerie Vance
				        1  -- REPORT_ID: Valerie Vance's report on insect recipe
				    ),
				    (
				        'Explicit / inappropriate avatar and text in user bio.',
				        '2025-06-12 09:30:00',
				        'Reversed',
				        'Severe',
				        'N/A',
				        NULL,
				        6, -- VIOLATOR_ID: Sue mei
				        2  -- REPORT_ID: Valerie Vance reporting Sue mei''s bio
				    ),
				    (
				        'Copyright infringement regarding Classic American Breakfast Sandwich recipe.',
				        '2026-09-07 10:15:00',
				        'Permanent',
				        'Moderate',
				        'Recipe',
				        '2026-10-07 10:15:00',
				        9, -- VIOLATOR_ID: RealGordanRamsay
				        5  -- REPORT_ID: Jeremy Lin reporting RealGordanRamsay
				    ),
				    (
				        'Copyright infringement regarding Curry Rigatoni recipe.',
				        '2026-09-08 19:19:19',
				        'Permanent',
				        'Moderate',
				        'N/A',
				        '2026-10-08 19:19:19',
				        9, -- VIOLATOR_ID: RealGordanRamsay
				        6  -- REPORT_ID: Steph Curry reporting RealGordanRamsay
				    ),
				    (
				        'Abusive, hateful, and harassing language left on recipe review.',
				        '2026-09-13 11:20:00',
				        'Permanent',
				        'Minor',
				        'N/A',
				        '2026-10-13 11:20:00',
				        8, -- VIOLATOR_ID: 67_67
				        7  -- REPORT_ID: Peter Parker reporting 67_67
				    ),
				    (
				        'Hostile and inappropriate language left on recipe review.',
				        '2026-09-14 16:45:00',
				        'Permanent',
				        'Moderate',
				        'All',
				        '2026-11-14 16:45:00',
				        8, -- VIOLATOR_ID: 67_67
				        8  -- REPORT_ID: Sue mei reporting 67_67
				    ),
				    (
				        'Toxic biography description.',
				        '2026-10-09 10:10:10',
				        'Permanent',
				        'Minor',
				        'N/A',
				        NULL,
				        8, -- VIOLATOR_ID: 67_67
				        9  -- REPORT_ID: Grill Boss reporting 67_67
				    ),
				    (
				        'Automated spam recipe creation and platform manipulation.',
				        '2026-11-11 06:09:09',
				        'Permanent',
				        'Severe',
				        'N/A',
				        NULL,
				        10, -- VIOLATOR_ID: Timothy32612
				        NULL -- Standalone violation detected by system
				    ),
				    (
				        'Unauthorized automated web scraping and bulk extraction of platform recipe data.',
				        '2026-11-12 06:08:08',
				        'Permanent',
				        'Severe',
				        'N/A',
				        NULL,
				        11, -- VIOLATOR_ID: Timothy32613
				        NULL -- Standalone violation detected by system
				    ),
				    (
				        'Automated spamming of reports.',
				        '2026-11-11 06:07:07',
				        'Permanent',
				        'Severe',
				        'N/A',
				        NULL,
				        12, -- VIOLATOR_ID: Timothy32614
				        NULL -- Standalone violation detected by system
				    );
				""",
				"""
				INSERT INTO APPEALS (APPEAL_DESCRIPTION, APPEAL_ACTIVE_TIMESTAMP, APPEAL_STATUS, DECISION_DESCRIPTION, RESOLVED_TIMESTAMP, AUDITOR_ID, VIOLATION_ID) VALUES
				    (
				        'My avatar was me when I was little. "I am a predator" meant I like raw meat, not children',
				        '2025-06-13 10:00:00',
				        'Approved',
				        'Verified the account holder''s image was herself when she was little. The phrasing meant something else and does not strongly imply sexual attraction to children.',
				        '2025-06-14 14:20:00',
				        1, 
				        1  
				    ),
				    (
				        'Breakfast sandwiches are universal culinary concepts. Nobody owns the trademark or copyright to putting egg, cheese, and bacon between toasted bread.',
				        '2026-09-08 09:00:00',
				        'Dismissed',
				        'The instructions were copied word for word, that is a copyright violation.',
				        '2026-09-09 11:30:00',
				        1, 
				        2  
				    ),
				    (
				        'I adapted this recipe from my own private restaurant notes. Fusion pasta dishes are common and this was an independent creation.',
				        '2026-09-09 12:00:00',
				        'Dismissed',
				        NULL,
				        '2026-09-10 16:00:00',
				        1, 
				        3  
				    ),
				    (
				        'I was merely giving an honest culinary critique. The review reflected my genuine reaction to the ingredients listed.',
				        '2026-09-14 08:30:00',
				        'Dismissed',
				        'Review contained explicit profanity and direct personal attacks against other platform members, violating community standards.',
				        '2026-09-15 10:15:00',
				        1, 
				        4  
				    ),
				    (
				        'I was frustrated and emotionally exaggerated my disappointment with the recipe. It was not meant to be taken literally.',
				        '2026-09-15 12:00:00',
				        'Dismissed',
				        NULL,
				        '2026-09-16 09:45:00',
				        1,
				        5 
				    ),
				    (
				        'My bio is satire aimed at hyper-sensitive reviewers. I did not single out any specific user by name.',
				        '2026-10-09 11:00:00',
				        'Dismissed',
				        NULL,
				        '2026-10-10 15:30:00',
				        1, 
				        6 
				    ),
				    (
				        'I submitted the report in good faith believing the dish violated basic health and safety standards for consumer recipes.',
				        '2024-04-09 09:15:00',
				        'Dismissed',
				        'You did not say it was for health concern, rather you said it was because of personal dislike, reports of which are disruptive for moderation.',
				        '2024-04-10 13:00:00',
				        1,
				        7 
				    ),
				    (
				        'I am a human user testing an automated recipe publishing assistant script to share quick home meals.',
				        '2026-11-11 06:15:00',
				        'Dismissed',
				        NULL,
				        '2026-11-11 07:00:00',
				        1,
				        8  
				    ),
				    (
				        'My account was accessing public recipe pages for personal educational research and nutritional data indexing.',
				        '2026-11-12 06:15:00',
				        'Dismissed',
				        NULL,
				        '2026-11-12 07:00:00',
				        1,
				        9 
				    ),
				    (
				        'I noticed platform bugs and used automated reporting scripts to flag suspicious recipes for the support team.',
				        '2026-11-11 06:15:00',
				        'Dismissed',
				        NULL,
				        '2026-11-11 07:00:00',
				        1,
				        10 
				    );
				"""
		};
		
		starter.executeAll(codeblocks);
	}
	
	public void populateRelationships() throws SQLException {
		String[] codeblocks = {
				"""
				INSERT INTO BOOKMARK (USER_ID, RECIPE_ID) VALUES
				    (1, 5), -- Jeremy Lin -> Deep Forest Smoothie
				    (1, 6), -- Jeremy Lin -> Microwave Tomato Soup
				    (2, 1), -- Mary Baker -> Comforting Vegan Congee for Stomach Relief
				    (2, 4), -- Mary Baker -> Toasted Sandwich with Egg, Cheese, and Veggies!
				    (3, 7), -- Peter Parker -> Grilled Steak
				    (3, 8), -- Peter Parker -> Sashimi Salad
				    (5, 3), -- Grill Boss -> Air-Fried Crispy Korean Beondegi
				    (5, 4), -- Grill Boss -> Toasted Sandwich with Egg, Cheese, and Veggies!
				    (5, 8), -- Grill Boss -> Sashimi Salad
				    (6, 3), -- Sue mei -> Air-Fried Crispy Korean Beondegi
				    (6, 7), -- Sue mei -> Grilled Steak
				    (7, 4), -- Steph Curry -> Toasted Sandwich with Egg, Cheese, and Veggies!
				    (7, 7); -- Steph Curry -> Grilled Steak
				""",
				"""
				INSERT INTO FOLLOW (FOLLOWER_ID, FOLLOWING_ID) VALUES
				    (2, 1), -- Mary Baker follows Jeremy Lin
				    (3, 5), -- Peter Parker follows Grill Boss
				    (3, 6), -- Peter Parker follows Sue mei
				    (5, 1), -- Grill Boss follows Jeremy Lin
				    (5, 3), -- Grill Boss follows Peter Parker
				    (5, 6), -- Grill Boss follows Sue mei
				    (6, 3), -- Sue mei follows Peter Parker
				    (6, 5), -- Sue mei follows Grill Boss
				    (7, 1), -- Steph Curry follows Jeremy Lin
				    (7, 5); -- Steph Curry follows Grill Boss
				""",
				"""
				INSERT INTO RECIPES_COOK_INGREDIENTS (RECIPE_ID, INGREDIENT_ID, AMOUNT, AMOUNT_UNIT) VALUES
				    -- 1. Comforting Vegan Congee for Stomach Relief
				    (1, 1, 1.00, 'cup'),        -- Rice
				    (1, 7, 2.00, 'leaves'),     -- Cabbage
				    (1, 10, 0.50, 'root'),      -- Nagaimo
				    (1, 11, 1.00, 'piece'),     -- Ginger
				    (1, 23, 1.00, 'block'),     -- Tofu
				    
				    -- 2. Homemade Chocolate Chip Cookies
				    (2, 3, 2.25, 'cups'),       -- Flour
				    (2, 45, 1.00, 'cup'),       -- Butter
				    (2, 47, 0.75, 'cup'),       -- White Sugar
				    (2, 48, 0.75, 'cup'),       -- Brown Sugar
				    (2, 24, 2.00, NULL),        -- Eggs
				    (2, 49, 1.00, 'teaspoon'),  -- Vanilla Extract
				    (2, 50, 2.00, 'cups'),      -- Chocolate
				    (2, 51, 1.00, 'teaspoon'),  -- Baking Soda
				    
				    -- 3. Air-Fried Crispy Korean Beondegi
				    (3, 27, 1.00, 'can'),       -- Silkworm Pupae
				    (3, 39, 1.00, 'tablespoon'),-- Sesame Oil
				    (3, 40, 1.00, 'tablespoon'),-- Soy Sauce
				    (3, 54, 1.00, 'teaspoon'),  -- Gochugaru
				    (3, 22, 0.50, 'teaspoon'),  -- Garlic Powder
				    (3, 46, 0.50, 'teaspoon'),  -- Sugar
				    
				    -- 4. Toasted Sandwich with Egg, Cheese, and Veggies!
				    (4, 4, 2.00, 'slices'),     -- Bread
				    (4, 14, 2.00, 'leaves'),    -- Butterleaf Lettuce
				    (4, 17, 0.50, NULL),        -- Beefsteak Tomato
				    (4, 24, 2.00, NULL),        -- Eggs
				    (4, 26, 1.00, 'slice'),     -- American Cheese
				    (4, 53, 0.25, 'teaspoon'),  -- Black Pepper
				    
				    -- 5. Deep Forest Smoothie
				    (5, 36, 1.00, 'cup'),       -- Almond Milk
				    (5, 30, 0.50, NULL),        -- Avocado
				    (5, 12, 1.00, 'cup'),       -- Spinach
				    (5, 31, 0.50, 'cup'),       -- Berries
				    (5, 33, 1.00, NULL),        -- Banana
				    
				    -- 6. Microwave Tomato Soup
				    (6, 44, 1.00, 'cup'),       -- Tomato Puree
				    (6, 37, 2.00, 'cups'),      -- Vegetable Broth
				    (6, 19, 0.25, NULL),        -- Onion
				    (6, 21, 1.00, 'clove'),     -- Garlic
				    (6, 53, 0.25, 'teaspoon'),  -- Black Pepper
				    
				    -- 7. Grilled Steak
				    (7, 28, 1.00, 'strip'),     -- Steak
				    (7, 53, 0.50, 'teaspoon'),  -- Black Pepper
				    
				    -- 8. Sashimi Salad
				    (8, 15, 1.00, 'head'),      -- Romaine Lettuce
				    (8, 18, 1.00, 'cup'),       -- Grape Tomato
				    (8, 40, 2.00, 'tablespoons'),-- Soy Sauce
				    (8, 42, 1.00, 'tablespoon'),-- Rice Vinegar
				    (8, 39, 1.00, 'tablespoon'),-- Sesame Oil
				    (8, 57, 1.00, 'teaspoon'),  -- Wasabi Paste
				    (8, 29, 0.50, 'pound'),     -- Sashimi Salmon
				    (8, 38, 1.00, 'teaspoon'),  -- Sesame
				    (8, 20, 1.00, 'stalk'),     -- Green Onion
				    (8, 58, 0.25, 'cup'),       -- Seaweed
				    
				    -- 9. Curry Rigatoni
				    (9, 6, 1.00, 'pound'),      -- Rigatoni
				    (9, 19, 1.00, NULL),        -- Onion
				    (9, 21, 2.00, 'cloves'),    -- Garlic
				    (9, 55, 2.00, 'tablespoons'),-- Curry
				    (9, 56, 1.00, 'teaspoon'),  -- Chili
				    (9, 35, 1.00, 'can'),       -- Coconut Milk
				    (9, 53, 0.25, 'teaspoon'),  -- Black Pepper
				    (9, 59, 0.25, 'cup'),       -- Basil
				    
				    -- 10. Classic American Breakfast Sandwich
				    (10, 4, 2.00, 'slices'),    -- Bread
				    (10, 14, 2.00, 'leaves'),   -- Butterleaf Lettuce
				    (10, 17, 0.50, NULL),       -- Beefsteak Tomato
				    (10, 24, 2.00, NULL),       -- Eggs
				    (10, 26, 1.00, 'slice'),    -- American Cheese
				    (10, 53, 0.25, 'teaspoon'), -- Black Pepper
				    
				    -- 11. Next Level Fusion: Curry Pasta!!!
				    (11, 6, 1.00, 'pound'),     -- Rigatoni
				    (11, 19, 1.00, NULL),       -- Onion
				    (11, 21, 2.00, 'cloves'),   -- Garlic
				    (11, 55, 2.00, 'tablespoons'),-- Curry
				    (11, 56, 1.00, 'teaspoon'), -- Chili
				    (11, 35, 1.00, 'can'),      -- Coconut Milk
				    (11, 53, 0.25, 'teaspoon'), -- Black Pepper
				    (11, 59, 0.25, 'cup');      -- Basil
				""",
				"""
				INSERT INTO RECIPES_HAS_DISH_TYPES (RECIPE_ID, DISH_TYPE_ID) VALUES
				    (1, 10),     -- Rice
				    (2, 12),     -- Cookie
				    (3, 13),     -- Meat
				    (4, 8),      -- Sandwich
				    (5, 2),      -- Smoothie
				    (6, 3),      -- Soup
				    (7, 13),     -- Meat
				    (8, 6),      -- Salad
				    (8, 13),     -- Meat
				    (9, 9),      -- Pasta & Noodles
				    (10, 8),     -- Sandwich
				    (11, 9);     -- Pasta & Noodles
				""",
				"""
				INSERT INTO RECIPES_FEATURE_CUISINES (RECIPE_ID, CUISINE_ID) VALUES
				    (1, 4),      -- Chinese
				    (2, 8),      -- Western
				    (3, 5),      -- Korean
				    (4, 8),      -- Western
				    (6, 8),      -- Western
				    (7, 9),      -- American
				    (8, 6),      -- Japanese
				    (8, 8),      -- Western
				    (9, 7),      -- Indian
				    (9, 11),     -- Italian
				    (10, 8),     -- Western
				    (11, 7),     -- Indian
				    (11, 11);    -- Italian
				""",
				"""
				INSERT INTO RECIPES_USE_APPLIANCES (RECIPE_ID, APPLIANCE_ID) VALUES
				    (1, 1),      -- Rice Cooker
				    (2, 2),      -- Oven
				    (2, 9),      -- Mixer
				    (3, 5),      -- Air Fryer
				    (4, 3),      -- Toaster
				    (4, 4),      -- Stovetop
				    (5, 6),      -- Blender
				    (6, 7),      -- Microwave
				    (7, 10),     -- Grill
				    (8, 8),      -- Food Processor
				    (9, 4),      -- Stovetop
				    (10, 3),     -- Toaster
				    (10, 4),     -- Stovetop
				    (11, 4);     -- Stovetop
				""",
				"""
				INSERT INTO INGREDIENTS_VIOLATE_DIETARY_RESTRICTIONS (INGREDIENT_ID, DIETARY_RESTRICTION_ID) VALUES
				    (27, 1),     -- Silkworm Pupae (Pescatarian violation)
				    (28, 1),     -- Steak (Pescatarian violation)
				    (29, 2),     -- Sashimi Salmon (Vegetarian violation)
				    (24, 3),     -- Eggs (Vegan violation)
				    (25, 3),     -- Cheese (Vegan violation)
				    (26, 3),     -- American Cheese (Vegan violation)
				    (34, 3),     -- Milk (Vegan violation)
				    (45, 3),     -- Butter (Vegan violation)
				    (3, 4),      -- Flour (Gluten-Free violation)
				    (4, 4),      -- Bread (Gluten-Free violation)
				    (5, 4),      -- Pasta (Gluten-Free violation)
				    (6, 4),      -- Rigatoni (Gluten-Free violation)
				    (40, 4),     -- Soy Sauce (Gluten-Free violation)
				    (25, 5),     -- Cheese (Dairy-Free violation)
				    (26, 5),     -- American Cheese (Dairy-Free violation)
				    (34, 5),     -- Milk (Dairy-Free violation)
				    (45, 5),     -- Butter (Dairy-Free violation)
				    (36, 6),     -- Almond Milk (Nut-Free violation)
				    (40, 7),     -- Soy Sauce (Sodium-Free violation)
				    (46, 8),     -- Sugar (Sugar-Free violation)
				    (47, 8),     -- White Sugar (Sugar-Free violation)
				    (48, 8),     -- Brown Sugar (Sugar-Free violation)
				    (24, 9),     -- Eggs (Egg-Free violation)
				    (1, 10),     -- Rice (Low-Carb violation)
				    (2, 10),     -- White Rice (Low-Carb violation)
				    (3, 10),     -- Flour (Low-Carb violation)
				    (4, 10),     -- Bread (Low-Carb violation)
				    (5, 10),     -- Pasta (Low-Carb violation)
				    (6, 10),     -- Rigatoni (Low-Carb violation)
				    (46, 10),    -- Sugar (Low-Carb violation)
				    (47, 10),    -- White Sugar (Low-Carb violation)
				    (48, 10),    -- Brown Sugar (Low-Carb violation)
				    (23, 11);    -- Tofu (Paleo violation)
				"""
		};
		
		starter.executeAll(codeblocks);
	}
}