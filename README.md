# 🏋️‍♂️ GymApp

GymApp is a modern backend service supporting strength training applications, offering personalized workout plans, progress tracking, and advanced training statistics. The application enables users to efficiently manage their workouts and monitor progress in real-time.

## 🛠️ Technology Stack

### Backend

- Spring Boot
- Spring Cloud (Eureka, Config Server, Gateway)
- Spring Security + JWT
- PostgreSQL
- Docker
- Maven

## 📋 System Requirements

### Backend

- JDK 24 or newer
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL 17+

## 🚀 Installation Guide

1. Clone the repository:

```bash
git clone https://github.com/stejzy/GymApp.git
cd GymApp
```

2. Create a PostgreSQL database named `gymapp`

3. Create `.env` file in the root directory with the following content:

```
DB_USERNAME=your_username
DB_PASSWORD=your_password
OPENAI_API_KEY=your_openai_token
```

4. Start services using Docker Compose:

```bash
docker-compose up -d
```

5. Wait for all services to start:

- Eureka Server (port 8761)
- Config Server (port 8088)
- Gateway Service (port 8050)
- Auth Service (port 8024)
- User Service (port 8021)
- Workout Generation Service (port 8022)
- Schedule Service (port 8023)

## ✨ Key functionalities

- 🔐 User registration and authentication
- 📊 Workout plan generation and management
- 📈 Progress tracking and statistics
- 👤 User profile management
- 📅 Training schedule management
- 🎯 Exercise tracking and history

## 📊 Feature Status

- [x] REST API integration with external services
- [x] OpenAPI documentation and API design
- [x] Microservices architecture with Spring Cloud Eureka
- [x] Centralized configuration with Spring Cloud Config Server
- [x] Authentication & Authorization with Spring Security
- [x] Comprehensive testing (Unit, Integration, BDD with Cucumber)
- [x] Code quality with SonarQube
- [ ] Modern JDK features implementation
- [x] AI/LLM integration

## 👨‍💻 Authors

- **Kacper Stasiak** - [GitHub](https://github.com/stejzy)
- **Jakub Cendalski** - [GitHub](https://github.com/Ceendi)
- **Kacper Witek** - [GitHub](https://github.com/KacperWitek)
- **Kacper Przybylski** - [GitHub](https://github.com/KacperFTIMS/)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
