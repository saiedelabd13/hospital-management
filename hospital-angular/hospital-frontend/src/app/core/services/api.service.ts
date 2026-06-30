import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import {
  Department, DepartmentRequest,
  Doctor, DoctorRequest,
  Patient, PatientRequest,
  Appointment, AppointmentRequest, AppointmentStatus, AppointmentStatusUpdate,
  MedicalRecord, MedicalRecordRequest,
  DashboardStats
} from '@models/index';

const BASE = environment.apiUrl;

// ── Department Service ────────────────────────────────
@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private url = `${BASE}/departments`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Department[]>                { return this.http.get<Department[]>(this.url); }
  getActive(): Observable<Department[]>             { return this.http.get<Department[]>(`${this.url}/active`); }
  getById(id: number): Observable<Department>       { return this.http.get<Department>(`${this.url}/${id}`); }
  create(body: DepartmentRequest): Observable<Department>   { return this.http.post<Department>(this.url, body); }
  update(id: number, body: DepartmentRequest): Observable<Department> { return this.http.put<Department>(`${this.url}/${id}`, body); }
  delete(id: number): Observable<void>              { return this.http.delete<void>(`${this.url}/${id}`); }
}

// ── Doctor Service ────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class DoctorService {
  private url = `${BASE}/doctors`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Doctor[]>                    { return this.http.get<Doctor[]>(this.url); }
  getActive(): Observable<Doctor[]>                 { return this.http.get<Doctor[]>(`${this.url}/active`); }
  getById(id: number): Observable<Doctor>           { return this.http.get<Doctor>(`${this.url}/${id}`); }
  getByDepartment(deptId: number): Observable<Doctor[]> { return this.http.get<Doctor[]>(`${this.url}/department/${deptId}`); }
  search(keyword: string): Observable<Doctor[]>     {
    return this.http.get<Doctor[]>(`${this.url}/search`, { params: new HttpParams().set('keyword', keyword) });
  }
  create(body: DoctorRequest): Observable<Doctor>   { return this.http.post<Doctor>(this.url, body); }
  update(id: number, body: DoctorRequest): Observable<Doctor> { return this.http.put<Doctor>(`${this.url}/${id}`, body); }
  delete(id: number): Observable<void>              { return this.http.delete<void>(`${this.url}/${id}`); }
}

// ── Patient Service ───────────────────────────────────
@Injectable({ providedIn: 'root' })
export class PatientService {
  private url = `${BASE}/patients`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Patient[]>                   { return this.http.get<Patient[]>(this.url); }
  getActive(): Observable<Patient[]>                { return this.http.get<Patient[]>(`${this.url}/active`); }
  getById(id: number): Observable<Patient>          { return this.http.get<Patient>(`${this.url}/${id}`); }
  search(keyword: string): Observable<Patient[]>    {
    return this.http.get<Patient[]>(`${this.url}/search`, { params: new HttpParams().set('keyword', keyword) });
  }
  create(body: PatientRequest): Observable<Patient> { return this.http.post<Patient>(this.url, body); }
  update(id: number, body: PatientRequest): Observable<Patient> { return this.http.put<Patient>(`${this.url}/${id}`, body); }
  delete(id: number): Observable<void>              { return this.http.delete<void>(`${this.url}/${id}`); }
  countActive(): Observable<number>                 { return this.http.get<number>(`${this.url}/count/active`); }
}

// ── Appointment Service ───────────────────────────────
@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private url = `${BASE}/appointments`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Appointment[]>                       { return this.http.get<Appointment[]>(this.url); }
  getToday(): Observable<Appointment[]>                     { return this.http.get<Appointment[]>(`${this.url}/today`); }
  getById(id: number): Observable<Appointment>              { return this.http.get<Appointment>(`${this.url}/${id}`); }
  getByPatient(id: number): Observable<Appointment[]>       { return this.http.get<Appointment[]>(`${this.url}/patient/${id}`); }
  getByDoctor(id: number): Observable<Appointment[]>        { return this.http.get<Appointment[]>(`${this.url}/doctor/${id}`); }
  getByStatus(s: AppointmentStatus): Observable<Appointment[]> { return this.http.get<Appointment[]>(`${this.url}/status/${s}`); }
  create(body: AppointmentRequest): Observable<Appointment> { return this.http.post<Appointment>(this.url, body); }
  update(id: number, body: AppointmentRequest): Observable<Appointment> { return this.http.put<Appointment>(`${this.url}/${id}`, body); }
  updateStatus(id: number, body: AppointmentStatusUpdate): Observable<Appointment> {
    return this.http.patch<Appointment>(`${this.url}/${id}/status`, body);
  }
  cancel(id: number): Observable<void>                      { return this.http.delete<void>(`${this.url}/${id}/cancel`); }
}

// ── Medical Record Service ────────────────────────────
@Injectable({ providedIn: 'root' })
export class MedicalRecordService {
  private url = `${BASE}/medical-records`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<MedicalRecord[]>                         { return this.http.get<MedicalRecord[]>(this.url); }
  getById(id: number): Observable<MedicalRecord>                { return this.http.get<MedicalRecord>(`${this.url}/${id}`); }
  getByPatient(id: number): Observable<MedicalRecord[]>         { return this.http.get<MedicalRecord[]>(`${this.url}/patient/${id}`); }
  getByDoctor(id: number): Observable<MedicalRecord[]>          { return this.http.get<MedicalRecord[]>(`${this.url}/doctor/${id}`); }
  create(body: MedicalRecordRequest): Observable<MedicalRecord> { return this.http.post<MedicalRecord>(this.url, body); }
  update(id: number, body: MedicalRecordRequest): Observable<MedicalRecord> { return this.http.put<MedicalRecord>(`${this.url}/${id}`, body); }
  delete(id: number): Observable<void>                          { return this.http.delete<void>(`${this.url}/${id}`); }
}

// ── Dashboard Service ─────────────────────────────────
@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}
  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${environment.apiUrl}/dashboard/stats`);
  }
}
