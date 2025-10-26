# Vallexia - Smart Meal Planning App

A comprehensive meal planning application with user authentication and profile management built with Spring Boot and Vue.js.

## Tech Stack

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.5.x**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **PostgreSQL 17** database
- **MapStruct** for DTO mapping
- **Maven** for dependency management

### Frontend
- **Vue.js 3** with Composition API
- **Vite** for build tooling
- **Pinia** for state management
- **Vue Router** for navigation
- **Axios** for HTTP requests
- **Tailwind CSS** for styling

## Architecture

Vallexia follows a **feature-based (vertical slice) architecture** where code is organized by business domains rather than technical layers. This approach provides several benefits:

- **Better Cohesion**: Related code (controllers, services, repositories, DTOs) is grouped together by feature
- **Easier Navigation**: All user-related code is in the `user/` module, all auth code in `auth/`, etc.
- **Scalability**: Adding new features doesn't clutter existing structure
- **Clear Boundaries**: Features are self-contained modules with minimal coupling
- **Future-Proof**: Easier to extract features into microservices later if needed

### Feature Modules

- **user**: User profile management, dietary preferences, and nutritional goals
- **auth**: Authentication, registration, login, and token management
- **nutrition**: Nutritional calculations and macro tracking
- **audit**: Security and event audit logging

### Cross-Cutting Concerns

- **config**: Application configuration classes
- **security**: JWT security, filters, and authentication
- **exception**: Global exception handling and error responses

## Project Structure

```
vallexia/
├── src/main/java/com/vallexia/     # Spring Boot backend
│   ├── user/                        # User management domain
│   │   ├── controller/              # User REST controllers
│   │   ├── service/                 # User business logic
│   │   ├── repository/              # User data access
│   │   ├── entity/                  # User entities & enums
│   │   ├── dto/                     # User DTOs
│   │   └── mapper/                  # User mappers
│   ├── auth/                        # Authentication domain
│   │   ├── controller/              # Auth REST controllers
│   │   ├── service/                 # Auth business logic
│   │   └── dto/                     # Auth DTOs
│   ├── nutrition/                   # Nutrition calculation domain
│   │   └── service/                 # Nutrition services
│   ├── audit/                       # Audit logging domain
│   │   ├── service/                 # Audit services
│   │   ├── repository/              # Audit repositories
│   │   └── entity/                  # Audit entities
│   ├── config/                      # Cross-cutting: Configuration
│   ├── security/                    # Cross-cutting: Security
│   ├── exception/                   # Cross-cutting: Exception handling
│   └── web/                         # Vue.js frontend
│       ├── src/
│       │   ├── components/          # Vue components
│       │   ├── views/               # Page components
│       │   ├── stores/              # Pinia stores
│       │   ├── services/            # API services
│       │   ├── router/              # Vue Router
│       │   └── assets/              # Static assets
│       ├── package.json
│       └── vite.config.js
├── src/main/resources/
│   ├── application-dev.yml          # Development profile
│   ├── application-prod.yml         # Production profile
│   └── db/migration/                # Database migrations
├── deployment/                      # Deployment configurations
│   ├── docker/                      # Docker configurations
│   ├── nginx/                       # Nginx configuration
│   └── database/                    # Database scripts
├── pom.xml
└── README.md
```

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE vallexia;
CREATE USER vallexia_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE vallexia TO vallexia_user;
```

### 2. Environment Configuration

1. Copy environment template:
```bash
cp .env.example .env
```

2. Edit `.env` with your values:
```bash
JWT_SECRET=your_secure_jwt_secret_key_here_minimum_32_characters
DB_PASSWORD=your_secure_database_password_here
SPRING_PROFILES_ACTIVE=dev
```

### 3. Backend Setup

1. Navigate to the project root directory
2. Build and run the backend:

```bash
# Using Maven wrapper (recommended)
./mvnw clean compile
./mvnw spring-boot:run

# Or using Maven directly
mvn clean compile
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 4. Frontend Setup

1. Navigate to the frontend directory:
```bash
cd src/main/java/com/vallexia/web
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## Docker Setup (Recommended)

### Prerequisites
- **Docker** and **Docker Compose** installed
- **Environment variables** configured (see step 2 above)

### Development Environment

1. Navigate to the deployment directory:
```bash
cd deployment/docker
```

2. Set up environment variables:
```bash
# Windows PowerShell
$env:DB_PASSWORD="your_secure_password"
$env:JWT_SECRET="your_jwt_secret_key_minimum_32_characters_long"
$env:SPRING_PROFILES_ACTIVE="dev"

# Linux/macOS
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret_key_minimum_32_characters_long
export SPRING_PROFILES_ACTIVE=dev
```

3. Start all services with Docker Compose:
```bash
docker-compose -f docker-compose.dev.yml up --build
```

This will start:
- PostgreSQL database on port 5432
- Redis on port 6379  
- Backend API on port 8080
- Frontend dev server on port 5173

4. Stop services:
```bash
docker-compose -f docker-compose.dev.yml down
```

### Production Environment

1. Navigate to the deployment directory:
```bash
cd deployment/docker
```

2. Set up environment variables:
```bash
# Windows PowerShell
$env:DB_PASSWORD="your_secure_password"
$env:JWT_SECRET="your_jwt_secret_key_minimum_32_characters_long"
$env:SPRING_PROFILES_ACTIVE="prod"
$env:CORS_ALLOWED_ORIGINS="https://yourdomain.com"

