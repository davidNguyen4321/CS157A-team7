<%@ page import="java.sql.*"
import="Search.*"
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recipe Genie</title>
    <!-- Stylesheet lives in the webapp/css folder -->
    <link rel="stylesheet" type="text/css" href="css/index.css">
</head>
<body>

    <%
        // Retrieve username from session or database (e.g., session.getAttribute("username"))
        String dbUsername = (String) session.getAttribute("username");
        String displayUsername = (dbUsername != null && !dbUsername.trim().isEmpty()) ? dbUsername : "Username";

        // Tracks which tab is currently selected
        String currentTab = request.getParameter("tab");
        if (currentTab == null || currentTab.trim().isEmpty()) {
            currentTab = "discover";
        }
    %>

    <%
        String dbURL = "jdbc:mysql://localhost:3306/recipegenie?serverTimezone=UTC";
        String dbUser = "root";
        String dbPassword = request.getParameter("dbPassword");
        
        Connection dbConnection = null;
        boolean isConnected = false;
        if (dbPassword != null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                isConnected = true;
                DatabaseAccess.setConnection(dbConnection);
            } catch (Exception e) {
                // Connection failed (wrong password, etc.)
            }
        }
    %>

    <!-- Temporary Password Prompt Box (if not connected yet) -->
    <% if (!isConnected) { %>
        <div style="background: #f8d7da; padding: 15px; text-align: center; border-bottom: 1px solid #f5c6cb;">
            <form method="post" action="index.jsp?tab=<%= currentTab %>">
                <label for="dbPassword"><strong>Enter MySQL Root Password to Connect:</strong></label>
                <input type="password" id="dbPassword" name="dbPassword" required>
                <button type="submit">Connect Database</button>
            </form>
        </div>
    <% } %>

    <!--
      =========================================================================
      NAVIGATION HEADER
      =========================================================================
    -->
    <header>
        <div class="header-inner">
            <div class="header-container">
                <!-- Left: Plate Logo + Discover, Share, Library Nav -->
                <div class="nav-group">
                    <div class="plate-logo" title="Recipe Genie">
                        <div class="plate-rim">
                            <div class="plate-text-container">
                                <span class="logo-recipe">Recipe</span>
                                <span class="logo-genie">Genie</span>
                            </div>
                        </div>
                    </div>
                    <nav class="main-nav">
                        <a href="index.jsp?tab=discover" class="header-btn <%= "discover".equals(currentTab) ? "active" : "" %>">Discover</a>
                        <a href="index.jsp?tab=share" class="header-btn <%= "share".equals(currentTab) ? "active" : "" %>">Share</a>
                        <a href="index.jsp?tab=library" class="header-btn <%= "library".equals(currentTab) ? "active" : "" %>">Library</a>
                    </nav>
                </div>

                <!-- Center: Empty (Balanced Spacer) -->
                <div class="header-spacer"></div>

                <!-- Right: Username and Profile Picture -->
                <div class="header-actions">
                    <div class="user-profile-group">
                        <span class="username-display"><%= displayUsername %></span>
                        <div class="user-avatar" title="Account">
                            👤
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </header>

    <!--
      =========================================================================
      DISCOVER TAB MAIN SECTION
      =========================================================================
    -->
    <main>
        <!-- Hero & Smart Search Box Section -->
        <section class="hero-section">
            <div class="hero-bg-blobs">
                <div class="blob-1"></div>
                <div class="blob-2"></div>
            </div>
            <div class="hero-content-wrapper">
                <!-- Use inline-block style so this wrapper matches the exact width of the title text -->
                <div style="display: inline-block; max-width: 100%;">
                    <div class="hero-badge">
                        <span class="hero-badge-dot"></span>
                        Cook smarter, not harder
                    </div>
                    <h1 class="hero-title">
                        What can you cook <span style="white-space: nowrap;"><em>right now?</em></span>
                    </h1>
                    <div class="hero-desc">
                        <p style="margin: 0 0 0.5rem 0;">Search by ingredients and appliances you already have.</p>
                        <p style="margin: 0;">Discover recipes that fit your kitchen, not the other way around.</p>
                    </div>

                    <!-- Smart search form -->
                    <form action="index.jsp" method="GET" class="search-form" style="width: 100%;">
                        <div class="search-top-bar">
                            <span class="search-icon-fixed">🔍</span>
                            <input id="RecipesSearch" type="text" name="search" placeholder="Search recipe..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">

                            <!-- Filters button next to the search bar -->
                            <button type="button" class="filter-btn" onclick="handleFilterTypeClick('GeneralPanel', this);">
                                ⚙️ Filters
                            </button>
                        </div>

                        <div class="search-bottom-row">
                           <button type="button" id="Ingredients" class="search-filter-col" onclick="handleFilterTypeClick('IngredientsPanel', this);">
                               <div class="filter-icon-box">🥚</div>
                               <div>
                                   <div class="filter-label">Ingredients</div>
                                   <div class="filter-sub">What do you have?</div>
                               </div>
                           </button>
                           
                           <button type="button" id="Appliances" class="search-filter-col" onclick="handleFilterTypeClick('AppliancesPanel', this);">
                               <div class="filter-icon-box">🍳</div>
                               <div>
                                   <div class="filter-label">Appliances</div>
                                   <div class="filter-sub">What can you use?</div>
                               </div>
                           </button>
                           
                           <button type="button" id="Authors" class="search-filter-col" onclick="handleFilterTypeClick('AuthorsPanel', this);">
                               <div class="filter-icon-box">👤</div>
                               <div>
                                   <div class="filter-label">Authors</div>
                                   <div class="filter-sub">Who to learn from?</div>
                               </div>
                           </button>
                           <button type="button" class="find-recipes-submit" onclick="searchRecipes('RecipesSearch');">Find Recipes</button>
                       </div>
                       
						<div id="GeneralPanel" class="filter-panel">
							<div class="filter-panel-container-no-grid" style="align-items: center;">
								<h1 class="filter-panel-name">
								General Filters
		                    	</h1>
		                    	<div class="vrounded-divider" style="border-color:#1F1800; height:50px; opacity:0.6;"> </div>
		                    	<button id="PublicationDateSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'PublicationDateSortText', 'Publication Date');">
		                    		<p id="PublicationDateSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Publication Date
		                    	</button>
		                    	<button id="BookmarksSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'BookmarksSortText', 'Bookmarks');">
									<p id="BookmarksSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Bookmarks
								</button>
		                    	<button id="ReviewSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'ReviewsSortText', 'Reviews');">
		                    		<p id="ReviewsSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Reviews
		                    	</button>
		                    	<button id="RatingsSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'ReviewsSortText', 'Ratings');">
		                    		<p id="RatingsSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Ratings
		                    	</button>
	                    	</div>
	                    	<div class="filter-panel-container-no-grid">
	                    		<div class="filter-panel-mini" style="width:1rem;">
				                    <h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Dish Types
			                    	</h2>
			                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
			                    	<div style="color: #F5F5DC" class="search-top-bar">
			                           <span class="search-icon-fixed">🔍</span>
			                           <input id="DishTypesSearch" type="text" name="search" placeholder="Search dish types..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
			                       	</div>
			                       	<div id="DishTypesSearchContainer" class="filter-panel-search-container">
									</div>
				                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
				                    	<h2 class="mini-text" style="color:#1F1800; font-size: 0.75rem;">
										Selected Dish Types
				                    	</h2>
			                       	<div id="DishTypesContainer" class="filter-panel-container">
									</div>
	                    		</div>
	                    		<div class="filter-panel-mini" style="width:1rem;">
				                    <h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Cuisines
			                    	</h2>
			                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
			                    	<div style="color: #F5F5DC" class="search-top-bar">
			                           <span class="search-icon-fixed">🔍</span>
			                           <input id="CuisinesSearch" type="text" name="search" placeholder="Search cuisines..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
			                       	</div>
			                       	<div id="CuisinesSearchContainer" class="filter-panel-search-container">
									</div>
				                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
				                    	<h2 class="mini-text" style="color:#1F1800; font-size: 0.75rem;">
										Selected Cuisines
				                    	</h2>
			                       	<div id="CuisinesContainer" class="filter-panel-container">
									</div>
	                    		</div>
	                    		<div class="filter-panel-mini"  style="flex: 0; min-width: 200px; display: block;">
	                    			<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Options
			                    	</h2>
			                    	<label for="AiGeneratedDescription" class="mini-text" style="color:#826300; font-size: 0.8rem;">Ai Generated Description</label>
    								<input type="checkbox" name="AiGeneratedDescription" id="AiGeneratedDescription"/><br>
    								<label for="AiGeneratedAppliances" class="mini-text" style="color:#826300; font-size: 0.8rem;">Ai Generated Appliances</label>
			                    	<input type="checkbox" name="AiGeneratedAppliances" id="AiGeneratedAppliances"/><br>
			                    	<label for="AiGeneratedCuisines" class="mini-text" style="color:#826300; font-size: 0.8rem;">Ai Generated Cuisines</label>
			                    	<input type="checkbox" name="AiGeneratedCuisines" id="AiGeneratedCuisines"/><br>
			                    	<label for="AiGeneratedDishTypes" class="mini-text" style="color:#826300; font-size: 0.8rem;">Ai Generated Dish Types</label>
			                    	<input type="checkbox" name="AiGeneratedDishTypes" id="AiGeneratedDishTypes"/><br>
			                    	<label for="AiGeneratedCookingMinutes" class="mini-text" style="color:#826300; font-size: 0.8rem;">Ai Generated Cooking Minutes</label>
			                    	<input type="checkbox" name="AiGeneratedCookingMinutes" id="AiGeneratedCookingMinutes"/><br>
			                    	<label for="AiGeneratedCourses" class="mini-text" style="color:#826300; font-size: 0.8rem;">Ai Generated Courses</label>
			                    	<input type="checkbox" name="AiGeneratedCourses" id="AiGeneratedCourses"/>
	                    		</div>
	                    	</div>
	                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
	                    	<div class="filter-panel-container-no-grid">
	                    		<div class="filter-panel-mini"  style="min-width: 200px;">
	                    			<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Dietary Restrictions
			                    	</h2>
			                    	<div class="filter-panel-container">
			                    		<%
					       					 if (dbConnection != null) {
					       						String sql = 
			     					    				"SELECT DIETARY_RESTRICTION_ID, DIETARY_RESTRICTION_NAME " +
			     					    				"FROM DIETARY_RESTRICTIONS " +
			     					    				"ORDER BY DIETARY_RESTRICTION_NAME ASC";
					     					    try (
					     	                            PreparedStatement popularStatement =
					                                    dbConnection.prepareStatement(sql);
					                                ResultSet popularRecipes =
					                                    popularStatement.executeQuery()
					                            ) {
					     					    	
					     					    	PreparedStatement statement = dbConnection.prepareStatement(sql);
					     					    	ResultSet dietaryRestrictions = statement.executeQuery();
					     					    	
					     					    	while(dietaryRestrictions.next()){
					     					    		int dietaryRestrictionId = dietaryRestrictions.getInt("DIETARY_RESTRICTION_ID");
					     					    		String dietaryRestrictionName = dietaryRestrictions.getString("DIETARY_RESTRICTION_NAME");
			                    		%>
			                    			<button type="button" class="mini-container-toggleable" style="cursor:pointer; justify-content: center; align-items: center;" onclick="toggleBasicFilter(this, 'Dietary Restriction', <%=dietaryRestrictionId%> )">
			                    				<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
												<%=dietaryRestrictionName%>
						                    	</h2>
			                    			</button>
			                    		<%
			                    					}
			                    				}
					     					   	catch (Exception error){
					   					    	out.println("<p>Database error: " +error.getMessage() + "</p>");
					   					    	}
			                    			}
			                    		%>
									</div>
	                    		</div>
	                    		<div class="filter-panel-mini"  style="min-width: 200px;">
	                    			<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Courses
			                    	</h2>
			                    	<div class="filter-panel-container">
			                    		<%
					       					 if (dbConnection != null) {
					       						String sql = 
			     					    				"SELECT DISTINCT COURSE " +
			     					    				"FROM RECIPES " +
			     					    				"WHERE COURSE IS NOT NULL " +
			     					    				"ORDER BY COURSE ASC";
					     					    try (
					     	                            PreparedStatement popularStatement =
					                                    dbConnection.prepareStatement(sql);
					                                ResultSet popularRecipes =
					                                    popularStatement.executeQuery()
					                            ) {
					     					    	
					     					    	PreparedStatement statement = dbConnection.prepareStatement(sql);
					     					    	ResultSet dietaryRestrictions = statement.executeQuery();
					     					    	
					     					    	while(dietaryRestrictions.next()){
					     					    		String course = dietaryRestrictions.getString("COURSE");
			                    		%>
			                    			<button type="button" class="mini-container-toggleable" style="cursor:pointer; justify-content: center; align-items: center;" onclick="toggleBasicFilter(this, 'Course', '<%=course%>' )">
			                    				<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
												<%=course%>
						                    	</h2>
			                    			</button>
			                    		<%
			                    					}
			                    				}
					     					   	catch (Exception error){
					   					    	out.println("<p>Database error: " +error.getMessage() + "</p>");
					   					    	}
			                    			}
			                    		%>
									</div>
	                    		</div>
	                    		<div class="filter-panel-mini"  style="flex: 0; min-width: 450px;">
	                    			<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Ranges
			                    	</h2>
			                    	<div class="filter-panel-mini">
