# 🏥 Hospital Management System
## نظام إدارة المستشفى الشامل - Spring Boot

---

## 📋 نظرة عامة

نظام إدارة مستشفى متكامل مبني بـ **Spring Boot 3.2** يغطي جميع العمليات الأساسية للمستشفى.

---

## 🗂️ هيكل المشروع

```
hospital-management/
├── src/main/java/com/hospital/
│   ├── HospitalManagementApplication.java     ← نقطة الدخول
│   ├── config/
│   │   ├── SecurityConfig.java                ← إعدادات الأمان (JWT)
│   │   ├── OpenApiConfig.java                 ← إعدادات Swagger
│   │   └── DataInitializer.java               ← بيانات أولية تلقائية
│   ├── controller/
│   │   ├── AuthController.java                ← تسجيل الدخول والتسجيل
│   │   ├── DepartmentController.java          ← إدارة الأقسام
│   │   ├── DoctorController.java              ← إدارة الأطباء
│   │   ├── PatientController.java             ← إدارة المرضى
│   │   ├── AppointmentController.java         ← إدارة المواعيد
│   │   ├── MedicalRecordController.java       ← السجلات الطبية
│   │   └── DashboardController.java           ← إحصائيات
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── DepartmentService.java
│   │   ├── DoctorService.java
│   │   ├── PatientService.java
│   │   ├── AppointmentService.java
│   │   ├── MedicalRecordService.java
│   │   └── DashboardService.java
│   ├── entity/
│   │   ├── BaseEntity.java                    ← كيان أساسي (id, timestamps)
│   │   ├── User.java
│   │   ├── Department.java
│   │   ├── Doctor.java
│   │   ├── Patient.java
│   │   ├── Appointment.java
│   │   ├── MedicalRecord.java
│   │   └── Prescription.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── DepartmentRepository.java
│   │   ├── DoctorRepository.java
│   │   ├── PatientRepository.java
│   │   ├── AppointmentRepository.java
│   │   └── MedicalRecordRepository.java
│   ├── dto/
│   │   ├── AuthDTO.java
│   │   ├── DepartmentDTO.java
│   │   ├── DoctorDTO.java
│   │   ├── PatientDTO.java
│   │   ├── AppointmentDTO.java
│   │   ├── MedicalRecordDTO.java
│   │   └── PrescriptionDTO.java
│   ├── security/
│   │   ├── JwtUtils.java
│   │   ├── AuthTokenFilter.java
│   │   ├── UserDetailsImpl.java
│   │   └── UserDetailsServiceImpl.java
│   └── exception/
│       ├── ResourceNotFoundException.java
│       └── GlobalExceptionHandler.java
└── src/main/resources/
    └── application.properties
```

---

## 🚀 تشغيل المشروع

### المتطلبات
- Java 17+
- Maven 3.8+

### للتشغيل
```bash
cd hospital-management
mvn spring-boot:run
```

### للبناء
```bash
mvn clean package
java -jar target/hospital-management-1.0.0.jar
```

---

## 🔗 الروابط المهمة

| الرابط | الوصف |
|--------|-------|
| `http://localhost:8080/api/swagger-ui.html` | واجهة Swagger التفاعلية |
| `http://localhost:8080/api/api-docs` | OpenAPI JSON |
| `http://localhost:8080/api/h2-console` | قاعدة بيانات H2 (للتطوير) |
| `http://localhost:8080/api/actuator/health` | فحص صحة التطبيق |

---

## 👥 المستخدمون الافتراضيون

| اسم المستخدم | كلمة المرور | الدور |
|-------------|-------------|-------|
| `admin` | `admin123` | مدير النظام (وصول كامل) |
| `dr.ahmed` | `doctor123` | طبيب |
| `dr.sara` | `doctor123` | طبيبة |
| `receptionist` | `reception123` | موظف استقبال |

---

## 📡 API Endpoints

### 🔐 المصادقة
```
POST /api/auth/login        ← تسجيل الدخول
POST /api/auth/register     ← تسجيل مستخدم جديد
```

