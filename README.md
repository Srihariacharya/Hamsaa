# ContactPro - Full-Stack Intelligent Relationship Management System

**ContactPro** is a professional-grade, full-stack Contact Management System designed for modern workflows. Built with a focus on premium aesthetics, seamless user experience, and robust data persistence, it provides everything you need to manage your professional relationships effectively.

## 🚀 Key Features

- 📊 **Advanced Analytics**: Gain deep insights into your contact database with interactive and beautiful data visualizations.
- 🗃️ **Smart Contact Management**: Full CRUD operations for contacts, including categorization (Clients, leads, vendors), gender profiling, and interaction scoring.
- 🔔 **Follow-Up System**: Stay on top of your outreach with an intelligent tracking system that calculates "Overdue" and "Upcoming" contacts based on interaction history.
- 📋 **Integrated Task Board**: Organize your operations with a powerful Kanban-style task management board (Pending, In Progress, Completed).
- 📤 **Import/Export**: Seamlessly migrate your data with built-in vCard (.vcf) import and export functionality.
- 🌙 **Premium Dark Mode**: A sophisticated theme designed with deep navy and indigo accents for a premium feel.
- 👤 **Profile & Security**: Manage your professional identity and secure your account with built-in profile updates and password management.

## 🛠️ Technology Stack

### Frontend
- **Framework**: React 18+ (Vite)
- **Styling**: Tailwind CSS
- **Visualization**: Recharts
- **Icons**: Lucide React
- **State/Auth**: Context API & Axios

### Backend
- **Core**: Spring Boot 3.4+
- **Database**: PostgreSQL (Persistent)
- **Security**: Spring Security
- **Data Access**: Spring Data JPA (Hibernate)
- **Mapping**: DTO-based architecture

## 📂 Project Structure

```text
.
├── Frontend/           # React + Vite application
│   ├── src/pages/      # Dashboard, Analytics, Contacts, etc.
│   └── src/context/    # Auth and Theme state management
├── Backend/            # Spring Boot application
│   ├── controller/     # REST API Endpoints
│   ├── dto/            # Data Transfer Objects
│   └── service/        # Business Logic layer
└── README.md           # Main documentation
```

## 🚀 Getting Started

### Prerequisites
- Node.js & npm
- Java 17+
- PostgreSQL

### 1. Database Setup
Create a PostgreSQL database named `contactpro`.

### 2. Backend Setup
```bash
cd Backend
# Configure your db credentials in src/main/resources/application.properties
./mvnw spring-boot:run
```

### 3. Frontend Setup
```bash
cd Frontend
npm install
npm run dev
```

The application will be available at `http://localhost:5173`.

## 🤝 Contributing
Feel free to fork this repository and submit pull requests.

## ✍️ Author
**Srihari Acharya**

---
Designed with ❤️ for premium productivity.
