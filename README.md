# CS157A-team7

## Instructions for Collaborators

Follow these steps to get the project running and to share your changes with the team.

> **Do not create a new Dynamic Web Project.** Clone this repo and import it as described below, since the project settings are already in the repo.

### 1. Clone and import (one time)

1. In Eclipse, go to **File → Import → Git → Projects from Git → Clone URI**, then click Next.
2. Paste the repo URL: `https://github.com/davidNguyen4321/CS157A-team7.git`. If it asks for a login, sign in with your GitHub account.
3. Select the `main` branch, then choose a folder to clone into (preferably not inside another Eclipse project).
4. Choose **Import existing Eclipse projects**, tick the project, and click **Finish**.

### 2. Run it

1. Right-click the project and choose **Run As → Run on Server**, then pick your Tomcat.
2. If the project shows red errors, open **Properties → Java Build Path** (or **Targeted Runtimes**) and select your own Tomcat runtime. A runtime name mismatch is the usual cause.

### 3. Every time after that

1. **Pull first:** right-click the project, then **Team → Pull**.
2. Make your changes.
3. **Commit and push:** right-click the project, then **Team → Commit...**, write a message, and click **Commit and Push**.
