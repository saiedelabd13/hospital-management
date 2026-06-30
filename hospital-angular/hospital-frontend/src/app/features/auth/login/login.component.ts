import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-page">
      <div class="login-card">
        <!-- Logo -->
        <div class="login-logo">
          <div class="logo-circle"><i class="fas fa-hospital"></i></div>
          <h1>نظام إدارة المستشفى</h1>
          <p>سجّل دخولك للمتابعة</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label class="form-label">اسم المستخدم أو البريد الإلكتروني</label>
            <div class="input-icon">
              <i class="fas fa-user"></i>
              <input formControlName="usernameOrEmail" class="form-control"
                     placeholder="أدخل اسم المستخدم" [class.error]="hasError('usernameOrEmail')">
            </div>
            <div class="form-error" *ngIf="hasError('usernameOrEmail')">
              <i class="fas fa-circle-exclamation"></i> الحقل مطلوب
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">كلمة المرور</label>
            <div class="input-icon">
              <i class="fas fa-lock"></i>
              <input [type]="showPass ? 'text' : 'password'" formControlName="password"
                     class="form-control" placeholder="أدخل كلمة المرور" [class.error]="hasError('password')">
              <button type="button" class="pass-toggle" (click)="showPass = !showPass">
                <i class="fas" [class.fa-eye]="!showPass" [class.fa-eye-slash]="showPass"></i>
              </button>
            </div>
            <div class="form-error" *ngIf="hasError('password')">
              <i class="fas fa-circle-exclamation"></i> الحقل مطلوب
            </div>
          </div>

          <!-- Error alert -->
          <div class="alert-error" *ngIf="errorMsg">
            <i class="fas fa-circle-xmark"></i> {{errorMsg}}
          </div>

          <button type="submit" class="btn btn-primary login-btn" [disabled]="loading">
            <span *ngIf="!loading"><i class="fas fa-right-to-bracket"></i> تسجيل الدخول</span>
            <span *ngIf="loading"><i class="fas fa-spinner fa-spin"></i> جارٍ التسجيل...</span>
          </button>
        </form>

        <!-- Quick logins -->
        <div class="quick-logins">
          <p class="quick-title">حسابات تجريبية:</p>
          <div class="quick-btns">
            <button (click)="quickLogin('admin','admin123')" class="quick-btn blue">
              <i class="fas fa-crown"></i> مدير
            </button>
            <button (click)="quickLogin('dr.ahmed','doctor123')" class="quick-btn green">
              <i class="fas fa-user-doctor"></i> طبيب
            </button>
            <button (click)="quickLogin('receptionist','reception123')" class="quick-btn orange">
              <i class="fas fa-headset"></i> استقبال
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-page {
      min-height: 100vh; background: linear-gradient(135deg,#1a2035 0%,#1a73e8 100%);
      display: flex; align-items: center; justify-content: center; padding: 20px;
    }
    .login-card {
      background: #fff; border-radius: 20px; padding: 40px;
      width: 100%; max-width: 420px; box-shadow: 0 20px 60px rgba(0,0,0,.25);
    }
    .login-logo { text-align: center; margin-bottom: 32px; }
    .logo-circle {
      width: 72px; height: 72px; background: var(--primary); border-radius: 20px;
      display: inline-flex; align-items: center; justify-content: center;
      font-size: 30px; color: #fff; margin-bottom: 14px;
      box-shadow: 0 8px 20px rgba(26,115,232,.35);
    }
    .login-logo h1 { font-size: 18px; font-weight: 700; color: var(--gray-900); margin-bottom: 4px; }
    .login-logo p  { font-size: 13px; color: var(--gray-500); }
    .input-icon { position: relative; }
    .input-icon > i:first-child { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: var(--gray-500); font-size: 13px; }
    .input-icon .form-control { padding-right: 36px; }
    .pass-toggle { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: var(--gray-500); font-size: 13px; }
    .alert-error { display: flex; align-items: center; gap: 8px; background: var(--danger-light); color: var(--danger); padding: 10px 12px; border-radius: 8px; font-size: 13px; margin-bottom: 16px; }
    .login-btn { width: 100%; padding: 12px; font-size: 14px; border-radius: 10px; margin-top: 4px; }
    .quick-logins { margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--gray-200); }
    .quick-title { font-size: 11px; color: var(--gray-500); text-align: center; margin-bottom: 10px; }
    .quick-btns { display: flex; gap: 8px; }
    .quick-btn { flex: 1; padding: 7px; border: 1.5px solid; border-radius: 8px; cursor: pointer; font-family: inherit; font-size: 11.5px; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 5px; transition: all .18s; background: #fff; }
    .quick-btn.blue   { border-color: var(--primary); color: var(--primary); }
    .quick-btn.blue:hover   { background: var(--primary-light); }
    .quick-btn.green  { border-color: var(--success); color: var(--success); }
    .quick-btn.green:hover  { background: var(--success-light); }
    .quick-btn.orange { border-color: var(--warning); color: var(--warning); }
    .quick-btn.orange:hover { background: var(--warning-light); }
  `]
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  showPass = false;
  errorMsg = '';

  constructor(private fb: FormBuilder, private auth: AuthService,
              private router: Router, private toast: ToastService) {
    this.form = this.fb.group({
      usernameOrEmail: ['', Validators.required],
      password:        ['', Validators.required]
    });
  }

  hasError(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.doLogin(this.form.value.usernameOrEmail, this.form.value.password);
  }

  quickLogin(user: string, pass: string): void {
    this.form.setValue({ usernameOrEmail: user, password: pass });
    this.doLogin(user, pass);
  }

  private doLogin(user: string, pass: string): void {
    this.loading = true; this.errorMsg = '';
    this.auth.login({ usernameOrEmail: user, password: pass }).subscribe({
      next: () => {
        this.toast.success('مرحباً بك في نظام المستشفى!');
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.errorMsg = 'اسم المستخدم أو كلمة المرور غير صحيحة';
        this.loading = false;
      }
    });
  }
}
