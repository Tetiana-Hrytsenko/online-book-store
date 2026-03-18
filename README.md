## ⚙️ Project Setup & Environment Configuration

Before running the application, you need to set up your environment variables. This project uses
a `.env` file to manage sensitive data and port configurations.

### 1. Configure Environment Variables

1. Locate the `.env.template` file in the root directory.
2. Create a copy of it and rename it to `.env`:
   ```bash
   cp .env.template .env
   ```
3. Open the `.env` file and fill in the required values.

### 📝 Variable Recommendations

| Variable | Recommended Value | Description |
| :--- | :--- | :--- |
| **MYSQL_USER** | `db_user` | Your application database username. |
| **MYSQL_PASSWORD** | *your_secure_password* | Password for the application user. |
| **MYSQL_ROOT_PASSWORD** | *root_secure_password* | Administrative password for the MySQL server. |
| **MYSQL_DATABASE** | `app_db` | Name of the database to be created on startup. |
| **MYSQL_LOCAL_PORT** | `3307` | Port on your **Host machine** (use 3307 if 3306 is already in use). |
| **MYSQL_DOCKER_PORT** | `3306` | Internal MySQL port inside the container (default is 3306). |
| **SPRING_LOCAL_PORT** | `8080` | Port to access the application from your browser (e.g., localhost:8080). |
| **SPRING_DOCKER_PORT** | `8080` | Internal port where the Spring Boot app runs in Docker. |
| **DEBUG_PORT** | `5005` | Port for Remote Debugging from your IDE (IntelliJ, VS Code). |

> ⚠️ **Warning:** Never commit your `.env` file to version control. It is already added
to `.gitignore` to keep your credentials safe.

### 2. Running the Application

Once your `.env` file is ready, launch the services using Docker Compose:

```bash
docker-compose up 
```

### 3. Stopping the Application

To stop the containers:

```bash
docker-compose stop
```

To stop and remove containers, networks, and images:

```bash
docker-compose down

```


