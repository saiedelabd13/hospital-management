import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="header">
      <button class="toggle-btn" (click)="toggleSidebar.emit()">
        <i class="fas" [class.fa-bars]="!sidebarCollapsed" [class.fa-bars-staggered]="sidebarCollapsed"></i>
      </button>
      <div class="header-center">
        <span class="page-title">{{ getPageTitle() }}</span>
      </div>
      <div class="header-right">
        <span class="current-time">{{ now | date: 'EEEE، d MMMM yyyy' : '' : 'ar' }}</span>
      </div>
    </header>
  `,
  styles: [`
    .header {
      height: var(--header-h); background: #fff;
      border-bottom: 1px solid var(--gray-200);
      display: flex; align-items: center; gap: 12px;
      padding: 0 24px; position: sticky; top: 0; z-index: 50;
      box-shadow: 0 1px 3px rgba(0,0,0,.06);
    }
    .toggle-btn {
      width: 38px; height: 38px; border: none; background: var(--gray-100);
      border-radius: 10px; cursor: pointer; font-size: 15px; color: var(--gray-700);
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      transition: background .18s;
    }
    .toggle-btn:hover { background: var(--gray-200); }
    .header-center { flex: 1; }
    .page-title { font-size: 15px; font-weight: 600; color: var(--gray-900); }
    .header-right { display: flex; align-items: center; gap: 12px; }
    .current-time { font-size: 12px; color: var(--gray-500); }
  `]
})
export class HeaderComponent {
  @Input() sidebarCollapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  now = new Date();
  constructor(private router: Router) {
    setInterval(() => this.now = new Date(), 60000);
  }

  getPageTitle(): string {
    const map: Record<string, string> = {
      '/dashboard':       'لوحة التحكم',
      '/departments':     'إدارة الأقسام',
      '/doctors':         'إدارة الأطباء',
      '/patients':        'إدارة المرضى',
      '/appointments':    'إدارة المواعيد',
      '/medical-records': 'السجلات الطبية'
    };
    return map[this.router.url] ?? 'نظام إدارة المستشفى';
  }
}
