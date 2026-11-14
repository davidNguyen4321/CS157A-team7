<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="database.DbAccess" %>

<%@ page import="util.WebUtils" %>

<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>

<%@ include file="/WEB-INF/fragments/auth_check.jspf" %>
<%@ include file="/WEB-INF/fragments/recipe_card_maker.jspf" %>

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
        // Tracks which tab is currently selected
        String currentTab = request.getParameter("tab");
        if (currentTab == null || currentTab.trim().isEmpty()) {
            currentTab = "discover";
        }
    %>
    <%-- reuses connection that DbInit opened at startup --%>
    <%
    	Connection dbConnection = DbAccess.getConnection();
        int sessionUserId = (Integer) session.getAttribute("userId");
    %>

    <!--
      =========================================================================
      NAVIGATION HEADER
      =========================================================================
    -->
    <%@ include file="/WEB-INF/fragments/header.jspf" %>
    <!--
      =========================================================================
      DISCOVER TAB MAIN SECTION
      =========================================================================
    -->
    <main>
    	<%
    	if ("discover".equals(currentTab)) {
    	%>
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
	                           <input id="RecipesSearch" type="text" name="search" placeholder="Search recipe..." value="<%=request.getParameter("search") != null ? request.getParameter("search") : ""%>">
	
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
							<div class="filter-panel-container-no-grid sort-row" style="align-items: center;">
								<h1 class="filter-panel-name">
								Sort By
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
		                    	<button id="RatingsSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'RatingsSortText', 'Ratings');">
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
			                           <input id="DishTypesSearch" type="text" name="search" placeholder="Search dish types..." value="<%=request.getParameter("search") != null ? request.getParameter("search") : ""%>">
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
			                           <input id="CuisinesSearch" type="text" name="search" placeholder="Search cuisines..." value="<%=request.getParameter("search") != null ? request.getParameter("search") : ""%>">
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
			                    	<label for="AiGeneratedDescription" class="mini-text" style="color:#826300; font-size: 0.8rem;">AI Gen Description</label>
	   								<input type="checkbox" name="AiGeneratedDescription" id="AiGeneratedDescription" checked/><br>
	   								<label for="AiGeneratedAppliances" class="mini-text" style="color:#826300; font-size: 0.8rem;">AI Gen Appliances</label>
			                    	<input type="checkbox" name="AiGeneratedAppliances" id="AiGeneratedAppliances" checked/><br>
			                    	<label for="AiGeneratedCuisines" class="mini-text" style="color:#826300; font-size: 0.8rem;">AI Gen Cuisine Tags</label>
			                    	<input type="checkbox" name="AiGeneratedCuisines" id="AiGeneratedCuisines" checked/><br>
			                    	<label for="AiGeneratedDishTypes" class="mini-text" style="color:#826300; font-size: 0.8rem;">AI Gen Dish Type Tags</label>
			                    	<input type="checkbox" name="AiGeneratedDishTypes" id="AiGeneratedDishTypes" checked/><br>
			                    	<label for="AiGeneratedCookingMinutes" class="mini-text" style="color:#826300; font-size: 0.8rem;">AI Gen Cooking Minutes</label>
			                    	<input type="checkbox" name="AiGeneratedCookingMinutes" id="AiGeneratedCookingMinutes" checked/><br>
			                    	<label for="AiGeneratedCourses" class="mini-text" style="color:#826300; font-size: 0.8rem;">Ai Generated Courses</label>
			                    	<input type="checkbox" name="AiGeneratedCourses" id="AiGeneratedCourses" checked/>
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
				     	                        PreparedStatement statement = dbConnection.prepareStatement(sql);
				                                ResultSet dietaryRestrictions = statement.executeQuery()
				                            ) {
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
			                    			String sql = "SELECT DISTINCT COURSE, COURSE + 0 AS COURSE_ID " +
			                    			     		 "FROM RECIPES " +
			                    			     		 "WHERE COURSE IS NOT NULL " +
			                    			     		 "ORDER BY COURSE + 0 ASC";
			                    			try (
			                    				PreparedStatement courseStatement = dbConnection.prepareStatement(sql);
			                    				ResultSet courseResults = courseStatement.executeQuery()
			                    			) {
			                    				while(courseResults.next()){
			                    		            int courseId = courseResults.getInt("COURSE_ID");
			                    		            String courseName = courseResults.getString("COURSE");
						                    		%>
													    <button type="button" class="mini-container-toggleable" style="cursor:pointer; justify-content: center; align-items: center;" onclick="toggleBasicFilter(this, 'Course', <%=courseId%>)">
													        <h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
													            <%=courseName%>
													        </h2>
													    </button>
													<%
		                    					}
		                    				}
				     					   	catch (Exception error) {
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
			                    			<h2 class="mini-text" style="color:#523F00; font-size: 0.9rem;">
											Reviews
		                    			</h2>
				                    	<div class="filter-panel-mini-flex">
				                    		<input type="number" id="ReviewsMinRange" name="ReviewsMinRange" class="filter-range" step="1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="ReviewsMaxRange" name="ReviewsMaxRange" class="filter-range" step="1"/>
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
				                    		<input type="number" id="BookmarksMinRange" name="BookmarksMinRange" class="filter-range" step="1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="BookmarksMaxRange" name="BookmarksMaxRange" class="filter-range" step="1"/>
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
				                    		<input type="number" id="RatingsMinRange" name="RatingsMinRange" class="filter-range" step="0.1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="RatingsMaxRange" name="RatingsMaxRange" class="filter-range" step="0.1"/>
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
				                    		<input type="number" id="CookingTimeMinRange" name="CookingTimeMinRange" class="filter-range" step="1"/>
			                    			<div class="filter-panel-mini-flex">
			                    				<h2 class="mini-text" style="color:#826300; font-size: 0.8rem;">
												min to
				                    			</h2>
				                    		</div>
				                    		<input type="number" id="CookingTimeMaxRange" name="CookingTimeMaxRange" class="filter-range" step="1"/>
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
	                          <input id="IngredientsSearch" type="text" name="search" placeholder="Search ingredients..." value="<%=request.getParameter("search") != null ? request.getParameter("search") : ""%>">
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
							Recipe may be missing up to 0 of my selected ingredients
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
	                           <input id="AppliancesSearch" type="text" name="search" placeholder="Search appliances..." value="<%=request.getParameter("search") != null ? request.getParameter("search") : ""%>">
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
							Recipe may need up to 0 appliances I don't have
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
	                           <input id="AuthorsSearch" type="text" name="search" placeholder="Search authors..." value="<%=request.getParameter("search") != null ? request.getParameter("search") : ""%>">
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
						const sortOptions = new Map(); 
						for (let key of ["Publication Date", "Bookmarks", "Reviews", "Ratings"]){
							let elementBaseId = key.replace(" ", "");
							let object = {
								sort: null,
								order: null, // Initially unselected
								sortTextId: elementBaseId + "SortText"
							}
							sortOptions.set(key	, object);
						}
						
						const filterIds = new Map();
						for (let key of ["Ingredient", "Appliance", "Author", "Cuisine", "Dish Type", "Dietary Restriction", "Course"]){
							filterIds.set(key, []);
						}
						
						addDropdownListener('DishTypesSearch', 'Dish Type', 'DishTypesSearchContainer', 'DishTypesContainer', '${pageContext.request.contextPath}/Dropdown/DishTypes?');
						addDropdownListener('CuisinesSearch', 'Cuisine', 'CuisinesSearchContainer', 'CuisinesContainer', '${pageContext.request.contextPath}/Dropdown/Cuisines?');
						addDropdownListener('IngredientsSearch', 'Ingredient', 'IngredientsSearchContainer', 'IngredientsContainer', '${pageContext.request.contextPath}/Dropdown/Ingredients?');
						addDropdownListener('AppliancesSearch', 'Appliance', 'AppliancesSearchContainer', 'AppliancesContainer', '${pageContext.request.contextPath}/Dropdown/Appliances?');
						addDropdownListener('AuthorsSearch', 'Author', 'AuthorsSearchContainer', 'AuthorsContainer', '${pageContext.request.contextPath}/Dropdown/Users?');
						
						const tolerances = new Map();
						for (let key of ["Ingredient", "Appliance"]){
							tolerances.set(key, "0");
						}
						
						addSliderListener("IngredientsSlider", "IngredientsToleranceDisplay", "Ingredient",
							    n => "Recipe may be missing up to " + n + " of my selected ingredients");

						addSliderListener("AppliancesSlider", "AppliancesToleranceDisplay", "Appliance",
						    n => "Recipe may need up to " + n + (n == 1 ? " appliance" : " appliances") + " I don't have");
	
						const options = new Map();
						for (let key of ["Ai Generated Description", "Ai Generated Appliances", "Ai Generated Cuisines", "Ai Generated Dish Types", "Ai Generated Cooking Minutes", "Ai Generated Courses"]){
							options.set(key, true);
						}
						
						const ranges = new Map();

						function addRangeListener(minId, maxId, key){
							const minEl = document.getElementById(minId);
							const maxEl = document.getElementById(maxId);
							const range = { min: null, max: null };
							ranges.set(key, range);
							minEl.addEventListener('input', () => range.min = minEl.value === "" ? null : Number(minEl.value));
							maxEl.addEventListener('input', () => range.max = maxEl.value === "" ? null : Number(maxEl.value));
						}

						addRangeListener("BookmarksMinRange", "BookmarksMaxRange", "Bookmarks");
						addRangeListener("CookingTimeMinRange", "CookingTimeMaxRange", "Cooking Time");
						addRangeListener("RatingsMinRange", "RatingsMaxRange", "Ratings");
						addRangeListener("ReviewsMinRange", "ReviewsMaxRange", "Reviews");
						
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
						
						function sortSelect(self, textElement, type) {
							let option = sortOptions.get(type);
							if (self.classList.contains('descending')) { // State 1: descending --> ascending
								self.classList.remove('descending');
								self.classList.add('ascending');
								option.sort='ascending';
								if (option.order === null) assignNextOrder(type);
							}
							else if (self.classList.contains('ascending')) { // State 2: ascending --> not sorted
								self.classList.remove('ascending');
								option.sort = null;
								option.order = null;
								reindexOrders();
							}
							else { // State 3: not sorted --> descending
								self.classList.add('descending');
								option.sort = 'descending';
								if (option.order === null) {
									assignNextOrder(type);
								}
							}
							updateSortTextElements();
						}
						
						function assignNextOrder(type) {
						    let max = 0;
						    for (let opt of sortOptions.values()) {
						        if (opt.order !== null) max = Math.max(max, opt.order);
						    }
						    sortOptions.get(type).order = max + 1;
						}
						
						function getActiveSortEntries() {
						    return Array.from(sortOptions.entries())
						        .filter(([key, opt]) => opt.order !== null)
						        .sort((a, b) => a[1].order - b[1].order);
						}

						function reindexOrders() {
						    getActiveSortEntries().forEach(([key, opt], i) => opt.order = i + 1);
						}

						function activeSortOptions() {
						    return Object.fromEntries(getActiveSortEntries().map(([key, opt]) => [key, opt.sort]));
						}
						
						function updateSortTextElements() {
						    for (let [key, opt] of sortOptions.entries()) {
						        let element = document.getElementById(opt.sortTextId);
						        let active = opt.order !== null;
						        let arrow = opt.sort === 'ascending' ? '↑' : '↓';

						        element.innerHTML =
						            '<span class="priority-badge' + (active ? '' : ' sort-hidden') + '">' + (active ? opt.order : 1) + '</span>' +
						            '<span class="sort-label">' + key + '</span>' +
						            '<span class="direction-arrow' + (active ? '' : ' sort-hidden') + '">' + (active ? arrow : '↓') + '</span>';
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
						
						function addDropdownListener(searchId, type, panelSearchContainerId, panelContainerId, url){
							let input = document.getElementById(searchId);
							let timeout = null;
							input.addEventListener('keyup', function (e) {//tracking keyboard input
									clearTimeout(timeout);
								    timeout = setTimeout(function () { // to prevent the user from overloading the server with too many requests
								    	let searchTerm = input.value;
								    	if(searchTerm&&searchTerm.trim()){ //to check if the search term is of value
								    		searchFilters(searchId, searchTerm, type, panelSearchContainerId, panelContainerId, url);
								    	}
								    	else{
								        	removeFilterPanelSearchResults(panelSearchContainerId);
								    	}
								    }, 10);
							});
						}
						
						async function searchFilters(searchId, searchTerm, type, panelSearchContainerId, panelContainerId, url){
				    		searchTerm = searchTerm.trim();
					        let parameters = new URLSearchParams();
					        parameters.append('searchTerm', searchTerm);
					        parameters.append('excludeArray', JSON.stringify(filterIds.get(type)));
					        
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
				        	newFilterPanelSearchResults(searchId, objects, type, panelSearchContainerId, panelContainerId);
						}
						
						function removeFilterPanelSearchResults(panelSearchContainerId){ //clear results to make way for an updated version
			            	let panel = document.getElementById(panelSearchContainerId);
							let FPSRs = panel.querySelectorAll('.filter-panel-search-autocomplete');
							for (FPSR of FPSRs){
								FPSR.remove();
							}
						}
						
						function newFilterPanelSearchResults(searchId, objects, type, panelSearchContainerId, panelContainerId){
						    let panel = document.getElementById(panelSearchContainerId);
						    
						    // Define default placeholders dynamically based on the dropdown type
						    const placeholderMap = {
						        'Author': '/images/users/user-placeholder.png',
						    };

						    for (let object of objects){
						        let id = object["id"];
						        let name = object["name"];
						        
						        let newElement = document.createElement("button");
						        newElement.classList.add("filter-panel-search-autocomplete");
						        newElement.setAttribute('type','button');
						        
						        // Check if the "image" key exists in the backend response
						        if ('image' in object) {
						            let image = object["image"]; // Can be a string path or null
						            let effectiveImage = image || placeholderMap[type]; // Uses DB image, or falls back to map
						            
						            if (effectiveImage) {
						                let newPic = document.createElement("img");
						                newPic.classList.add("mini-pic");
						                newPic.setAttribute('src', "${pageContext.request.contextPath}" + effectiveImage);
						                newPic.setAttribute('alt', "'" + type + "'");
						                newElement.append(newPic);
						            }
						            
						            newElement.setAttribute('onclick', "addFromSearch('"+ searchId +"', this, " + id + ", '" + name + "', " + (effectiveImage ? "'" + effectiveImage + "'" : "null") + ", '" + type + "', '" + panelContainerId + "', '"+panelSearchContainerId+"');");
						        } else {
						            // The "image" key doesn't exist at all for this servlet
						            newElement.setAttribute('onclick', "addFromSearch('"+ searchId +"', this, " + id + ", '" + name + "', null, '" + type + "', '" + panelContainerId + "', '"+ panelSearchContainerId+"');");
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
						
						function addSliderListener(sliderId, toleranceDisplayId, name, makeText){
						    let slider = document.getElementById(sliderId);
						    let toleranceDisplay = document.getElementById(toleranceDisplayId);
						    slider.oninput = function() {
						        toleranceDisplay.textContent = makeText(this.value);
						        tolerances.set(name, this.value);
						    }
						}
				
					    recipeSearchTimeout = null;
					    
						async function searchRecipes(searchBarId){
						    clearTimeout(recipeSearchTimeout);
						    recipeSearchTimeout = setTimeout(async function () { // to prevent the user from overloading the server with too many requests
								searchBar = document.getElementById(searchBarId);
					    		searchTerm = searchBar.value.trim();
						        let parameters = new URLSearchParams();
						        parameters.append('searchTerm', searchTerm);
						        parameters.append('sortOptions', JSON.stringify(activeSortOptions()));
						        parameters.append('filterIds', JSON.stringify(Object.fromEntries(filterIds)));
						        parameters.append('tolerances', JSON.stringify(Object.fromEntries(tolerances)));
						        parameters.append('ranges', JSON.stringify(Object.fromEntries(ranges)));
						        parameters.append('options', JSON.stringify(Object.fromEntries(options)));
								let response = await fetch("${pageContext.request.contextPath}/Recipes/Search?" + parameters.toString(), {
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
					        	addResults(objects);
						    }, 10);
						}
											
						function addResults(objects){
				        	document.getElementById("personalizedStrips").classList.remove("active");
				        	let resultContainer = document.getElementById("recipeResults")
				        	resultContainer.classList.add("active");
							let previousRecipes = document.querySelectorAll(".search-recipe-card");
	
							//removing past recipes
					    	for (let previousRecipe of previousRecipes) {
					    		previousRecipe.remove();
					    	}
							for (let recipe of objects){
							    addRecipeCard("recipeResultsScrollRow", recipe["recipeId"], recipe["recipeName"], 
							            recipe["recipeImagePath"], recipe["recipeDescription"],
							            recipe["recipeTotalMinutes"], recipe["recipeActiveTimestamp"], 
							            recipe["reviewCount"], recipe["averageRating"], recipe["bookmarkCount"],
							            recipe["userBookmarked"], recipe["userId"],
							            recipe["userName"], recipe["userImagePath"], <%=sessionUserId%>, true);
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
	               <div id="followedScrollRow" class="horizontal-scroll-row"></div>
					 <%
					 if (dbConnection != null) {
					 		    	String sql =
					 		    		    "SELECT r.RECIPE_NAME, r.TOTAL_MINUTES, r.RECIPE_ACTIVE_TIMESTAMP, r.RECIPE_ID, " +
					 		    		    "r.RECIPE_IMAGE_PATH, r.RECIPE_DESCRIPTION, r.AUTHOR_ID, u.USER_IMAGE_PATH, " +
					 		    		    "u.USER_NAME, COUNT(DISTINCT b.USER_ID) AS BOOKMARK_COUNT, SUM(b.USER_ID = ?) AS HAS_BOOKMARKED, " + // really cheeky
					 						"recRevSubquery.REVIEW_COUNT, recRevSubquery.AVERAGE_RATING " +
					 		    		    "FROM RECIPES r INNER JOIN USERS u ON r.AUTHOR_ID = u.USER_ID " +
					 				    	"INNER JOIN FOLLOW f ON r.AUTHOR_ID = f.FOLLOWING_ID " +
					 						"AND f.FOLLOWER_ID = ? " +
					 				    	"INNER JOIN ( SELECT recRevSubquery.RECIPE_ID, " +
					 	                		"COUNT(rev.REVIEW_ID) AS REVIEW_COUNT, " +
					 	                		"AVG(rev.RATING*1.0) AS AVERAGE_RATING " + 
					 	                		"FROM RECIPES recRevSubquery " +
					 		           			"LEFT JOIN REVIEWS rev ON " +
					 		           			"recRevSubquery.RECIPE_ID = rev.RECIPE_ID " + 
					 		            		"AND rev.REVIEW_STATUS = 'Active' " +
					 	                   		"GROUP BY recRevSubquery.RECIPE_ID " + 
					 		           		") recRevSubquery ON r.RECIPE_ID = recRevSubquery.RECIPE_ID " + 
					 		             			"LEFT JOIN (SELECT b.USER_ID, b.RECIPE_ID FROM BOOKMARK b " +
					 		  							"INNER JOIN USERS u ON u.USER_ID = b.USER_ID " +
					 		  		  					"AND u.USER_STATUS = 'Active' " +
					             			  		") b ON r.RECIPE_ID = b.RECIPE_ID " +
					 		  		  		"WHERE r.RECIPE_STATUS = 'Active' GROUP BY r.RECIPE_ID, r.RECIPE_NAME, " +
					 		    		    "r.TOTAL_MINUTES, r.RECIPE_ACTIVE_TIMESTAMP, r.RECIPE_IMAGE_PATH, " +
					 		    		    "r.RECIPE_DESCRIPTION, r.AUTHOR_ID, u.USER_IMAGE_PATH, " +
					 				    	"recRevSubquery.REVIEW_COUNT, recRevSubquery.AVERAGE_RATING, " +
					 		    		    "u.USER_NAME ORDER BY r.RECIPE_ACTIVE_TIMESTAMP DESC ";
					 			    try (
					 			    	PreparedStatement statement = dbConnection.prepareStatement(sql);
					 		    		){
					 			    	statement.setInt(1, sessionUserId);
					 			    	statement.setInt(2, sessionUserId);
					 	    			try(
					 	    				ResultSet recipes = statement.executeQuery();		
					 					)	{
               	                            ArrayList<Map<String, Object>> followedRecipesList = new ArrayList<>();
	                   	                         while (recipes.next()) {
	                   	                        	 Map<String, Object> recipe = new HashMap<>();
	                   	                        	 recipe.put("id", recipes.getInt("RECIPE_ID"));
	                   	                        	 recipe.put("name", recipes.getString("RECIPE_NAME"));
	                   	                        	 recipe.put("image", recipes.getString("RECIPE_IMAGE_PATH"));
	                   	                        	 recipe.put("description", recipes.getString("RECIPE_DESCRIPTION"));
	                   	                        	 recipe.put("minutes", recipes.getObject("TOTAL_MINUTES"));
	                   	                        	 recipe.put("timestamp", recipes.getString("RECIPE_ACTIVE_TIMESTAMP"));
	                   	                        	 recipe.put("reviews", recipes.getInt("REVIEW_COUNT"));
	                   	                        	 recipe.put("rating", String.format("%.1f", recipes.getFloat("AVERAGE_RATING")));
	                   	                        	 recipe.put("bookmarks", recipes.getInt("BOOKMARK_COUNT"));
	                   	                        	 recipe.put("hasBookmarked", recipes.getInt("HAS_BOOKMARKED")>0);
	                   	                        	 recipe.put("authorId", recipes.getInt("AUTHOR_ID"));
	                   	                        	 recipe.put("authorName", recipes.getString("USER_NAME"));
	                   	                        	 recipe.put("authorImage", recipes.getString("USER_IMAGE_PATH"));
                  	                             
	                   	                        	followedRecipesList.add(recipe);
                  	                         }
                  	                         
                  	                         ObjectMapper mapper = new ObjectMapper();
                  	                         String followedRecipesJson = mapper.writeValueAsString(followedRecipesList);	
					    		%>
					    	<script>
							{
							    const followedRecipesData = <%= followedRecipesJson %>;
							    const sessionUserId = <%= sessionUserId %>;
							
							    followedRecipesData.forEach((recipe, index) => {
							        let recipeCard = addRecipeCard(
							            "followedScrollRow", 
							            recipe.id, 
							            recipe.name, 
							            recipe.image, 
							            recipe.description,
							            recipe.minutes, 
							            recipe.timestamp,
							            recipe.reviews, 
							            parseFloat(recipe.rating),
							            recipe.bookmarks, 
							            recipe.hasBookmarked, 
							            recipe.authorId,
							            recipe.authorName, 
							            recipe.authorImage,
							            sessionUserId,
							            true
							        );
							
							        let recipeImage = recipeCard.querySelector(".recipe-card-img");
							        let recipeCardBadges = recipeImage.querySelector(".recipe-card-badges");
							        let rankBadge = document.createElement("div");
							        rankBadge.classList.add("rank-badge");
							        rankBadge.textContent = "#" + (index + 1);
							        recipeImage.insertBefore(rankBadge, recipeCardBadges);
							    });
							}
							</script>
							<% 
							}}catch (Exception error){
					    				out.println("<p>Database error: " +error.getMessage() + "</p>");
					    	}}%>
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
	                   	                           "SELECT r.RECIPE_ID, r.RECIPE_NAME, r.RECIPE_IMAGE_PATH, " +
	                   	                           "v.REVIEW_DESCRIPTION, v.RATING, " +
	                   	                           "v.REVIEW_ACTIVE_TIMESTAMP, v.REVIEWER_ID, u.USER_NAME, " +
	                   	                           "u.USER_IMAGE_PATH FROM REVIEWS v " +
	                   	                           "JOIN RECIPES r ON v.RECIPE_ID = r.RECIPE_ID AND r.AUTHOR_ID = ? " +
	                   	                           "LEFT JOIN USERS u ON v.REVIEWER_ID = u.USER_ID " +
	                   	                           "WHERE v.REVIEW_STATUS = 'Active' " +
	                   	                           "ORDER BY v.REVIEW_ACTIVE_TIMESTAMP DESC";
	                   	
	                   	                       try (
	                   	                           PreparedStatement reviewStatement =
	                   	                               dbConnection.prepareStatement(reviewSql);
	                   	                       ) {
	                   	                       	reviewStatement.setInt(1, sessionUserId);
	                   	                       	try(
	                   	                       		ResultSet reviews = reviewStatement.executeQuery()
	                   						){
	                   	                       		while (reviews.next()) {
	                   	                       			String reviewImage =
	                   	                       					reviews.getString("RECIPE_IMAGE_PATH");
	                   	                       			if (reviewImage == null || reviewImage.isEmpty()) {
	                   	                       				reviewImage = "/images/recipe-placeholder.jpg";
	                   	                       			}
	                   	                       			String reviewerAvatar = reviews.getString("USER_IMAGE_PATH");
	                   	                       			if (reviewerAvatar == null || reviewerAvatar.isEmpty()){
	                   	                       				reviewerAvatar = "/images/users/user-placeholder.png";
	                   	                       			}
	                   %>
	                   <article class="review-card"
						         style="cursor: pointer;"
						         onclick="window.location.href='recipe.jsp?recipeId=<%=reviews.getInt("RECIPE_ID")%>'">
	                       <div class="review-thumb">
	                           <img src="${pageContext.request.contextPath}<%=reviewImage%>" alt="Review" />
	                       </div>
	                       <div class="review-body">
	                           <p class="review-title">
	                               <%=WebUtils.esc(reviews.getString("RECIPE_NAME"))%>
	                           </p>
	                           <p class="review-text">
	                               <%=reviews.getString("REVIEW_DESCRIPTION")%>
	                           </p>
	                           <div class="review-footer">
	                               <div class="card-author">
	                                   <img class="mini-pic" src="<%= request.getContextPath() + reviewerAvatar %>" alt="Reviewer"/>
	                                 	<%
	                                 	String an = reviews.getString("USER_NAME"); int aid = reviews.getInt("REVIEWER_ID");
	                                 	%>
									<%
									if (an != null) {
									%>
									    <a class="author-link" href="profile.jsp?id=<%=aid%>"><%=WebUtils.esc(an)%></a>
									<%
									} else {
									%><span>Anonymous</span><%
									}
									%>
	                               </div>
	                               <div class="review-meta">
	                                   <span class="review-date">
	                                       <%=reviews.getTimestamp("REVIEW_ACTIVE_TIMESTAMP")%>
	                                   </span>
	                                   <span class="card-rating">
	                                       <span class="stars"
	                                           style="--rating: <%=reviews.getInt("RATING")%>;"
	                                           aria-label="<%=reviews.getInt("RATING")%> out of 5 stars"></span>
	                                       <%=reviews.getInt("RATING")%>/5
	                                   </span>
	                               </div>
	                           </div>
	                       </div>
	                   </article>
	                   <%
	                   }
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
	               <div id="popularScrollRow" class="horizontal-scroll-row"></div>
	                   <%
	                   if (dbConnection != null) {
	                   	                       String popularSql =
	                   	                           "SELECT r.RECIPE_NAME, r.RECIPE_IMAGE_PATH, " +
	                   	                           "r.RECIPE_DESCRIPTION, r.TOTAL_MINUTES, " +
	                   	                           "COUNT(DISTINCT b.USER_ID) AS BOOKMARK_COUNT, SUM(b.USER_ID = ?) AS HAS_BOOKMARKED, " +
	                   	                           "recRevSubquery.AVERAGE_RATING, " +
	                   	                           "recRevSubquery.REVIEW_COUNT, "+
	                   	                           "r.RECIPE_ID, r.RECIPE_ACTIVE_TIMESTAMP FROM RECIPES r " +
	                   	             			   "LEFT JOIN (SELECT b.USER_ID, b.RECIPE_ID FROM BOOKMARK b " +
	                   		  						   "INNER JOIN USERS u ON u.USER_ID = b.USER_ID " +
	                   		  		  				   "AND u.USER_STATUS = 'Active' " +
	                               			  	   ") b ON r.RECIPE_ID = b.RECIPE_ID " +
	                   	                   		"INNER JOIN ( SELECT recRevSubquery.RECIPE_ID, " +
	                   	                		"COUNT(rev.REVIEW_ID) AS REVIEW_COUNT, " +
	                   	                		"AVG(rev.RATING*1.0) AS AVERAGE_RATING " + 
	                   	                		"FROM RECIPES recRevSubquery " +
	                   		           			"LEFT JOIN REVIEWS rev ON " +
	                   		           			"recRevSubquery.RECIPE_ID = rev.RECIPE_ID " + 
	                   		            		"AND rev.REVIEW_STATUS = 'Active' " +
	                   	                   		"GROUP BY recRevSubquery.RECIPE_ID " + 
	                   	                           ") recRevSubquery ON r.RECIPE_ID = recRevSubquery.RECIPE_ID " + 
	                   	                           "WHERE r.RECIPE_STATUS = 'Active' " +
	                   	                           "AND r.AUTHOR_ID = ? " +
	                   	                           "GROUP BY r.RECIPE_ID, r.RECIPE_NAME, r.RECIPE_ACTIVE_TIMESTAMP, " +
	                   	                           "r.RECIPE_IMAGE_PATH, r.RECIPE_DESCRIPTION, " +
	                   	                           "r.TOTAL_MINUTES, recRevSubquery.AVERAGE_RATING, " +
	                   	                           "recRevSubquery.REVIEW_COUNT " +
	                   	                           "ORDER BY BOOKMARK_COUNT DESC";
	                   	
	                   	                       try (
	                   	                           PreparedStatement popularStatement =
	                   	                               dbConnection.prepareStatement(popularSql);
	                   	                       ) {
	                   	                    	   popularStatement.setInt(1, sessionUserId);
	                   	                    	   popularStatement.setInt(2, sessionUserId);
	                   	                       	try(                           
	                   	                       		ResultSet popularRecipes =
	                   	                       				popularStatement.executeQuery()){
	                   	                            ArrayList<Map<String, Object>> popularRecipesList = new ArrayList<>();
	                   	                         while (popularRecipes.next()) {
	                   	                             Map<String, Object> recipe = new HashMap<>();
	                   	                             recipe.put("id", popularRecipes.getInt("RECIPE_ID"));
	                   	                             recipe.put("name", popularRecipes.getString("RECIPE_NAME"));
	                   	                             recipe.put("image", popularRecipes.getString("RECIPE_IMAGE_PATH"));
	                   	                             recipe.put("description", popularRecipes.getString("RECIPE_DESCRIPTION"));
	                   	                             recipe.put("minutes", popularRecipes.getObject("TOTAL_MINUTES"));
	                   	                             recipe.put("timestamp", popularRecipes.getString("RECIPE_ACTIVE_TIMESTAMP"));
	                   	                             recipe.put("reviews", popularRecipes.getInt("REVIEW_COUNT"));
	                   	                          	 recipe.put("hasBookmarked", popularRecipes.getInt("HAS_BOOKMARKED")>0);
	                   	                             recipe.put("rating", String.format("%.1f", popularRecipes.getFloat("AVERAGE_RATING")));
	                   	                             recipe.put("bookmarks", popularRecipes.getInt("BOOKMARK_COUNT"));
	                   	                             popularRecipesList.add(recipe);
	                   	                         }
	                   	                         
	                   	                         ObjectMapper mapper = new ObjectMapper();
	                   	                         String popularRecipesJson = mapper.writeValueAsString(popularRecipesList);	                   	                            	   
	                   %>
						<script>
						    const popularRecipesData = <%= popularRecipesJson %>;
						    const sessionUserId = <%= sessionUserId %>;
						    
						    popularRecipesData.forEach((recipe, index) => {
						        let recipeCard = addRecipeCard(
						            "popularScrollRow", 
						            recipe.id, 
						            recipe.name, 
						            recipe.image, 
						            recipe.description,
						            recipe.minutes, 
						            recipe.timestamp,
						            recipe.reviews, 
						            parseFloat(recipe.rating),
						            recipe.bookmarks, 
						            recipe.hasBookmarked, 
						            sessionUserId,
						            null, // null because never have to be read
						            null,
						            sessionUserId,
						            false
						        );
						
						        let recipeImage = recipeCard.querySelector(".recipe-card-img");
						        let recipeCardBadges = recipeImage.querySelector(".recipe-card-badges");
						        let rankBadge = document.createElement("div");
						        rankBadge.classList.add("rank-badge");
						        rankBadge.textContent = "#" + (index + 1);
						        recipeImage.insertBefore(rankBadge, recipeCardBadges);
						    });
						</script>
	                   <%
	                   	                        }
	                   	                       }
	                   	                       catch (Exception error){
	                   			    	out.println("<p>Database error: " +error.getMessage() + "</p>");
	                   			    }
	                   	                   }
	                   %>
	           </section>
	       </div>
	       <script>
	       		document.getElementById("personalizedStrips").classList.add("active");
	       </script>
            <%
            } else if ("share".equals(currentTab)) {
            %>
        	<p>Not Implemented</p>
            <%
            } else {
            %> <!-- Fallback: library (also the final option) -->
           	<div id="library" class="sections-container">
	        	<section>
		               <div>
		                   <h2 class="section-title">Your Library</h2>
		                   <p class="section-subtitle">Recipes Too Good to Forget</p>
		               </div>
		               <div id="libraryScrollRow" class="horizontal-scroll-row"></div>
		               	<%
		                   if (dbConnection != null) {
		                   		                       String bookmarkSql =
		                   		                           "SELECT r.RECIPE_NAME, r.RECIPE_IMAGE_PATH, " +
		                   		                           "r.RECIPE_DESCRIPTION, r.TOTAL_MINUTES, r.RECIPE_ACTIVE_TIMESTAMP, " +
		                   		                           "COUNT(DISTINCT b.USER_ID) AS BOOKMARK_COUNT, " +
				                   		                   "recRevSubquery.REVIEW_COUNT, " +
				                   				           "recRevSubquery.AVERAGE_RATING, " +
		                   		                           "r.RECIPE_ID, r.AUTHOR_ID, u.USER_NAME, " +
		                   		                           "u.USER_IMAGE_PATH FROM RECIPES r " +
		                   		             			   "LEFT JOIN (SELECT b.USER_ID, b.RECIPE_ID FROM BOOKMARK b " +
			                   	  						   "INNER JOIN USERS u ON u.USER_ID = b.USER_ID " +
			                   	  		  				   "AND u.USER_STATUS = 'Active' " +
		                   	            			  	   ") b ON r.RECIPE_ID = b.RECIPE_ID " +
			                   		                   		"LEFT JOIN USERS u ON r.AUTHOR_ID = u.USER_ID " +
			                   		                   		"INNER JOIN ( SELECT recRevSubquery.RECIPE_ID, " +
				                   		                		"COUNT(rev.REVIEW_ID) AS REVIEW_COUNT, " +
				                   		                		"AVG(rev.RATING*1.0) AS AVERAGE_RATING " + 
				                   		                		"FROM RECIPES recRevSubquery " +
				                   			           			"LEFT JOIN REVIEWS rev ON " +
				                   			           			"recRevSubquery.RECIPE_ID = rev.RECIPE_ID " + 
				                   			            		"AND rev.REVIEW_STATUS = 'Active' " +
				                   		                   		"GROUP BY recRevSubquery.RECIPE_ID " + 
		                   		                           ") recRevSubquery ON r.RECIPE_ID = recRevSubquery.RECIPE_ID " + 
		                   		                   			"WHERE r.RECIPE_STATUS = 'Active' " +
		                   		                           "GROUP BY r.RECIPE_ID, r.RECIPE_NAME, " +
		                   		                           "r.RECIPE_IMAGE_PATH, r.RECIPE_DESCRIPTION, " +
		                   		                           "r.TOTAL_MINUTES, u.USER_ID, u.USER_NAME, " + 
		                   		                           "u.USER_IMAGE_PATH, recRevSubquery.REVIEW_COUNT, " +
		                   		                           "recRevSubquery.AVERAGE_RATING " + 
		                   		                           "HAVING SUM(b.USER_ID = ?)>0 " +
		                   		                           "ORDER BY r.RECIPE_ACTIVE_TIMESTAMP DESC";
		                   		
		                   		                       try (
		                   		                           PreparedStatement bookmarkedStatement =
		                   		                               dbConnection.prepareStatement(bookmarkSql);
		                   		                       ) {
		                   		                    	   bookmarkedStatement.setInt(1, sessionUserId);
		                   		                       	try(                           
		                   		                       		ResultSet bookmarkedRecipes =
		                   		                       				bookmarkedStatement.executeQuery()){
			                   	                            ArrayList<Map<String, Object>> bookmarkedRecipesList = new ArrayList<>();
		      	                   	                         while (bookmarkedRecipes.next()) {
		      	                   	                        	 Map<String, Object> recipe = new HashMap<>();
		      	                   	                        	 recipe.put("id", bookmarkedRecipes.getInt("RECIPE_ID"));
		      	                   	                        	 recipe.put("name", bookmarkedRecipes.getString("RECIPE_NAME"));
		      	                   	                        	 recipe.put("image", bookmarkedRecipes.getString("RECIPE_IMAGE_PATH"));
		      	                   	                        	 recipe.put("description", bookmarkedRecipes.getString("RECIPE_DESCRIPTION"));
		      	                   	                        	 recipe.put("minutes", bookmarkedRecipes.getObject("TOTAL_MINUTES"));
		      	                   	                        	 recipe.put("timestamp", bookmarkedRecipes.getString("RECIPE_ACTIVE_TIMESTAMP"));
		      	                   	                        	 recipe.put("reviews", bookmarkedRecipes.getInt("REVIEW_COUNT"));
		      	                   	                        	 recipe.put("rating", String.format("%.1f", bookmarkedRecipes.getFloat("AVERAGE_RATING")));
		      	                   	                        	 recipe.put("bookmarks", bookmarkedRecipes.getInt("BOOKMARK_COUNT"));
		      	                   	                        	 recipe.put("authorId", bookmarkedRecipes.getInt("AUTHOR_ID"));
		      	                   	                        	 recipe.put("authorName", bookmarkedRecipes.getString("USER_NAME"));
		      	                   	                        	 recipe.put("authorImage", bookmarkedRecipes.getString("USER_IMAGE_PATH"));
		    	                   	                             
		      	                   	                        	 bookmarkedRecipesList.add(recipe);
		    	                   	                         }
		    	                   	                         
		    	                   	                         ObjectMapper mapper = new ObjectMapper();
		    	                   	                         String bookmarkedRecipesJson = mapper.writeValueAsString(bookmarkedRecipesList);	 
		                   %>
							<script>
							{
							    const bookmarkedRecipesData = <%= bookmarkedRecipesJson %>;
							    const sessionUserId = <%= sessionUserId %>;
							    
							    bookmarkedRecipesData.forEach((recipe, index) => {
							        let recipeCard = addRecipeCard(
							            "libraryScrollRow", 
							            recipe.id, 
							            recipe.name, 
							            recipe.image, 
							            recipe.description,
							            recipe.minutes, 
							            recipe.timestamp,
							            recipe.reviews, 
							            parseFloat(recipe.rating),
							            recipe.bookmarks, 
							            true, 
							            recipe.authorId,
							            recipe.authorName, 
							            recipe.authorImage,
							            sessionUserId,
							            true
							        );
							
							        let recipeImage = recipeCard.querySelector(".recipe-card-img");
							        let recipeCardBadges = recipeImage.querySelector(".recipe-card-badges");
							        let rankBadge = document.createElement("div");
							        rankBadge.classList.add("rank-badge");
							        rankBadge.textContent = "#" + (index + 1);
							        recipeImage.insertBefore(rankBadge, recipeCardBadges);
							    });
							}
							</script>
		                   <%
		                        }
		                       }
		                       catch (Exception error){
						    	out.println("<p>Database error: " +error.getMessage() + "</p>");
						    }
		                   }
		                   %>
		           </section>
		      </div>
		   <script>
	       		document.getElementById("library").classList.add("active");
	       </script>
        <% } %>
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
