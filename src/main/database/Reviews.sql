-- Not human-made
INSERT INTO REVIEWS (REVIEW_ID, REVIEW_NAME, REVIEW_IMAGE_PATH, REVIEW_DESCRIPTION, RATING, RECIPE_ID, REVIEWER_ID, REVIEW_STATUS) VALUES
(1, 'Great Pasta', NULL, 'Loved this traditional recipe. Very easy.', 5, 1, 2, 'Active'),
(2, 'A bit salty', '/images/reviews/saltygarlicchicken.jpg', 'Good, but cut down on the salt next time.', 4, 2, 4, 'Active'),
(3, 'Perfect Rice', NULL, 'Came out fluffy and perfect.', 5, 3, 1, 'Active'),
(4, 'Burnt my eggs', '/images/reviews/burnteggs.jpg', 'Instructions were unclear, eggs burnt.', 2, 4, 3, 'Active'),
(5, 'Delicious Soup', NULL, 'Very comforting on a cold day.', 5, 5, 8, 'Active'),
(6, 'Kids loved it', NULL, 'Great pasta salad for summer picnics.', 5, 6, 7, 'Active'),
(7, 'Took too long', NULL, 'Bread was good but took 4 hours.', 3, 7, 5, 'Active'),
(8, 'Sweet and savory', NULL, 'Onions caramelized perfectly.', 5, 8, 9, 'Active'),
(9, 'Better than takeout', NULL, 'Amazing fried rice, quick too.', 5, 9, 10, 'Active'),
(10, 'Too sweet', NULL, 'Cookies had way too much sugar for my diet.', 3, 10, 6, 'Active'),
(11, 'YOU SUCK', NULL, 'This is the worst recipe ever, quit cooking you idiot.', 1, 1, 11, 'Banned'),
(12, 'MAKE $5000 A DAY', NULL, 'Work from home! Visit www.spam123.com', 5, 2, 12, 'Banned'),
(13, 'Gross', NULL, 'The author is ugly and their food is poison.', 1, 3, 11, 'Banned'),
(14, 'Stolen!', NULL, 'I own this recipe, take it down now or I will sue.', 1, 4, 13, 'Banned'),
(15, 'BUY CRYPTO', NULL, 'Invest in Bitcoin now at www.cryptoscam.net', 5, 5, 12, 'Banned');