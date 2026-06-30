import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <aside class="sidebar" [class.collapsed]="collapsed">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-icon"><i class="fas fa-hospital"></i></div>
        <span class="logo-text">مستشفى الأمل</span>
      </div>

      <!-- Nav -->
      <nav class="sidebar-nav">
        <a *ngFor="let item of navItems"
           [routerLink]="item.route"
           routerLinkActive="active"
           [routerLinkActiveOptions]="{exact: item.route === '/'}"
           class="nav-item"
           [title]="collapsed ? item.label : ''">
          <i class="fas {{item.icon}} nav-icon"></i>
          <span class="nav-label">{{item.label}}</span>
        </a>
      </nav>

      <!-- User section -->
      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="user-details">
            <span class="user-name">{{(auth.currentUser$ | async)?.username}}</span>
            <span class="user-role">{{getRoleLabel()}}</span>
          </div>
        </div>
        <button class="btn-logout" (click)="auth.logout()" title="تسجيل الخروج">
          <i class="fas fa-right-from-bracket"></i>
        </button>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      position: fixed; top: 0; right: 0; bottom: 0;
      width: var(--sidebar-w); background: #1a2035;
      display: flex; flex-direction: column; z-index: 100;
      transition: width .25s ease; overflow: hidden;
    }
    .sidebar.collapsed { width: 72px; }
    .sidebar-logo {
      display: flex; align-items: center; gap: 12px;
      padding: 20px 18px; border-bottom: 1px solid rgba(255,255,255,.08);
    }
    .logo-icon {
      width: 36px; height: 36px; background: var(--primary); border-radius: 10px;
      display: flex; align-items: center; justify-content: center;
      color: #fff; font-size: 16px; flex-shrink: 0;
    }
    .logo-text { font-size: 15px; font-weight: 700; color: #fff; white-space: nowrap; }
    .collapsed .logo-text { display: none; }
    .sidebar-nav { flex: 1; padding: 12px 0; overflow-y: auto; overflow-x: hidden; }
    .nav-item {
      display: flex; align-items: center; gap: 12px;
      padding: 11px 18px; color: rgba(255,255,255,.6); text-decoration: none;
      font-size: 13.5px; font-weight: 500; border-radius: 0;
      transition: all .18s; white-space: nowrap;
      border-right: 3px solid transparent;
    }
    .nav-item:hover { background: rgba(255,255,255,.06); color: #fff; }
    .nav-item.active { background: rgba(26,115,232,.18); color: var(--primary); border-right-color: var(--primary); }
    .nav-icon { width: 18px; text-align: center; font-size: 15px; flex-shrink: 0; }
    .nav-label { transition: opacity .2s; }
    .collapsed .nav-label { opacity: 0; pointer-events: none; width: 0; overflow: hidden; }
    .sidebar-footer {
      display: flex; align-items: center; gap: 10px;
      padding: 14px 14px; border-top: 1px solid rgba(255,255,255,.08);
    }
    .user-avatar {
      width: 34px; height: 34px; background: rgba(255,255,255,.12);
      border-radius: 10px; display: flex; align-items: center; justify-content: center;
      color: rgba(255,255,255,.7); font-size: 14px; flex-shrink: 0;
    }
    .user-details { flex: 1; min-width: 0; overflow: hidden; }
    .user-name { display: block; font-size: 12.5px; font-weight: 600; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .user-role { display: block; font-size: 11px; color: rgba(255,255,255,.45); }
    .collapsed .user-details { display: none; }
    .btn-logout {
      width: 30px; height: 30px; background: rgba(255,255,255,.08); border: none;
      border-radius: 8px; color: rgba(255,255,255,.5); cursor: pointer;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      transition: all .18s;
    }
    .btn-logout:hover { background: rgba(217,48,37,.25); color: #ef5350; }
    .collapsed .btn-logout { margin: 0 auto; }
  `]
})
export class SidebarComponent {
  @Input() collapsed = false;

  navItems: NavItem[] = [
    { label: 'لوحة التحكم',  icon: 'fa-gauge',        route: '/dashboard' },
    { label: 'الأقسام',      icon: 'fa-building',      route: '/departments' },
    { label: 'الأطباء',      icon: 'fa-user-doctor',   route: '/doctors' },
    { label: 'المرضى',       icon: 'fa-users',         route: '/patients' },
    { label: 'المواعيد',     icon: 'fa-calendar-days', route: '/appointments' },
    { label: 'السجلات الطبية', icon: 'fa-notes-medical', route: '/medical-records' },
  ];

  constructor(public auth: AuthService) {}

  getRoleLabel(): string {
    if (this.auth.isAdmin()) return 'مدير النظام';
    if (this.auth.isDoctor()) return 'طبيب';
    if (this.auth.isReceptionist()) return 'موظف استقبال';
    return 'مستخدم';
  }
}
