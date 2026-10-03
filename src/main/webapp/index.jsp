<%@ page import="java.sql.*" %>
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
                            <button type="button" class="filter-btn">
                                ⚙️ Filters
                            </button>
                        </div>

                        <div class="search-bottom-row">
                            <div class="search-filter-col">
                                <div class="filter-icon-box">🥚</div>
                                <div>
                                    <div class="filter-label">Ingredients</div>
                                    <div class="filter-sub">What do you have?</div>
                                </div>
                            </div>
                            <div class="search-filter-col">
                                <div class="filter-icon-box">🍳</div>
                                <div>
                                    <div class="filter-label">Appliances</div>
                                    <div class="filter-sub">What can you use?</div>
                                </div>
                            </div>
                            <div class="search-filter-col">
                                <div class="filter-icon-box">👤</div>
                                <div>
                                    <div class="filter-label">Authors</div>
                                    <div class="filter-sub">Who to learn from?</div>
                                </div>
                            </div>
                            <button type="submit" class="find-recipes-submit">Find Recipes</button>
                        </div>
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
					    			imagePath = "images/recipe-placeholder.jpg";	
					    		}
					    		%>

					    		<article class="recipe-card">
					    		    <div class="recipe-card-img">
					    		        <img src="<%= imagePath %>" alt="Recipe">

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
                            "v.REVIEW_ACTIVE_TIMESTAMP, u.USER_NAME " +
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
                                    reviewImage = "images/recipe-placeholder.jpg";
                                }

                                String reviewer = reviews.getString("USER_NAME");
                                if (reviewer == null) {
                                    reviewer = "Anonymous";
                                }
                    %>
                    <article class="review-card">
                        <div class="review-thumb">
                            <img src="<%= reviewImage %>" alt="Recipe thumbnail" />
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
                                    <span class="card-avatar">👤</span>
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
                                    popularImage = "images/recipe-placeholder.jpg";
                                }
                    %>
                    <article class="recipe-card">
                        <div class="recipe-card-img">
                            <img src="<%= popularImage %>" alt="Recipe" />
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
