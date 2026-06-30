// ── Auth ─────────────────────────────────────────────
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

// ── Department ────────────────────────────────────────
export interface Department {
  id: number;
  name: string;
  description?: string;
  location?: string;
  phoneNumber?: string;
  status: 'ACTIVE' | 'INACTIVE';
  doctorCount: number;
  createdAt?: string;
}

export interface DepartmentRequest {
  name: string;
  description?: string;
  location?: string;
  phoneNumber?: string;
  status: 'ACTIVE' | 'INACTIVE';
}

// ── Doctor ────────────────────────────────────────────
export interface Doctor {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  licenseNumber: string;
  phoneNumber?: string;
  specialization: string;
  yearsOfExperience?: number;
  qualification?: string;
  status: 'ACTIVE' | 'ON_LEAVE' | 'INACTIVE';
  departmentId?: number;
  departmentName?: string;
  createdAt?: string;
}

export interface DoctorRequest {
  firstName: string;
  lastName: string;
  email: string;
  licenseNumber: string;
  phoneNumber?: string;
  specialization: string;
  yearsOfExperience?: number;
  qualification?: string;
  status: 'ACTIVE' | 'ON_LEAVE' | 'INACTIVE';
  departmentId?: number;
}

// ── Patient ───────────────────────────────────────────
export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE';
  phoneNumber?: string;
  address?: string;
  nationalId?: string;
  bloodType?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  allergies?: string;
  chronicDiseases?: string;
  status: 'ACTIVE' | 'DISCHARGED' | 'DECEASED';
  createdAt?: string;
}

export interface PatientRequest {
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE';
  phoneNumber?: string;
  address?: string;
  nationalId?: string;
  bloodType?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  allergies?: string;
  chronicDiseases?: string;
  status: 'ACTIVE' | 'DISCHARGED' | 'DECEASED';
}

// ── Appointment ───────────────────────────────────────
export interface Appointment {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  doctorSpecialization?: string;
  appointmentDateTime: string;
  reason?: string;
  status: AppointmentStatus;
  type: AppointmentType;
  notes?: string;
  durationMinutes?: number;
  createdAt?: string;
}

export type AppointmentStatus =
  'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

export type AppointmentType =
  'REGULAR' | 'EMERGENCY' | 'FOLLOW_UP' | 'CONSULTATION';

export interface AppointmentRequest {
  patientId: number;
  doctorId: number;
  appointmentDateTime: string;
  reason?: string;
  type: AppointmentType;
  durationMinutes?: number;
  notes?: string;
}

export interface AppointmentStatusUpdate {
  status: AppointmentStatus;
  notes?: string;
}

// ── Medical Record ────────────────────────────────────
export interface MedicalRecord {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  appointmentId?: number;
  diagnosis: string;
  symptoms?: string;
  treatmentPlan?: string;
  doctorNotes?: string;
  weight?: number;
  height?: number;
  bloodPressure?: string;
  heartRate?: number;
  temperature?: number;
  prescriptions: Prescription[];
  createdAt?: string;
}

export interface MedicalRecordRequest {
  patientId: number;
  doctorId: number;
  appointmentId?: number;
  diagnosis: string;
  symptoms?: string;
  treatmentPlan?: string;
  doctorNotes?: string;
  weight?: number;
  height?: number;
  bloodPressure?: string;
  heartRate?: number;
  temperature?: number;
  prescriptions?: PrescriptionRequest[];
}

// ── Prescription ──────────────────────────────────────
export interface Prescription {
  id: number;
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays: number;
  startDate?: string;
  endDate?: string;
  instructions?: string;
  refillAllowed?: boolean;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED';
  createdAt?: string;
}

export interface PrescriptionRequest {
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays: number;
  startDate?: string;
  instructions?: string;
  refillAllowed?: boolean;
}

// ── Dashboard ─────────────────────────────────────────
export interface DashboardStats {
  totalPatients: number;
  activePatients: number;
  totalDoctors: number;
  activeDoctors: number;
  totalDepartments: number;
  todayAppointments: number;
  thisMonthAppointments: number;
  totalMedicalRecords: number;
  appointmentsByStatus: Record<string, number>;
}

// ── API Response ──────────────────────────────────────
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
  errors?: Record<string, string>;
}

// ── Pagination ────────────────────────────────────────
export interface PagedResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
