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
                            <input type="text" name="search" placeholder="Search recipe name..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">

                            <!-- Filters button next to the search bar -->
                            <button type="button" class="filter-btn" onclick="handleFilterTypeClick('GeneralPanel', this)">
                                ⚙️ Filters
                            </button>
                        </div>

                        <div class="search-bottom-row">
                           <button type="button" id="Ingredients" class="search-filter-col" onclick="handleFilterTypeClick('IngredientsPanel', this)">
                               <div class="filter-icon-box">🥚</div>
                               <div>
                                   <div class="filter-label">Ingredients</div>
                                   <div class="filter-sub">What do you have?</div>
                               </div>
                           </button>
                           
                           <button type="button" id="Appliances" class="search-filter-col" onclick="handleFilterTypeClick('AppliancesPanel', this)">
                               <div class="filter-icon-box">🍳</div>
                               <div>
                                   <div class="filter-label">Appliances</div>
                                   <div class="filter-sub">What can you use?</div>
                               </div>
                           </button>
                           
                           <button type="button" id="Authors" class="search-filter-col" onclick="handleFilterTypeClick('AuthorsPanel', this)">
                               <div class="filter-icon-box">👤</div>
                               <div>
                                   <div class="filter-label">Authors</div>
                                   <div class="filter-sub">Who to learn from?</div>
                               </div>
                           </button>
                           <button type="submit" class="find-recipes-submit">Find Recipes</button>
                       </div>
                       
						<div id="GeneralPanel" class="filter-panel">
							<div class="filter-panel-container-no-grid" style="align-items: center;">
								<h1 class="filter-panel-name">
								General Filters
		                    	</h1>
		                    	<div class="vrounded-divider" style="border-color:#1F1800; height:50px; opacity:0.6;"> </div>
		                    	<button id="PublicationDateSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'PublicationDateSortText', 'Publication Date')">
		                    		<p id="PublicationDateSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Publication Date
		                    	</button>
		                    	<button id="BookmarksSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'BookmarksSortText', 'Bookmarks')">
									<p id="BookmarksSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Bookmarks
								</button>
		                    	<button id="ReviewSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'ReviewsSortText', 'Reviews')">
		                    		<p id="ReviewsSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Reviews
		                    	</button>
		                    	<button id="RatingsSortOption" type="button" class="filter-panel-sort-option" onclick="sortSelect(this, 'ReviewsSortText', 'Ratings')">
		                    		<p id="RatingsSortText" class="mini-text" style="color:#1F1800; font-size: 0.75rem;">Ratings
		                    	</button>
	                    	</div>
	                    	<div class="filter-panel-container-no-grid">
	                    		<div class="filter-panel-mini" style="width:1rem;">
				                    <h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Dish Type
			                    	</h2>
			                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
			                    	<div style="color: #F5F5DC" class="search-top-bar">
			                           <span class="search-icon-fixed">🔍</span>
			                           <input id="DishTypesSearch" type="text" name="search" placeholder="Search appliances..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
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
	                    		<div class="vrounded-divider" style="border-color:#1F1800; height:100%; opacity:0.6;"> </div>
	                    		<div class="filter-panel-mini" style="width:1rem;">
				                    <h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Cuisine
			                    	</h2>
			                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
			                    	<div style="color: #F5F5DC" class="search-top-bar">
			                           <span class="search-icon-fixed">🔍</span>
			                           <input id="CuisinesSearch" type="text" name="search" placeholder="Search appliances..." value="<%= request.getParameter("search") != null ? request.getParameter("search") : "" %>">
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
	                    	</div>
	                    	<hr class="hrounded-divider" style="border-color:#1F1800; opacity:0.6;"> 
	                    	<div class="filter-panel-container-no-grid">
	                    		<div class="filter-panel-mini"  style="width:1rem;">
	                    			<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Dietary Restrictions
			                    	</h2>
			                    	<div id="DietaryRestrictionsContainer" class="filter-panel-container">
									</div>
	                    		</div>
	                    		<div class="vrounded-divider" style="border-color:#1F1800; height:100%; opacity:0.6;"> </div>
	                    		<div class="filter-panel-mini"  style="width:1rem;">
	                    			<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Courses
			                    	</h2>
			                    	<div id="CoursesContainer" class="filter-panel-container">
									</div>
	                    		</div>
	                    		<div class="vrounded-divider" style="border-color:#1F1800; height:100%; opacity:0.6;"> </div>
	                    		<div class="filter-panel-mini"  style="width:1rem;">
	                    			<h2 class="mini-text" style="color:#1F1800; font-size: 1rem;">
										Ranges
			                    	</h2>
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
						  <input id="IngredientsSlider" type="range" min="0" max="3" value="0" class="slider" id="IngredientsSlider">
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
						  <input id="AppliancesSlider" type="range" min="0" max="3" value="0" class="slider" id="AppliancesSlider">
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
						
						const sortOrder = [];
						const sortOptions = new Map(); // use an array that is just the sort option AND a map that contains whether it ascends or not
						for (key of ["Publication Date", "Bookmarks", "Reviews", "Ratings"]){
							elementBaseId = key.replace(" ", "");
							let object = {
									sort: "ascending",
									sortTextId: elementBaseId+"SortText"
								}
							sortOptions.set(key, object); //True or false refers to ascending (True) or descending (False)
						}
						sortSelect(document.getElementById("PublicationDateSortOption"), "PublicationDateSortText", "Publication Date")
						
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
							for (key of sortOptions.keys()){
								let element = document.getElementById(sortOptions.get(key).sortTextId);
								sortPosition = sortOrder.indexOf(key)+1;
								if(sortPosition == 0){
									element.textContent = key
									continue;
								}
								element.textContent = sortPosition+ " " + key  +": " + sortOptions.get(key).sort;
								element.innerHTML = element.innerHTML.replace(sortPosition, "<span style='color: #1E4E3B;'>"+sortPosition+"</span>");
							}
						}
						
						const filterIds = new Map();
						for (key of ["Ingredient", "Appliance", "Author", "Cuisine", "Dish Type", "Dietary Restriction"]){
							filterIds.set(key, []);
						}
						
						addSearchListenerWithImage('DishTypesSearch', 'Dish Type', 'DishTypesSearchContainer', 'DishTypesContainer', '/RecipeGenie/DishTypes/Search?', false);
						addSearchListenerWithImage('CuisinesSearch', 'Cuisine', 'CuisinesSearchContainer', 'CuisinesContainer', '/RecipeGenie/Cuisines/Search?', false);
						addSearchListenerWithImage('IngredientsSearch', 'Ingredient', 'IngredientsSearchContainer', 'IngredientsContainer', '/RecipeGenie/Ingredients/Search?', true);
						addSearchListenerWithImage('AppliancesSearch', 'Appliance', 'AppliancesSearchContainer', 'AppliancesContainer', '/RecipeGenie/Appliances/Search?', true);
						addSearchListenerWithImage('AuthorsSearch', 'Author', 'AuthorsSearchContainer', 'AuthorsContainer', '/RecipeGenie/Users/Search?', true);
						
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
							for (objectString of Object.values(objects)){
								//initialization
				            	let object = JSON.parse(objectString);
				            	let id = object["id"];
				            	let name = object["name"];
				            	
								//the base element
				            	let newElement = document.createElement("button");
				            	newElement.classList.add("filter-panel-search-autocomplete");
				            	newElement.setAttribute('type','button');
				            	
				            					            	
				            	if(imageBoolean){
					            	image = object["image"];
					            	let newPic = document.createElement("img");
					            	newPic.classList.add("mini-pic");
					            	newPic.setAttribute('src', "${pageContext.request.contextPath}"+image);
					            	newPic.setAttribute('alt', "'" + type + "'");
					            	newElement.setAttribute('onclick', "addFromSearch('"+ searchId +"', this, " + id + ", '" + name + "', '" + image + "', '" + type + "', '" + panelContainerId + "', '"+panelSearchContainerId+"')");
					            	newElement.append(newPic);
				            	}
				            	else{
					            	newElement.setAttribute('onclick', "addFromSearch('"+ searchId +"', this, " + id + ", '" + name + "', " + false + ", '" + type + "', '" + panelContainerId + "', '"+ panelSearchContainerId+"')");
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
			            	filter.setAttribute("onclick", "removeFilterAndSelf(this, " + id + ", '" + type + "' )");

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
						
						function removeFilterAndSelf(self, id, type){
							const index = filterIds.get(type).indexOf(id);
							if (index !== -1){
								filterIds.get(type).splice(index, 1);
							}
							self.remove();
						}
						
						function toggleActiveInclusive(element){ //for elements that are not mutually exclusive in activity
							let isActive = element && element.classList.contains('active');
							if(isActive){
								element.classList.remove('active');
							}
							else{
								element.classList.add('active');
							}
						}
						
						const tolerances = new Map();
						for (key of ["Ingredients", "Appliances"]){
							tolerances.set(key, 0);
						}
						
						addSliderListener("IngredientsSlider", "IngredientsToleranceDisplay", "Ingredients")
						addSliderListener("AppliancesSlider", "AppliancesToleranceDisplay", "Applianes")
						
						function addSliderListener(sliderId, toleranceDisplayId, name){
							let slider = document.getElementById(sliderId);
							let toleranceDisplay = document.getElementById(toleranceDisplayId);
							updateSliderBackground(slider);
							slider.oninput = function() {
								toleranceDisplay.textContent="Allow up to "+ this.value + " missing " + name.toLowerCase();
								tolerances[name]=this.value;
								updateSliderBackground(sliderId);
							} 
						}
						
						function updateSliderBackground(slider){
							const value = slider.value;
							  const max = slider.max || 100;
							  const percentage = (value / max) * 100;
							  slider.style.background = `linear-gradient(to right, #04AA6D 0%, #04AA6D ${percentage}%, #d3d3d3 ${percentage}%, #d3d3d3 100%)`;
						}
						</script>

                    </form>
                </div>
            </div>
        </section>

        <!-- Personalized Strips Section (Your Feed, Reviews for You, Your Most Popular) -->
        <div class="sections-container">

            <!-- Your Feed -->
            <section>
                <div>
                    <h2 class="section-title">Your Feed</h2>
                    <p class="section-subtitle">Latest recipes from your favorite creators</p>
                </div>
                <div class="horizontal-scroll-row">
					 <%
					 if (dbConnection != null) {
					    try {
					    	String sql = 
					    				"SELECT RECIPE_NAME, TOTAL_MINUTES, RECIPE_ACTIVE_TIMESTAMP, "+
					    				"RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION " +
					    				"FROM RECIPES WHERE RECIPE_STATUS = 'Active' " +
					    				"ORDER BY RECIPE_ACTIVE_TIMESTAMP DESC";
					    	
					    	PreparedStatement statement = dbConnection.prepareStatement(sql);
					    	ResultSet recipes = statement.executeQuery();
					    	
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
					    recipes.close();
					    statement.close();
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
                    }
                    %>
                </div>
            </section>

        </div>
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
