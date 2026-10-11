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
				        'Sue Sushi',
				        '/images/users/sue_sushi.jpg',
				        'I am a predator who only eats sushi. What are you going to do? Sue me? Ha, dream on!',
				        '2025-06-06 16:09:16',
				        NULL,
				        'Active',
				        'User',
				        'Asia/Tokyo',
				        '$argon2id$v=19$m=65536,t=3,p=1$c2FsdF9qZkllajQzag$D76tBiqd93gScqfEnjSKcEA1nsv252ZuWfPic/Lbj34=',
				        'suesushi8375@gmail.com'
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
						'images/appliances/stovetop.jpg',
						'Gas or electric range for boiling and frying'
					),
					(
						'Air Fryer', 
						'images/appliances/airfryer.jpg', 
						'Circulate super-heated air at high speeds, delivering crispy food using little to no oil.'
					),
					
					('Microwave', 'images/appliances/microwave.jpg', 'Quick heating appliance.'),
					('Blender', 'images/appliances/blender.jpg', 'Used for pureeing and mixing liquids.'),
					('Food Processor', 'images/appliances/processor.jpg', 'Used for chopping, slicing, and dicing.'),
					('Stand Mixer', 'images/appliances/mixer.jpg', 'Heavy duty mixer for dough and batter.'),
					('Slow Cooker', 'images/appliances/slowcooker.jpg', 'Simmers food at low temperatures.'),
				""",
				"""
				INSERT INTO RECIPES (RECIPE_NAME, RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION, INSTRUCTIONS, TOTAL_MINUTES, COURSE, AI_GEN_DESCRIPTION, AI_GEN_APPLIANCES, AI_GEN_CUISINES, AI_GEN_DISH_TYPES, AI_GEN_TOTAL_MINUTES, AI_GEN_COURSE, AUTHOR_ID) VALUES
					('Garlic Chicken', 'images/recipes/garlic_chicken.jpg', 'Juicy pan-seared chicken with garlic.', '1. Season chicken. 2. Heat oil in pan. 3. Cook chicken for 4-5 minutes. 4. Flip and cook for 3-4 more minutes 5. Add garlic until fragrant. 6. Add rice.', 25, 'Entrée', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, 3, 'Active'),
					('Tomato Soup', 'images/recipes/tomatosoup.jpg', 'Warm and comforting tomato soup.', '1. Roast cut tomato 2. Lightly mix cut tomatoes, diced onion, chopped garlic, salt, pepper, and olive oil. 3. Roast again, for 15 minutes. 4. Stir. 5. Bake for 15 minutes (again).  6. Blend on high speed until smooth. 7. Melt butter over medium-high heat. 8. Add flour to melted butter and mix. 9. Cook until mixture is bubbly and slightly thickened. 10. Add broth and sugar and mix. 11. Add blended mixure from earlier. 12. Add milk. 13. Cook on medium-low for 15 minutes, adding salt and pepper as needed. 14. Serve.', 120, 'Appetizer / Side', TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 2, 'Active'),
					('Pasta Salad', 'images/recipes/pastasalad.jpg', 'Cold pasta mixed with vegetables.', '1. Boil and cool pasta. 2. Chop vegetables and mozzarella. 3. Mix all ingredients. 4. Let set for 30 minutes so flavors can meld.', 60, 'Appetizer / Side', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 1, 'Active'),
					('Caramelized Onions', 'images/recipes/caramelizedonions.jpg', 'Sweet and savory slow-cooked onions.', '1. Slice onions and thyme. 2. Heat oil on skillet over medium heat. 3. Add onions and cook, tossing and stir occasionally for 15-20 minutes, or until softened. 4. Reduce heat to low and toss/stir occasionally for 50-60 minutes, or until onions are completely soft and caramelized. 5. Remove onions from skillet and add salt, pepper, and thyme.', 90, 'Appetizer / Side', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, 1, 'Active'),
					('Chicken Fried Rice', 'images/recipes/chickenfriedrice.webp', 'Rice stir-fried with chicken.', '1. Cook rice. 2. Dice carrots and chicken. 3. Add oils and chicken to large nonstick skillet or wok. 4. Cook on medium-high heat for 5 minutes. 5. Move chicken to a plate. 6. Move peas, carrot, corn to the skillet/wok. 7. Once the veggies are softened, add garlic. 8. Push the veggies to one side, and add eggs to the other. 9. Scramble the eggs. 10. Add chicken then rice to the pan. 11. Drizzle in soy sauce. 12. Cook until everything is heated. 13. Season and mix.', 40, 'Entrée', FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, 4, 'Active'),
					('Bleach Cleanse Smoothie', NULL, 'Cure everything with this smoothie.', '1. Mix bleach and ammonia. 2. Drink.', 10, 'Appetizer / Side', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 11, 'Banned'),
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
						'/images/recipes/air_fried_insects.jpg',
						'A modern twist on a beloved South Korean street food staple. Silkworm pupae are air-fried to a satisfying crunch. Rich, nutty, and bursting with umami flavor—it's street food made effortlessly modern',
						'2024-03-03 23:23:23',
				        NULL,
				        'Active',
						'1. Drain canned silkworm pupae. \n2. In a small bowl, evenly coat pupae with mixture of sesame oil, soy sauce, gochugaru, garlic powder, sugar, and salt. \n3. Preheat air fryer to 375°F (190°C) for 3 minutes. \n4. Spread the seasoned pupae in a single layer in the air fryer basket. \n5. Air fry for 9 minutes, shaking the basket every 3 minutes. \n6. Cool for a minute before serving. Enjoy!',
						15, 'Appetizer / Side', TRUE, FALSE, TRUE, FALSE, TRUE, FALSE, 3
					),
					(
						'Toasted Sandwich with Egg, Cheese, and Veggies!',
						'/images/recipes/sandwich.jpg',
						'Elevate your morning routine with this rich, fresh, and quick breakfast sandwich. Fluffy scrambled eggs, gooey melted American cheese, tender butterleaf lettuce, and juicy cherry tomatoes stacked high between golden crispy toast. The ultimate balanced breakfast sandwich!',
						'2024-03-15 15:15:15',
				        NULL,
				        'Active',
						'1. Toast 2 slices of bread until golden. \n2. Wash lettuce leaves, slice tomatoes, season with pinch of salt and black pepper. \n3. Whisk eggs with pinch of salt in small bowl. \n4. Medium-heat skillet with oil, pour in whisked eggs, gently push with spatula to form soft curds. \n5. Gather cooked eggs, turn off heat, lay cheese slice on top to melt. \n6. Lay 1 toast slice on plate, top with lettuce, cheesy scrambled eggs, and tomatoes, and close with remaining toast slice.',
						10, 'Main Course', TRUE, TRUE, FALSE, FALSE, TRUE, TRUE, 1
					),
					(
						'Curry Rigatoni',
						'/images/recipes/curry_rigatoni.jpg',
						'Global fusion dish featuring ridged, hollow rigatoni tubes coated in a rich, velvety curry cream sauce. The inner chambers hold onto every drop of sauce for a flavorful bite.', 
						'2025-07-17 17:17:17',
				        NULL,
				        'Active',
						'1. Boil 5 quarts of water with 1.5 tablespoons of salt, then add rigatoni and cook for 10 minutes, reserving 1/2 cup of starchy pasta water before draining. \n2. Heat oil in a deep skillet over medium heat and sauté diced onions and minced garlic until translucent. \n3. Stir in curry powder, cumin, and chili flakes, toasting the spices for 30 seconds. \n4. Pour in coconut milk, simmering gently for 3 to 5 minutes until thickened. \n5. Add drained rigatoni and a splash of reserved pasta water, stirring until sauce fills the hollow tubes. \n6. Season with salt and pepper, garnish with fresh basil.', 
						30, 'Main Course', TRUE, TRUE, FALSE, TRUE, TRUE, FALSE, 7
					),
					(
						'Classic American Breakfast Sandwich',
						'/images/recipes/sandwich.jpg',
						'If you got the skills, simple ingredients can transform into heavenly taste!',
						'2026-08-15 08:15:08',
						'2026-08-18 18:18:18',
				        'Banned',
						'1. Toast 2 slices of bread until golden. \n2. Wash lettuce leaves, slice tomatoes, season with pinch of salt and black pepper. \n3. Whisk eggs with pinch of salt in small bowl. \n4. Medium-heat skillet with oil, pour in whisked eggs, gently push with spatula to form soft curds. \n5. Gather cooked eggs, turn off heat, lay cheese slice on top to melt. \n6. Lay 1 toast slice on plate, top with lettuce, cheesy scrambled eggs, and tomatoes, and close with remaining toast slice.',
						10, 'Main Course', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 9
					),
					(
						'Next Level Global Fusion: Curry Pasta!!!',
						'/images/recipes/curry_rigatoni.jpg',
						'Combining Indian and Italian Cuisine for a super modern, extremely trendy, and most delicious dish!!!', 
						'2026-09-05 09:05:09',
						'2026-09-08 19:19:19',
				        'Banned',
						'1. Boil 5 quarts of water with 1.5 tablespoons of salt, then add rigatoni and cook for 10 minutes, reserving 1/2 cup of starchy pasta water before draining. \n2. Heat oil in a deep skillet over medium heat and sauté diced onions and minced garlic until translucent. \n3. Stir in curry powder, cumin, and chili flakes, toasting the spices for 30 seconds. \n4. Pour in coconut milk, simmering gently for 3 to 5 minutes until thickened. \n5. Add drained rigatoni and a splash of reserved pasta water, stirring until sauce fills the hollow tubes. \n6. Season with salt and pepper, garnish with fresh basil.', 
						30, 'Main Course', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 9
					)	
				"""
		};
		
		starter.executeAll(codeblocks);
		
		String[] setpaths = {
				"src/main/database/Appliances.sql",
				"src/main/database/Cuisines.sql",
				"src/main/database/DietaryRestrictions.sql",
				"src/main/database/DishTypes.sql",
				"src/main/database/Ingredients.sql",
				"src/main/database/Recipes.sql",
				"src/main/database/Reviews.sql",
				"src/main/database/Reports.sql",
				"src/main/database/RecipeReports.sql",
				"src/main/database/ReviewReports.sql",
				"src/main/database/Violations.sql",
				"src/main/database/Appeals.sql",
				};
		for (String setpath : setpaths) {
			SqlReader.rExecute(starter.connection, setpath);
		}
	}
	
	public void populateRelationships() throws SQLException {
		String[] setpaths = {
				"src/main/database/IngredientsFitDietaryRestrictions.sql",
				"src/main/database/RecipesCookIngredients.sql",
				"src/main/database/RecipesHasDishTypes.sql",
				"src/main/database/RecipesFeatureCuisines.sql",
				"src/main/database/RecipesUseAppliances.sql",
				"src/main/database/Follow.sql",
				"src/main/database/Bookmark.sql"
				};
		for (String setpath : setpaths) {
			SqlReader.rExecute(starter.connection, setpath);
		}
	}
}