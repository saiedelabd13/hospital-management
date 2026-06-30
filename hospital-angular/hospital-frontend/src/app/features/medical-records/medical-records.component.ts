import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MedicalRecordService, PatientService, DoctorService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { MedicalRecord, MedicalRecordRequest, Patient, Doctor } from '../../models';

@Component({
  selector: 'app-medical-records',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="page-header">
      <div class="page-header-left">
        <h2><i class="fas fa-notes-medical" style="color:var(--primary);margin-left:8px"></i>السجلات الطبية</h2>
        <p>{{filtered.length}} سجل طبي</p>
      </div>
      <button class="btn btn-primary" (click)="openCreate()">
        <i class="fas fa-plus"></i> إضافة سجل
      </button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-bar">
          <i class="fas fa-magnifying-glass"></i>
          <input [(ngModel)]="search" (ngModelChange)="filter()" placeholder="بحث بالتشخيص أو اسم المريض...">
        </div>
      </div>

      <div *ngIf="loading" class="loading-overlay"><div class="spinner"></div></div>

      <div class="table-wrapper" *ngIf="!loading">
        <table>
          <thead>
            <tr><th>#</th><th>المريض</th><th>الطبيب</th><th>التشخيص</th><th>ضغط الدم</th><th>الحرارة</th><th>الوصفات</th><th>التاريخ</th><th>إجراءات</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of paginated; let i=index">
              <td style="color:var(--gray-500)">{{(page-1)*pageSize+i+1}}</td>
              <td><div style="font-weight:600">{{r.patientName}}</div></td>
              <td><div style="font-size:13px">{{r.doctorName}}</div></td>
              <td style="max-width:180px"><div style="font-weight:500">{{r.diagnosis}}</div><div *ngIf="r.symptoms" style="font-size:11px;color:var(--gray-500);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">{{r.symptoms}}</div></td>
              <td>{{r.bloodPressure || '—'}}</td>
              <td>{{r.temperature ? r.temperature + '°' : '—'}}</td>
              <td>
                <span class="badge badge-info" *ngIf="r.prescriptions?.length">
                  <i class="fas fa-pills"></i> {{r.prescriptions.length}}
                </span>
                <span *ngIf="!r.prescriptions?.length" style="color:var(--gray-400)">—</span>
              </td>
              <td style="font-size:12px;color:var(--gray-500)">{{r.createdAt | date:'dd/MM/yyyy'}}</td>
              <td>
                <div class="table-actions">
                  <button class="btn btn-ghost btn-sm btn-icon" (click)="openView(r)" title="عرض"><i class="fas fa-eye"></i></button>
                  <button class="btn btn-ghost btn-sm btn-icon" (click)="openEdit(r)" title="تعديل"><i class="fas fa-pen"></i></button>
                  <button class="btn btn-sm btn-icon" style="color:var(--danger);background:var(--danger-light)" (click)="confirmDelete(r)" title="حذف"><i class="fas fa-trash"></i></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div *ngIf="filtered.length===0" class="empty-state">
          <i class="fas fa-file-medical"></i><h3>لا توجد سجلات</h3><p>أضف سجلاً طبياً جديداً</p>
        </div>
      </div>
      <div class="pagination" *ngIf="totalPages>1">
        <button class="page-btn" (click)="page=1" [disabled]="page===1"><i class="fas fa-angles-right"></i></button>
        <button class="page-btn" (click)="page=page-1" [disabled]="page===1"><i class="fas fa-angle-right"></i></button>
        <button class="page-btn" *ngFor="let p of pages" [class.active]="p===page" (click)="page=p">{{p}}</button>
        <button class="page-btn" (click)="page=page+1" [disabled]="page===totalPages"><i class="fas fa-angle-left"></i></button>
        <button class="page-btn" (click)="page=totalPages" [disabled]="page===totalPages"><i class="fas fa-angles-left"></i></button>
      </div>
    </div>

    <!-- View Modal -->
    <div class="modal-overlay" *ngIf="viewRecord" (click)="viewRecord=null">
      <div class="modal modal-lg" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <span class="modal-title"><i class="fas fa-file-medical"></i> تفاصيل السجل الطبي</span>
          <button class="btn-close" (click)="viewRecord=null">✕</button>
        </div>
        <div class="modal-body" *ngIf="viewRecord">
          <div class="record-detail-grid">
            <div class="detail-section">
              <h4 class="section-title"><i class="fas fa-user"></i> بيانات المريض</h4>
              <div class="detail-row"><span>المريض</span><strong>{{viewRecord.patientName}}</strong></div>
              <div class="detail-row"><span>الطبيب</span><strong>{{viewRecord.doctorName}}</strong></div>
              <div class="detail-row"><span>التاريخ</span><strong>{{viewRecord.createdAt | date:'dd/MM/yyyy HH:mm'}}</strong></div>
            </div>
            <div class="detail-section">
              <h4 class="section-title"><i class="fas fa-heartbeat"></i> العلامات الحيوية</h4>
              <div class="vital-signs">
                <div class="vital"><i class="fas fa-weight-scale"></i><span>{{viewRecord.weight || '—'}} kg</span><label>الوزن</label></div>
                <div class="vital"><i class="fas fa-ruler-vertical"></i><span>{{viewRecord.height || '—'}} cm</span><label>الطول</label></div>
                <div class="vital"><i class="fas fa-heart-pulse"></i><span>{{viewRecord.bloodPressure || '—'}}</span><label>ضغط الدم</label></div>
                <div class="vital"><i class="fas fa-temperature-half"></i><span>{{viewRecord.temperature ? viewRecord.temperature + '°' : '—'}}</span><label>الحرارة</label></div>
                <div class="vital"><i class="fas fa-heart"></i><span>{{viewRecord.heartRate || '—'}} bpm</span><label>النبض</label></div>
              </div>
            </div>
          </div>
          <div class="detail-section" style="margin-top:16px">
            <h4 class="section-title"><i class="fas fa-stethoscope"></i> التشخيص والعلاج</h4>
            <div class="detail-row"><span>التشخيص</span><strong>{{viewRecord.diagnosis}}</strong></div>
            <div class="detail-row" *ngIf="viewRecord.symptoms"><span>الأعراض</span><span>{{viewRecord.symptoms}}</span></div>
            <div class="detail-row" *ngIf="viewRecord.treatmentPlan"><span>خطة العلاج</span><span>{{viewRecord.treatmentPlan}}</span></div>
            <div class="detail-row" *ngIf="viewRecord.doctorNotes"><span>ملاحظات الطبيب</span><span>{{viewRecord.doctorNotes}}</span></div>
          </div>
          <div class="detail-section" *ngIf="viewRecord.prescriptions?.length" style="margin-top:16px">
            <h4 class="section-title"><i class="fas fa-pills"></i> الوصفات الطبية ({{viewRecord.prescriptions.length}})</h4>
            <div class="presc-grid">
              <div class="presc-card" *ngFor="let p of viewRecord.prescriptions">
                <div class="presc-name">{{p.medicationName}}</div>
                <div class="presc-detail"><i class="fas fa-capsules"></i> {{p.dosage}} — {{p.frequency}}</div>
                <div class="presc-detail"><i class="fas fa-calendar-days"></i> {{p.durationDays}} يوم</div>
                <div class="presc-detail" *ngIf="p.instructions"><i class="fas fa-circle-info"></i> {{p.instructions}}</div>
                <span class="badge badge-success" *ngIf="p.status==='ACTIVE'">نشطة</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-outline" (click)="viewRecord=null">إغلاق</button></div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div class="modal-overlay" *ngIf="showModal" (click)="closeModal()">
      <div class="modal modal-lg" (click)="$event.stopPropagation()" style="max-width:860px">
        <div class="modal-header">
          <span class="modal-title"><i class="fas fa-file-medical"></i> {{editId ? 'تعديل السجل الطبي' : 'إضافة سجل طبي جديد'}}</span>
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
                  <option *ngFor="let d of doctors" [value]="d.id">{{d.fullName}}</option>
                </select>
                <div class="form-error" *ngIf="err('doctorId')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">التشخيص <span class="required">*</span></label>
              <input formControlName="diagnosis" class="form-control" [class.error]="err('diagnosis')" placeholder="التشخيص الطبي">
              <div class="form-error" *ngIf="err('diagnosis')"><i class="fas fa-circle-exclamation"></i> مطلوب</div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">الأعراض</label>
                <textarea formControlName="symptoms" class="form-control" rows="2" placeholder="الأعراض التي يعاني منها المريض..."></textarea>
              </div>
              <div class="form-group">
                <label class="form-label">خطة العلاج</label>
                <textarea formControlName="treatmentPlan" class="form-control" rows="2" placeholder="خطة العلاج المقترحة..."></textarea>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">ملاحظات الطبيب</label>
              <textarea formControlName="doctorNotes" class="form-control" rows="2" placeholder="ملاحظات إضافية..."></textarea>
            </div>
            <!-- Vitals -->
            <div class="section-divider">العلامات الحيوية</div>
            <div class="form-row-3">
              <div class="form-group"><label class="form-label">الوزن (kg)</label><input formControlName="weight" type="number" class="form-control" placeholder="75.5"></div>
              <div class="form-group"><label class="form-label">الطول (cm)</label><input formControlName="height" type="number" class="form-control" placeholder="170"></div>
              <div class="form-group"><label class="form-label">ضغط الدم</label><input formControlName="bloodPressure" class="form-control" placeholder="120/80"></div>
              <div class="form-group"><label class="form-label">النبض (bpm)</label><input formControlName="heartRate" type="number" class="form-control" placeholder="72"></div>
              <div class="form-group"><label class="form-label">الحرارة (°C)</label><input formControlName="temperature" type="number" step="0.1" class="form-control" placeholder="37.0"></div>
            </div>
            <!-- Prescriptions -->
            <div class="section-divider">
              الوصفات الطبية
              <button type="button" class="btn btn-ghost btn-sm" (click)="addPrescription()" style="margin-right:auto">
                <i class="fas fa-plus"></i> إضافة دواء
              </button>
            </div>
            <div formArrayName="prescriptions">
              <div *ngFor="let p of prescriptionsArray.controls; let i=index" [formGroupName]="i" class="presc-form-row">
                <div class="presc-form-grid">
                  <div class="form-group"><label class="form-label">اسم الدواء *</label><input formControlName="medicationName" class="form-control" placeholder="اسم الدواء"></div>
                  <div class="form-group"><label class="form-label">الجرعة *</label><input formControlName="dosage" class="form-control" placeholder="500mg"></div>
                  <div class="form-group"><label class="form-label">التكرار *</label><input formControlName="frequency" class="form-control" placeholder="3 مرات يومياً"></div>
                  <div class="form-group"><label class="form-label">المدة (يوم) *</label><input formControlName="durationDays" type="number" class="form-control" placeholder="7"></div>
                  <div class="form-group" style="grid-column:1/-1"><label class="form-label">التعليمات</label><input formControlName="instructions" class="form-control" placeholder="تعليمات التناول..."></div>
                </div>
                <button type="button" class="btn btn-sm" style="color:var(--danger);background:var(--danger-light);align-self:flex-start;margin-top:22px" (click)="removePrescription(i)">
                  <i class="fas fa-trash"></i>
                </button>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline" (click)="closeModal()">إلغاء</button>
            <button type="submit" class="btn btn-primary" [disabled]="saving">
              <i class="fas" [class.fa-save]="!saving" [class.fa-spinner]="saving" [class.fa-spin]="saving"></i>
              {{saving ? 'جارٍ الحفظ...' : 'حفظ السجل'}}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Confirm delete -->
    <div class="modal-overlay" *ngIf="deleteTarget" (click)="deleteTarget=null">
      <div class="modal" style="max-width:400px" (click)="$event.stopPropagation()">
        <div class="modal-header"><span class="modal-title" style="color:var(--danger)"><i class="fas fa-triangle-exclamation"></i> تأكيد الحذف</span><button class="btn-close" (click)="deleteTarget=null">✕</button></div>
        <div class="modal-body" style="text-align:center;padding:32px">
          <i class="fas fa-file-circle-xmark" style="font-size:48px;color:var(--danger);margin-bottom:16px;display:block"></i>
          <p>هل تريد حذف هذا السجل الطبي؟</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" (click)="deleteTarget=null">إلغاء</button>
          <button class="btn btn-danger" (click)="doDelete()" [disabled]="saving"><i class="fas fa-trash"></i> حذف</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .section-divider { display:flex;align-items:center;gap:10px;font-size:13px;font-weight:600;color:var(--gray-700);margin:20px 0 12px;padding-bottom:8px;border-bottom:1.5px solid var(--gray-200); }
    .presc-form-row { display:flex;gap:12px;background:var(--gray-50);border-radius:10px;padding:14px;margin-bottom:10px;border:1px solid var(--gray-200); }
    .presc-form-grid { flex:1;display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:10px; }
    .record-detail-grid { display:grid;grid-template-columns:1fr 1fr;gap:20px; }
    .detail-section { }
    .section-title { font-size:13px;font-weight:600;color:var(--primary);display:flex;align-items:center;gap:7px;margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--gray-100); }
    .detail-row { display:flex;gap:12px;margin-bottom:8px;font-size:13px; }
    .detail-row span:first-child { color:var(--gray-500);min-width:100px; }
    .vital-signs { display:grid;grid-template-columns:repeat(3,1fr);gap:10px; }
    .vital { background:var(--gray-50);border-radius:10px;padding:12px;text-align:center; }
    .vital i { font-size:18px;color:var(--primary);margin-bottom:6px;display:block; }
    .vital span { font-size:14px;font-weight:700;display:block; }
    .vital label { font-size:11px;color:var(--gray-500); }
    .presc-grid { display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:10px; }
    .presc-card { background:var(--gray-50);border-radius:10px;padding:14px;border:1px solid var(--gray-200); }
    .presc-name { font-weight:700;font-size:14px;margin-bottom:8px;color:var(--gray-900); }
    .presc-detail { font-size:12px;color:var(--gray-600);display:flex;align-items:center;gap:6px;margin-bottom:4px; }
    @media(max-width:700px){ .record-detail-grid{grid-template-columns:1fr} .presc-form-grid{grid-template-columns:1fr 1fr} }
  `]
})
export class MedicalRecordsComponent implements OnInit {
  all: MedicalRecord[] = [];
  filtered: MedicalRecord[] = [];
  paginated: MedicalRecord[] = [];
  patients: Patient[] = [];
  doctors: Doctor[] = [];
  loading = true; saving = false;
  search = '';
  page = 1; pageSize = 10;
  showModal = false; editId: number | null = null;
  viewRecord: MedicalRecord | null = null;
  deleteTarget: MedicalRecord | null = null;
  form!: FormGroup;

  constructor(
    private svc: MedicalRecordService, private patSvc: PatientService,
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
      patientId: [null, Validators.required], doctorId: [null, Validators.required],
      diagnosis: ['', Validators.required], symptoms: [''], treatmentPlan: [''],
      doctorNotes: [''], weight: [null], height: [null], bloodPressure: [''],
      heartRate: [null], temperature: [null], prescriptions: this.fb.array([])
    });
  }

  get prescriptionsArray(): FormArray { return this.form.get('prescriptions') as FormArray; }

  addPrescription(): void {
    this.prescriptionsArray.push(this.fb.group({
      medicationName: ['', Validators.required], dosage: ['', Validators.required],
      frequency: ['', Validators.required], durationDays: [7, Validators.required],
      startDate: [''], instructions: [''], refillAllowed: [false]
    }));
  }

  removePrescription(i: number): void { this.prescriptionsArray.removeAt(i); }

  filter(): void {
    const q = this.search.toLowerCase();
    this.filtered = this.all.filter(r => !q || r.patientName.toLowerCase().includes(q) || r.diagnosis.toLowerCase().includes(q));
    this.page = 1; this.updatePage();
  }

  updatePage(): void { const s = (this.page - 1) * this.pageSize; this.paginated = this.filtered.slice(s, s + this.pageSize); }
  get totalPages(): number { return Math.ceil(this.filtered.length / this.pageSize); }
  get pages(): number[] { return Array.from({length: this.totalPages}, (_, i) => i + 1); }

  openCreate(): void { this.editId = null; this.form.reset({ patientId: null, doctorId: null }); while (this.prescriptionsArray.length) this.prescriptionsArray.removeAt(0); this.showModal = true; }

  openEdit(r: MedicalRecord): void {
    this.editId = r.id;
    this.form.patchValue(r);
    while (this.prescriptionsArray.length) this.prescriptionsArray.removeAt(0);
    this.showModal = true;
  }

  openView(r: MedicalRecord): void { this.viewRecord = r; }
  closeModal(): void { this.showModal = false; }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const body = this.form.value as MedicalRecordRequest;
    const req = this.editId ? this.svc.update(this.editId, body) : this.svc.create(body);
    req.subscribe({
      next: () => { this.toast.success(this.editId ? 'تم تعديل السجل الطبي' : 'تم إضافة السجل الطبي'); this.saving = false; this.closeModal(); this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  confirmDelete(r: MedicalRecord): void { this.deleteTarget = r; }
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.saving = true;
    this.svc.delete(this.deleteTarget.id).subscribe({
      next: () => { this.toast.success('تم حذف السجل الطبي'); this.saving = false; this.deleteTarget = null; this.load(); },
      error: (e) => { this.toast.error(e.error?.message ?? 'حدث خطأ'); this.saving = false; }
    });
  }

  err(f: string): boolean { const c = this.form.get(f); return !!(c?.invalid && c?.touched); }
}
