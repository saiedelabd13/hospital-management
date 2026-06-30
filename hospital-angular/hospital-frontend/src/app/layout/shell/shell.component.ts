import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="app-layout">
      <app-sidebar [collapsed]="sidebarCollapsed"></app-sidebar>
      <div class="main-content" [class.sidebar-collapsed]="sidebarCollapsed">
        <app-header [sidebarCollapsed]="sidebarCollapsed" (toggleSidebar)="sidebarCollapsed = !sidebarCollapsed"></app-header>
        <div class="page-body">
          <router-outlet></router-outlet>
        </div>
      </div>
    </div>
  `
})
export class ShellComponent {
  sidebarCollapsed = false;
}