### 🏢 الأقسام
```
GET    /api/departments          ← جلب الكل
GET    /api/departments/active   ← الأقسام النشطة
GET    /api/departments/{id}     ← قسم محدد
POST   /api/departments          ← إنشاء (ADMIN)
PUT    /api/departments/{id}     ← تعديل (ADMIN)
DELETE /api/departments/{id}     ← حذف (ADMIN)
```

### 👨‍⚕️ الأطباء
```
GET    /api/doctors                            ← جلب الكل
GET    /api/doctors/active                     ← النشطين
GET    /api/doctors/{id}                       ← طبيب محدد
GET    /api/doctors/search?keyword=...         ← بحث
GET    /api/doctors/department/{id}            ← حسب القسم
GET    /api/doctors/specialization/{spec}      ← حسب التخصص
POST   /api/doctors                            ← إضافة (ADMIN/RECEPTIONIST)
PUT    /api/doctors/{id}                       ← تعديل (ADMIN/RECEPTIONIST)
DELETE /api/doctors/{id}                       ← حذف (ADMIN)
```

### 🧑‍🤝‍🧑 المرضى
```
GET    /api/patients                    ← جلب الكل
GET    /api/patients/active             ← النشطين
GET    /api/patients/{id}               ← مريض محدد
GET    /api/patients/search?keyword=... ← بحث
GET    /api/patients/count/active       ← عدد المرضى
POST   /api/patients                    ← تسجيل مريض جديد
PUT    /api/patients/{id}               ← تعديل
DELETE /api/patients/{id}               ← حذف (ADMIN)
```

### 📅 المواعيد
```
GET    /api/appointments                     ← جلب الكل
GET    /api/appointments/today               ← مواعيد اليوم
GET    /api/appointments/{id}                ← موعد محدد
GET    /api/appointments/patient/{id}        ← مواعيد مريض
GET    /api/appointments/doctor/{id}         ← مواعيد طبيب
GET    /api/appointments/status/{status}     ← حسب الحالة
GET    /api/appointments/range?start=&end=   ← نطاق زمني
POST   /api/appointments                     ← حجز موعد
PUT    /api/appointments/{id}                ← تعديل موعد
PATCH  /api/appointments/{id}/status         ← تحديث الحالة
DELETE /api/appointments/{id}/cancel         ← إلغاء
```

### 📋 السجلات الطبية
```
GET    /api/medical-records              ← جلب الكل
GET    /api/medical-records/{id}         ← سجل محدد
GET    /api/medical-records/patient/{id} ← سجلات مريض
GET    /api/medical-records/doctor/{id}  ← سجلات طبيب
POST   /api/medical-records              ← إنشاء سجل + وصفة
PUT    /api/medical-records/{id}         ← تعديل
DELETE /api/medical-records/{id}         ← حذف (ADMIN)
```

### 📊 لوحة التحكم
```
GET    /api/dashboard/stats     ← إحصائيات شاملة
```

---

## 🔒 الصلاحيات

| الدور | الوصف |
|-------|-------|
| `ROLE_ADMIN` | وصول كامل لجميع العمليات |
| `ROLE_DOCTOR` | إدارة مرضاه ومواعيده وسجلاته الطبية |
| `ROLE_NURSE` | قراءة السجلات والمواعيد |
| `ROLE_RECEPTIONIST` | إدارة المرضى والمواعيد |
| `ROLE_PATIENT` | وصول محدود لبياناته الشخصية |

---

## 🗄️ قاعدة البيانات (MySQL للإنتاج)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

---

## 🧪 تشغيل الاختبارات

```bash
mvn test
```

---

## 🏗️ التقنيات المستخدمة

| التقنية | الإصدار | الغرض |
|---------|---------|-------|
| Spring Boot | 3.2.0 | الإطار الأساسي |
| Spring Security | 6.x | الأمان والمصادقة |
| Spring Data JPA | 3.x | قاعدة البيانات |
| JWT (jjwt) | 0.11.5 | التوكن |
| H2 Database | - | قاعدة بيانات التطوير |
| MySQL | 8.x | قاعدة بيانات الإنتاج |
| Lombok | latest | تقليل الكود |
| SpringDoc OpenAPI | 2.3.0 | توثيق API |
| Maven | 3.8+ | إدارة المشروع |
