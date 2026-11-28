<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="database.DbAccess" %>

<%@ page import="util.WebUtils" %>
<%@ page import="util.TimeFormatter" %>

<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDateTime" %>

<%@ include file="/WEB-INF/fragments/auth_check.jspf" %>
<%@ include file="/WEB-INF/fragments/recipe_card_maker.jspf" %>
<%@ include file="/WEB-INF/fragments/review_card_maker.jspf" %>


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recipe Genie</title>
    <link rel="stylesheet" type="text/css" href="css/index.css">
</head>
<body>

    <%
    String currentTab = request.getParameter("tab"); 
            if (currentTab == null || currentTab.trim().isEmpty()) {
                currentTab = "discover";
            }
            
            Connection dbConnection = DbAccess.getConnection();
            int sessionUserId = (Integer) session.getAttribute("userId");
            String sessionUserTimezone = (String) session.getAttribute("userTimezone");
    %>
    
    <%@ include file="/WEB-INF/fragments/header.jspf" %>
    <%@ include file="/WEB-INF/fragments/dropdown.jspf" %>
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
	               <div>
	                   <h1 class="hero-title">
	                       What can you cook <span style="white-space: nowrap;"><em>right now?</em></span>
	                   </h1>
	
	                   <!-- Smart search form -->
	                   <form action="index.jsp" method="GET" class="search-form" style="width: 100%;">
	                       <div class="search-top-bar">
	                           <span class="search-icon-fixed">🔍</span>
							   <input id="RecipesSearch" type="text" name="search" placeholder="Search recipe..." value="<%=request.getParameter("search") != null ? WebUtils.esc(request.getParameter("search")) : ""%>">	
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
							<div class="filter-panel-top">
								<h1 class="filter-panel-name">
								Sort By
		                    	</h1>
		                    	<div class="vrounded-divider" style="border-color:#1F1800; height:50px; opacity:0.6;"> </div>
		                    	<div>
			                    	<button id="PublicationDateSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'PublicationDateSortText', 'Publication Date');">
									    <p id="PublicationDateSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;"></p>
									</button>
			                    	<button id="BookmarksSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'BookmarksSortText', 'Bookmarks');">
										<p id="BookmarksSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;"></p>
									</button>
			                    	<button id="ReviewSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'ReviewsSortText', 'Reviews');">
			                    		<p id="ReviewsSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;"></p>
			                    	</button>
			                    	<button id="RatingsSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'RatingsSortText', 'Ratings');">
			                    		<p id="RatingsSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;"></p>
			                    	</button>
		                    	</div>
		                    	<h1 class="filter-panel-name">
								Courses
		                    	</h1>
								    <div class="vrounded-divider" style="border-color:#1F1800; height:50px; opacity:0.6;"> </div>
		                    		<div>
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
													    <button type="button" class="filter-panel-mini-container-2" onclick="toggleBasicFilter(this, 'Course', <%=courseId%>)">
													        <h2 class="mini-text" style="color:#1F1800; font-size: 0.75rem;  font-weight: bold;">
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
	                    	<div class="filter-panel-container-no-grid">
	                    		<div class="filter-panel-mini" style="width:1rem;">
				                    <h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Dish Types
			                    	</h2>
			                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
			                    	<div class="filter-search-wrap">
									    <div style="color: #F5F5DC" class="search-top-bar">
									        <span class="search-icon-fixed">🔍</span>
									        <input id="DishTypesSearch" type="text" name="search" placeholder="Search dish types..." value="<%=request.getParameter("search") != null ? WebUtils.esc(request.getParameter("search")) : ""%>">
									    </div>
									    <div id="DishTypesSearchContainer" class="filter-panel-search-container"></div>
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
			                    	<div class="filter-search-wrap">
									    <div style="color: #F5F5DC" class="search-top-bar">
									        <span class="search-icon-fixed">🔍</span>
									        <input id="CuisinesSearch" type="text" name="search" placeholder="Search cuisines..." value="<%=request.getParameter("search") != null ? WebUtils.esc(request.getParameter("search")) : ""%>">
									    </div>
									    <div id="CuisinesSearchContainer" class="filter-panel-search-container"></div>
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
			                    	<label for="AiGeneratedCourses" class="mini-text" style="color:#826300; font-size: 0.8rem;">AI Gen Courses</label>
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
	                      	<div class="filter-search-wrap">
							    <div style="color: #F5F5DC" class="search-top-bar">
							        <span class="search-icon-fixed">🔍</span>
							        <input id="IngredientsSearch" type="text" name="search" placeholder="Search ingredients..." value="<%=request.getParameter("search") != null ? WebUtils.esc(request.getParameter("search")) : ""%>">
							    </div>
							    <div id="IngredientsSearchContainer" class="filter-panel-search-container"></div>
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
	                       	<div class="filter-search-wrap">
							    <div style="color: #F5F5DC" class="search-top-bar">
							        <span class="search-icon-fixed">🔍</span>
							        <input id="AppliancesSearch" type="text" name="search" placeholder="Search appliances..." value="<%=request.getParameter("search") != null ? WebUtils.esc(request.getParameter("search")) : ""%>">
							    </div>
							    <div id="AppliancesSearchContainer" class="filter-panel-search-container"></div>
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
	                       	<div class="filter-search-wrap">
							    <div style="color: #F5F5DC" class="search-top-bar">
							        <span class="search-icon-fixed">🔍</span>
							        <input id="AuthorsSearch" type="text" name="search" placeholder="Search authors..." value="<%=request.getParameter("search") != null ? WebUtils.esc(request.getParameter("search")) : ""%>">
							    </div>
							    <div id="AuthorsSearchContainer" class="filter-panel-search-container"></div>
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
						
						updateSortTextElements();
						
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
						
						document.querySelectorAll('.filter-range').forEach(el => {
						  el.addEventListener('wheel', e => e.preventDefault(), { passive: false });
						});
						
						function addCheckListener(checkboxId, key){
						    const box = document.getElementById(checkboxId);
						    box.addEventListener('change', () => options.set(key, box.checked));
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
						
						function addSliderListener(sliderId, toleranceDisplayId, name, makeText) {
						    let slider = document.getElementById(sliderId);
						    let toleranceDisplay = document.getElementById(toleranceDisplayId);
						    
						    const updateProgress = () => {
						        const min = slider.min || 0;
						        const max = slider.max || 100;
						        const percentage = ((slider.value - min) / (max - min)) * 100;
						        
						        slider.style.background = `linear-gradient(to right, #826300 ${percentage}%, #F5F5DC ${percentage}%)`;
						    };

						    updateProgress();

						    slider.oninput = function() {
						        toleranceDisplay.textContent = makeText(this.value);
						        tolerances.set(name, this.value);
						        updateProgress(); // Update fill when dragged
						    }
						}
				
					    recipeSearchTimeout = null;
					    
					    async function searchRecipes(searchBarId){
						    clearTimeout(recipeSearchTimeout);
						    recipeSearchTimeout = setTimeout(async function () { // to prevent the user from overloading the server with too many requests
						        let searchTerm = document.getElementById(searchBarId).value.trim();
						        addResults(await fetchRecipes(searchTerm, activeSortOptions(),
						            Object.fromEntries(filterIds), Object.fromEntries(tolerances),
						            Object.fromEntries(ranges), Object.fromEntries(options)));
						    }, 10);
						}
											
						function addResults(objects){
				        	document.getElementById("personalizedStrips").classList.remove("active");
				        	let resultContainer = document.getElementById("recipeResults")
				        	resultContainer.classList.add("active");
							let previousRecipes = document.querySelectorAll(".search-recipe-card");
	
					    	for (let previousRecipe of previousRecipes) previousRecipe.remove();
					    	renderRecipeCards("recipeResultsScrollRow", objects, <%=sessionUserId%>, { showUser: true });
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
									    ArrayList<Integer> followedIds = new ArrayList<>();
									    String sql = "SELECT following_id FROM follow WHERE follower_id = ?";
									    try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
									        ps.setInt(1, sessionUserId);
									        try (ResultSet rs = ps.executeQuery()) {
									            while (rs.next()) followedIds.add(rs.getInt("following_id"));
									        }
									    } 
									    catch (Exception error) {
									        out.println("<p>Database error: " + error.getMessage() + "</p>");
									    }
									    if (!followedIds.isEmpty()) {
					%>
							<script>
							    loadRecipeStrip(
						    		"followedScrollRow", 
						    		<%=followedIds%>, 
						    		{ "Publication Date": "descending" },
						            <%=sessionUserId%>, 
						            { showUser: true, rank: false },
						            false
							    );
							</script>
					<%
					}
									}
					%>
	           </section>
	
	           <!-- Reviews for You -->
				<section>
	               <div>
	                   <h2 class="section-title">Reviews for You</h2>
	                   <p class="section-subtitle">What people are saying about your recipes</p>
	               </div>
	               <div id="reviewsScrollRow" class="horizontal-scroll-row">
	                   <%
	                   if (dbConnection != null) {
	                                              String reviewSql =
	                                                  "SELECT r.RECIPE_ID, r.RECIPE_NAME, r.RECIPE_IMAGE_PATH, " +
	                                                  "v.REVIEW_DESCRIPTION, v.RATING, v.REVIEW_IMAGE_PATH, " +
	                                                  "v.REVIEW_ACTIVE_TIMESTAMP, v.REVIEWER_ID, u.USER_NAME, " +
	                                                  "u.USER_IMAGE_PATH FROM REVIEWS v " +
	                                                  "JOIN RECIPES r ON v.RECIPE_ID = r.RECIPE_ID AND r.AUTHOR_ID = ? " +
	                                                  "LEFT JOIN USERS u ON v.REVIEWER_ID = u.USER_ID " +
	                                                  "WHERE v.REVIEW_STATUS = 'Active' " +
	                                                  "ORDER BY v.REVIEW_ACTIVE_TIMESTAMP DESC";
	                   	
	                                              try (PreparedStatement reviewStatement = dbConnection.prepareStatement(reviewSql)) {
	                                                  reviewStatement.setInt(1, sessionUserId);
	                                                  try (ResultSet reviews = reviewStatement.executeQuery()) {
	                                                      while (reviews.next()) {
	                                                           String rImage = reviews.getString("RECIPE_IMAGE_PATH");
	                                                           String vImage = reviews.getString("REVIEW_IMAGE_PATH");
	                                                           String uImage = reviews.getString("USER_IMAGE_PATH");
	                                                           String uName = reviews.getString("USER_NAME");
	                                                           String formattedTime = TimeFormatter.formatTimeAgo(reviews.getObject("REVIEW_ACTIVE_TIMESTAMP", LocalDateTime.class), sessionUserTimezone);
	                   %>
						<script>
						    addReviewCard(
						        "reviewsScrollRow",
						        <%=reviews.getInt("RECIPE_ID")%>,
						        "<%=WebUtils.escJS(reviews.getString("RECIPE_NAME"))%>",
						        <%=rImage != null ? "\"" + WebUtils.escJS(rImage) + "\"" : "null"%>,
						        "<%=WebUtils.escJS(reviews.getString("REVIEW_DESCRIPTION"))%>",
						        <%=reviews.getInt("RATING")%>,
						        <%=vImage != null ? "\"" + WebUtils.escJS(vImage) + "\"" : "null"%>,
								"<%=WebUtils.escJS(formattedTime)%>",
						        <%=reviews.getInt("REVIEWER_ID")%>,
						        <%=uName != null ? "\"" + WebUtils.escJS(uName) + "\"" : "null"%>,
						        <%=uImage != null ? "\"" + WebUtils.escJS(uImage) + "\"" : "null"%>,
						        true,
						        "${pageContext.request.contextPath}"
						    );
						</script>
	                   <%
                                   }
                               }
                           } catch (Exception error) {
                               out.println("<p>Database error: " + error.getMessage() + "</p>");
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
	               	<script>
	               	loadRecipeStrip(
	                	"popularScrollRow", 
	                	[<%= sessionUserId %>],
	                    { "Bookmarks": "descending", "Publication Date": "descending" },
	                    <%= sessionUserId %>, 
	                    { showUser: false, rank: true },
						false
	                );
	               	</script>
	           	</section>
	       </div>
	       <script>
	       		document.getElementById("personalizedStrips").classList.add("active");
	       </script>
            <%
            } else if ("share".equals(currentTab)) {
            %>
        	<div class="sections-container active">
			  <section>
			    <div>
			      <h2 class="section-title">Share a Recipe</h2>
			      <p class="section-subtitle">Fill things in yourself, or press ✨ and let Recipe Genie pick</p>
			    </div>
			
			    <form id="shareForm" class="share-form">
			      <label>Recipe name
			        <input id="shareName" type="text" maxlength="100" required>
			      </label>
			      <label>Instructions
			        <textarea id="shareInstructions" rows="8" maxlength="10000" required></textarea>
			      </label>
			
			      <div class="share-field">
			        <div class="share-field-head"><h3>Ingredients</h3>
			          <button type="button" class="genie-btn" data-genie="ingredients">✨ Let Recipe Genie pick</button></div>
			        <div class="filter-search-wrap">
			          <div class="search-top-bar"><span class="search-icon-fixed">🔍</span>
			            <input id="IngredientsSearch" type="text" placeholder="Search ingredients..."></div>
			          <div id="IngredientsSearchContainer" class="filter-panel-search-container"></div>
			        </div>
			        <div id="IngredientsContainer" class="filter-panel-container"></div>
			        <div id="IngredientAmounts"></div>
			      </div>
			
			      <div class="share-field">
			        <div class="share-field-head"><h3>Appliances</h3>
			          <button type="button" class="genie-btn" data-genie="appliances">✨ Let Recipe Genie pick</button></div>
			        <div class="filter-search-wrap">
			          <div class="search-top-bar"><span class="search-icon-fixed">🔍</span>
			            <input id="AppliancesSearch" type="text" placeholder="Search appliances..."></div>
			          <div id="AppliancesSearchContainer" class="filter-panel-search-container"></div>
			        </div>
			        <div id="AppliancesContainer" class="filter-panel-container"></div>
			      </div>
			
			      <div class="share-field">
			        <div class="share-field-head"><h3>Cuisines</h3>
			          <button type="button" class="genie-btn" data-genie="cuisines">✨ Let Recipe Genie pick</button></div>
			        <div class="filter-search-wrap">
			          <div class="search-top-bar"><span class="search-icon-fixed">🔍</span>
			            <input id="CuisinesSearch" type="text" placeholder="Search cuisines..."></div>
			          <div id="CuisinesSearchContainer" class="filter-panel-search-container"></div>
			        </div>
			        <div id="CuisinesContainer" class="filter-panel-container"></div>
			      </div>
			
			      <div class="share-field">
			        <div class="share-field-head"><h3>Dish types</h3>
			          <button type="button" class="genie-btn" data-genie="dishTypes">✨ Let Recipe Genie pick</button></div>
			        <div class="filter-search-wrap">
			          <div class="search-top-bar"><span class="search-icon-fixed">🔍</span>
			            <input id="DishTypesSearch" type="text" placeholder="Search dish types..."></div>
			          <div id="DishTypesSearchContainer" class="filter-panel-search-container"></div>
			        </div>
			        <div id="DishTypesContainer" class="filter-panel-container"></div>
			      </div>
			
			      <div class="share-field">
			        <div class="share-field-head"><h3>Description</h3>
			          <button type="button" class="genie-btn" data-genie="description">✨ Let Recipe Genie write</button></div>
			        <textarea id="shareDescription" rows="3" maxlength="2000"></textarea>
			      </div>
			
			      <div class="share-field">
			        <div class="share-field-head"><h3>Total minutes</h3>
			          <button type="button" class="genie-btn" data-genie="totalMinutes">✨ Let Recipe Genie estimate</button></div>
			        <input id="shareMinutes" type="number" min="1" max="10080">
			      </div>
			
			      <div class="share-field">
			        <div class="share-field-head"><h3>Course</h3>
			          <button type="button" class="genie-btn" data-genie="course">✨ Let Recipe Genie choose</button></div>
			        <select id="shareCourse">
			          <option value="">(none)</option>
			          <option>Main Course</option>
			          <option>Appetizer / Side</option>
			          <option>Dessert</option>
			        </select>
			      </div>
			
			      <button type="submit" class="recipe-button">Create recipe</button>
			      <p id="shareStatus" class="mini-text" role="status"></p>
			    </form>
			  </section>
			</div>
			
			<script>
			const shareCtx = '${pageContext.request.contextPath}';
			
			const filterIds = new Map(); // the dropdown functions read this global
			const SHARE = {
			    "Ingredient": { field: "ingredients", path: "Ingredients", search: "IngredientsSearch", results: "IngredientsSearchContainer", chosen: "IngredientsContainer" },
			    "Appliance":  { field: "appliances",  path: "Appliances",  search: "AppliancesSearch",  results: "AppliancesSearchContainer",  chosen: "AppliancesContainer" },
			    "Cuisine":    { field: "cuisines",    path: "Cuisines",    search: "CuisinesSearch",    results: "CuisinesSearchContainer",    chosen: "CuisinesContainer" },
			    "Dish Type":  { field: "dishTypes",   path: "DishTypes",   search: "DishTypesSearch",   results: "DishTypesSearchContainer",   chosen: "DishTypesContainer" }
			};
			for (const [type, c] of Object.entries(SHARE)) {
			    filterIds.set(type, []);
			    addDropdownListener(c.search, type, c.results, c.chosen, shareCtx + '/Dropdown/' + c.path + '?');
			}
			
			const el = id => document.getElementById(id);
			const val = id => el(id).value.trim();
			const typeOf = field => Object.keys(SHARE).find(t => SHARE[t].field === field);
			
			// Fields whose current value is exactly what Recipe Genie picked (drives the ai_gen_* flags)
			const aiPicked = new Set();
			let applyingGenie = false;
			const ingredientRows = new Map(); // id -> {amount, unit, row}
			
			// Hooks called by the shared dropdown code
			window.onFilterAdded = (type, id, name) => {
			    if (type === 'Ingredient') addIngredientRow(id, name);
			    if (!applyingGenie) aiPicked.delete(SHARE[type].field);
			};
			window.onFilterRemoved = (type, id) => {
			    if (type === 'Ingredient') {
			        const r = ingredientRows.get(id);
			        if (r) { r.row.remove(); ingredientRows.delete(id); }
			    }
			    if (!applyingGenie) aiPicked.delete(SHARE[type].field);
			};
			
			function addIngredientRow(id, name) {
			    const row = document.createElement('div');
			    row.className = 'picker-row';
			    const label = document.createElement('p');
			    label.className = 'mini-text';
			    label.textContent = name;
			    const amount = Object.assign(document.createElement('input'),
			        { type: 'number', min: '0.01', max: '999.99', step: '0.01', placeholder: 'Amount' });
			    const unit = Object.assign(document.createElement('input'),
			        { type: 'text', maxLength: 25, placeholder: 'Unit (cups, g...)' });
			    row.append(label, amount, unit);
			    el('IngredientAmounts').append(row);
			    ingredientRows.set(id, { amount, unit, row });
			}
			
			// Typing in these fields means they're no longer Genie's value
			for (const [field, id] of [['description', 'shareDescription'], ['totalMinutes', 'shareMinutes'], ['course', 'shareCourse']]) {
			    el(id).addEventListener('input', () => aiPicked.delete(field));
			}
			
			function setStatus(text) { el('shareStatus').textContent = text; }
			
			document.querySelectorAll('[data-genie]').forEach(btn =>
			    btn.addEventListener('click', () => askGenie(btn.dataset.genie, btn)));
			
			async function askGenie(field, btn) {
			    if (!val('shareInstructions')) {
			        setStatus('Write the instructions first so Recipe Genie has something to work from.');
			        return;
			    }
			    const label = btn.textContent;
			    btn.disabled = true;
			    btn.textContent = 'Thinking...';
			    setStatus('');
			    try {
			        const res = await fetch(shareCtx + '/Recipes/Suggest', {
			            method: 'POST',
			            headers: { 'Content-Type': 'application/json' },
			            body: JSON.stringify({
			                name: val('shareName'),
			                instructions: val('shareInstructions'),
			                ingredientIds: filterIds.get('Ingredient'),
			                fields: [field]
			            })
			        });
			        if (!res.ok) throw new Error('suggest failed');
			        const suggestion = await res.json();
			
			        applyingGenie = true;
			        let applied;
			        try { applied = applySuggestion(field, suggestion); } finally { applyingGenie = false; }
			
			        if (applied) aiPicked.add(field);
			        else setStatus("Recipe Genie couldn't find a good pick for that, so nothing was changed.");
			    } catch (err) {
			        setStatus("Recipe Genie couldn't help right now. Try again in a moment.");
			    } finally {
			        btn.disabled = false;
			        btn.textContent = label;
			    }
			}
			
			function applySuggestion(field, s) {
			    if (field === 'description')  { if (!s.description) return false;  el('shareDescription').value = s.description; return true; }
			    if (field === 'totalMinutes') { if (!s.totalMinutes) return false;  el('shareMinutes').value = s.totalMinutes; return true; }
			    if (field === 'course')       { if (!s.course) return false;        el('shareCourse').value = s.course; return true; }
			
			    const type = typeOf(field), c = SHARE[type];
			    const items = s[field] || [];
			    if (items.length === 0) return false;      // keep their picks if Genie found nothing
			    clearFilters(type, c.chosen);              // wipe existing selections
			    for (const it of items) {
			        addFromSearch(c.search, null, it.id, it.name, it.image, type, c.chosen, c.results);
			    }
			    return true;
			}
			
			el('shareForm').addEventListener('submit', async (e) => {
			    e.preventDefault();
			
			    const ingredients = filterIds.get('Ingredient').map(id => {
			        const r = ingredientRows.get(id);
			        return { id, amount: r.amount.value ? Number(r.amount.value) : null, unit: r.unit.value.trim() || null };
			    });
			    if (ingredients.length === 0) { setStatus('Add at least one ingredient.'); return; }
			
			    const body = {
			        name: val('shareName'),
			        instructions: val('shareInstructions'),
			        description: val('shareDescription') || null,
			        totalMinutes: val('shareMinutes') ? Number(val('shareMinutes')) : null,
			        course: val('shareCourse') || null,
			        ingredients,
			        applianceIds: filterIds.get('Appliance'),
			        cuisineIds: filterIds.get('Cuisine'),
			        dishTypeIds: filterIds.get('Dish Type'),
			        aiFill: [...aiPicked].filter(f => f !== 'ingredients') // no ai_gen column for ingredients
			    };
			
			    const submit = e.target.querySelector('button[type=submit]');
			    submit.disabled = true;
			    setStatus('Creating your recipe...');
			    const res = await fetch(shareCtx + '/Recipes/Create', {
			        method: 'POST',
			        headers: { 'Content-Type': 'application/json' },
			        body: JSON.stringify(body)
			    });
			    if (!res.ok) {
			        setStatus('Could not create the recipe. Check the fields and try again.');
			        submit.disabled = false;
			        return;
			    }
			    const { recipeId } = await res.json();
			    location.href = 'recipe.jsp?recipeId=' + recipeId;
			});
			</script>
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
		               	<script>
							loadRecipeStrip(
						    	"libraryScrollRow", [], 
						    	{ "Publication Date": "descending" },
						        <%= sessionUserId %>, 
						        { showUser: true }, true
						    );
						</script>
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
            <div>
                <p style="margin: 0;">© 2026 Recipe Genie</p>
            </div>
        </div>
    </footer>


</body>
</html>