`		                    			<h2 class="mini-text" style="color:#523F00; font-size: 0.9rem;">
											Reviews
		                    			</h2>
				                    	<div class="filter-panel-mini-flex">
				                    		<input type="number" id="ReviewsMinRange" name="ReviewsMinRange" class="filter-range" min="0" step="1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="ReviewsMaxRange" name="ReviewsMaxRange" class="filter-range" min="0" step="1"/>
				                    		<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												max
				                    			</h2>
				                    		</div>
				                    	</div>
			                    	</div>
			                    	<div class="filter-panel-mini">
	                    				<h2 class="mini-text" style="color:#523F00; font-size: 0.9rem;">
											Bookmarks
		                    			</h2>
				                    	<div class="filter-panel-mini-flex">
				                    		<input type="number" id="BookmarksMinRange" name="BookmarksMinRange" class="filter-range" min="0" step="1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="BookmarksMaxRange" name="BookmarksMaxRange" class="filter-range" min="0" step="1"/>
				                    		<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												max
				                    			</h2>
				                    		</div>
			                    		</div>
			                    	</div>
			                    	<div class="filter-panel-mini">
	                    				<h2 class="mini-text" style="color:#523F00; font-size: 0.9rem;">
											Average Ratings
		                    			</h2>
				                    	<div class="filter-panel-mini-flex">
				                    		<input type="number" id="RatingsMinRange" name="RatingsMinRange" class="filter-range" min="0" max="5" step="1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="RatingsMaxRange" name="RatingsMaxRange" class="filter-range" min="0" max="5" step="1"/>
				                    		<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												max
				                    			</h2>
				                    		</div>
			                    		</div>
			                    	</div>
			                    	<div class="filter-panel-mini">
	                    				<h2 class="mini-text" style="color:#523F00; font-size: 0.9rem;">
											Cooking Time (Minutes)
		                    			</h2>
				                    	<div class="filter-panel-mini-flex">
				                    		<input type="number" id="CookingTimeMinRange" name="CookingTimeMinRange" class="filter-range" min="0" step="1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="CookingTimeMaxRange" name="CookingTimeMaxRange" class="filter-range" min="0" step="1"/>
				                    		<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												max
				                    			</h2>
				                    		</div>
			                    		</div>
			                    	</div>
	                    		</div>
	                    	</div>
                       </div>
                       
                       <div id="IngredientsPanel" class="filter-panel">
							<h1 class="filter-panel-name">
							Ingredients
	                    	</h1>
	                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
                       	<div style="color: #F5F5DC" class="search-top-bar">
                           <span class="search-icon-fixed">🔍</span>
                           <input id="IngredientsSearch" type="text" name="search" placeholder="Search ingredients..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
                       	</div>
                       <div id="IngredientsSearchContainer" class="filter-panel-search-container">
						</div>
	                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
	                    	<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
							Selected ingredients
	                    	</h2>
                       	<div id="IngredientsContainer" class="filter-panel-container">
						</div>
                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
                    	<h2 id="IngredientsToleranceDisplay" class="mini-text" style="color:#1F1800; font-size: 1rem;">
							Allow up to 0 missing ingredients
                    	</h2>
						<div class="slidecontainer">
						  <input id="IngredientsSlider" type="range" min="0" max="3" value="0" class="slider">
						</div>
                       </div>
                       
						<div id="AppliancesPanel" class="filter-panel">
							<h1 class="filter-panel-name">
							Appliances
	                    	</h1>
	                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
	                       	<div style="color: #F5F5DC" class="search-top-bar">
	                           <span class="search-icon-fixed">🔍</span>
	                           <input id="AppliancesSearch" type="text" name="search" placeholder="Search appliances..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
	                       	</div>
	                       <div id="AppliancesSearchContainer" class="filter-panel-search-container">
						</div>
	                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
	                    	<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
							Selected appliances
	                    	</h2>
	                    <div id="AppliancesContainer" class="filter-panel-container">
						</div>
	                    <hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
						<h2 id="AppliancesToleranceDisplay" class="mini-text" style="color:#1F1800; font-size: 1rem;">
							Allow up to 0 missing appliances
	                    </h2>
						<div class="slidecontainer">
						  <input id="AppliancesSlider" type="range" min="0" max="3" value="0" class="slider">
						</div>
	                    </div>
	                    
						<div id="AuthorsPanel" class="filter-panel">
							<h1 class="filter-panel-name">
							Authors
	                    	</h1>
	                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
	                       	<div style="color: #F5F5DC" class="search-top-bar">
	                           <span class="search-icon-fixed">🔍</span>
	                           <input id="AuthorsSearch" type="text" name="search" placeholder="Search authors..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
	                       	</div>
	                       <div id="AuthorsSearchContainer" class="filter-panel-search-container">
						</div>
	        	            	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
	                    	<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
							Selected authors
	                    	</h2>
	                    <div id="AuthorsContainer" class="filter-panel-container">
						</div>
                       </div>
						<script>
						const sortOrder = [];
						const sortOptions = new Map(); // use an array that is just the sort option AND a map that contains whether it ascends or not
						for (let key of ["Publication Date", "Bookmarks", "Reviews", "Ratings"]){
							let elementBaseId = key.replace(" ", "");
							let object = {
									sort: "ascending",
									sortTextId: elementBaseId+"SortText"
								}
							sortOptions.set(key	, object); //True or false refers to ascending (True) or descending (False)
						}
						
						sortSelect(document.getElementById("PublicationDateSortOption"), "PublicationDateSortText", "Publication Date");
						const filterIds = new Map();
						for (let key of ["Ingredient", "Appliance", "Author", "Cuisine", "Dish Type", "Dietary Restriction", "Course"]){
							filterIds.set(key, []);
						}
						
						addSearchListenerWithImage('DishTypesSearch', 'Dish Type', 'DishTypesSearchContainer', 'DishTypesContainer', '/RecipeGenie/DishTypes/Search?', false);
						addSearchListenerWithImage('CuisinesSearch', 'Cuisine', 'CuisinesSearchContainer', 'CuisinesContainer', '/RecipeGenie/Cuisines/Search?', false);
						addSearchListenerWithImage('IngredientsSearch', 'Ingredient', 'IngredientsSearchContainer', 'IngredientsContainer', '/RecipeGenie/Ingredients/Search?', true);
						addSearchListenerWithImage('AppliancesSearch', 'Appliance', 'AppliancesSearchContainer', 'AppliancesContainer', '/RecipeGenie/Appliances/Search?', true);
						addSearchListenerWithImage('AuthorsSearch', 'Author', 'AuthorsSearchContainer', 'AuthorsContainer', '/RecipeGenie/Users/Search?', true);
						
						const tolerances = new Map();
						for (let key of ["Ingredient", "Appliance"]){
							tolerances.set(key, "0");
						}
						
						addSliderListener("IngredientsSlider", "IngredientsToleranceDisplay", "Ingredient");
						addSliderListener("AppliancesSlider", "AppliancesToleranceDisplay", "Appliance");
						
						const ranges = new Map();
						for (let key of ["Bookmarks", "Cooking Time", "Ratings", "Reviews"]){
							let object = {
									min: "",
									max: ""
							}
							ranges.set(key, object);
						}
						
						addMinMaxListener("BookmarksMinRange", "BookmarksMaxRange", true, 0, null, "Bookmarks");
						addMinMaxListener("CookingTimeMinRange", "CookingTimeMaxRange", true, 0, null, "Cooking Time");
						addMinMaxListener("RatingsMinRange", "RatingsMaxRange", true, 0, 5, "Ratings");
						addMinMaxListener("ReviewsMinRange", "ReviewsMaxRange", true, 0, null, "Reviews");

						const options = new Map();
						for (let key of ["Ai Generated Description", "Ai Generated Appliances", "Ai Generated Cuisines", "Ai Generated Dish Types", "Ai Generated Cooking Minutes", "Ai Generated Courses"]){
							options.set(key, false);
						}
						
						addCheckListener("AiGeneratedDescription", "Ai Generated Description");
						addCheckListener("AiGeneratedAppliances", "Ai Generated Appliances");
						addCheckListener("AiGeneratedCuisines", "Ai Generated Cuisines");
						addCheckListener("AiGeneratedDishTypes", "Ai Generated Dish Types");
						addCheckListener("AiGeneratedCookingMinutes", "Ai Generated Cooking Minutes");
						addCheckListener("AiGeneratedCourses", "Ai Generated Courses");
						
						function handleFilterTypeClick(type, button) {
							//toggling both the filter panel and the button
					    	toggleActiveExclusive(document.getElementById(type), '.filter-panel');
					    	toggleActiveExclusive(button, '.search-filter-col, .filter-btn');
					    }
						
						function toggleActiveExclusive(element, classname){
							//initialization
							let classInstances = document.querySelectorAll(classname);
							let wasActive = element && element.classList.contains('active');
							
							//removing other activity
					    	for (let classInstance of classInstances) {
					    		classInstance.classList.remove('active');
					    	}
					    	
							//toggle
							if (!wasActive) {
					            element.classList.add('active');
					        }
						}
						
						function sortSelect(self, textElement, type){
							if (self.classList.contains('descending')){ // State 1: descending --> ascending
								self.classList.remove('descending');
								self.classList.add('ascending');
								sortOptions.get(type).sort='ascending';
								updateSortSelectIndex(type);
							}
							else if (self.classList.contains('ascending')){ // State 2: ascending --> not sorted
								self.classList.remove('ascending');
								sortOptions.get(type).sort=null;
								let index = sortOrder.indexOf(type);
								if (index !== -1){
									sortOrder.splice(index, 1);
								}
							}
							else{ // State 3: not sorted --> descending
								self.classList.add('descending');
								sortOptions.get(type).sort='descending';
								updateSortSelectIndex(type);
							}
							updateSortTextElements();
						}
						
						function updateSortSelectIndex(type){
							let index = sortOrder.indexOf(type);
							if (index !== -1){
								sortOrder.splice(index, 1);
							}
							sortOrder.push(type);
							updateSortTextElements();
						}
						
						function updateSortTextElements(){
							for (let key of sortOptions.keys()){
								let element = document.getElementById(sortOptions.get(key).sortTextId);
								let sortPosition = sortOrder.indexOf(key)+1;
								if(sortPosition == 0){
									element.textContent = key;
									continue;
								}
								element.textContent = sortPosition+ " " + key  +": " + sortOptions.get(key).sort;
								element.innerHTML = element.innerHTML.replace(sortPosition, "<span style='color: #1E4E3B;'>"+sortPosition+"</span>");
							}
						}
						
						function toggleBasicFilter(self, type, id){
							if(self.classList.contains('active')){
								self.classList.remove('active');
								removeFilter(id, type);
							}
							else{
								self.classList.add('active');
				            	filterIds.get(type).push(id);
							}
						}
						
						function addSearchListenerWithImage(searchId, type, panelSearchContainerId, panelContainerId, url, imageBoolean){
							let input = document.getElementById(searchId);
							let timeout = null;
							input.addEventListener('keyup', function (e) {//tracking keyboard input
								    clearTimeout(timeout);
								    timeout = setTimeout(function () { // to prevent the user from overloading the server with too many requests
								    	let searchTerm = input.value;
								    	if(searchTerm&&searchTerm.trim()){ //to check if the search term is of value
								    		searchFilters(searchId, searchTerm, type, panelSearchContainerId, panelContainerId, url, imageBoolean);
								    	}
								    	else{
								        	removeFilterPanelSearchResults(panelSearchContainerId);
								    	}
								    }, 100);
							});
						}
						
						async function searchFilters(searchId, searchTerm, type, panelSearchContainerId, panelContainerId, url, imageBoolean){
				    		searchTerm = searchTerm.trim();
					        let parameters = new URLSearchParams();
					        parameters.append('searchTerm', searchTerm);
					        parameters.append('imageBoolean', imageBoolean);
					        parameters.append('excludeString', JSON.stringify(filterIds.get(type)));
					        
					        //connect to web server
							let response = await fetch(url + parameters.toString(), {
					        	method:'GET',
					        	headers:{
					                "Content-Type": "application/json",
					                "Accept": "application/json",
					        	},
					        });
							
							if (!response.ok) {
					        	removeFilterPanelSearchResults(panelSearchContainerId); // There are multiple calls of these at multiple diverging points to avoid the ugly flickering of existing and not existing
					        	throw new Error('Network response was not ok');
					        }
				        	let objects = await response.json(); // Creates a JSON object
				        	removeFilterPanelSearchResults(panelSearchContainerId);
				        	newFilterPanelSearchResults(searchId, objects, type, panelSearchContainerId, panelContainerId, imageBoolean);
						}
						
						function removeFilterPanelSearchResults(panelSearchContainerId){ //clear results to make way for an updated version
			            	let panel = document.getElementById(panelSearchContainerId);
							let FPSRs = panel.querySelectorAll('.filter-panel-search-autocomplete');
							for (FPSR of FPSRs){
								FPSR.remove();
							}
						}
						
						function newFilterPanelSearchResults(searchId, objects, type, panelSearchContainerId, panelContainerId, imageBoolean){
			            	let panel = document.getElementById(panelSearchContainerId);
			            	//create a result for each object
							for (let objectString of Object.values(objects)){
								//initialization
				            	let object = JSON.parse(objectString);
				            	let id = object["id"];
				            	let name = object["name"];
				            	
								//the base element
				            	let newElement = document.createElement("button");
				            	newElement.classList.add("filter-panel-search-autocomplete");
				            	newElement.setAttribute('type','button');
				            	
				            					            	
				            	if(imageBoolean){
					            	let image = object["image"];
					            	let newPic = document.createElement("img");
					            	newPic.classList.add("mini-pic");
					            	newPic.setAttribute('src', "${pageContext.request.contextPath}"+image);
					            	newPic.setAttribute('alt', "'" + type + "'");
					            	newElement.setAttribute('onclick', "addFromSearch('"+ searchId +"', this, " + id + ", '" + name + "', '" + image + "', '" + type + "', '" + panelContainerId + "', '"+panelSearchContainerId+"');");
					            	newElement.append(newPic);
				            	}
				            	else{
					            	newElement.setAttribute('onclick', "addFromSearch('"+ searchId +"', this, " + id + ", '" + name + "', " + false + ", '" + type + "', '" + panelContainerId + "', '"+ panelSearchContainerId+"');");
				            	}
				            	
				            	//the text element
				            	let newName = document.createElement("p");
				            	newName.classList.add("mini-text");
				            	newName.textContent=name;
				            	newElement.append(newName);
				            	
				            	//connecting element
				            	panel.append(newElement);
				            }
						}
						
						function addFromSearch(searchId, self, id, name, image, type, panelContainerId, panelSearchContainerId){	
							// the base element
			            	let filter = document.createElement("button");
			            	filter.classList.add("mini-container");
			            	filter.setAttribute('type','button');
			            	filter.setAttribute("onclick", "removeFilter(" + id + ", '" + type + "' );this.remove();");

			            	if(image){
				            	// the picture element
				            	let filterPic = document.createElement("img");
				            	filterPic.classList.add("mini-pic");
				            	filterPic.setAttribute('src', "${pageContext.request.contextPath}"+image);
				            	filterPic.setAttribute('alt', "'"+ type +"'");
				            	filter.append(filterPic);
			            	}
			            	
			            	//the text element
			            	let filterText = document.createElement("p");
			            	filterText.classList.add("mini-text");
			            	filterText.textContent=name;
			            	filter.append(filterText);
			            	
			            	//connecting element
			            	filterIds.get(type).push(id);
			            	let panelContainer = document.getElementById(panelContainerId);
			            	panelContainer.append(filter);
				        	removeFilterPanelSearchResults(panelSearchContainerId);
							document.getElementById(searchId).value="";
						}
						
						function removeFilter(id, type){
							let index = filterIds.get(type).indexOf(id);
							if (index !== -1){
								filterIds.get(type).splice(index, 1);
							}
						}
						
						function addSliderListener(sliderId, toleranceDisplayId, name){
							let slider = document.getElementById(sliderId);
							let toleranceDisplay = document.getElementById(toleranceDisplayId);
							slider.oninput = function() {
								toleranceDisplay.textContent="Allow up to "+ this.value + " missing " + name.toLowerCase() + "s";
								tolerances.set(name, this.value);
							}
						}
						
						function addMinMaxListener(selfId, otherId, isMin, minMin, maxMax, key){//expects that self and other both share the same range
							let self = document.getElementById(selfId);
							let other = document.getElementById(otherId);
							if(maxMax===null){
								maxMax = Math.pow(2, 31)-1; //2147483647, max int for java
								if(self.max!=='' && self.max!==undefined && self.hasAttribute('max')){
									maxMax = Math.min(maxMax, self.max);
								}
								else{
									self.max=maxMax;
									other.max=maxMax;
								}
							}
							if(minMin===null){
								minMin = -Math.pow(2, 31); //-2147483648, min int for java
								if(self.min!=='' && self.min!==undefined && self.hasAttribute('min')){
									minMin = Math.max(minMin, self.min);
								}
								else{
									self.min=minMin;
									other.min=minMin;
								}
							}
							self.addEventListener('input', function (e) {//tracking input
							    enforceMinMax(self, other, isMin, minMin, maxMax, key);
							});
							other.addEventListener('input', function (e) {//tracking input
							    enforceMinMax(other, self, !isMin, minMin, maxMax, key);
							});
							enforceMinMax(self, other, isMin, minMin, maxMax, key); // to finish initialization
						}
						
						function enforceMinMax(self, other, isMin, minMin, maxMax, key){
							let minElement = self;
							let maxElement = other;
							if (!isMin){
								minElement = other;
								maxElement = self;
							}
							let min = minElement.value;
							let max = maxElement.value;
							min = safeMinMax(minElement, min, minMin, maxMax);
							max = safeMinMax(maxElement, max, minMin, maxMax);
							if(!(min==="")&&!(max==="")){
								minElement.value = Math.min(min, max);
								maxElement.value = Math.max(min, max);
							}
							
							if(minElement.value===""){ //to make parsing in java easier
								ranges.get(key).min = minElement.min;
							}
							else{
								ranges.get(key).min = minElement.value;
							}
							if(maxElement.value===""){ //to make parsing in java easier
								ranges.get(key).max = maxElement.max;
							}
							else{
								ranges.get(key).max = maxElement.value;
							}
						}
						
						function safeMinMax(element, num, minMin, maxMax){
							if(num===""){ // if num is empty or has non numeric characters
								num=""; //set it empty
							}
							else{
								num = Math.min(maxMax, Math.max(minMin, num)); //bound num
							}
							element.value = num;
							return num;
						}
						
						function addCheckListener(selfId, key){
							let self = document.getElementById(selfId);
							self.addEventListener('input', function (e) {//tracking input
								options.set(key, !options.get(key));
							});
						}
						
					    recipeSearchTimeout = null;
					    
						async function searchRecipes(searchBarId){
						    clearTimeout(recipeSearchTimeout);
						    recipeSearchTimeout = setTimeout(async function () { // to prevent the user from overloading the server with too many requests
								searchBar = document.getElementById(searchBarId);
					    		searchTerm = searchBar.value.trim();
						        let parameters = new URLSearchParams();
						        parameters.append('searchTerm', searchTerm);
						        parameters.append('sortOrder', JSON.stringify(sortOrder));
						        parameters.append('sortOptions', JSON.stringify(Object.fromEntries(sortOptions)));
						        parameters.append('filterIds', JSON.stringify(Object.fromEntries(filterIds)));
						        parameters.append('tolerances', JSON.stringify(Object.fromEntries(tolerances)));
						        parameters.append('ranges', JSON.stringify(Object.fromEntries(ranges)));
						        parameters.append('options', JSON.stringify(Object.fromEntries(options)));
						        console.log(parameters);
								let response = await fetch("/RecipeGenie/Recipes/Search?" + parameters.toString(), {
						        	method:'GET',
						        	headers:{
						                "Content-Type": "application/json",
						                "Accept": "application/json"
						        	},
						        });
								if (!response.ok) {
						        	throw new Error('Network response was not ok');
						        }
					        	let objects = await response.json(); // Creates a JSON object
					        	console.log(objects);
					        	addResults(objects);
						    }, 100);
						}
											
						function addResults(objects){
				        	document.getElementById("personalizedStrips").classList.remove("active");
				        	let resultContainer = document.getElementById("recipeResults")
				        	resultContainer.classList.add("active");
				        	let scrollRow = document.getElementById("recipeResultsScrollRow")
							let previousRecipes = document.querySelectorAll(".search-recipe-card");

							//removing past recipes
					    	for (let previousRecipe of previousRecipes) {
					    		previousRecipe.remove();
					    	}
				        	for (let recipeString of Object.values(objects)){
				            	let recipe = JSON.parse(recipeString);
				            	let recipeId = recipe["recipeId"]; // not used until a later functionality (where clicking on the recipe leads to the recipe page)
				            	let recipeName = recipe["recipeName"];
				            	let recipeImagePath = recipe["recipeImagePath"];
				            	let recipeDescription = recipe["recipeDescription"];
				            	let recipeTotalMinutes = recipe["recipeTotalMinutes"];
				            	let recipeActiveTimestamp = recipe["recipeActiveTimestamp"];
				            	let reviewCount = recipe["reviewCount"];
				            	let averageRating = recipe["averageRating"];
				            	let bookmarkCount = recipe["bookmarkCount"];
				            	let userId = recipe["userId"]; // not used until a later functionality (where clicking on the user leads to the user page)
				            	let userName = recipe["userName"];
				            	let userImagePath = recipe["userImagePath"];
				            	
				            	let recipeCard = document.createElement("article");
				            	recipeCard.classList.add("search-recipe-card");
				            	
				            	let recipeImageContainer = document.createElement("div");
				            	recipeImageContainer.classList.add("recipe-card-img");
				            	let recipeImage = document.createElement("img");
				            	recipeImage.setAttribute('src', "${pageContext.request.contextPath}"+recipeImagePath);
				            	recipeImage.setAttribute('alt', "${pageContext.request.contextPath}"+"/images/recipes/recipe-placeholder.jpg");
				            	recipeImageContainer.append(recipeImage);
				            	
				            	let recipeBadges = document.createElement("div");
				            	recipeBadges.classList.add("recipe-card-badges");
								let cookingTimeBadgePill = document.createElement("span");
								if(recipeTotalMinutes>0){
									cookingTimeBadgePill.classList.add("badge-pill");
									cookingTimeBadgePill.textContent=recipeTotalMinutes + " m";
									recipeBadges.append(cookingTimeBadgePill);
								}
								let publicationDateBadgePill = document.createElement("span");
								publicationDateBadgePill.classList.add("badge-pill");
								publicationDateBadgePill.textContent=recipeActiveTimestamp;
								recipeBadges.append(cookingTimeBadgePill);
								recipeBadges.append(publicationDateBadgePill);
								recipeImageContainer.append(recipeBadges);
				            	recipeCard.append(recipeImageContainer);
								
								let cardBody = document.createElement("div");
								cardBody.classList.add("recipe-card-body");
								let cardTitle = document.createElement("h3");
								cardTitle.classList.add("recipe-card-title");
								cardTitle.textContent=recipeName;
								let cardDescription = document.createElement("p");
								cardDescription.classList.add("recipe-card-desc");
								cardDescription.textContent=recipeDescription;
								cardBody.append(cardTitle);
								cardBody.append(cardDescription);
								recipeCard.append(cardBody);
								
								let cardFooter = document.createElement("div");
								cardFooter.classList.add("recipe-card-footer");
								cardFooter.setAttribute("style", "padding:1rem;");
								let cardAuthor = document.createElement("div");
								cardAuthor.classList.add("card-author");
								let authorImage = document.createElement("img");
								authorImage.classList.add("mini-pic");
								authorImage.setAttribute('src', "${pageContext.request.contextPath}"+userImagePath);
								authorImage.setAttribute('alt', "${pageContext.request.contextPath}"+"/images/users/user-placeholder.png");
								let authorName = document.createElement("span");
								authorName.textContent=userName;
								cardAuthor.append(authorImage);
								cardAuthor.append(authorName);
								cardFooter.append(cardAuthor);
								
								let reviewMeta = document.createElement("div");
								reviewMeta.classList.add("review-meta");
								let ratings = document.createElement("span");
								ratings.classList.add("card-rating");
								ratings.textContent="Reviews: "+averageRating+"/5";
								stars = document.createElement("span");
								stars.classList.add("stars");
								stars.setAttribute("style", "--rating: " + averageRating);
								stars.setAttribute("aria-label", averageRating + " out of 5 stars");
								let reviews = document.createElement("span");
								reviews.classList.add("card-rating");
								reviews.textContent=" (" + reviewCount +")";
								ratings.append(stars);
								ratings.append(reviews);
								reviewMeta.append(ratings);
								cardFooter.append(reviewMeta);
								
								let cardStats = document.createElement("div");
								cardStats.classList.add("card-stats");
								let cardBookmark = document.createElement("span");
								cardBookmark.classList.add("card-bookmark");
								cardBookmark.textContent = bookmarkCount;
								let cardBookmarkImage = document.createElement("img");
								cardBookmarkImage.setAttribute('src', "${pageContext.request.contextPath}"+"/images/bookmark_icon.svg");
								cardBookmarkImage.setAttribute('alt', "bookmark");
								cardBookmarkImage.setAttribute('width', "16");
								cardBookmarkImage.setAttribute('height', "16");
								cardBookmarkImage.setAttribute('style', "vertical-align: middle; stroke: currentColor;");
								cardBookmark.append(cardBookmarkImage);
								cardStats.append(cardBookmark);
								cardFooter.append(cardStats);
								recipeCard.append(cardFooter);
								scrollRow.append(recipeCard);
				        	}
						}
						</script>
                    </form>
                </div>
            </div>
        </section>
        <div id="recipeResults" class="sections-container">
        	<section>
        		<div>
                    <h2 class="section-title">Recipes Search Results</h2>
                    <p class="section-subtitle">Recipes that you can make</p>
                </div>
			<div id="recipeResultsScrollRow" class="horizontal-scroll-row">
			</div>
        	</section>
		</div>
        <!-- Personalized Strips Section (Your Feed, Reviews for You, Your Most Popular) -->
        <div id="personalizedStrips" class="sections-container">

            <!-- Your Feed -->
            <section>
                <div>
                    <h2 class="section-title">Your Feed</h2>
                    <p class="section-subtitle">Latest recipes from your favorite creators</p>
                </div>
                <div class="horizontal-scroll-row">
					 <%
					 if (dbConnection != null) {
					    	String sql = 
				    				"SELECT RECIPE_NAME, TOTAL_MINUTES, RECIPE_ACTIVE_TIMESTAMP, "+
				    				"RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION " +
				    				"FROM RECIPES WHERE RECIPE_STATUS = 'Active' " +
				    				"ORDER BY RECIPE_ACTIVE_TIMESTAMP DESC";
					    try (	PreparedStatement statement = dbConnection.prepareStatement(sql);
						    	ResultSet recipes = statement.executeQuery();){
					    	
					    	while(recipes.next()){
					    		String imagePath = recipes.getString("RECIPE_IMAGE_PATH");
					    		if(imagePath == null || imagePath.isEmpty()){
					    			imagePath = "/images/recipe-placeholder.jpg";	
					    		}
					    		%>

					    		<article class="recipe-card">
					    		    <div class="recipe-card-img">
					    		        <img src="${pageContext.request.contextPath}<%=imagePath%>" alt="Recipe">

					    		        <div class="recipe-card-badges">
					    		            <span class="badge-pill">
					    		                <%= recipes.getObject("TOTAL_MINUTES") == null ? "N/A" : recipes.getInt("TOTAL_MINUTES") + "m" %>
					    		            </span>

					    		            <span class="badge-pill">
					    		                <%= recipes.getTimestamp("RECIPE_ACTIVE_TIMESTAMP") %>
					    		            </span>
					    		        </div>
					    		    </div>

					    		    <div class="recipe-card-body">
					    		        <h3 class="recipe-card-title">
					    		            <%= recipes.getString("RECIPE_NAME") %>
					    		        </h3>

					    		        <p class="recipe-card-desc">
					    		            <%= recipes.getString("RECIPE_DESCRIPTION") %>
					    		        </p>

					    		        <div class="recipe-card-footer">
					    		            <span>Database recipe</span>
					    		        </div>
					    		    </div>
					    		</article>

					    		<%
					    		        }
					    }catch (Exception error){
					    	out.println("<p>Database error: " +error.getMessage() + "</p>");
					    }
					 }%>
                </div>
            </section>

            <!-- Reviews for You -->
            <section>
                <div>
                    <h2 class="section-title">Reviews for You</h2>
                    <p class="section-subtitle">What people are saying about your recipes</p>
                </div>
                <div class="horizontal-scroll-row">
                    <%
                    if (dbConnection != null) {
                        String reviewSql =
                            "SELECT r.RECIPE_NAME, r.RECIPE_IMAGE_PATH, " +
                            "v.REVIEW_DESCRIPTION, v.RATING, " +
                            "v.REVIEW_ACTIVE_TIMESTAMP, u.USER_NAME, u.USER_IMAGE_PATH " +
                            "FROM REVIEWS v " +
                            "JOIN RECIPES r ON v.RECIPE_ID = r.RECIPE_ID " +
                            "LEFT JOIN USERS u ON v.REVIEWER_ID = u.USER_ID " +
                            "WHERE v.REVIEW_STATUS = 'Active' " +
                            "ORDER BY v.REVIEW_ACTIVE_TIMESTAMP DESC";

                        try (
                            PreparedStatement reviewStatement =
                                dbConnection.prepareStatement(reviewSql);
                            ResultSet reviews = reviewStatement.executeQuery()
                        ) {
                            while (reviews.next()) {
                                String reviewImage =
                                    reviews.getString("RECIPE_IMAGE_PATH");
                                if (reviewImage == null || reviewImage.isEmpty()) {
                                    reviewImage = "/images/recipe-placeholder.jpg";
                                }

                                String reviewer = reviews.getString("USER_NAME");
                                if (reviewer == null) {
                                    reviewer = "Anonymous";
                                }
                                String reviewerAvatar = reviews.getString("USER_IMAGE_PATH");
                                if (reviewerAvatar == null || reviewerAvatar.isEmpty()){
                                	reviewerAvatar = "/images/users/user-placeholder.png";
                                }
                    %>
                    <article class="review-card">
                        <div class="review-thumb">
                            <img src="${pageContext.request.contextPath}<%=reviewImage%>" alt="Review" />
                        </div>
                        <div class="review-body">
                            <p class="review-title">
                                <%= reviews.getString("RECIPE_NAME") %>
                            </p>
                            <p class="review-text">
                                <%= reviews.getString("REVIEW_DESCRIPTION") %>
                            </p>
                            <div class="review-footer">
                                <div class="card-author">
                                    <img class="mini-pic" src="${pageContext.request.contextPath}<%=reviewerAvatar%>" alt="Author"/>
                                    <span><%= reviewer %></span>
                                </div>
                                <div class="review-meta">
                                    <span class="review-date">
                                        <%= reviews.getTimestamp("REVIEW_ACTIVE_TIMESTAMP") %>
                                    </span>
                                    <span class="card-rating">
                                        <span class="stars"
                                            style="--rating: <%= reviews.getInt("RATING") %>;"
                                            aria-label="<%= reviews.getInt("RATING") %> out of 5 stars"></span>
                                        <%= reviews.getInt("RATING") %>/5
                                    </span>
                                </div>
                            </div>
                        </div>
                    </article>
                    <%
                            }
                        }
                        catch (Exception error){
					    	out.println("<p>Database error: " +error.getMessage() + "</p>");
					    }
                    }
                    %>
                </div>
            </section>

            <!-- Your Most Popular -->
            <section>
                <div>
                    <h2 class="section-title">Your Most Popular</h2>
                    <p class="section-subtitle">Which of your recipes are the most popular</p>
                </div>
                <div class="horizontal-scroll-row">
                    <%
                    if (dbConnection != null) {
                        String popularSql =
                            "SELECT r.RECIPE_NAME, r.RECIPE_IMAGE_PATH, " +
                            "r.RECIPE_DESCRIPTION, r.TOTAL_MINUTES, " +
                            "COUNT(b.USER_ID) AS BOOKMARK_COUNT " +
                            "FROM RECIPES r " +
                            "LEFT JOIN BOOKMARK b ON r.RECIPE_ID = b.RECIPE_ID " +
                            "WHERE r.RECIPE_STATUS = 'Active' " +
                            "GROUP BY r.RECIPE_ID, r.RECIPE_NAME, " +
                            "r.RECIPE_IMAGE_PATH, r.RECIPE_DESCRIPTION, " +
                            "r.TOTAL_MINUTES " +
                            "ORDER BY BOOKMARK_COUNT DESC LIMIT 5";

                        try (
                            PreparedStatement popularStatement =
                                dbConnection.prepareStatement(popularSql);
                            ResultSet popularRecipes =
                                popularStatement.executeQuery()
                        ) {
                            int rank = 1;
                            while (popularRecipes.next()) {
                                String popularImage =
                                	popularRecipes.getString("RECIPE_IMAGE_PATH");
                                if (popularImage == null || popularImage.isEmpty()) {
                                    popularImage = "/images/recipes/recipe-placeholder.jpg";
                                }
                    %>
                    <article class="recipe-card">
                        <div class="recipe-card-img">
                            <img src="${pageContext.request.contextPath}<%=popularImage%>" alt="Recipe" />
                            <div class="rank-badge">#<%= rank %></div>
                            <div class="recipe-card-badges">
                                <div class="card-badge-group">
                                    <span class="badge-pill">
                                        <%= popularRecipes.getObject("TOTAL_MINUTES") == null ? "N/A" : popularRecipes.getInt("TOTAL_MINUTES") + "m" %>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="recipe-card-body">
                            <h3 class="recipe-card-title">
                                <%= popularRecipes.getString("RECIPE_NAME") %>
                            </h3>
                            <p class="recipe-card-desc">
                                <%= popularRecipes.getString("RECIPE_DESCRIPTION") %>
                            </p>
                            <div class="recipe-card-footer">
                                <div class="card-stats">
                                    <span class="card-bookmark">
									    <img src="images/bookmark_icon.svg" alt="Bookmark icon" width="16" height="16" style="vertical-align: middle; stroke: currentColor;" />
									    <%= popularRecipes.getInt("BOOKMARK_COUNT") %>
									</span>
                                </div>
                            </div>
                        </div>
                    </article>
                    <%
                                rank++;
                            }
                        }
                        catch (Exception error){
					    	out.println("<p>Database error: " +error.getMessage() + "</p>");
					    }
                    }
                    %>
                </div>
            </section>

        </div>
        <script>
        	document.getElementById("personalizedStrips").classList.add("active");
        </script>
    </main>

    <!-- Footer -->
    <footer>
        <div class="footer-inner">
            <div class="footer-brand">
                <div class="plate-logo" title="Recipe Genie">
                    <div class="plate-rim">
                        <div class="plate-text-container">
                            <span class="logo-recipe">Recipe</span>
                            <span class="logo-genie">Genie</span>
                        </div>
                    </div>
                </div>
            </div>
            <p style="margin: 0;">© 2026 Recipe Genie</p>
            <div class="footer-links">
                <a href="#">Terms of Service</a>
            </div>
        </div>
    </footer>


</body>
</html>
