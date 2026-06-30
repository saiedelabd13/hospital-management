import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { DepartmentService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Department, DepartmentRequest } from '../../models';

@Component({
  selector: 'app-departments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="page-header">
      <div class="page-header-left">
        <h2><i class="fas fa-building" style="color:var(--primary);margin-left:8px"></i>إدارة الأقسام</h2>
        <p>{{filtered.length}} قسم مسجل</p>
      </div>
      <button class="btn btn-primary" (click)="openCreate()">
        <i class="fas fa-plus"></i> إضافة قسم
      </button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-bar">
          <i class="fas fa-magnifying-glass"></i>
          <input [(ngModel)]="search" (ngModelChange)="filter()" placeholder="بحث باسم القسم...">
        </div>
        <select class="form-control" style="width:130px" [(ngModel)]="statusFilter" (change)="filter()">
          <option value="">كل الحالات</option>
          <option value="ACTIVE">نشط</option>
          <option value="INACTIVE">غير نشط</option>
        </select>
      </div>

      <div *ngIf="loading" class="loading-overlay"><div class="spinner"></div></div>

      <!-- Cards Grid -->
      <div class="dept-grid" *ngIf="!loading">
        <div class="dept-card" *ngFor="let d of filtered">
          <div class="dept-card-header">
            <div class="dept-icon"><i class="fas fa-hospital-user"></i></div>
            <div class="dept-status">
              <span class="badge" [class]="d.status==='ACTIVE'?'badge-success':'badge-danger'">
                {{d.status==='ACTIVE'?'نشط':'غير نشط'}}
              </span>
            </div>
          </div>
          <h3 class="dept-name">{{d.name}}</h3>
          <p class="dept-desc">{{d.description || 'لا يوجد وصف'}}</p>
          <div class="dept-meta">
            <span *ngIf="d.location"><i class="fas fa-location-dot"></i> {{d.location}}</span>
            <span *ngIf="d.phoneNumber"><i class="fas fa-phone"></i> {{d.phoneNumber}}</span>
          </div>
          <div class="dept-footer">
            <div class="dept-count">
              <i class="fas fa-user-doctor"></i>
              <span>{{d.doctorCount}} طبيب</span>
            </div>
            <div class="dept-actions">
              <button class="btn btn-ghost btn-sm btn-icon" (click)="openEdit(d)" title="تعديل">
                <i class="fas fa-pen"></i>
              </button>
              <button class="btn btn-sm btn-icon" style="color:var(--danger);background:var(--danger-light)"
                      (click)="confirmDelete(d)" title="حذف">
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
        </div>

        <div *ngIf="filtered.length === 0" class="empty-state" style="grid-column:1/-1">
          <i class="fas fa-building-circle-xmark"></i>
          <h3>لا توجد أقسام</h3>
          <p>أضف قسماً جديداً للبدء</p>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div class="modal-overlay" *ngIf="showModal" (click)="closeModal()">
      <div class="modal" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <span class="modal-title">
            <i class="fas" [class.fa-plus-circle]="!editId" [class.fa-pen-to-square]="editId"></i>
            {{editId ? 'تعديل قسم' : 'إضافة قسم جديد'}}
          </span>
          <button class="btn-close" (click)="closeModal()">✕</button>
        </div>
        <form [formGroup]="form" (ngSubmit)="save()">
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">اسم القسم <span class="required">*</span></label>
              <input formControlName="name" class="form-control" [class.error]="err('name')" placeholder="مثال: قسم القلب">
              <div class="form-error" *ngIf="err('name')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
            </div>
            <div class="form-group">
              <label class="form-label">الوصف</label>
              <textarea formControlName="description" class="form-control" rows="3" placeholder="وصف مختصر للقسم..."></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">الموقع</label>
                <input formControlName="location" class="form-control" placeholder="مثال: الطابق الثاني">
              </div>
              <div class="form-group">
                <label class="form-label">رقم الهاتف</label>
                <input formControlName="phoneNumber" class="form-control" placeholder="02-xxxx-xxxx">
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">الحالة</label>
              <select formControlName="status" class="form-control">
                <option value="ACTIVE">نشط</option>
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
          <i class="fas fa-building-circle-xmark" style="font-size:48px;color:var(--danger);margin-bottom:16px;display:block"></i>
          <p>هل تريد حذف قسم <strong>{{deleteTarget.name}}</strong>؟</p>
          <p style="font-size:12px;color:var(--gray-500);margin-top:6px">لا يمكن حذف القسم إذا كان يحتوي على أطباء.</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" (click)="deleteTarget=null">إلغاء</button>
          <button class="btn btn-danger" (click)="doDelete()" [disabled]="saving">
            <i class="fas fa-trash"></i> حذف
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dept-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:16px; padding:20px; }
    .dept-card { background:#fff; border:1.5px solid var(--gray-200); border-radius:var(--radius); padding:20px; transition:all .2s; }
    .dept-card:hover { border-color:var(--primary); box-shadow:var(--shadow-md); transform:translateY(-2px); }
    .dept-card-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:14px; }
    .dept-icon { width:46px; height:46px; background:var(--primary-light); border-radius:12px; display:flex; align-items:center; justify-content:center; color:var(--primary); font-size:20px; }
    .dept-name { font-size:15px; font-weight:700; color:var(--gray-900); margin-bottom:6px; }
    .dept-desc { font-size:12px; color:var(--gray-500); line-height:1.5; margin-bottom:12px; min-height:36px; }
    .dept-meta { display:flex; flex-direction:column; gap:4px; margin-bottom:14px; }
    .dept-meta span { font-size:12px; color:var(--gray-700); display:flex; align-items:center; gap:6px; }
    .dept-meta i { color:var(--gray-500); width:14px; }
    .dept-footer { display:flex; justify-content:space-between; align-items:center; padding-top:12px; border-top:1px solid var(--gray-100); }
    .dept-count { display:flex; align-items:center; gap:6px; font-size:12px; font-weight:600; color:var(--gray-700); }
    .dept-count i { color:var(--primary); }
    .dept-actions { display:flex; gap:6px; }
  `]
})
export class DepartmentsComponent implements OnInit {
  all: Department[] = [];
  filtered: Department[] = [];
  loading = true; saving = false;
  search = ''; statusFilter = '';
  showModal = false; editId: number | null = null;
  deleteTarget: Department | null = null;
  form!: FormGroup;

  constructor(private svc: DepartmentService, private toast: ToastService, private fb: FormBuilder) {}

  ngOnInit(): void { this.load(); this.buildForm(); }

  load(): void {
    this.loading = true;
    this.svc.getAll().subscribe({ next: d => { this.all = d; this.filter(); this.loading = false; }, error: () => this.loading = false });
  }

  buildForm(): void {
    this.form = this.fb.group({
      name: ['', Validators.required], description: [''],
      location: [''], phoneNumber: [''], status: ['ACTIVE']
    });
  }

  filter(): void {
    this.filtered = this.all.filter(d => {
      const matchSearch = !this.search || d.name.toLowerCase().includes(this.search.toLowerCase());
      const matchStatus = !this.statusFilter || d.status === this.statusFilter;
      return matchSearch && matchStatus;
    });
  }

  openCreate(): void { this.editId = null; this.form.reset({ status: 'ACTIVE' }); this.showModal = true; }
  openEdit(d: Department): void { this.editId = d.id; this.form.patchValue(d); this.showModal = true; }
  closeModal(): void { this.showModal = false; }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const body = this.form.value as DepartmentRequest;
    const req = this.editId ? this.svc.update(this.editId, body) : this.svc.create(body);
    req.subscribe({
      next: () => { this.toast.success(this.editId ? 'تم تعديل القسم' : 'تم إضافة القسم'); this.saving = false; this.closeModal(); this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  confirmDelete(d: Department): void { this.deleteTarget = d; }
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.saving = true;
    this.svc.delete(this.deleteTarget.id).subscribe({
      next: () => { this.toast.success('تم حذف القسم'); this.saving = false; this.deleteTarget = null; this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  err(f: string): boolean { const c = this.form.get(f); return !!(c?.invalid && c?.touched); }
}