# Linux/macOS
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret_key_minimum_32_characters_long
export SPRING_PROFILES_ACTIVE=prod
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

3. Build and start production services:
```bash
docker-compose -f docker-compose.prod.yml up --build -d
```

4. Stop production services:
```bash
docker-compose -f docker-compose.prod.yml down
```

### Deployment Structure

All deployment files are organized in the `deployment/` directory:
- `deployment/docker/` - Docker configurations
- `deployment/nginx/` - Nginx configuration
- `deployment/database/` - Database initialization scripts

Environment variables are set directly in your system/shell and read by Spring Boot through the configuration files in `src/main/resources/`.

See `deployment/README.md` for detailed deployment documentation.

### Docker Benefits
- ✅ **No local dependencies** - No need to install Java, PostgreSQL, Redis
- ✅ **Consistent environment** - Same setup everywhere
- ✅ **Easy deployment** - Deploy anywhere Docker runs
- ✅ **Isolated services** - Each service runs in its own container
- ✅ **Production-ready** - Includes Nginx reverse proxy and SSL support

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Token refresh
- `POST /api/v1/auth/logout` - User logout

### User Management
- `GET /api/v1/users/profile` - Get user profile
- `PUT /api/v1/users/profile` - Update user profile
- `GET /api/v1/users/dietary-preferences` - Get dietary preferences
- `PUT /api/v1/users/dietary-preferences` - Update dietary preferences
- `GET /api/v1/users/nutritional-goals` - Get nutritional goals
- `PUT /api/v1/users/nutritional-goals` - Update nutritional goals

## Features Implemented

### ✅ User Authentication & Profile Management
- User registration with email/password validation
- JWT-based authentication with refresh tokens
- User profile management with personal information
- Dietary preferences management (structure ready)
- Nutritional goals management (structure ready)
- Responsive design with modern UI/UX
- Form validation on both client and server side
- Error handling with user-friendly messages
- Navigation guards for protected routes

### 🚧 Coming Soon
- Recipe management (CRUD operations)
- Meal planning (weekly/monthly planning)
- Grocery list generation
- Nutritional tracking and analysis
- Smart recommendations

## Security & Quality

### 🔒 Security Features
- **JWT Authentication** - Secure token-based authentication with refresh tokens
- **Password Security** - BCrypt password hashing with salting
- **Rate Limiting** - API rate limiting with Bucket4j to prevent abuse
- **Token Blacklisting** - Redis-backed token revocation on logout
- **Audit Logging** - Comprehensive security event logging
- **CORS Protection** - Configured CORS policies for secure cross-origin requests
- **Account Lockout** - Automatic account lockout after failed login attempts

### 🛡️ Security Tools & Scanning
- **OWASP Dependency Check** - Automated vulnerability scanning (fails build on CVSS ≥ 7)
- **Flyway Migrations** - Controlled database schema changes with rollback capability
- **Maven Enforcer** - Enforces dependency convergence and build standards
- **Testcontainers** - Isolated testing with PostgreSQL and Redis containers

### 📊 Code Quality
- **JaCoCo Code Coverage** - Minimum 60% line coverage enforced
- **MapStruct** - Type-safe DTO mapping
- **Lombok** - Reduced boilerplate code
- **Validation** - Jakarta Bean Validation for input validation
- **Compiler Warnings** - Unchecked operations and deprecation warnings enabled

### 📝 Database Management
- **Flyway Migrations** - Version-controlled database schema changes
- **No DDL Auto-update** - Production database changes only through migrations
- **Migration Validation** - Automatic validation of applied migrations
- **Audit Trail** - Complete history of all schema changes

### 🔧 Developer Tools
- **Versions Plugin** - Track available dependency updates
- **Dependency Tree** - Visualize and analyze dependency relationships
- **Security Reports** - HTML reports for vulnerability analysis
- **Coverage Reports** - Interactive code coverage reports

For detailed security audit results, see [SECURITY_AUDIT_REPORT.md](SECURITY_AUDIT_REPORT.md)  
For developer quick reference, see [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)

## Development

### Backend Development
- Follow Java coding standards (Google Java Style)
- Use proper Javadoc documentation
- Implement comprehensive error handling
- Write unit and integration tests
- Use MapStruct for entity-DTO mapping

### Frontend Development
- Follow Vue.js best practices
- Use Composition API
- Implement proper form validation
- Use Tailwind CSS for styling
- Write component tests

## Testing

### Backend Tests
```bash
./mvnw test
```

### Frontend Tests
```bash
cd src/main/java/com/vallexia/web
npm run test
```

## Building for Production

### Backend
```bash
./mvnw clean package
java -jar target/vallexia-1.0.0.jar
```

### Frontend
```bash
cd src/main/java/com/vallexia/web
npm run build
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes following the coding standards
4. Write tests for your changes
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository.







