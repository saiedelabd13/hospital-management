import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { DoctorService, DepartmentService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Doctor, DoctorRequest, Department } from '../../models';

@Component({
  selector: 'app-doctors',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="page-header">
      <div class="page-header-left">
        <h2><i class="fas fa-user-doctor" style="color:var(--primary);margin-left:8px"></i>إدارة الأطباء</h2>
        <p>{{filtered.length}} طبيب مسجل</p>
      </div>
      <button class="btn btn-primary" (click)="openCreate()">
        <i class="fas fa-plus"></i> إضافة طبيب
      </button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-bar">
          <i class="fas fa-magnifying-glass"></i>
          <input [(ngModel)]="search" (ngModelChange)="filter()" placeholder="بحث بالاسم أو التخصص...">
        </div>
        <select class="form-control" style="width:150px" [(ngModel)]="deptFilter" (change)="filter()">
          <option value="">كل الأقسام</option>
          <option *ngFor="let d of departments" [value]="d.id">{{d.name}}</option>
        </select>
        <select class="form-control" style="width:120px" [(ngModel)]="statusFilter" (change)="filter()">
          <option value="">كل الحالات</option>
          <option value="ACTIVE">متاح</option>
          <option value="ON_LEAVE">إجازة</option>
          <option value="INACTIVE">غير نشط</option>
        </select>
      </div>

      <div *ngIf="loading" class="loading-overlay"><div class="spinner"></div></div>

      <div class="table-wrapper" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>#</th><th>الطبيب</th><th>التخصص</th><th>القسم</th>
              <th>الخبرة</th><th>الترخيص</th><th>الحالة</th><th>إجراءات</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let d of paginated; let i = index">
              <td style="color:var(--gray-500)">{{(page-1)*pageSize+i+1}}</td>
              <td>
                <div style="display:flex;align-items:center;gap:10px">
                  <div class="doc-avatar">{{d.firstName.charAt(0)}}{{d.lastName.charAt(0)}}</div>
                  <div>
                    <div style="font-weight:600">{{d.fullName}}</div>
                    <div style="font-size:11px;color:var(--gray-500)">{{d.email}}</div>
                  </div>
                </div>
              </td>
              <td><span class="spec-tag">{{d.specialization}}</span></td>
              <td>{{d.departmentName || '—'}}</td>
              <td>{{d.yearsOfExperience ? d.yearsOfExperience + ' سنوات' : '—'}}</td>
              <td style="font-size:12px;color:var(--gray-500)">{{d.licenseNumber}}</td>
              <td><span class="badge" [class]="statusClass(d.status)">{{statusLabel(d.status)}}</span></td>
              <td>
                <div class="table-actions">
                  <button class="btn btn-ghost btn-sm btn-icon" (click)="openEdit(d)" title="تعديل">
                    <i class="fas fa-pen"></i>
                  </button>
                  <button class="btn btn-sm btn-icon" style="color:var(--danger);background:var(--danger-light)"
                          (click)="confirmDelete(d)" title="حذف">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div *ngIf="filtered.length===0" class="empty-state">
          <i class="fas fa-user-doctor"></i>
          <h3>لا توجد نتائج</h3><p>جرّب تغيير معايير البحث</p>
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

    <!-- Create/Edit Modal -->
    <div class="modal-overlay" *ngIf="showModal" (click)="closeModal()">
      <div class="modal modal-lg" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <span class="modal-title">
            <i class="fas fa-user-doctor"></i>
            {{editId ? 'تعديل بيانات طبيب' : 'إضافة طبيب جديد'}}
          </span>
          <button class="btn-close" (click)="closeModal()">✕</button>
        </div>
        <form [formGroup]="form" (ngSubmit)="save()">
          <div class="modal-body">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">الاسم الأول <span class="required">*</span></label>
                <input formControlName="firstName" class="form-control" [class.error]="err('firstName')" placeholder="الاسم الأول">
                <div class="form-error" *ngIf="err('firstName')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
              <div class="form-group">
                <label class="form-label">الاسم الأخير <span class="required">*</span></label>
                <input formControlName="lastName" class="form-control" [class.error]="err('lastName')" placeholder="الاسم الأخير">
                <div class="form-error" *ngIf="err('lastName')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">البريد الإلكتروني <span class="required">*</span></label>
                <input formControlName="email" class="form-control" [class.error]="err('email')" placeholder="dr@hospital.com">
                <div class="form-error" *ngIf="err('email')"><i class="fas fa-circle-exclamation"></i> بريد غير صحيح</div>
              </div>
              <div class="form-group">
                <label class="form-label">رقم الترخيص <span class="required">*</span></label>
                <input formControlName="licenseNumber" class="form-control" [class.error]="err('licenseNumber')" placeholder="LIC-001">
                <div class="form-error" *ngIf="err('licenseNumber')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">التخصص <span class="required">*</span></label>
                <input formControlName="specialization" class="form-control" [class.error]="err('specialization')" placeholder="مثال: أمراض القلب">
                <div class="form-error" *ngIf="err('specialization')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
              <div class="form-group">
                <label class="form-label">رقم الهاتف</label>
                <input formControlName="phoneNumber" class="form-control" placeholder="010xxxxxxxx">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">سنوات الخبرة</label>
                <input formControlName="yearsOfExperience" type="number" class="form-control" placeholder="10" min="0">
              </div>
              <div class="form-group">
                <label class="form-label">القسم</label>
                <select formControlName="departmentId" class="form-control">
                  <option [ngValue]="null">-- اختر قسماً --</option>
                  <option *ngFor="let d of departments" [value]="d.id">{{d.name}}</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">المؤهل العلمي</label>
              <input formControlName="qualification" class="form-control" placeholder="مثال: دكتوراه في طب القلب">
            </div>
            <div class="form-group">
              <label class="form-label">الحالة</label>
              <select formControlName="status" class="form-control">
                <option value="ACTIVE">متاح</option>
                <option value="ON_LEAVE">إجازة</option>
                <option value="INACTIVE">غير نشط</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline" (click)="closeModal()">إلغاء</button>
            <button type="submit" class="btn btn-primary" [disabled]="saving">
              <i class="fas" [class.fa-save]="!saving" [class.fa-spinner]="saving" [class.fa-spin]="saving"></i>
              {{saving ? 'جارٍ الحفظ...' : 'حفظ'}}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Confirm delete -->
    <div class="modal-overlay" *ngIf="deleteTarget" (click)="deleteTarget=null">
      <div class="modal" style="max-width:400px" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <span class="modal-title" style="color:var(--danger)"><i class="fas fa-triangle-exclamation"></i> تأكيد الحذف</span>
          <button class="btn-close" (click)="deleteTarget=null">✕</button>
        </div>
        <div class="modal-body" style="text-align:center;padding:32px">
          <i class="fas fa-user-slash" style="font-size:48px;color:var(--danger);margin-bottom:16px;display:block"></i>
          <p>هل تريد حذف الطبيب <strong>{{deleteTarget.fullName}}</strong>؟</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" (click)="deleteTarget=null">إلغاء</button>
          <button class="btn btn-danger" (click)="doDelete()" [disabled]="saving"><i class="fas fa-trash"></i> حذف</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .doc-avatar { width:38px;height:38px;border-radius:10px;background:var(--primary-light);color:var(--primary);display:flex;align-items:center;justify-content:center;font-weight:700;font-size:13px;flex-shrink:0; }
    .spec-tag { background:#f3e5f5;color:#7b1fa2;border-radius:6px;padding:3px 10px;font-size:12px;font-weight:500; }
  `]
})
export class DoctorsComponent implements OnInit {
  all: Doctor[] = [];
  filtered: Doctor[] = [];
  paginated: Doctor[] = [];
  departments: Department[] = [];
  loading = true; saving = false;
  search = ''; statusFilter = ''; deptFilter = '';
  page = 1; pageSize = 10;
  showModal = false; editId: number | null = null;
  deleteTarget: Doctor | null = null;
  form!: FormGroup;

  constructor(
    private svc: DoctorService, private deptSvc: DepartmentService,
    private toast: ToastService, private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.load();
    this.deptSvc.getActive().subscribe(d => this.departments = d);
    this.buildForm();
  }

  load(): void {
    this.loading = true;
    this.svc.getAll().subscribe({ next: d => { this.all = d; this.filter(); this.loading = false; }, error: () => this.loading = false });
  }

  buildForm(): void {
    this.form = this.fb.group({
      firstName: ['', Validators.required], lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      licenseNumber: ['', Validators.required],
      specialization: ['', Validators.required],
      phoneNumber: [''], yearsOfExperience: [null],
      qualification: [''], departmentId: [null], status: ['ACTIVE']
    });
  }

  filter(): void {
    this.filtered = this.all.filter(d => {
      const q = this.search.toLowerCase();
      const ms = !q || d.fullName.toLowerCase().includes(q) || d.specialization.toLowerCase().includes(q);
      const md = !this.deptFilter || d.departmentId === +this.deptFilter;
      const mst = !this.statusFilter || d.status === this.statusFilter;
      return ms && md && mst;
    });
    this.page = 1; this.updatePage();
  }

  updatePage(): void { const s = (this.page - 1) * this.pageSize; this.paginated = this.filtered.slice(s, s + this.pageSize); }
  get totalPages(): number { return Math.ceil(this.filtered.length / this.pageSize); }
  get pages(): number[] { return Array.from({length: this.totalPages}, (_, i) => i + 1); }

  openCreate(): void { this.editId = null; this.form.reset({ status: 'ACTIVE', departmentId: null }); this.showModal = true; }
  openEdit(d: Doctor): void { this.editId = d.id; this.form.patchValue({ ...d }); this.showModal = true; }
  closeModal(): void { this.showModal = false; }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const body = this.form.value as DoctorRequest;
    const req = this.editId ? this.svc.update(this.editId, body) : this.svc.create(body);
    req.subscribe({
      next: () => { this.toast.success(this.editId ? 'تم تعديل بيانات الطبيب' : 'تم إضافة الطبيب بنجاح'); this.saving = false; this.closeModal(); this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  confirmDelete(d: Doctor): void { this.deleteTarget = d; }
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.saving = true;
    this.svc.delete(this.deleteTarget.id).subscribe({
      next: () => { this.toast.success('تم حذف الطبيب'); this.saving = false; this.deleteTarget = null; this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  err(f: string): boolean { const c = this.form.get(f); return !!(c?.invalid && c?.touched); }
  statusLabel(s: string): string { return { ACTIVE: 'متاح', ON_LEAVE: 'إجازة', INACTIVE: 'غير نشط' }[s] ?? s; }
  statusClass(s: string): string { return { ACTIVE: 'badge-success', ON_LEAVE: 'badge-warning', INACTIVE: 'badge-danger' }[s] ?? 'badge-gray'; }
}
