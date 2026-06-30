import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { PatientService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Patient, PatientRequest } from '../../models';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <!-- Page header -->
    <div class="page-header">
      <div class="page-header-left">
        <h2><i class="fas fa-users" style="color:var(--primary);margin-left:8px"></i>إدارة المرضى</h2>
        <p>{{filtered.length}} مريض مسجل</p>
      </div>
      <button class="btn btn-primary" (click)="openCreate()">
        <i class="fas fa-plus"></i> إضافة مريض
      </button>
    </div>

    <!-- Table card -->
    <div class="card">
      <div class="toolbar">
        <div class="search-bar">
          <i class="fas fa-magnifying-glass"></i>
          <input [(ngModel)]="search" (ngModelChange)="filter()" placeholder="بحث بالاسم أو الرقم القومي...">
        </div>
        <select class="form-control" style="width:130px" [(ngModel)]="statusFilter" (change)="filter()">
          <option value="">كل الحالات</option>
          <option value="ACTIVE">نشط</option>
          <option value="DISCHARGED">خُرّج</option>
          <option value="DECEASED">متوفى</option>
        </select>
        <select class="form-control" style="width:110px" [(ngModel)]="genderFilter" (change)="filter()">
          <option value="">الجنسين</option>
          <option value="MALE">ذكر</option>
          <option value="FEMALE">أنثى</option>
        </select>
      </div>

      <div *ngIf="loading" class="loading-overlay"><div class="spinner"></div></div>

      <div class="table-wrapper" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>#</th><th>الاسم</th><th>الجنس</th><th>تاريخ الميلاد</th>
              <th>فصيلة الدم</th><th>الهاتف</th><th>الحالة</th><th>إجراءات</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let p of paginated; let i = index">
              <td style="color:var(--gray-500)">{{(page-1)*pageSize + i + 1}}</td>
              <td>
                <div style="display:flex;align-items:center;gap:10px">
                  <div class="avatar" [style.background]="p.gender==='MALE'?'#e8f0fe':'#fce4ec'">
                    {{p.firstName.charAt(0)}}
                  </div>
                  <div>
                    <div style="font-weight:600">{{p.fullName}}</div>
                    <div style="font-size:11px;color:var(--gray-500)">{{p.email}}</div>
                  </div>
                </div>
              </td>
              <td>
                <span class="badge" [class]="p.gender==='MALE'?'badge-info':'badge-warning'">
                  <i class="fas" [class.fa-mars]="p.gender==='MALE'" [class.fa-venus]="p.gender==='FEMALE'"></i>
                  {{p.gender==='MALE'?'ذكر':'أنثى'}}
                </span>
              </td>
              <td>{{p.dateOfBirth | date:'yyyy/MM/dd'}}</td>
              <td><span class="blood-badge">{{p.bloodType || '—'}}</span></td>
              <td>{{p.phoneNumber || '—'}}</td>
              <td><span class="badge" [class]="statusClass(p.status)">{{statusLabel(p.status)}}</span></td>
              <td>
                <div class="table-actions">
                  <button class="btn btn-ghost btn-sm btn-icon" (click)="openEdit(p)" title="تعديل">
                    <i class="fas fa-pen"></i>
                  </button>
                  <button class="btn btn-sm btn-icon" style="color:var(--danger);background:var(--danger-light)"
                          (click)="confirmDelete(p)" title="حذف">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div *ngIf="filtered.length === 0" class="empty-state">
          <i class="fas fa-user-slash"></i>
          <h3>لا توجد نتائج</h3>
          <p>جرّب تغيير كلمة البحث أو الفلتر</p>
        </div>
      </div>

      <!-- Pagination -->
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
            <i class="fas" [class.fa-user-plus]="!editId" [class.fa-user-pen]="editId"></i>
            {{editId ? 'تعديل بيانات مريض' : 'إضافة مريض جديد'}}
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
                <input formControlName="email" class="form-control" [class.error]="err('email')" placeholder="example@email.com">
                <div class="form-error" *ngIf="err('email')"><i class="fas fa-circle-exclamation"></i> بريد غير صحيح</div>
              </div>
              <div class="form-group">
                <label class="form-label">رقم الهاتف</label>
                <input formControlName="phoneNumber" class="form-control" placeholder="010xxxxxxxx">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">تاريخ الميلاد <span class="required">*</span></label>
                <input formControlName="dateOfBirth" type="date" class="form-control" [class.error]="err('dateOfBirth')">
              </div>
              <div class="form-group">
                <label class="form-label">الجنس <span class="required">*</span></label>
                <select formControlName="gender" class="form-control">
                  <option value="">-- اختر --</option>
                  <option value="MALE">ذكر</option>
                  <option value="FEMALE">أنثى</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">الرقم القومي</label>
                <input formControlName="nationalId" class="form-control" placeholder="14 رقماً">
              </div>
              <div class="form-group">
                <label class="form-label">فصيلة الدم</label>
                <select formControlName="bloodType" class="form-control">
                  <option value="">-- اختر --</option>
                  <option *ngFor="let b of bloodTypes" [value]="b">{{b}}</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">العنوان</label>
              <input formControlName="address" class="form-control" placeholder="العنوان الكامل">
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">جهة الطوارئ</label>
                <input formControlName="emergencyContactName" class="form-control" placeholder="اسم الشخص">
              </div>
              <div class="form-group">
                <label class="form-label">هاتف الطوارئ</label>
                <input formControlName="emergencyContactPhone" class="form-control" placeholder="010xxxxxxxx">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">الحساسية</label>
                <textarea formControlName="allergies" class="form-control" rows="2" placeholder="أدخل أي حساسية..."></textarea>
              </div>
              <div class="form-group">
                <label class="form-label">أمراض مزمنة</label>
                <textarea formControlName="chronicDiseases" class="form-control" rows="2" placeholder="أدخل الأمراض المزمنة..."></textarea>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">الحالة</label>
              <select formControlName="status" class="form-control">
                <option value="ACTIVE">نشط</option>
                <option value="DISCHARGED">مُخرَّج</option>
                <option value="DECEASED">متوفى</option>
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
    <div class="modal-overlay" *ngIf="deleteTarget" (click)="deleteTarget = null">
      <div class="modal" style="max-width:400px" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <span class="modal-title" style="color:var(--danger)"><i class="fas fa-triangle-exclamation"></i> تأكيد الحذف</span>
          <button class="btn-close" (click)="deleteTarget=null">✕</button>
        </div>
        <div class="modal-body" style="text-align:center;padding:32px">
          <i class="fas fa-user-slash" style="font-size:48px;color:var(--danger);margin-bottom:16px;display:block"></i>
          <p>هل تريد حذف المريض <strong>{{deleteTarget.fullName}}</strong>؟</p>
          <p style="font-size:12px;color:var(--gray-500);margin-top:6px">لا يمكن التراجع عن هذا الإجراء.</p>
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
    .avatar { width:36px;height:36px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:14px;color:var(--primary);flex-shrink:0; }
    .blood-badge { display:inline-block;background:#fff3e0;color:#e65100;border:1px solid #ffcc80;border-radius:6px;padding:2px 8px;font-size:11px;font-weight:700; }
  `]
})
export class PatientsComponent implements OnInit {
  all: Patient[] = [];
  filtered: Patient[] = [];
  paginated: Patient[] = [];
  loading = true; saving = false;
  search = ''; statusFilter = ''; genderFilter = '';
  page = 1; pageSize = 10;
  showModal = false; editId: number | null = null;
  deleteTarget: Patient | null = null;
  form!: FormGroup;
  bloodTypes = ['A+','A-','B+','B-','AB+','AB-','O+','O-'];

  constructor(private svc: PatientService, private toast: ToastService, private fb: FormBuilder) {}

  ngOnInit(): void { this.load(); this.buildForm(); }

  load(): void {
    this.loading = true;
    this.svc.getAll().subscribe({ next: d => { this.all = d; this.filter(); this.loading = false; }, error: () => this.loading = false });
  }

  buildForm(): void {
    this.form = this.fb.group({
      firstName: ['', Validators.required], lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]], phoneNumber: [''],
      dateOfBirth: ['', Validators.required], gender: ['', Validators.required],
      nationalId: [''], bloodType: [''], address: [''],
      emergencyContactName: [''], emergencyContactPhone: [''],
      allergies: [''], chronicDiseases: [''], status: ['ACTIVE']
    });
  }

  filter(): void {
    this.filtered = this.all.filter(p => {
      const q = this.search.toLowerCase();
      const matchSearch = !q || p.fullName.toLowerCase().includes(q) || (p.nationalId ?? '').includes(q) || (p.phoneNumber ?? '').includes(q);
      const matchStatus = !this.statusFilter || p.status === this.statusFilter;
      const matchGender = !this.genderFilter || p.gender === this.genderFilter;
      return matchSearch && matchStatus && matchGender;
    });
    this.page = 1; this.updatePage();
  }

  updatePage(): void {
    const s = (this.page - 1) * this.pageSize;
    this.paginated = this.filtered.slice(s, s + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.filtered.length / this.pageSize); }
  get pages(): number[] { return Array.from({length: this.totalPages}, (_, i) => i + 1); }

  openCreate(): void { this.editId = null; this.form.reset({ status: 'ACTIVE' }); this.showModal = true; }
  openEdit(p: Patient): void {
    this.editId = p.id;
    this.form.patchValue({ ...p });
    this.showModal = true;
  }
  closeModal(): void { this.showModal = false; }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const body = this.form.value as PatientRequest;
    const req = this.editId ? this.svc.update(this.editId, body) : this.svc.create(body);
    req.subscribe({
      next: () => {
        this.toast.success(this.editId ? 'تم تعديل بيانات المريض' : 'تم إضافة المريض بنجاح');
        this.saving = false; this.closeModal(); this.load();
      },
      error: (e) => {
        this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false;
      }
    });
  }

  confirmDelete(p: Patient): void { this.deleteTarget = p; }
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.saving = true;
    this.svc.delete(this.deleteTarget.id).subscribe({
      next: () => { this.toast.success('تم حذف المريض'); this.saving = false; this.deleteTarget = null; this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  err(f: string): boolean { const c = this.form.get(f); return !!(c?.invalid && c?.touched); }
  statusLabel(s: string): string { return {ACTIVE:'نشط', DISCHARGED:'مُخرَّج', DECEASED:'متوفى'}[s] ?? s; }
  statusClass(s: string): string { return {ACTIVE:'badge-success', DISCHARGED:'badge-warning', DECEASED:'badge-danger'}[s] ?? 'badge-gray'; }
}
