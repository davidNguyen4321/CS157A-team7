<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="database.DbAccess" %>

<%@ page import="util.WebUtils" %>
<%@ page import="util.TimeFormatter" %>
<%@ page import="auth.UserDAO" %>

<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.time.LocalDateTime" %>

<%@ include file="/WEB-INF/fragments/auth_check.jspf" %>
<%@ include file="/WEB-INF/fragments/recipe_card_maker.jspf" %>
<%@ include file="/WEB-INF/fragments/review_card_maker.jspf" %>

<%
int profileId;
    try { profileId = Integer.parseInt(request.getParameter("id")); }
    catch (Exception e) { profileId = (Integer) session.getAttribute("userId"); }
    int sessionUserId = (Integer) session.getAttribute("userId");
    String sessionUserTimezone = (String) session.getAttribute("userTimezone");
    Connection con = DbAccess.getConnection();
    UserDAO.User profile = UserDAO.findById(profileId);
    boolean available = profile != null && "Active".equals(profile.status);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=available ? WebUtils.esc(profile.name) : "Profile"%> | Recipe Genie</title>
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="css/auth.css">
</head>
<body>
<%@ include file="/WEB-INF/fragments/header.jspf" %>
<main class="profile-page">
<%
if (!available) {
%>
    <h1>This profile is unavailable.</h1>
<%
} else {
       String pic = WebUtils.or(profile.imagePath, "/images/users/user-placeholder.png");
%>
    <section class="profile-hero">
        <img class="profile-avatar" src="${pageContext.request.contextPath}<%= pic %>" alt="Avatar">
        <div id ="profile-container">
            <h1 style="margin:0;"><%=WebUtils.esc(profile.name)%></h1>
            <p class="profile-meta">Timezone: <%=WebUtils.esc(profile.timezone)%></p>
        </div>
    </section>

    <section class="profile-section">
        <h2>Biography</h2>
        <p><%=profile.description == null || profile.description.trim().isEmpty()
                ? "This user hasn't written a biography yet." : WebUtils.esc(profile.description)%></p>
    </section>

    <section class="profile-section">
        <div class="profile-section-head">
            <h2>Recipes</h2>
            <a class="see-all">See all &rarr;</a>
        </div>
        <div id="recipeGrid" class="profile-grid">
      <script>      	
		function createNewFollow(self, followingId, userFollowed){
			self.classList.add("card-follow");
			self.setAttribute("onclick", "toggleFollow(this, " + followingId + ");");
			self.setAttribute("style", "cursor: pointer; border-radius: 0.5rem;");
			let followImage = document.createElement("img");
			followImage.classList.add("follow-icon");
			followImage.setAttribute('alt', "Follow icon");
			followImage.setAttribute('width', "16");
			followImage.setAttribute('height', "16");
			followImage.setAttribute('style', "vertical-align: middle; stroke: currentColor;");
			if(userFollowed){
				followImage.setAttribute('src', "${pageContext.request.contextPath}/images/following_icon.svg");
				self.textContent="Following";
				self.classList.add("active");
			}
			else{
				followImage.setAttribute('src', "${pageContext.request.contextPath}/images/follow_icon.svg");
				self.textContent="Follow";
			}
			self.append(followImage);
		}
		
	    followTimeout = null;
		function toggleFollow(self, followingID){
			let url = "${pageContext.request.contextPath}/Toggle/Follow";
		    clearTimeout(followTimeout);
		    followTimeout = setTimeout(async function () { // to prevent the user from overloading the server with too many requests
				let response = await fetch(url, {
		        	method:'POST',
		        	body: JSON.stringify({
		        		targetId: followingID
		        	}),
		        	headers:{
		                "Content-Type": "application/json; charset=UTF-8",
		                "Accept": "application/json; charset=UTF-8"
		        	}
		        });
				if (!response.ok) {
		        	throw new Error('Network response was not ok');
		        }
	        	let objects = await response.json(); // Creates a JSON object
	        	toggleFollowElement(self, objects["status"]);
		    }, 10);
		}
		
		function toggleFollowElement(self, toggleOnBoolean){
			let followImage = document.createElement("img");
			followImage.classList.add("follow-icon");
			followImage.setAttribute('alt', "Follow icon");
			followImage.setAttribute('width', "16");
			followImage.setAttribute('height', "16");
			followImage.setAttribute('style', "vertical-align: middle; stroke: currentColor; ");
			if(toggleOnBoolean){
				self.classList.add("active");
				followImage.setAttribute('src', "${pageContext.request.contextPath}/images/following_icon.svg");
				self.textContent="Following";
			}
			else{
				self.classList.remove("active");
				followImage.setAttribute('src', "${pageContext.request.contextPath}/images/follow_icon.svg");
				self.textContent="Follow";
			}
			self.append(followImage);
		}
		
	</script>
	<%
	if(profileId!=sessionUserId){
			String fsql = "SELECT 1 FROM FOLLOW " + 
				  "WHERE FOLLOWER_ID = ? " +
				  "AND FOLLOWING_ID = ? ";
			try(PreparedStatement followStatement = con.prepareStatement(fsql)){
		followStatement.setInt(1, sessionUserId);
		followStatement.setInt(2, profileId);
		try(ResultSet rs = followStatement.executeQuery()){
			boolean isFollowing = rs.next();
	%>
		<script>
			let container = document.getElementById("profile-container");		
			let button = document.createElement("button");
      		createNewFollow(button, <%=profileId%>, <%=isFollowing%>);
      		container.append(button);
		</script>
		<%
		}}}
		%>
        <script>
        loadRecipeStrip(
            "recipeGrid",
            [<%=profileId%>],
            { "Publication Date": "descending" },
            <%=sessionUserId%>,
            { showUser: false },
            false
        ).then(recipes => {
            if (recipes.length === 0) {
                document.getElementById("recipeGrid").innerHTML = "<p>No recipes yet.</p>";
            }
        });
        </script>
        </div>
    </section>

<section class="profile-section">
        <div class="profile-section-head">
            <h2>Reviews</h2>
            <a class="see-all">See all &rarr;</a>
        </div>
        <div id="profileReviewsGrid" class="profile-grid"></div>
        <%
        String vsql = "SELECT r.RECIPE_ID, v.REVIEW_NAME, v.REVIEW_DESCRIPTION, v.RATING, r.RECIPE_IMAGE_PATH, " +
                                  "v.REVIEW_ACTIVE_TIMESTAMP, r.RECIPE_NAME, v.REVIEW_IMAGE_PATH FROM REVIEWS v " +
                                  "JOIN RECIPES r ON v.RECIPE_ID = r.RECIPE_ID " +
                                  "WHERE v.REVIEWER_ID = ? AND v.REVIEW_STATUS = 'Active' " +
                                  "ORDER BY v.REVIEW_ACTIVE_TIMESTAMP DESC LIMIT 5";
                    boolean anyReview = false;
                    try (PreparedStatement ps = con.prepareStatement(vsql)) {
                        ps.setInt(1, profileId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                anyReview = true;
                                String rImage = rs.getString("RECIPE_IMAGE_PATH");
                                String vImage = rs.getString("REVIEW_IMAGE_PATH");
                                String combinedTitle = rs.getString("REVIEW_NAME") + " on " + rs.getString("RECIPE_NAME");
                                String formattedTime = TimeFormatter.formatTimeAgo(rs.getObject("REVIEW_ACTIVE_TIMESTAMP", LocalDateTime.class), sessionUserTimezone);
        %>
			<script>
			    addReviewCard(
			        "profileReviewsGrid",
			        <%=rs.getInt("RECIPE_ID")%>,
			        "<%=WebUtils.escJS(rs.getString("RECIPE_NAME"))%>",
			        <%=rImage != null ? "\"" + WebUtils.escJS(rImage) + "\"" : "null"%>,
			        "<%=WebUtils.escJS(rs.getString("REVIEW_DESCRIPTION"))%>",
			        <%=rs.getInt("RATING")%>,
			        <%=vImage != null ? "\"" + WebUtils.escJS(vImage) + "\"" : "null"%>,
					"<%=WebUtils.escJS(formattedTime)%>",
			        <%=profileId%>,
			        null,
			        null,
			        false,
			        "${pageContext.request.contextPath}"
			    );
			</script>
        <%      
                    }
                    if (!anyReview) { 
        %>
            <p>No reviews yet.</p>
        <%          } 
                }
            } catch (Exception error){
                out.println("<p>Database error: " + error.getMessage() + "</p>");
            }
        %>
    </section>
<% } %>
</main>
</body>
</html>