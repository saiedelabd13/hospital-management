import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService, AppointmentService } from '../../core/services/api.service';
import { DashboardStats, Appointment } from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div *ngIf="loading" class="loading-overlay">
      <div class="spinner"></div><p>جارٍ تحميل الإحصائيات...</p>
    </div>

    <ng-container *ngIf="!loading && stats">
      <!-- Stats grid -->
      <div class="stats-grid">
        <div class="stat-card" routerLink="/patients" style="cursor:pointer">
          <div class="stat-icon blue"><i class="fas fa-users"></i></div>
          <div class="stat-info">
            <div class="label">إجمالي المرضى</div>
            <div class="value">{{stats.totalPatients}}</div>
            <div class="sub">{{stats.activePatients}} نشط</div>
          </div>
        </div>
        <div class="stat-card" routerLink="/doctors" style="cursor:pointer">
          <div class="stat-icon green"><i class="fas fa-user-doctor"></i></div>
          <div class="stat-info">
            <div class="label">الأطباء</div>
            <div class="value">{{stats.totalDoctors}}</div>
            <div class="sub">{{stats.activeDoctors}} متاح</div>
          </div>
        </div>
        <div class="stat-card" routerLink="/appointments" style="cursor:pointer">
          <div class="stat-icon orange"><i class="fas fa-calendar-days"></i></div>
          <div class="stat-info">
            <div class="label">مواعيد اليوم</div>
            <div class="value">{{stats.todayAppointments}}</div>
            <div class="sub">{{stats.thisMonthAppointments}} هذا الشهر</div>
          </div>
        </div>
        <div class="stat-card" routerLink="/departments" style="cursor:pointer">
          <div class="stat-icon red"><i class="fas fa-building"></i></div>
          <div class="stat-info">
            <div class="label">الأقسام</div>
            <div class="value">{{stats.totalDepartments}}</div>
            <div class="sub">{{stats.totalMedicalRecords}} سجل طبي</div>
          </div>
        </div>
      </div>

      <!-- Two-column grid -->
      <div class="dash-grid">
        <!-- Appointments status -->
        <div class="card">
          <div class="card-header">
            <span class="card-title"><i class="fas fa-chart-pie"></i> المواعيد حسب الحالة</span>
          </div>
          <div class="card-body">
            <div class="status-rows">
              <div class="status-row" *ngFor="let s of statusRows">
                <div class="s-dot" [style.background]="s.color"></div>
                <span class="s-label">{{s.label}}</span>
                <div class="s-bar-wrap">
                  <div class="s-bar" [style.width.%]="getPercent(s.key)" [style.background]="s.color"></div>
                </div>
                <span class="s-count">{{stats.appointmentsByStatus[s.key] || 0}}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Today appointments -->
        <div class="card">
          <div class="card-header">
            <span class="card-title"><i class="fas fa-clock"></i> مواعيد اليوم</span>
            <a routerLink="/appointments" class="btn btn-ghost btn-sm">عرض الكل</a>
          </div>
          <div class="card-body" style="padding:0">
            <div *ngIf="todayAppts.length === 0" class="empty-state" style="padding:32px">
              <i class="fas fa-calendar-xmark"></i>
              <p>لا توجد مواعيد اليوم</p>
            </div>
            <div class="appt-list" *ngIf="todayAppts.length > 0">
              <div class="appt-row" *ngFor="let a of todayAppts.slice(0,6)">
                <div class="appt-time">{{a.appointmentDateTime | date:'HH:mm'}}</div>
                <div class="appt-info">
                  <span class="appt-patient">{{a.patientName}}</span>
                  <span class="appt-doctor">{{a.doctorName}}</span>
                </div>
                <span class="badge" [class]="getStatusClass(a.status)">{{getStatusLabel(a.status)}}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ng-container>
  `,
  styles: [`
    .stats-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; margin-bottom: 24px; }
    .stat-info .sub { font-size: 11px; color: var(--gray-500); margin-top: 3px; }
    .dash-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .status-rows { display: flex; flex-direction: column; gap: 14px; }
    .status-row { display: flex; align-items: center; gap: 10px; }
    .s-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
    .s-label { font-size: 12px; color: var(--gray-700); width: 90px; flex-shrink: 0; }
    .s-bar-wrap { flex: 1; height: 8px; background: var(--gray-100); border-radius: 4px; overflow: hidden; }
    .s-bar { height: 100%; border-radius: 4px; transition: width .4s ease; min-width: 4px; }
    .s-count { font-size: 13px; font-weight: 600; width: 28px; text-align: left; }
    .appt-list { }
    .appt-row { display: flex; align-items: center; gap: 12px; padding: 12px 20px; border-bottom: 1px solid var(--gray-100); }
    .appt-row:last-child { border-bottom: none; }
    .appt-time { font-size: 13px; font-weight: 600; color: var(--primary); width: 44px; flex-shrink: 0; }
    .appt-info { flex: 1; }
    .appt-patient { display: block; font-size: 13px; font-weight: 500; }
    .appt-doctor  { display: block; font-size: 11px; color: var(--gray-500); }
    @media(max-width:900px){ .stats-grid{grid-template-columns:1fr 1fr} .dash-grid{grid-template-columns:1fr} }
  `]
})
export class DashboardComponent implements OnInit {
  stats!: DashboardStats;
  todayAppts: Appointment[] = [];
  loading = true;

  statusRows = [
    { key: 'SCHEDULED',   label: 'مجدول',    color: '#1a73e8' },
    { key: 'CONFIRMED',   label: 'مؤكد',      color: '#0f9d58' },
    { key: 'IN_PROGRESS', label: 'جارٍ',       color: '#f29900' },
    { key: 'COMPLETED',   label: 'مكتمل',     color: '#9e9e9e' },
    { key: 'CANCELLED',   label: 'ملغي',       color: '#d93025' },
  ];

  constructor(private dashSvc: DashboardService, private apptSvc: AppointmentService) {}

  ngOnInit(): void {
    this.dashSvc.getStats().subscribe({
      next: s => { this.stats = s; this.loading = false; },
      error: () => this.loading = false
    });
    this.apptSvc.getToday().subscribe(list => this.todayAppts = list);
  }

  getPercent(key: string): number {
    const total = Object.values(this.stats.appointmentsByStatus).reduce((a, b) => a + b, 0);
    return total ? Math.round(((this.stats.appointmentsByStatus[key] || 0) / total) * 100) : 0;
  }

  getStatusLabel(s: string): string {
    const map: Record<string, string> = { SCHEDULED:'مجدول', CONFIRMED:'مؤكد', IN_PROGRESS:'جارٍ', COMPLETED:'مكتمل', CANCELLED:'ملغي', NO_SHOW:'غائب' };
    return map[s] ?? s;
  }
  getStatusClass(s: string): string {
    const map: Record<string, string> = { SCHEDULED:'badge-info', CONFIRMED:'badge-success', IN_PROGRESS:'badge-warning', COMPLETED:'badge-gray', CANCELLED:'badge-danger', NO_SHOW:'badge-danger' };
    return 'badge ' + (map[s] ?? 'badge-gray');
  }
}
