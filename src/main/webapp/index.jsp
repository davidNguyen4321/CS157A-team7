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
                    <%-- Placeholder loop: replace with database results, one <article> per recipe --%>
                    <% for (int i = 0; i < 5; i++) { %>
                    <article class="recipe-card">
                        <div class="recipe-card-img">
                            <img src="images/recipe-placeholder.jpg" alt="Recipe" />
                            <div class="recipe-card-badges">
                                <div class="card-badge-group">
                                    <span class="badge-pill">25m</span>
                                </div>
                                <span class="badge-pill">2025-05-25</span>
                            </div>
                        </div>
                        <div class="recipe-card-body">
                            <h3 class="recipe-card-title">Recipe Name</h3>
                            <p class="recipe-card-desc">Description of the recipe...</p>
                            <div class="recipe-card-footer">
                                <div class="card-author">
                                    <span class="card-avatar">👤</span>
                                    <span>Author</span>
                                </div>
                                <div class="card-stats">
                                    <span class="card-bookmark">
                                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 3h12a1 1 0 0 1 1 1v17l-7-4-7 4V4a1 1 0 0 1 1-1z"/></svg>
                                        2.5k
                                    </span>
                                    <span class="card-rating">
                                        <span class="stars" style="--rating: 4.5;" aria-label="4.5 out of 5 stars"></span>
                                        4.5 (125)
                                    </span>
                                </div>
                            </div>
                        </div>
                    </article>
                    <% } %>
                </div>
            </section>

            <!-- Reviews for You -->
            <section>
                <div>
                    <h2 class="section-title">Reviews for You</h2>
                    <p class="section-subtitle">What people are saying about your recipes</p>
                </div>
                <div class="horizontal-scroll-row">
                    <%-- Placeholder loop: replace with database results, one <article> per review --%>
                    <% for (int i = 0; i < 5; i++) { %>
                    <article class="review-card">
                        <div class="review-thumb">
                            <img src="images/recipe-placeholder.jpg" alt="Recipe thumbnail" />
                        </div>
                        <div class="review-body">
                            <p class="review-title">Recipe Name</p>
                            <p class="review-text">Content of the review...</p>
                            <div class="review-footer">
                                <div class="card-author">
                                    <span class="card-avatar">👤</span>
                                    <span>Reviewer</span>
                                </div>
                                <div class="review-meta">
                                    <span class="review-date">2025-10-20</span>
                                    <span class="card-rating">
                                        <span class="stars" style="--rating: 4.5;" aria-label="4.5 out of 5 stars"></span>
                                        4.5
                                    </span>
                                </div>
                            </div>
                        </div>
                    </article>
                    <% } %>
                </div>
            </section>

            <!-- Your Most Popular -->
            <section>
                <div>
                    <h2 class="section-title">Your Most Popular</h2>
                    <p class="section-subtitle">Which of your recipes are the most popular</p>
                </div>
                <div class="horizontal-scroll-row">
                    <%-- Placeholder loop: replace with database results ranked by popularity, one <article> per recipe --%>
                    <% for (int i = 1; i <= 5; i++) { %>
                    <article class="recipe-card">
                        <div class="recipe-card-img">
                            <img src="images/recipes/recipe-placeholder.jpg" alt="Recipe" />
                            <div class="rank-badge">#<%= i %></div>
                            <div class="recipe-card-badges">
                                <div class="card-badge-group">
                                    <span class="badge-pill">30m</span>
                                </div>
                            </div>
                        </div>
                        <div class="recipe-card-body">
                            <h3 class="recipe-card-title">Recipe Name</h3>
                            <p class="recipe-card-desc">Description of the recipe...</p>
                            <div class="recipe-card-footer">
                                <div class="card-stats">
                                    <span class="card-bookmark">
                                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 3h12a1 1 0 0 1 1 1v17l-7-4-7 4V4a1 1 0 0 1 1-1z"/></svg>
                                        2.5k
                                    </span>
                                    <span class="card-rating">
                                        <span class="stars" style="--rating: 4.5;" aria-label="4.5 out of 5 stars"></span>
                                        4.5 (125)
                                    </span>
                                </div>
                            </div>
                        </div>
                    </article>
                    <% } %>
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