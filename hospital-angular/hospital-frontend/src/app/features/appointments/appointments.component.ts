import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { AppointmentService, PatientService, DoctorService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Appointment, AppointmentRequest, AppointmentStatus, Patient, Doctor } from '../../models';

@Component({
  selector: 'app-appointments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="page-header">
      <div class="page-header-left">
        <h2><i class="fas fa-calendar-days" style="color:var(--primary);margin-left:8px"></i>إدارة المواعيد</h2>
        <p>{{filtered.length}} موعد</p>
      </div>
      <button class="btn btn-primary" (click)="openCreate()">
        <i class="fas fa-plus"></i> حجز موعد
      </button>
    </div>

    <!-- Status tabs -->
    <div class="status-tabs">
      <button *ngFor="let tab of tabs" class="status-tab"
              [class.active]="activeTab === tab.value"
              (click)="activeTab = tab.value; filter()">
        <span class="tab-dot" [style.background]="tab.color"></span>
        {{tab.label}}
        <span class="tab-count">{{countByStatus(tab.value)}}</span>
      </button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-bar">
          <i class="fas fa-magnifying-glass"></i>
          <input [(ngModel)]="search" (ngModelChange)="filter()" placeholder="بحث بالمريض أو الطبيب...">
        </div>
        <input type="date" class="form-control" style="width:150px" [(ngModel)]="dateFilter" (change)="filter()">
      </div>

      <div *ngIf="loading" class="loading-overlay"><div class="spinner"></div></div>

      <div class="table-wrapper" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>#</th><th>المريض</th><th>الطبيب</th><th>التاريخ والوقت</th>
              <th>السبب</th><th>النوع</th><th>الحالة</th><th>إجراءات</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let a of paginated; let i = index">
              <td style="color:var(--gray-500)">{{(page-1)*pageSize+i+1}}</td>
              <td>
                <div style="font-weight:600;font-size:13px">{{a.patientName}}</div>
              </td>
              <td>
                <div style="font-weight:500;font-size:13px">{{a.doctorName}}</div>
                <div style="font-size:11px;color:var(--gray-500)">{{a.doctorSpecialization}}</div>
              </td>
              <td>
                <div style="font-weight:600;color:var(--primary)">{{a.appointmentDateTime | date:'HH:mm'}}</div>
                <div style="font-size:11px;color:var(--gray-500)">{{a.appointmentDateTime | date:'yyyy/MM/dd'}}</div>
              </td>
              <td style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{a.reason || '—'}}</td>
              <td><span class="badge badge-info">{{typeLabel(a.type)}}</span></td>
              <td>
                <span class="badge" [class]="statusBadge(a.status)">
                  <i class="fas fa-circle" style="font-size:7px"></i>
                  {{statusLabel(a.status)}}
                </span>
              </td>
              <td>
                <div class="table-actions">
                  <button class="btn btn-ghost btn-sm btn-icon" (click)="openStatusModal(a)" title="تحديث الحالة">
                    <i class="fas fa-arrow-rotate-right"></i>
                  </button>
                  <button class="btn btn-ghost btn-sm btn-icon" (click)="openEdit(a)" title="تعديل">
                    <i class="fas fa-pen"></i>
                  </button>
                  <button class="btn btn-sm btn-icon" style="color:var(--danger);background:var(--danger-light)"
                          (click)="cancelAppt(a)" title="إلغاء" [disabled]="a.status==='CANCELLED'||a.status==='COMPLETED'">
                    <i class="fas fa-ban"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div *ngIf="filtered.length===0" class="empty-state">
          <i class="fas fa-calendar-xmark"></i>
          <h3>لا توجد مواعيد</h3><p>لا توجد مواعيد تطابق هذا البحث</p>
        </div>
      </div>

      <div class="pagination" *ngIf="totalPages > 1">
        <button class="page-btn" (click)="page=1" [disabled]="page===1"><i class="fas fa-angles-right"></i></button>
        <button class="page-btn" (click)="page=page-1" [disabled]="page===1"><i class="fas fa-angle-right"></i></button>
        <button class="page-btn" *ngFor="let p of pages" [class.active]="p===page" (click)="page=p">{{p}}</button>
        <button class="page-btn" (click)="page=page+1" [disabled]="page===totalPages"><i class="fas fa-angle-left"></i></button>
        <button class="page-btn" (click)="page=totalPages" [disabled]="page===totalPages"><i class="fas fa-angles-left"></i></button>
      </div>
    </div>

    <!-- Create/Edit Appointment Modal -->
    <div class="modal-overlay" *ngIf="showModal" (click)="closeModal()">
      <div class="modal modal-lg" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <span class="modal-title"><i class="fas fa-calendar-plus"></i> {{editId ? 'تعديل موعد' : 'حجز موعد جديد'}}</span>
          <button class="btn-close" (click)="closeModal()">✕</button>
        </div>
        <form [formGroup]="form" (ngSubmit)="save()">
          <div class="modal-body">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">المريض <span class="required">*</span></label>
                <select formControlName="patientId" class="form-control" [class.error]="err('patientId')">
                  <option [ngValue]="null">-- اختر مريضاً --</option>
                  <option *ngFor="let p of patients" [value]="p.id">{{p.fullName}}</option>
                </select>
                <div class="form-error" *ngIf="err('patientId')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
              <div class="form-group">
                <label class="form-label">الطبيب <span class="required">*</span></label>
                <select formControlName="doctorId" class="form-control" [class.error]="err('doctorId')">
                  <option [ngValue]="null">-- اختر طبيباً --</option>
                  <option *ngFor="let d of doctors" [value]="d.id">{{d.fullName}} - {{d.specialization}}</option>
                </select>
                <div class="form-error" *ngIf="err('doctorId')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">تاريخ ووقت الموعد <span class="required">*</span></label>
                <input formControlName="appointmentDateTime" type="datetime-local" class="form-control" [class.error]="err('appointmentDateTime')">
                <div class="form-error" *ngIf="err('appointmentDateTime')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
              <div class="form-group">
                <label class="form-label">مدة الموعد (دقيقة)</label>
                <input formControlName="durationMinutes" type="number" class="form-control" placeholder="30" min="10">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">نوع الموعد</label>
                <select formControlName="type" class="form-control">
                  <option value="REGULAR">عادي</option>
                  <option value="EMERGENCY">طارئ</option>
                  <option value="FOLLOW_UP">متابعة</option>
                  <option value="CONSULTATION">استشارة</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">سبب الزيارة</label>
                <input formControlName="reason" class="form-control" placeholder="سبب الزيارة...">
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">ملاحظات</label>
              <textarea formControlName="notes" class="form-control" rows="2" placeholder="أي ملاحظات إضافية..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline" (click)="closeModal()">إلغاء</button>
            <button type="submit" class="btn btn-primary" [disabled]="saving">
              <i class="fas" [class.fa-calendar-check]="!saving" [class.fa-spinner]="saving" [class.fa-spin]="saving"></i>
              {{saving ? 'جارٍ الحفظ...' : (editId ? 'تعديل' : 'حجز الموعد')}}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Status Update Modal -->
    <div class="modal-overlay" *ngIf="showStatusModal" (click)="showStatusModal=false">
      <div class="modal" style="max-width:420px" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <span class="modal-title"><i class="fas fa-arrow-rotate-right"></i> تحديث حالة الموعد</span>
          <button class="btn-close" (click)="showStatusModal=false">✕</button>
        </div>
        <div class="modal-body">
          <div *ngIf="selectedAppt" style="background:var(--gray-50);border-radius:10px;padding:14px;margin-bottom:18px">
            <div style="font-weight:600">{{selectedAppt.patientName}}</div>
            <div style="font-size:12px;color:var(--gray-500)">{{selectedAppt.doctorName}} — {{selectedAppt.appointmentDateTime | date:'dd/MM/yyyy HH:mm'}}</div>
          </div>
          <div class="form-group">
            <label class="form-label">الحالة الجديدة</label>
            <select class="form-control" [(ngModel)]="newStatus">
              <option value="SCHEDULED">مجدول</option>
              <option value="CONFIRMED">مؤكد</option>
              <option value="IN_PROGRESS">جارٍ</option>
              <option value="COMPLETED">مكتمل</option>
              <option value="CANCELLED">ملغي</option>
              <option value="NO_SHOW">لم يحضر</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">ملاحظات</label>
            <textarea class="form-control" rows="2" [(ngModel)]="statusNotes" placeholder="ملاحظات اختيارية..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" (click)="showStatusModal=false">إلغاء</button>
          <button class="btn btn-primary" (click)="saveStatus()" [disabled]="saving">
            <i class="fas fa-check"></i> تحديث
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .status-tabs { display:flex; gap:6px; margin-bottom:16px; flex-wrap:wrap; }
    .status-tab { display:flex; align-items:center; gap:7px; padding:8px 14px; border:1.5px solid var(--gray-300); background:#fff; border-radius:20px; font-family:inherit; font-size:12.5px; font-weight:500; cursor:pointer; transition:all .18s; color:var(--gray-700); }
    .status-tab:hover { border-color:var(--primary); color:var(--primary); }
    .status-tab.active { border-color:var(--primary); background:var(--primary-light); color:var(--primary); }
    .tab-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
    .tab-count { background:var(--gray-200); border-radius:10px; padding:1px 7px; font-size:11px; }
    .status-tab.active .tab-count { background:rgba(26,115,232,.15); }
  `]
})
export class AppointmentsComponent implements OnInit {
  all: Appointment[] = [];
  filtered: Appointment[] = [];
  paginated: Appointment[] = [];
  patients: Patient[] = [];
  doctors: Doctor[] = [];
  loading = true; saving = false;
  search = ''; dateFilter = ''; activeTab = '';
  page = 1; pageSize = 10;
  showModal = false; editId: number | null = null;
  showStatusModal = false;
  selectedAppt: Appointment | null = null;
  newStatus: AppointmentStatus = 'SCHEDULED';
  statusNotes = '';
  form!: FormGroup;

  tabs = [
    { label: 'الكل',    value: '',            color: '#9aa0a6' },
    { label: 'مجدول',   value: 'SCHEDULED',   color: '#1a73e8' },
    { label: 'مؤكد',    value: 'CONFIRMED',   color: '#0f9d58' },
    { label: 'جارٍ',    value: 'IN_PROGRESS', color: '#f29900' },
    { label: 'مكتمل',  value: 'COMPLETED',   color: '#9e9e9e' },
    { label: 'ملغي',    value: 'CANCELLED',   color: '#d93025' },
  ];

  constructor(
    private svc: AppointmentService, private patSvc: PatientService,
    private docSvc: DoctorService, private toast: ToastService, private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.load();
    this.patSvc.getActive().subscribe(p => this.patients = p as unknown as Patient[]);
    this.docSvc.getActive().subscribe(d => this.doctors = d as unknown as Doctor[]);
    this.buildForm();
  }

  load(): void {
    this.loading = true;
    this.svc.getAll().subscribe({ next: d => { this.all = d; this.filter(); this.loading = false; }, error: () => this.loading = false });
  }

  buildForm(): void {
    this.form = this.fb.group({
      patientId: [null, Validators.required],
      doctorId:  [null, Validators.required],
      appointmentDateTime: ['', Validators.required],
      reason: [''], type: ['REGULAR'], durationMinutes: [30], notes: ['']
    });
  }

  filter(): void {
    const q = this.search.toLowerCase();
    this.filtered = this.all.filter(a => {
      const ms = !q || a.patientName.toLowerCase().includes(q) || a.doctorName.toLowerCase().includes(q);
      const mt = !this.activeTab || a.status === this.activeTab;
      const md = !this.dateFilter || a.appointmentDateTime.startsWith(this.dateFilter);
      return ms && mt && md;
    });
    this.page = 1; this.updatePage();
  }

  updatePage(): void { const s = (this.page - 1) * this.pageSize; this.paginated = this.filtered.slice(s, s + this.pageSize); }
  get totalPages(): number { return Math.ceil(this.filtered.length / this.pageSize); }
  get pages(): number[] { return Array.from({length: this.totalPages}, (_, i) => i + 1); }
  countByStatus(status: string): number { return status ? this.all.filter(a => a.status === status).length : this.all.length; }

  openCreate(): void { this.editId = null; this.form.reset({ type: 'REGULAR', durationMinutes: 30 }); this.showModal = true; }
  openEdit(a: Appointment): void {
    this.editId = a.id;
    const dt = a.appointmentDateTime.replace('Z', '').substring(0, 16);
    this.form.patchValue({ ...a, appointmentDateTime: dt });
    this.showModal = true;
  }
  closeModal(): void { this.showModal = false; }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const v = this.form.value;
    const body: AppointmentRequest = { ...v, appointmentDateTime: new Date(v.appointmentDateTime).toISOString() };
    const req = this.editId ? this.svc.update(this.editId, body) : this.svc.create(body);
    req.subscribe({
      next: () => { this.toast.success(this.editId ? 'تم تعديل الموعد' : 'تم حجز الموعد بنجاح'); this.saving = false; this.closeModal(); this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  openStatusModal(a: Appointment): void { this.selectedAppt = a; this.newStatus = a.status; this.statusNotes = ''; this.showStatusModal = true; }

  saveStatus(): void {
    if (!this.selectedAppt) return;
    this.saving = true;
    this.svc.updateStatus(this.selectedAppt.id, { status: this.newStatus, notes: this.statusNotes }).subscribe({
      next: () => { this.toast.success('تم تحديث حالة الموعد'); this.saving = false; this.showStatusModal = false; this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  cancelAppt(a: Appointment): void {
    if (!confirm(`هل تريد إلغاء موعد ${a.patientName}؟`)) return;
    this.svc.cancel(a.id).subscribe({
      next: () => { this.toast.success('تم إلغاء الموعد'); this.load(); },
      error: (e) => this.toast.error(e.error?.message ?? 'حدث خطأ')
    });
  }

  err(f: string): boolean { const c = this.form.get(f); return !!(c?.invalid && c?.touched); }
  statusLabel(s: string): string { return { SCHEDULED:'مجدول', CONFIRMED:'مؤكد', IN_PROGRESS:'جارٍ', COMPLETED:'مكتمل', CANCELLED:'ملغي', NO_SHOW:'لم يحضر' }[s] ?? s; }
  statusBadge(s: string): string { return { SCHEDULED:'badge-info', CONFIRMED:'badge-success', IN_PROGRESS:'badge-warning', COMPLETED:'badge-gray', CANCELLED:'badge-danger', NO_SHOW:'badge-danger' }[s] ?? 'badge-gray'; }
  typeLabel(t: string): string { return { REGULAR:'عادي', EMERGENCY:'طارئ', FOLLOW_UP:'متابعة', CONSULTATION:'استشارة' }[t] ?? t; }
}
